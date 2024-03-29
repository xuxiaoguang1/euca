// -*- mode: C; c-basic-offset: 4; tab-width: 4; indent-tabs-mode: nil -*-
// vim: set softtabstop=4 shiftwidth=4 tabstop=4 expandtab:

/*
  Copyright (c) 2009  Eucalyptus Systems, Inc.

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, only version 3 of the License.

  This file is distributed in the hope that it will be useful, but WITHOUT
  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
  for more details.

  You should have received a copy of the GNU General Public License along
  with this program.  If not, see <http://www.gnu.org/licenses/>.

  Please contact Eucalyptus Systems, Inc., 130 Castilian
  Dr., Goleta, CA 93101 USA or visit <http://www.eucalyptus.com/licenses/>
  if you need additional information or have any questions.

  This file may incorporate work covered under the following copyright and
  permission notice:

  Software License Agreement (BSD License)

  Copyright (c) 2008, Regents of the University of California


  Redistribution and use of this software in source and binary forms, with
  or without modification, are permitted provided that the following
  conditions are met:

  Redistributions of source code must retain the above copyright notice,
  this list of conditions and the following disclaimer.

  Redistributions in binary form must reproduce the above copyright
  notice, this list of conditions and the following disclaimer in the
  documentation and/or other materials provided with the distribution.

  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
  IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
  TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
  PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
  OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
  EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
  PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
  LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. USERS OF
  THIS SOFTWARE ACKNOWLEDGE THE POSSIBLE PRESENCE OF OTHER OPEN SOURCE
  LICENSED MATERIAL, COPYRIGHTED MATERIAL OR PATENTED MATERIAL IN THIS
  SOFTWARE, AND IF ANY SUCH MATERIAL IS DISCOVERED THE PARTY DISCOVERING
  IT MAY INFORM DR. RICH WOLSKI AT THE UNIVERSITY OF CALIFORNIA, SANTA
  BARBARA WHO WILL THEN ASCERTAIN THE MOST APPROPRIATE REMEDY, WHICH IN
  THE REGENTS' DISCRETION MAY INCLUDE, WITHOUT LIMITATION, REPLACEMENT
  OF THE CODE SO IDENTIFIED, LICENSING OF THE CODE SO IDENTIFIED, OR
  WITHDRAWAL OF THE CODE CAPABILITY TO THE EXTENT NEEDED TO COMPLY WITH
  ANY SUCH LICENSES OR RIGHTS.
*/

/*
 * blobstore.c
 */

#define _GNU_SOURCE
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <assert.h>
#include <unistd.h> // close
#include <time.h> // time
#include <sys/time.h> // gettimeofday
#include <sys/stat.h> // mkdir
#include <errno.h> // errno
#include <sys/types.h> // *dir, etc, wait
#include <sys/file.h> // flock
#include <dirent.h>
#include <sys/wait.h> // wait
#include <execinfo.h> // backtrace
#include <pthread.h>
#include "blobstore.h"
#include <sys/types.h> // gettid
#include "diskutil.h"
#include <regex.h>
#include "misc.h" // ensure_...
#include "eucalyptus.h" // euca user
#include "ipc.h"

#define BLOBSTORE_METADATA_FILE ".blobstore"
#define BLOBSTORE_METADATA_TIMEOUT_USEC 1000000LL * 60 * 2 // it may take dozens of seconds to open blobstore when others are LRU-purging it
#define BLOBSTORE_LOCK_TIMEOUT_USEC 50000LL
#define BLOBSTORE_FIND_TIMEOUT_USEC 50000LL
#define BLOBSTORE_DELETE_TIMEOUT_USEC 50000LL
#define BLOBSTORE_SLEEP_INTERVAL_USEC 99999LL
#define BLOBSTORE_DMSETUP_TIMEOUT_SEC 30
#define BLOBSTORE_MAX_CONCURRENT 99
#define BLOBSTORE_NO_TIMEOUT -1L
#define BLOBSTORE_SIG_MAX 32768
#define DM_PATH "/dev/mapper/"
#define DM_FORMAT DM_PATH "%s" // TODO: do not hardcode?
#define MIN_BLOCKS_SNAPSHOT 32 // otherwise dmsetup fails with 
                               // device-mapper: reload ioctl failed: Cannot allocate memory OR
                               // device-mapper: reload ioctl failed: Input/output error
#define EUCA_ZERO "euca-zero"
#define EUCA_ZERO_SIZE "2199023255552" // is one petabyte enough?

typedef enum { // paths to files containing... 
    BLOCKBLOB_PATH_NONE = 0, // sentinel for identifying files that are not blockblob related
    BLOCKBLOB_PATH_BLOCKS, // ...blocks, either in flat format or as a snapshot backing
    BLOCKBLOB_PATH_LOCK, // ...nothing, but needed for safe locking of access to the blob
    BLOCKBLOB_PATH_DM, // ...device mapper devices created for this clone, if any
    BLOCKBLOB_PATH_DEPS, // ...names of blockblobs that this blockblob depends on, if any
    BLOCKBLOB_PATH_LOOPBACK, // ...name of the loopback device for this blob, when attached
    BLOCKBLOB_PATH_SIG, // ...signature of the blob, if provided from outside
    BLOCKBLOB_PATH_REFS, // ...names of blockblobs that depend on this blockblob, if any
    BLOCKBLOB_PATH_HOLLOW, // ...nothing, but the file acts as a marker of 'hollow' blobs
    BLOCKBLOB_PATH_TOTAL,
} blockblob_path_t; // if changing, change the array below and set_blockblob_metadata_path()

static const char * blobstore_metadata_suffixes [] = { // entries must match the ones in enum above
    "none", // sentinel entry so that all actual entries have indeces > 0
    "blocks", // MUST be second so loop in check_metadata_name() works
    "lock",
    "dm",
    "deps",
    "loopback",
    "sig",
    "refs",
    "hollow"
};

typedef struct _blobstore_filelock {
    char path [PATH_MAX]; // path that the file was open with (TODO: canonicalize?)
    int refs; // number of open file descriptors (some holding the lock, some waiting) for this path in this process
    int next_fd; // next available file descriptor in the table below:
    int fd        [BLOBSTORE_MAX_CONCURRENT];
    int fd_status [BLOBSTORE_MAX_CONCURRENT]; // 0 = unused, 1 = open
    pthread_rwlock_t lock; // reader/writer lock for controlling intra-process access
    pthread_mutex_t mutex; // for locking this specific struct during manipulations
    sem * sem; /// semaphore for debugging
    struct _blobstore_filelock * next; // pointer for constructing a LL
} blobstore_filelock;

__thread blobstore_error_t _blobstore_errno = BLOBSTORE_ERROR_OK; // thread-local errno
static void (* err_fn) (const char * msg) = NULL; 
static unsigned char _do_print_errors = 1;
static unsigned char _do_print_trace = 1;
static pthread_mutex_t _blobstore_mutex = PTHREAD_MUTEX_INITIALIZER; // process-global mutex
static blobstore_filelock * locks_list = NULL; // process-global LL head (TODO: replace this with a hash table)

// debugging counters (TODO: remove these)
static long _locks_list_add_ctr = 0L;
static long _locks_list_rem_ctr = 0L;
static long _open_success_ctr = 0L;
static long _close_success_ctr = 0L;
static long _open_error_ctr = 0L;
static long _open_timeout_ctr = 0L;
static long _close_error_ctr = 0L;
static char zero_buf [1] = "\0";

static void myprintf (int loglevel, const char * format, ...)
{
    char buf [1024];

    va_list ap;
    va_start (ap, format);
    vsnprintf (buf, sizeof (buf), format, ap);
    va_end (ap);

    if (err_fn) 
        err_fn (buf);
    else
        puts (buf);
}

static void dump_trace (char * buf, int buf_size)
{
    void *array[64];
    size_t size;
    char **strings;
    size_t i;
    
    size = backtrace (array, sizeof(array)/sizeof(void *));
    strings = backtrace_symbols (array, size);
    
    buf [0] = '\0';
    for (i = 0; i < size; i++) {
        int left = buf_size - 1 - strlen (buf);
        if (left < 0) break;
        char line [512];
        snprintf (line, sizeof(line), "\t%s\n", strings [i]);
        strncat (buf, line, left);
    }

    free (strings);
}

const char * blobstore_get_error_str ( blobstore_error_t error ) {
    return _blobstore_error_strings [error];
}

static __thread char _blobstore_last_msg [512] = "";
const char * blobstore_get_last_msg () {
    return _blobstore_last_msg;
}

static __thread char _blobstore_last_trace [8172] = "";
const char * blobstore_get_last_trace() {
    return _blobstore_last_trace;
}

#define __INLINE__ __inline__

__INLINE__ static void _err_on (void)  { _do_print_errors = 1; }
__INLINE__ static void _err_off (void) { _do_print_errors = 0; }

#define ERR(_ERRNO,_MSG) err(_ERRNO,_MSG,__LINE__,__FILE__)

static void err (blobstore_error_t error, const char * custom_msg, const int src_line_no, const char * src_file_name)
{
    const char * msg = custom_msg;
    if ( msg == NULL ) {
        msg = blobstore_get_error_str (error);
    }
    snprintf (_blobstore_last_msg, sizeof(_blobstore_last_msg), "%s:%d %s", src_file_name, src_line_no, msg);
    dump_trace (_blobstore_last_trace, sizeof(_blobstore_last_trace));
    
    if (_do_print_errors) {
        myprintf (EUCAERROR, "error: %s\n", _blobstore_last_msg);
        if (_do_print_trace)
            myprintf (EUCAERROR, "%s", _blobstore_last_trace);
    }
    _blobstore_errno = error;
}

#define PROPAGATE_ERR(_ERRNO) propagate_system_errno(_ERRNO,__LINE__,__FILE__)

__INLINE__ static void propagate_system_errno (blobstore_error_t default_errno, const int src_line_no, const char * src_file_name)
{
    switch (errno) {
    case ENOENT: _blobstore_errno = BLOBSTORE_ERROR_NOENT; break;
    case ENOMEM: _blobstore_errno = BLOBSTORE_ERROR_NOMEM; break;
    case EACCES: _blobstore_errno = BLOBSTORE_ERROR_ACCES; break;
    case EEXIST: _blobstore_errno = BLOBSTORE_ERROR_EXIST; break;
    case EINVAL: _blobstore_errno = BLOBSTORE_ERROR_INVAL; break;
    case ENOSPC: _blobstore_errno = BLOBSTORE_ERROR_NOSPC; break;
    case EAGAIN: _blobstore_errno = BLOBSTORE_ERROR_AGAIN; break;
    default:
        perror ("blobstore");
        _blobstore_errno = default_errno;
    }
    err (_blobstore_errno, NULL, src_line_no, src_file_name);
}

void blobstore_set_error_function ( void (* fn) (const char * msg) )
{
    err_fn = fn;
}

static void gen_id (char * str, unsigned int size)
{
    struct timeval tv;
    gettimeofday (&tv, NULL);
    srandom ((unsigned int) ((unsigned long long)str * (unsigned long long)tv.tv_usec));
    snprintf (str, size, "%08lx%08lx%08lx", (unsigned long)random(), (unsigned long)random(), (unsigned long)random());
}

struct flock * flock_whole_file (struct flock * l, short type) 
{
    l->l_type = type;
    l->l_pid = 0;

    // set params so as to lock the whole file
    l->l_start = 0;
    l->l_whence = SEEK_SET;
    l->l_len = 0;

    return l;
}

// MUST be called with _blobstore_mutex held
static void close_filelock (blobstore_filelock * l)
{
    // close all file descriptors at once (we do this because 
    // closing any one removes the lock for all descriptors
    // held by a process)
    for (int i=0; i<l->next_fd; i++) {
        close (l->fd [i]);
    }
    l->next_fd = 0; // knock the open fd counter back to 0
}

// MUST be called with _blobstore_mutex held
static void free_filelock (blobstore_filelock * l)
{
    pthread_rwlock_destroy (&(l->lock));
    pthread_mutex_destroy (&(l->mutex));    
    free (l);
}

// This function must be used to close files opened with open_and_lock().
// (Simply doing close() will leave the file locked via pthreads
// and future open_and_lock() requests from the same process may
// fail.)  Also, closing the file descriptor releases the OS file lock
// for the process, so any other read-only descriptors held by the
// process are no longer guarded since other processes may open the
// file for writing.
static int close_and_unlock (int fd)
{
    if (fd<0) {
        ERR (BLOBSTORE_ERROR_BADF, NULL);
        return -1;
    }
    int ret = 0;
    { // critical section
        pthread_mutex_lock (&_blobstore_mutex); // grab global lock (we will not block below and we may be deallocating)
        logprintfl (EUCADEBUG2, "{%u} close_and_unlock: obtained global lock for closing of fd=%d\n", (unsigned int)pthread_self(), fd);
        
        blobstore_filelock * path_lock = NULL; // lock struct to which this fd belongs
        int index = -1; // index of this fd entry in the lock struct
        int open_fds = 0; // count of other open file descriptors for this lock
        
        // traverse all locks, looking for one with fd,
        // when found, compute index and open_fds
        blobstore_filelock ** next_ptr = &locks_list;
        for (blobstore_filelock * l = locks_list; l; l=l->next) { // look for the fd 
            assert (l->next_fd>=0 && l->next_fd<=BLOBSTORE_MAX_CONCURRENT);
            for (int i=0; i<l->next_fd; i++) {
                if (l->fd_status [i] && l->fd [i] == fd) {
                    path_lock = l; // found it!
                    index = i; 
                    break;
                }
            }
            if (index!=-1)
                break;
            next_ptr = &(l->next); // list head or prev element
        }

        if (path_lock) { 
            assert (* next_ptr == path_lock);
            assert (index>=0 && index<BLOBSTORE_MAX_CONCURRENT);
            
            boolean did_close = FALSE;
            boolean do_free = FALSE;
            { // inner critical section to protect changes to 'path_lock', if any
                pthread_mutex_lock (&(path_lock->mutex)); // grab path-specific mutex
                if (path_lock->fd_status [index] == 1) { // has not been closed yet
                    path_lock->fd_status [index] = 0; // set status to 'unused'
                    did_close = TRUE;
                    path_lock->refs--;

                    int open_fds = 0;
                    for (int i=0; i<path_lock->next_fd; i++) {
                        if (path_lock->fd_status [i]) {
                            assert (path_lock->fd [i] != fd);
                            open_fds++;
                        }
                    }

                    if (open_fds==0 && path_lock->refs==0) { // no open blockblob file descriptors in this process
                        close_filelock (path_lock);
                        * next_ptr = path_lock->next; // remove from LL
                        do_free = TRUE;
                        _locks_list_rem_ctr++;
                        logprintfl (EUCADEBUG2, "{%u} close_and_unlock: unlocked and freed fd=%d path=%s\n", 
                                    (unsigned int)pthread_self(), fd, path_lock->path);
                        
                    } else {
                        logprintfl (EUCADEBUG2, "{%u} close_and_unlock: kept fd=%d path=%d open/refs=%d/%d\n", 
                                    (unsigned int)pthread_self(), fd, path_lock->path, open_fds, path_lock->refs);
                    }
                    pthread_rwlock_unlock (&(path_lock->lock)); // give up the Posix lock
                    /* lock testing code
                    if (path_lock->sem) {
                        sem_v (path_lock->sem);
                        sem_free (path_lock->sem);
                        path_lock->sem = NULL;
                    }
                    */
                }
                pthread_mutex_unlock (&(path_lock->mutex));
            } // end of inner critical section
            
            if (do_free)
                free_filelock (path_lock);
            
            if (! did_close) {
                ERR (BLOBSTORE_ERROR_BADF, "file descriptor already closed");
                ret = -1;
            }
        } else { // no match
            ERR (BLOBSTORE_ERROR_BADF, "not an open file descriptor");
            ret = -1;
        }
        
        if (ret==0)
            _close_success_ctr++;
        else
            _close_error_ctr++;
        
        logprintfl (EUCADEBUG2, "{%u} close_and_unlock: releasing global lock for closing of fd=%d ret=%d\n", (unsigned int)pthread_self(), fd, ret);
        pthread_mutex_unlock (&_blobstore_mutex);
    } // end of critical section
    
    return ret;
}

#ifdef _TEST_LOCKS
static char * path_to_sem_name (const char * path, char * name, int name_size) 
{
    snprintf (name, name_size, "euca%s", path);
    for (int i=0; i<name_size && name [i]; i++)
        if (name [i] == '/')
            name [i] = '-';
    return name;
}
#endif // _UNIT_TEST

// This function creates or opens a file and locks it; the lock is 
// - exclusive if the file is being created or written to, or a
// - non-exclusive readers' lock if the file was opened RDONLY.
// The lock works both across threads and processes.
// File descriptors obtained from this function should be
// released with close_and_unlock(). All locks held by a process 
// are released upon termination, whether normal or abnormal.
// 
// flags: BLOBSTORE_FLAG_RDONLY - open with O_RDONLY, reader lock
//        BLOBSTORE_FLAG_RDWR - open with O_RDWR, writer lock
//        BLOBSTORE_FLAG_CREAT - open with O_RDWR | O_CREAT, writer lock
//        BLOBSTORE_FLAG_EXCL - can be added to _CREAT, as with open()
//
// timeout_usec: timeout in microseconds for waiting on a lock
//               BLOBSTORE_NO_TIMEOUT / -1 - wait forever
//               BLOBSTORE_NO_WAIT / 0 - do not wait at all
//
// mode: gets passed to open() directly

static int open_and_lock (const char * path, 
                          int flags,
                          long long timeout_usec,
                          mode_t mode)
{
    short l_type;
    int o_flags = 0;
    long long started = time_usec();
    long long deadline = started + timeout_usec;

    // verify the flags and, based on them,
    // decide what type of lock to use
    if (flags & BLOBSTORE_FLAG_RDONLY) {
        l_type = F_RDLCK; // use shared (read) lock
        o_flags |= O_RDONLY; // required when using F_RDLCK

    } else if ((flags & BLOBSTORE_FLAG_RDWR) ||
               (flags & BLOBSTORE_FLAG_CREAT)) {
        l_type = F_WRLCK; // use exclusive (write) lock
        o_flags |= O_RDWR; // required when using F_WRLCK
        if (flags & BLOBSTORE_FLAG_CREAT) {
            o_flags |= O_CREAT;
            // intentionally ignore _EXCL supplied without _CREAT
            if (flags & BLOBSTORE_FLAG_EXCL) o_flags |= O_EXCL;
        }

    } else {
        ERR (BLOBSTORE_ERROR_INVAL, "flags to open_and_lock must include either _RDONLY or _RDWR or _CREAT");
        return -1;
    }

    // handle intra-process locking, with a pthreads read-write lock
    // either find in a global linked list 'locks_list' or
    // allocate and append to it a 'blobstore_filelock' struct
    blobstore_filelock * path_lock = NULL;
    { // critical section
        pthread_mutex_lock (&_blobstore_mutex); // grab the global mutex
        blobstore_filelock ** next_ptr = &locks_list; 
        for (blobstore_filelock * l = locks_list; l; l=l->next) { // look through existing locks
            if (strcmp (path, l->path) == 0) {
                path_lock = l;
                break;
            }
            next_ptr = &(l->next); 
        }
        // next_ptr now points either to LL head or 
        // to the last non-matching element's next pointer
        
        if (path_lock==NULL) { // this path is not locked by any thread
            path_lock = calloc (1, sizeof(blobstore_filelock));
            if (path_lock==NULL) {
                pthread_mutex_unlock (&_blobstore_mutex);
                ERR (BLOBSTORE_ERROR_NOMEM, NULL);
                return -1;
            }
            safe_strncpy (path_lock->path, path, sizeof(path_lock->path));
            pthread_rwlock_init (&(path_lock->lock), NULL);
            pthread_mutex_init (&(path_lock->mutex), NULL);
            * next_ptr = path_lock; // add at the end of LL
            _locks_list_add_ctr++;
        } else {
            assert (* next_ptr == path_lock);
            if (path_lock->next_fd==BLOBSTORE_MAX_CONCURRENT) {
                pthread_mutex_unlock (&_blobstore_mutex);
                ERR (BLOBSTORE_ERROR_MFILE, "too many open file descriptors"); // to be precise, this means too many file descriptors with overlapping lifetimes
                return -1;
            }
        }
        path_lock->refs++; // increase the reference count while still under lock
        pthread_mutex_unlock (&_blobstore_mutex); // release global mutex
    } // end of critical section

    // open/create the file, using Posix file locks for inter-process locking
    int fd = open (path, o_flags, mode);
    logprintfl (EUCADEBUG2, "{%u} open_and_lock: open fd=%d flags=%0x path=%s\n", (unsigned int)pthread_self(), fd, o_flags, path);
    if (fd == -1) {
        PROPAGATE_ERR (BLOBSTORE_ERROR_UNKNOWN);
        goto error;
    }

    { // critical section
        pthread_mutex_lock (&_blobstore_mutex); // grab the global mutex
        
        // ensure we do not have this file descriptor already
        int count = 0;
        for (blobstore_filelock * l = locks_list; l; l=l->next)
            for (int i=0; i<l->next_fd; i++)
                if (l->fd [i] == fd)
                    count++;
        if (count>0) {
            ERR (BLOBSTORE_ERROR_INVAL, "blobstore lock closed outside close_and_unlock");
            pthread_mutex_unlock (&(path_lock->mutex)); // release global mutex
            pthread_mutex_unlock (&_blobstore_mutex); // release global mutex
            goto error;
        }
        
        { // inner critical section
            pthread_mutex_lock (&(path_lock->mutex)); // grab path-specific mutex for atomic update to the table of descriptors
            
            // record the file descriptor in the array regardless of whether
            // we ultimately succeed in obtaining the lock or not -- we must
            // ensure we do not close this file descriptor until all users 
            // of the lock are through
            path_lock->fd        [path_lock->next_fd] = fd; // record file descriptor to enable future lookups
            path_lock->fd_status [path_lock->next_fd] = 1;  // mark the slot as in-use
            path_lock->next_fd++; // move the index up (it only goes up because we close all file descriptors together)
            
            pthread_mutex_unlock (&(path_lock->mutex)); // release path-specific mutex
        } // end of inner critical section

        pthread_mutex_unlock (&_blobstore_mutex); // release global mutex
    } // end of critical section
    
    for (;;) {
        // first try getting the Posix rwlock
        int ret;
        if (l_type == F_WRLCK) 
            ret = pthread_rwlock_trywrlock (&(path_lock->lock));
        else 
            ret = pthread_rwlock_tryrdlock (&(path_lock->lock));
        if (ret==0) {
            // Posix rwlock succeeded, try the file lock
            errno = 0;
            struct flock l;
            if (fcntl (fd, F_SETLK, flock_whole_file (&l, l_type)) != -1)
                break; // success!
            pthread_rwlock_unlock (&(path_lock->lock)); // give up the Posix lock
            if (errno != EAGAIN) { // any error other than inability to get the lock
                PROPAGATE_ERR (BLOBSTORE_ERROR_UNKNOWN);
                goto error;
            }
        }
        long long now = time_usec();
        if (timeout_usec!=BLOBSTORE_NO_TIMEOUT && now >= deadline) { // we timed out waiting for the lock
            ERR (BLOBSTORE_ERROR_AGAIN, NULL);
            pthread_mutex_lock (&_blobstore_mutex);
            _open_timeout_ctr++;
            pthread_mutex_unlock (&_blobstore_mutex);
            goto error;
        }
        logprintfl (EUCADEBUG2, "{%u} open_and_lock: could not acquire %s lock, sleeping on %s\n", (unsigned int)pthread_self(), (ret==0)?("file"):("posix"), path);
        
        usleep (BLOBSTORE_SLEEP_INTERVAL_USEC);
    }

    // successully acquired both file and Posix locks

#ifdef _TEST_LOCKS
    if (l_type == F_WRLCK) {
        char sem_name [512];
        path_lock->sem = sem_alloc (1, path_to_sem_name (path, sem_name, sizeof (sem_name)));
        sem_p (path_lock->sem);
    }
#endif // _TEST_LOCKS

    pthread_mutex_lock (&_blobstore_mutex);
    _open_success_ctr++;
    pthread_mutex_unlock (&_blobstore_mutex);
    { // print out information about the newly acquired lock
        struct stat s;
        fstat (fd, &s);
        
        struct flock l;
        fcntl (fd, F_GETLK, flock_whole_file (&l, l_type));
        
        logprintfl (EUCADEBUG2, "{%u} open_and_lock: locked fd=%d path=%s flags=%d ino=%d mode=%0o [lock type=%d whence=%d start=%d length=%d]\n", 
                    (unsigned int)pthread_self(), fd, path, o_flags, s.st_ino, s.st_mode, l.l_type, l.l_whence, l.l_start, l.l_len);
    }
    return fd;

 error:

    // due to aproblem above (inability to open the file or
    // to acquire Posix locks within the deadline), the 
    // 'blobstore_filelock' struct will be removed from the 
    // global linked list 'locks_list', its files closed,
    // and its memory freed -- but only if this is the last 
    // thread using it

    { // critical section
        pthread_mutex_lock (&_blobstore_mutex); // grab the global lock to protect locks_list traversal

        // we must recalculate next_ptr since the element that it points to
        // may have been removed from the LL and freed while we were outside
        // the critical section
        blobstore_filelock ** next_ptr = &locks_list; 
        for (blobstore_filelock * l = locks_list; l; l=l->next) { // look through existing locks
            if (path_lock == l)
                break;
            next_ptr = &(l->next); 
        }
        // next_ptr must point at the struct we are looking for,
        // which must be in the list
        assert (* next_ptr == path_lock);

        boolean do_free = FALSE;        
        { // inner critical section 
            pthread_mutex_lock (&(path_lock->mutex)); // grab path-specific mutex for atomic update to the table of descriptors
            path_lock->refs--;

            int open_fds = 0;
            for (int i=0; i<path_lock->next_fd; i++) {
                if (path_lock->fd_status [i]) {
                    if (path_lock->fd [i] == fd) {
                        path_lock->fd_status [i] = 0; // mark as 'unused'
                    } else {
                        open_fds++;
                    }
                }
            }

            if (open_fds==0 && path_lock->refs==0) { // no open blockblob file descriptors in this process
                close_filelock (path_lock);
                * next_ptr = path_lock->next; // remove from LL                                                                                                         
                do_free = TRUE;
                _locks_list_rem_ctr++;
                logprintfl (EUCADEBUG2, "{%u} open_and_lock: freed fd=%d path=%s\n",
                            (unsigned int)pthread_self(), fd, path_lock->path);
                
            } else {
                logprintfl (EUCADEBUG2, "{%u} open_and_lock: kept fd=%d path=%d open/refs=%d/%d\n",
                            (unsigned int)pthread_self(), fd, path_lock->path, open_fds, path_lock->refs);
            }
            
            pthread_mutex_unlock (&(path_lock->mutex));
        } // end of inner critical section
        
        if (do_free)
            free_filelock (path_lock);
        
        _open_error_ctr++;
        pthread_mutex_unlock (&_blobstore_mutex);
    } // end of critical section
    
    return -1;
}

static char * get_val (const char * buf, const char * key) 
{
    char * val = NULL;
    char full_key [512];
    snprintf (full_key, sizeof (full_key), "%s: ", key);
    char * val_begin = strstr (buf, full_key);
    if (val_begin) {
        val_begin += strlen (full_key);
        char * val_end = val_begin;
        while (* val_end != '\n' && * val_end != '\0') val_end++;
        val = calloc (val_end-val_begin+1, sizeof(char)); // +1 for the \0
        if (val==NULL) { ERR (BLOBSTORE_ERROR_NOMEM, NULL); return NULL; }
        strncpy (val, val_begin, val_end-val_begin);
    }

    return val;
}

// helper for reading a file into a buffer
// returns number of bytes read or -1 if error
static int fd_to_buf (int fd, char * buf, int size_buf)
{
    if (lseek (fd, 0, SEEK_SET)==-1) 
        { ERR (BLOBSTORE_ERROR_ACCES, "failed to seek in metadata file"); return -1; }

    struct stat sb;
    if (fstat (fd, &sb)==-1) 
        { ERR (BLOBSTORE_ERROR_ACCES, "failed to stat metadata file"); return -1; }

    if (read (fd, buf, size_buf) != sb.st_size) // TODO: do this in a loop?
        { ERR (BLOBSTORE_ERROR_NOENT, "failed to read metadata file"); return -1; }

    return sb.st_size;
}

// helper for write buffer into a file at descriptor
// returns number of bytes written or -1 if error
static int buf_to_fd (int fd, const char * buf, int size_buf)
{
    if (lseek (fd, 0, SEEK_SET)==-1) 
        { ERR (BLOBSTORE_ERROR_ACCES, "failed to seek in metadata file"); return -1; }

    ssize_t size_wrote = write (fd, buf, size_buf); // TODO: do this in a loop?
    if (size_wrote < size_buf)
        { ERR (BLOBSTORE_ERROR_NOENT, "failed to write metadata file"); return -1; }

    // as a sanity check, stat the file and verify its size
    struct stat sb;
    if (fstat (fd, &sb)==-1) 
        { ERR (BLOBSTORE_ERROR_ACCES, "failed to stat metadata file"); return -1; }

    if (sb.st_size != size_buf)
        { ERR (BLOBSTORE_ERROR_NOENT, "failed to read back metadata file"); return -1; }

    return sb.st_size;
}

static int read_store_metadata (blobstore * bs)
{
    char buf [1024];
    int size = fd_to_buf (bs->fd, buf, sizeof (buf));

    if (size == -1)
        return -1;
    if (size<30) { 
        ERR (BLOBSTORE_ERROR_NOENT, "metadata size is too small"); 
        return -1; 
    }

    char * val;
    if ((val = get_val (buf, "id"))==NULL) 
        return -1; 
    safe_strncpy (bs->id, val, sizeof (bs->id)); 
    free (val);

    if ((val = get_val (buf, "limit"))==NULL) return -1; 
    errno = 0; bs->limit_blocks = strtoll (val, NULL, 10); free (val); if (errno!=0) { ERR (BLOBSTORE_ERROR_NOENT, "invalid metadata file (limit is missing)"); return -1; }

    if ((val = get_val (buf, "revocation"))==NULL) return -1; 
    errno = 0; bs->revocation_policy = strtoll (val, NULL, 10); free (val); if (errno!=0) { ERR (BLOBSTORE_ERROR_NOENT, "invalid metadata file (revocation is missing)"); return -1; }

    if ((val = get_val (buf, "snapshot"))==NULL) return -1; 
    errno = 0; bs->snapshot_policy = strtoll (val, NULL, 10); free (val); if (errno!=0) { ERR (BLOBSTORE_ERROR_NOENT, "invalid metadata file (snapshot is missing)"); return -1; }

    if ((val = get_val (buf, "format"))==NULL) return -1; 
    errno = 0; bs->format = strtoll (val, NULL, 10); free (val); if (errno!=0) { ERR (BLOBSTORE_ERROR_NOENT, "invalid metadata file (format is missing)"); return -1; }
    return 0;
}

static int write_store_metadata (blobstore * bs)
{
    if (ftruncate (bs->fd, 0)==-1)
        { ERR (BLOBSTORE_ERROR_NOENT, "failed to truncate the metadata file"); return -1; }
    if (lseek (bs->fd, 0, SEEK_SET)==-1) 
        { ERR (BLOBSTORE_ERROR_ACCES, "failed to seek in metadata file"); return -1; }
    char buf [1024];
    snprintf (buf, sizeof (buf), 
              "id: %s\n"      \
              "limit: %lld\n" \
              "revocation: %d\n" \
              "snapshot: %d\n" \
              "format: %d\n",
              bs->id,
              bs->limit_blocks,
              bs->revocation_policy,
              bs->snapshot_policy,
              bs->format);
    int slen = strlen (buf);
    int len = write (bs->fd, buf, slen);
    if (len != slen)
        { ERR (BLOBSTORE_ERROR_NOENT, "failed to write to the metadata file"); return -1; }
        
    return 0;
}

enum { 
    DMSETUP,
    ROOTWRAP, 
    LASTHELPER
};

static char * helpers [LASTHELPER] = {
    "dmsetup",
    "euca_rootwrap", 
};

static char * helpers_path [LASTHELPER];
static int initialized = 0;

int blobstore_init (void)
{
    int ret = 0;

    if (!initialized) {
        ret = diskutil_init(FALSE); // blobstore does not invoke GRUB-related functions
        if (ret) {
            ERR (BLOBSTORE_ERROR_UNKNOWN, "failed to initialize diskutil library");
        } else {
            ret = verify_helpers (helpers, helpers_path, LASTHELPER);
            if (ret) {
                for (int i=0; i<LASTHELPER; i++) {
                    if (helpers_path [i] == NULL)
                        logprintfl (EUCAERROR, "ERROR: missing a required handler: %s\n", helpers[i]);
                }
                ERR (BLOBSTORE_ERROR_UNKNOWN, "failed to initialize blobstore library");
            } else {
                initialized = 1;
            }
        }
    }
    
    return ret;
}

int blobstore_cleanup (void)
{
    diskutil_cleanup();
    return 0;
}

blobstore * blobstore_open ( const char * path, 
                             unsigned long long limit_blocks,
                             unsigned int flags, // BLOBSTORE_FLAG_CREAT - same semantcs as for open() flags
                             blobstore_format_t format,
                             blobstore_revocation_t revocation_policy,
                             blobstore_snapshot_t snapshot_policy)
{
    int saved_errno;

    if (blobstore_init()) 
        return NULL;

    blobstore * bs = calloc (1, sizeof (blobstore));
    if (bs==NULL) {
        ERR (BLOBSTORE_ERROR_NOMEM, NULL);
        goto out;
    }
    safe_strncpy (bs->path, path, sizeof(bs->path)); // TODO: canonicalize path
    char meta_path [PATH_MAX];
    snprintf (meta_path, sizeof(meta_path), "%s/%s", bs->path, BLOBSTORE_METADATA_FILE);

    int write_flags = 0;
    if (flags & BLOBSTORE_FLAG_CREAT) {
        write_flags = BLOBSTORE_FLAG_CREAT | BLOBSTORE_FLAG_EXCL;
    };

 write_metadata:
    
    if (write_flags) {
        _blobstore_errno = BLOBSTORE_ERROR_OK;
        _err_off();
        bs->fd = open_and_lock (meta_path, write_flags, 0, BLOBSTORE_FILE_PERM);
        _err_on();
        if (bs->fd != -1) { // managed to create or open blobstore metadata file and got exclusive lock
            
            // the intention is to create the blobstore for the first time
            if (write_flags & BLOBSTORE_FLAG_CREAT) {
                gen_id (bs->id, sizeof(bs->id));
                bs->limit_blocks = limit_blocks;
                bs->revocation_policy = (revocation_policy==BLOBSTORE_REVOCATION_ANY) ? BLOBSTORE_REVOCATION_NONE : revocation_policy;
                bs->snapshot_policy = (snapshot_policy==BLOBSTORE_SNAPSHOT_ANY) ? BLOBSTORE_SNAPSHOT_DM : snapshot_policy; // TODO: verify that DM is available?
                bs->format = (format==BLOBSTORE_FORMAT_ANY) ? BLOBSTORE_FORMAT_FILES : format;
                
                // write metadata to disk
                write_store_metadata (bs);

            } else if (write_flags & BLOBSTORE_FLAG_RDWR) { // the intention is to adjust metadata
                if (read_store_metadata (bs))
                    goto free;
                assert (bs->id);
                if (limit_blocks) 
                    bs->limit_blocks = limit_blocks;
                if (revocation_policy != BLOBSTORE_REVOCATION_ANY)
                    bs->revocation_policy = revocation_policy;
                write_store_metadata (bs);
            }
            close_and_unlock (bs->fd); // try to close, thus giving up the exclusive lock
        }
        if (_blobstore_errno != BLOBSTORE_ERROR_OK && // either open or write failed
            _blobstore_errno != BLOBSTORE_ERROR_EXIST && // it is OK if file already exists
            _blobstore_errno != BLOBSTORE_ERROR_AGAIN ) { // it is OK if we lost the race for the write lock
            ERR (_blobstore_errno, "failed to open or create blobstore");
            goto free;
        }
    }

    // now (re)open, with a shared read lock
    bs->fd = open_and_lock (meta_path, BLOBSTORE_FLAG_RDONLY, BLOBSTORE_METADATA_TIMEOUT_USEC, BLOBSTORE_FILE_PERM);
    if (bs->fd == -1) {
        goto free;
    }
    if ( read_store_metadata (bs) ) { // try reading metadata
        goto free;
    }

    // verify that parameters are not being changed
    if (limit_blocks && 
        limit_blocks != bs->limit_blocks) {
        if (flags & BLOBSTORE_FLAG_STRICT ) {
            ERR (BLOBSTORE_ERROR_INVAL, "'limit_blocks' does not match existing blobstore");
            goto free;
        } else {
            logprintfl (EUCAINFO, "adjusting blobstore limit from %d to %d\n", bs->limit_blocks, limit_blocks);
            write_flags = BLOBSTORE_FLAG_RDWR;
            close_and_unlock (bs->fd);
            goto write_metadata;
        }
    }
    if (snapshot_policy != BLOBSTORE_SNAPSHOT_ANY && 
        snapshot_policy != bs->snapshot_policy) {
        ERR (BLOBSTORE_ERROR_INVAL, "'snapshot_policy' does not match existing blobstore");
        goto free;
    }
    if (format != BLOBSTORE_FORMAT_ANY && 
        format != bs->format) {
        ERR (BLOBSTORE_ERROR_INVAL, "'format' does not match existing blobstore");
        goto free;
    } 
    if (revocation_policy != BLOBSTORE_REVOCATION_ANY &&
        revocation_policy != bs->revocation_policy) {
        if (flags & BLOBSTORE_FLAG_STRICT) {
            ERR (BLOBSTORE_ERROR_INVAL, "'revocation_policy' does not match existing blobstore"); // TODO: maybe make revocation_policy changeable after creation
            goto free;
        } else {
            write_flags = BLOBSTORE_FLAG_RDWR;
            close_and_unlock (bs->fd);
            goto write_metadata;
        }
    }
    int fd = bs->fd;
    bs->fd = -1;
    close_and_unlock (fd);
    goto out;

 free:
    saved_errno = _blobstore_errno;
    close_and_unlock (bs->fd);
    if (bs) {
        free (bs);
        bs = NULL;
    }
    _blobstore_errno = saved_errno;

 out:
    return bs;
}

// frees the blobstore handle
int blobstore_close ( blobstore * bs )
{
    free (bs);
    return 0;
}

static pthread_mutex_t _blobstore_lock_mutex = PTHREAD_MUTEX_INITIALIZER; // process-global mutex

// locks the blobstore 
int blobstore_lock ( blobstore * bs, long long timeout_usec)
{
    char meta_path [PATH_MAX];
    snprintf (meta_path, sizeof(meta_path), "%s/%s", bs->path, BLOBSTORE_METADATA_FILE);

    logprintfl (EUCADEBUG2, "{%u} blobstore_lock: called for %s\n", (unsigned int)pthread_self(), bs->path);
    int fd = open_and_lock (meta_path, BLOBSTORE_FLAG_RDWR, timeout_usec, BLOBSTORE_FILE_PERM);
    if (fd!=-1)
        bs->fd = fd;
    return fd;
}

// unlocks the blobstore
int blobstore_unlock ( blobstore * bs )
{
    int fd = bs->fd;
    bs->fd = -1;
    logprintfl (EUCADEBUG2, "{%u} blobstore_unlock: called for %s\n", (unsigned int)pthread_self(), bs->path);
    return close_and_unlock (fd);
}

// if no outside references to store or blobs exist, and 
// no blobs are protected, deletes the blobs, the store metadata, 
// and frees the blobstore handle 
int blobstore_delete ( blobstore * bs ) 
{
    char meta_path [PATH_MAX];
    snprintf (meta_path, sizeof(meta_path), "%s/%s", bs->path, BLOBSTORE_METADATA_FILE);
    unlink (meta_path);
    free (bs);
    
    return -1; // TODO: implement blobstore_delete properly
}

int blobstore_get_error (void) 
{
    return _blobstore_errno;
}

// helper for setting paths, depending on blockblob_path_t
//
//  given BLOCKBLOB_PATH_X: x = tolower(X)
//  
//  for BLOBSTORE_FORMAT_FILES:     BS/BB.x
//  for BLOBSTORE_FORMAT_DIRECTORY: BS/BB/x
//
//  where BS is blobstore path and BB is a blockblob id.
//  BB may have '/' in it, thus placing all blob-related
//  files in a deeper dir hierarchy

static int set_blockblob_metadata_path (blockblob_path_t path_t, const blobstore * bs, const char * bb_id, char * path, size_t path_size)
{
    char base [PATH_MAX];
    snprintf (base, sizeof (base), "%s/%s", bs->path, bb_id);

    char name [32];
    switch (path_t) {
    case BLOCKBLOB_PATH_BLOCKS:   safe_strncpy (name, blobstore_metadata_suffixes[BLOCKBLOB_PATH_BLOCKS],   sizeof (name)); break;
    case BLOCKBLOB_PATH_LOCK:     safe_strncpy (name, blobstore_metadata_suffixes[BLOCKBLOB_PATH_LOCK],     sizeof (name)); break;
    case BLOCKBLOB_PATH_DM:       safe_strncpy (name, blobstore_metadata_suffixes[BLOCKBLOB_PATH_DM],       sizeof (name)); break;
    case BLOCKBLOB_PATH_DEPS:     safe_strncpy (name, blobstore_metadata_suffixes[BLOCKBLOB_PATH_DEPS],     sizeof (name)); break;
    case BLOCKBLOB_PATH_LOOPBACK: safe_strncpy (name, blobstore_metadata_suffixes[BLOCKBLOB_PATH_LOOPBACK], sizeof (name)); break;
    case BLOCKBLOB_PATH_SIG:      safe_strncpy (name, blobstore_metadata_suffixes[BLOCKBLOB_PATH_SIG],      sizeof (name)); break;
    case BLOCKBLOB_PATH_REFS:     safe_strncpy (name, blobstore_metadata_suffixes[BLOCKBLOB_PATH_REFS],     sizeof (name)); break;
    case BLOCKBLOB_PATH_HOLLOW:   safe_strncpy (name, blobstore_metadata_suffixes[BLOCKBLOB_PATH_HOLLOW],   sizeof (name)); break;
    default:
        ERR (BLOBSTORE_ERROR_INVAL, "invalid path_t");
        return -1;
    }

    switch (bs->format) {
    case BLOBSTORE_FORMAT_FILES:
        snprintf (path, path_size, "%s.%s", base, name);
        break;
    case BLOBSTORE_FORMAT_DIRECTORY:
        snprintf (path, path_size, "%s/%s", base, name);
        break;
    default:
        ERR (BLOBSTORE_ERROR_INVAL, "invalid bs->format");
        return -1;
    }

    return 0;
}

// write string 'str' into a specific metadata file (based on 'path_t') of blob 'bb_id'
// returns 0 for success or -1 for error
static int write_blockblob_metadata_path (blockblob_path_t path_t, const blobstore * bs, const char * bb_id, const char * str)
{
    int ret = 0;
    char path [PATH_MAX];
    set_blockblob_metadata_path (path_t, bs, bb_id, path, sizeof (path));

    int fd = open_and_lock (path, 
                            BLOBSTORE_FLAG_CREAT | BLOBSTORE_FLAG_RDWR,
                            BLOBSTORE_METADATA_TIMEOUT_USEC, 
                            BLOBSTORE_FILE_PERM);
    if (fd == -1)
        return -1;
    int size = buf_to_fd (fd, str, strlen (str));
    int ret_close = close_and_unlock (fd);
    if (size != strlen (str)) {
        // set the error code, possibly overriding one set by close_and_unlock
        ERR (BLOBSTORE_ERROR_UNKNOWN, "failed to write desired number of characters to metadata file");
        ret = -1;
    } else if (ret_close != 0) {
        ret = -1; // close_and_unlock should have set the error code
    }
    
    return ret;
}

// reads contents of a specific metadata file (based on 'path_t') of blob 'bb_id' into string 'str' up to 'str_size'
// returns number of bytes read or -1 in case of error
static int read_blockblob_metadata_path (blockblob_path_t path_t, const blobstore * bs, const char * bb_id, char * str, int str_size)
{
    char path [PATH_MAX];
    set_blockblob_metadata_path (path_t, bs, bb_id, path, sizeof (path));

    int fd = open_and_lock (path, 
                            BLOBSTORE_FLAG_RDONLY,
                            BLOBSTORE_METADATA_TIMEOUT_USEC, 
                            BLOBSTORE_FILE_PERM);
    if (fd == -1)
        return -1;
    int size = fd_to_buf (fd, str, str_size);
    int ret_close = close_and_unlock (fd);
    if (size < 1) { 
        // set the error code, possibly overriding one set by close_and_unlock
        ERR (BLOBSTORE_ERROR_NOENT, "blockblob metadata size is too small"); 
        size = -1;
    } else if (ret_close != 0) {
        size = -1; // close_and_unlock should have set the error code
    }

    return size;
}

// writes strings from 'array' or size 'array_size' (which can be 0) line-by-line
// into a specific metadata file (based on 'path_t') of blob 'bb_id'
// returns 0 for success and -1 for error
static int write_array_blockblob_metadata_path (blockblob_path_t path_t, const blobstore * bs, const char * bb_id, char ** array, int array_size)
{
    int ret = 0;
    char path [MAX_PATH];
    set_blockblob_metadata_path (path_t, bs, bb_id, path, sizeof (path));

    mode_t old_umask = umask (~BLOBSTORE_FILE_PERM);
    FILE * fp = fopen (path, "w");
    umask (old_umask);
    if (fp == NULL) {
        PROPAGATE_ERR (BLOBSTORE_ERROR_UNKNOWN);
        return -1;
    }

    for (int i=0; i<array_size; i++) {
        if (fprintf (fp, "%s\n", array [i]) < 0) {
            PROPAGATE_ERR (BLOBSTORE_ERROR_UNKNOWN);
            ret = -1;
            break;
        }
    }
    if (fclose (fp) == -1) {
        PROPAGATE_ERR (BLOBSTORE_ERROR_UNKNOWN);
        ret = -1;
    }
    return ret;
}

// reads lines from a specific metadata file (based on 'path_t') of blob 'bb_id',
// places each line into a newly allocated string, arranges pointers to these
// strings into a newly allocated array of pointers, and places the size into 'array_size'
// caller must deallocate the array and the strings pointed to by the array
// returns 0 for success and -1 for error
static int read_array_blockblob_metadata_path (blockblob_path_t path_t, const blobstore * bs, const char * bb_id, char *** array, int * array_size)
{
    int ret = 0;
    char path [MAX_PATH];
    set_blockblob_metadata_path (path_t, bs, bb_id, path, sizeof (path));

    FILE * fp = fopen (path, "r");
    if (fp == NULL) {
        * array = NULL;
        * array_size = 0;
        return 0;
    }

    int i;
    size_t n;
    char ** lines = NULL;
    for (i=0; !feof(fp); i++) {
        char * line = NULL;
        if (getline (&line, &n, fp)==-1) {
            if (feof(fp)) { // need this when '\n' is last char in the file
                free (line);
                break;
            }
            PROPAGATE_ERR (BLOBSTORE_ERROR_UNKNOWN);
            ret = -1;
            break;
        }
        int len = strlen (line);
        if (len && line [len-1] == '\n') {
            line [len-1] = '\0'; // chop off '\n'
        }
        char ** bigger_lines = realloc (lines, (i+1) * sizeof(char *));
        if (bigger_lines==NULL) {
            ERR (BLOBSTORE_ERROR_NOMEM, NULL);
            ret = -1;
            break;
        }
        lines = bigger_lines;
        lines [i] = line;
    }
    if (fclose (fp) == -1) {
        PROPAGATE_ERR (BLOBSTORE_ERROR_UNKNOWN);
        ret = -1;
    }
    if (ret == -1) {
        if (lines!=NULL) {
            for (int j=0; j<i; j++) {
                free (lines [j]);
            }
            free (lines);
        }
    } else {
        * array = lines;
        * array_size = i;
    }

    return ret;
}

static int update_entry_blockblob_metadata_path (blockblob_path_t path_t, const blobstore * bs, const char * bb_id, const char * entry, int removing)
{
    int ret = 0;

    // read in current entries from a metadata file
    char ** entries;
    int entries_size;
    if (read_array_blockblob_metadata_path (path_t, bs, bb_id, &entries, &entries_size)==-1) {
        return -1;
    }

    // see if this entry is already in the metadata file
    int found = -1;
    for (int j=0; j<entries_size; j++) {
        if (!strcmp (entry, entries [j])) {
            found = j;
            break;
        }
    }
    
    if (found==-1 && !removing) { // not in the file and adding
        entries_size++;
        char ** bigger_entries = calloc (entries_size, sizeof (char *));
        if (bigger_entries == NULL) {
            ret = -1;
            goto cleanup;
        }
        for (int i=0; i<entries_size-1; i++) { // we do not trust realloc
            bigger_entries [i] = entries [i];
        }
        if (entries) free (entries);
        entries = bigger_entries;
        entries [entries_size-1] = strdup (entry);

    } else if (found!=-1 && removing) { // in the file and deleting
        free (entries [found]);
        entries_size--;
        if (entries_size && found!=entries_size) {  // still entries left and not deleting last one
            entries [found] = entries [entries_size]; // move the last one over the one we're deleting
        }

    } else { // nothing to do
        goto cleanup;
    }
    
    // save new entries into the metadata file
    if (write_array_blockblob_metadata_path (path_t, bs, bb_id, entries, entries_size)==-1) {
        ret = -1;
    }

 cleanup:
    if (entries != NULL) {
        for (int j=0; j<entries_size; j++) {
            free (entries [j]);
        }
        free (entries);
    }
    return ret;
}

// if 'path' looks like a blockblob metadata file (based on the suffix), 
// return the type of the file and set bb_id appropriately, else
// return 0 if it is an unrecognized file, else
// return -1 for error
static int typeof_blockblob_metadata_path (const blobstore * bs, const char * path, char * bb_id, unsigned int bb_id_size)
{
    assert (path);
    assert (bs->path);
    assert (strstr (path, bs->path) == path);

    const char * rel_path = path + strlen (bs->path) + 1; // +1 for '/'
    int p_len = strlen (rel_path);

    for (int i=1; i<BLOCKBLOB_PATH_TOTAL; i++) { // start at 1 to avoid BLOCKBLOB_PATH_NONE
        char suffix [1024];
        if (bs->format == BLOBSTORE_FORMAT_DIRECTORY) {
            snprintf (suffix, sizeof (suffix), "/%s", blobstore_metadata_suffixes [i]);
        } else {
            snprintf (suffix, sizeof (suffix), ".%s", blobstore_metadata_suffixes [i]);
        }
        unsigned int s_len = strlen (suffix);
        const char * sp = suffix   + s_len - 1; // last char of suffix
        const char * pp = rel_path + p_len - 1; // last char of (relative) path
        unsigned int matched;
        for (matched=0; * sp == * pp; sp--, pp--) {
            matched++;
            if (sp == suffix) 
                break;
            if (pp == rel_path) 
                break;
        }
        if (matched==s_len // whole suffix matched
            && matched<p_len) { // there is more than the suffix
            if ((bb_id_size - 1) < ( p_len - s_len)) // not enough room in bb_id
                return -1;
            strncpy (bb_id, rel_path, p_len - s_len); // extract the name, without the suffix
            bb_id [p_len - s_len] = '\0'; // terminate the string
            return i;
        }
    }
    return 0;
}

// returns the number of files and directories deleted as part of
// removing the blob (thus, 0 means there was nothing to delete)
static int delete_blockblob_files (const blobstore * bs, const char * bb_id) 
{
    int count = 0;

    for (int path_t=1; path_t<BLOCKBLOB_PATH_TOTAL; path_t++) { // go through all types of blob-related files...
        char path [PATH_MAX];
        set_blockblob_metadata_path ((blockblob_path_t) path_t, bs, bb_id, path, sizeof (path));
        if (unlink (path)==0) // ...and try deleting them
            count++;
    }

    // delete blob's subdirectories if there are any
    char path [PATH_MAX];
    snprintf (path, sizeof (path), "%s/%s%s", bs->path, bb_id, bs->format == BLOBSTORE_FORMAT_DIRECTORY ? "/" : "");
    for (int i = strlen (path) - 1; i > 0; i--) {
        if (path [i] == '/') {
            path [i] = '\0';
            if (rmdir (path)==0) {
                count++;
            } else {
                break;
            }
        }
    }
            
    return count;
}

// helper for ensuring a directory required by blob exists
// returns: 0 = already existed, 1 = created OK, -1 = error
static int ensure_blockblob_metadata_path (const blobstore * bs, const char * bb_id) 
{
    char base [PATH_MAX];
    snprintf (base, sizeof (base), "%s/%s", bs->path, bb_id);
    return ensure_directories_exist (base, !(bs->format == BLOBSTORE_FORMAT_DIRECTORY), NULL, NULL, BLOBSTORE_DIRECTORY_PERM);
}

static void free_bbs ( blockblob * bbs )
{
    while (bbs) {
        blockblob * next_bb = bbs->next;
        free (bbs);
        bbs = next_bb;
    }
}

static unsigned int check_in_use ( blobstore * bs, const char * bb_id, long long timeout_usec)
{
    unsigned int in_use = 0;
    char path [PATH_MAX];

    // determine the path of the .lock file for this blob
    set_blockblob_metadata_path (BLOCKBLOB_PATH_LOCK, bs, bb_id, path, sizeof (path));

    _err_off(); // do not complain if metadata files do not exist
    int fd = open_and_lock (path, BLOBSTORE_FLAG_RDWR, timeout_usec, BLOBSTORE_FILE_PERM); // try opening to see what happens
    if (fd != -1) {
        close_and_unlock (fd); 
    } else {
        in_use |= BLOCKBLOB_STATUS_OPENED; // TODO: check if open failed for other reason?
    }
    
    if (read_blockblob_metadata_path (BLOCKBLOB_PATH_REFS, bs, bb_id, path, sizeof (path))>0) {
        in_use |= BLOCKBLOB_STATUS_MAPPED;
    }

    if (read_blockblob_metadata_path (BLOCKBLOB_PATH_DEPS, bs, bb_id, path, sizeof (path))>0) {
        in_use |= BLOCKBLOB_STATUS_BACKED;
    }
    _err_on();

    return in_use;
}

static void set_device_path (blockblob * bb)
{
    char ** dm_devs = NULL;
    int dm_devs_size = 0;
    
    _err_off(); // do not care if .dm file does not exist
    read_array_blockblob_metadata_path (BLOCKBLOB_PATH_DM, bb->store, bb->id, &dm_devs, &dm_devs_size);
    _err_on();
    
    if (dm_devs_size>0) { // .dm is there => set device_path to the device-mapper path
        snprintf (bb->device_path, sizeof(bb->device_path), DM_FORMAT, dm_devs [dm_devs_size-1]); // main device is the last one
        safe_strncpy (bb->dm_name, dm_devs [dm_devs_size-1], sizeof (bb->dm_name));
        for (int i=0; i<dm_devs_size; i++) {
            free (dm_devs [i]);
        }
        free (dm_devs);
    } else { // .dm is not there => set device_path to loopback
        char lo_dev [PATH_MAX] = "";
        _err_off(); // do not care if loopback file does not exist
        read_blockblob_metadata_path (BLOCKBLOB_PATH_LOOPBACK, bb->store, bb->id, lo_dev, sizeof (lo_dev));
        _err_on();
        safe_strncpy (bb->device_path, lo_dev, sizeof (bb->device_path));
    }
}

static blockblob ** walk_bs (blobstore * bs, const char * dir_path, blockblob ** tail_bb, const blockblob * bb_to_avoid) 
{
    int ret = 0;
    DIR * dir;
    if ((dir=opendir(dir_path))==NULL) {
        return tail_bb; // ignore access errors in blobstore directory
    }

    struct dirent * dir_entry;
    while ((dir_entry=readdir(dir))!=NULL) {
        char * entry_name = dir_entry->d_name;

        if (!strcmp(".", entry_name) ||
            !strcmp("..", entry_name) ||
            !strcmp(BLOBSTORE_METADATA_FILE, entry_name))
            continue; // ignore known unrelated files

        // get the path of the directory item
        char entry_path [BLOBSTORE_MAX_PATH];
        snprintf (entry_path, sizeof (entry_path), "%s/%s", dir_path, entry_name);
        struct stat sb;
        if (stat(entry_path, &sb)==-1)
            continue; // ignore access errors in the blobstore directory (TODO: is this wise?)

        // recurse if this is a directory
        if (S_ISDIR (sb.st_mode)) {
            tail_bb = walk_bs (bs, entry_path, tail_bb, bb_to_avoid);
            if (tail_bb == NULL) {
                closedir(dir);
                return NULL;
            }
            continue;
        }
        
        char blob_id [BLOBSTORE_MAX_PATH];
        if (typeof_blockblob_metadata_path (bs, entry_path, blob_id, sizeof(blob_id)) != BLOCKBLOB_PATH_BLOCKS)
            continue; // ignore all files except .blocks file

        if (bb_to_avoid!=NULL && strncmp (blob_id, bb_to_avoid->id, sizeof (blob_id))==0)
            continue; // avoid that particular blockblob

        blockblob * bb = calloc (1, sizeof (blockblob));
        if (bb==NULL) {
            ret = 1;
            goto free;
        }
        * tail_bb = bb; // add to LL
        tail_bb = & (bb->next);

        // fill out the struct
        bb->store = bs;
        safe_strncpy (bb->id, blob_id, sizeof(bb->id));
        safe_strncpy (bb->blocks_path, entry_path, sizeof(bb->blocks_path));
        set_device_path (bb); // read .dm and .loopback and set bb->device_path accordingly
        bb->size_bytes = sb.st_size;
        bb->blocks_allocated = sb.st_blocks;
        bb->last_accessed = sb.st_atime;
        bb->last_modified = sb.st_mtime;
        bb->snapshot_type = BLOBSTORE_FORMAT_ANY; // it is not necessary to know whether this is a snapshot
        bb->in_use = check_in_use (bs, bb->id, 0);

        // see if it's hollow
        char buf [64];
        if (read_blockblob_metadata_path (BLOCKBLOB_PATH_HOLLOW, bb->store, bb->id, buf, sizeof (buf))!=-1) {
            bb->is_hollow = TRUE;
        }

        // if there is a .refs file, subtract the mapped blocks, if any, from the size
        char ** array = NULL;
        int array_size = 0;
        if (read_array_blockblob_metadata_path (BLOCKBLOB_PATH_DEPS, bb->store, bb->id, &array, &array_size)!=-1) {
            for (int i=0; i<array_size; i++) {
                char * store_path  = strtok (array [i], " ");
                char * blob_id     = strtok (NULL, " "); 
                char * rel_type    = strtok (NULL, " ");
                char * start_block = strtok (NULL, " ");
                char * len_blocks  = strtok (NULL, " ");
                if (rel_type && len_blocks && strcmp (rel_type, blobstore_relation_type_name [BLOBSTORE_MAP])==0) {
                    bb->size_bytes -= strtoull (len_blocks, NULL, 0) * 512LL;
                }
            }
        }
        if (array)
            free (array);
    }

 free:
    closedir (dir);
    return tail_bb;
}

// runs through the blobstore and puts all found blockblobs 
// into a linked list, returning its head
static blockblob * scan_blobstore ( blobstore * bs, const blockblob * bb_to_avoid )
{
    blockblob * bbs = NULL;
    if (walk_bs (bs, bs->path, &bbs, bb_to_avoid)==NULL) {
        if (bbs) 
            free_bbs (bbs);
        bbs = NULL;
    }
    
    return bbs;
}

static int compare_bbs (const void * bb1, const void * bb2) 
{
    return (int)((*(blockblob **)bb1)->last_modified - (*(blockblob **)bb2)->last_modified);
}

static int delete_blob_state (blockblob * bb, long long timeout_usec, char do_force);

static long long purge_blockblobs_lru ( blobstore * bs, blockblob * bb_list, long long need_blocks ) 
{
    int list_length = 0;
    long long purged = 0;

    for (blockblob * bb = bb_list; bb; bb = bb->next) {
        list_length++;
    }

    if (list_length) {
        blockblob * bb;
        int i;

        blockblob ** bb_array = (blockblob **) calloc (list_length, sizeof (blockblob *));
        if (!bb_array)
            return purged;

        for (i=0, bb = bb_list; bb; bb = bb->next, i++) {
            bb_array [i] = bb;
        }

        qsort (bb_array, list_length, sizeof (blockblob *), compare_bbs);

        int iteration = 0;
        int deleted;
        do { // iterate multiple times in case there are dependencies (TODO: unify with _fsck's iteration code?)
            deleted = 0; // deleted in this round
            for (i=0; i<list_length; i++) {
                bb = bb_array [i];
                if (bb == NULL) // was either deleted or deemed undeletable on previous iteration
                    continue;
                bb->in_use = check_in_use (bs, bb->id, 0); // record in-use status

                char code = '?';
                if (bb->in_use & BLOCKBLOB_STATUS_MAPPED) {
                    // mapped blobs have children, thus cannot be deleted at this iteration
                    code = 'C';
                    
                } else if (bb->in_use & BLOCKBLOB_STATUS_OPENED) {
                    bb_array [i] = NULL; // mark it to skip in the future
                    code = 'O'; 
                    
                } else if (delete_blob_state (bb, BLOBSTORE_DELETE_TIMEOUT_USEC, 1)==-1) {
                    bb_array [i] = NULL; // mark it to skip in the future
                    code = '!';
                    
                } else {
                    purged += round_up_sec (bb->size_bytes) / 512;
                    bb_array [i] = NULL; // mark it to skip in the future
                    code = 'D';
                    deleted++;
                }
                logprintfl (EUCADEBUG, "LRU %d %08d: %29s %c%c%c %c %9llu %s", iteration, purged, bb->id,
                            (bb->in_use & BLOCKBLOB_STATUS_OPENED) ? ('o') : ('-'), // o = open
                            (bb->in_use & BLOCKBLOB_STATUS_BACKED) ? ('p') : ('-'), // p = has parents
                            (bb->in_use & BLOCKBLOB_STATUS_MAPPED) ? ('c') : ('-'), // c = has children
                            code, // outcome codes: D=deleted, else C=children, !=undeletable, O=open
                            bb->size_bytes / 512L, // size is in sectors
                            ctime (&(bb->last_modified))); // ctime adds a newline
            }
            iteration++;
        } while (deleted && (purged<need_blocks));
        free (bb_array);
    }
    
    return purged;
}

int blobstore_stat (blobstore * bs, blobstore_meta * meta)
{
    int ret = 0;

    if (blobstore_lock(bs, BLOBSTORE_LOCK_TIMEOUT_USEC)==-1) { // lock it so we can traverse blobstore safely
        return ERROR;
    }
    
    // put existing items in the blobstore into a LL
    _blobstore_errno = BLOBSTORE_ERROR_OK;
    blockblob * bbs = scan_blobstore (bs, NULL);
    if (bbs==NULL) {
        if (_blobstore_errno != BLOBSTORE_ERROR_OK) {
            goto unlock;
        }
    }
    
    // analyze the LL, calculating sizes
    meta->blocks_allocated = 0;
    meta->blocks_unlocked = 0;
    meta->blocks_locked = 0;
    meta->num_blobs = 0;
    for (blockblob * abb = bbs; abb; abb=abb->next) { // TODO: unify this with locked/unlocked calculation in open()
        long long abb_size_blocks = round_up_sec (abb->size_bytes) / 512;
        if (abb->in_use & BLOCKBLOB_STATUS_OPENED) {
            meta->blocks_locked += abb_size_blocks; // these can't be purged if we need space (TODO: look into recursive purging of unused references?)
        } else {
            meta->blocks_unlocked += abb_size_blocks; // these potentially can be purged, unless they are depended on by locked ones
        }
        meta->blocks_allocated += abb->blocks_allocated;
        meta->num_blobs++;
    }
    
 unlock:

    if (blobstore_unlock(bs)==-1) {
        ERR (BLOBSTORE_ERROR_UNKNOWN, "failed to unlock the blobstore");
    }

    safe_strncpy (meta->id, bs->id, sizeof (meta->id));
    meta->revocation_policy = bs->revocation_policy;
    meta->snapshot_policy = bs->snapshot_policy;
    meta->format = bs->format;
    meta->blocks_limit = bs->limit_blocks;

    return ret;
}

/*
 * read .refs file content and return any entries that point to blobs that no longer exist
 * return value is size of the array placed into *refs, which caller must free, or -1 on error
 */
static int get_stale_refs (const blockblob * bb, char *** refs)
{
    blobstore * bs = bb->store;
    char ** array = NULL;
    int array_size = 0;
    int stale_refs = 0;

    if (read_array_blockblob_metadata_path (BLOCKBLOB_PATH_REFS, bb->store, bb->id, &array, &array_size)!=-1) {
        for (int i=0; i<array_size; i++) {
            char ref [BLOBSTORE_MAX_PATH+MAX_DM_NAME+1];
            safe_strncpy (ref, array [i], sizeof (ref));
            
            char * store_path = strtok (array [i], " ");
            char * blob_id    = strtok (NULL, " "); // the remaining entries in array[i] are ignored
            char ref_exists = 0;
            
            if (strlen (store_path)<1 || strlen (blob_id)<1)
                goto stale_ref;
            
            blobstore * ref_bs = bs;
            if (strcmp (bs->path, store_path)) { // if deleting reference in a different blobstore
                // need to open it
                ref_bs = blobstore_open (store_path, 0, BLOBSTORE_FLAG_CREAT, BLOBSTORE_FORMAT_ANY, BLOBSTORE_REVOCATION_ANY, BLOBSTORE_SNAPSHOT_ANY);
                if (ref_bs == NULL) // blobstore with a child blob does not exist
                    goto stale_ref;
            }
            
            blockblob * ref_bb = blockblob_open (ref_bs, blob_id, 0, 0, NULL, BLOBSTORE_FIND_TIMEOUT_USEC);
            if (ref_bb) {
                blockblob_close (ref_bb);
                ref_exists = 1;
            } else {
                if (_blobstore_errno != BLOBSTORE_ERROR_NOENT) // conservatively assume that unless the error says otherwise, the blob exists
                    ref_exists = 1;
            }
            if (ref_bs != bs) {
                blobstore_close (ref_bs);
            }
            
        stale_ref:
            
            if (ref_exists) {
                free (array [i]); // free names of refs that exist
                array [i] = NULL;
            } else {
                strcpy (array [i], ref); // since strtok() clobbered the original value
                stale_refs++;
            }
        }
    }

    if (stale_refs>0) {
        if (refs) {
            * refs = calloc (stale_refs, sizeof (char *));
            if (* refs == NULL) {
                stale_refs = -1; // OOM error
            }
        }
        for (int i=0, j=0; i<array_size; i++) {
            if (array [i]) { // ref does not exist
                if (refs && *refs) {
                    (* refs) [j++] = array [i];
                    assert (j<=stale_refs);
                } else {
                    free (array [i]);
                }
            }
        }
    }
    
    if (array_size > 0)
        free (array);

    return stale_refs;
}

static int blockblob_check (const blockblob * bb);

// integrity check of the blobstore
// 
// with a non-NULL examiner(), each found blob is passed to it
// for examination and the blob is deleted if function returns non-zero
int blobstore_fsck (blobstore * bs, int (* examiner) (const blockblob * bb))
{
    int ret = 0;
    
    if (blobstore_lock(bs, BLOBSTORE_LOCK_TIMEOUT_USEC)==-1) { // lock it so we can traverse blobstore safely
        ERR (BLOBSTORE_ERROR_UNKNOWN, "failed to lock the blobstore");
        return -1;
    }
    
    // put existing items in the blobstore into a LL
    _blobstore_errno = BLOBSTORE_ERROR_OK;
    blockblob * bbs = scan_blobstore (bs, NULL);

    if (blobstore_unlock (bs)==-1) {
        ERR (BLOBSTORE_ERROR_UNKNOWN, "failed to unlock the blobstore");
        ret = -1;
        goto free;
    }    

    if (bbs==NULL) {
        if (_blobstore_errno != BLOBSTORE_ERROR_OK) {
            ret = -1;
        }
        goto free;
    }

    {  // check objects in the blobstore

        unsigned int num_blobs = 0;
        unsigned int blobs_deleted = 0;
        unsigned int blobs_undeletable = 0;
        unsigned int blobs_unopenable = 0;
        unsigned int to_delete_prev = 0;
        unsigned int iterations = 1;
        for ( ; iterations < 10; iterations++) { // outer loop for multiple iterations over the list
            unsigned int to_delete = 0;
            
            // run through LL, examining each blockblob
            for (blockblob * abb = bbs; abb; abb=abb->next) {
                if (iterations==1)
                    num_blobs++; // count all blobs on the first iteration
                
                if (abb->store==NULL) // these were cleared or condemned on a previous iteration
                    continue;
                
                // examiner(), if specified, tell us whether to delete the blob
                if (blockblob_check (abb) ||        // blob state is inconsistent
                    (examiner && examiner (abb))) { // blobstore user condemned the blob
                    
                    blockblob * bb = blockblob_open (bs, abb->id, 0, 0, NULL, BLOBSTORE_FIND_TIMEOUT_USEC);
                    if (bb!=NULL) {
                        if (bb->in_use & BLOCKBLOB_STATUS_MAPPED) {

                            // Since we are checking integrity, do not trust .refs file blindly,
                            // but ensure that the entries -- blobs depending on this one -- exist

                            char ** stale_refs;
                            int num_stale_refs = get_stale_refs (bb, &stale_refs);
                            if (num_stale_refs > 0) {
                                for (int i=0; i<num_stale_refs; i++) {
                                    // update the .refs file to remove this entry
                                    logprintfl (EUCAINFO, "removing stale/corrupted reference in blob %s to %s\n", bb->id, stale_refs [i]);
                                    update_entry_blockblob_metadata_path (BLOCKBLOB_PATH_REFS, bb->store, bb->id, stale_refs [i], 1);
                                    free (stale_refs [i]);
                                }
                                free (stale_refs);
                            }
                            
                            // mapped blobs have children, thus cannot be deleted at this iteration
                            blockblob_close (bb);
                            to_delete++;
                           
                        } else if (blockblob_delete (bb, BLOBSTORE_DELETE_TIMEOUT_USEC, 1)==-1) {
                            logprintfl (EUCAWARN, "WARNING: failed to delete blockblob %s\n", abb->id);
                            blockblob_close (bb);
                            abb->store = NULL; // so it will get skipped on next iteration
                            blobs_undeletable++;
                            
                        } else {
                            logprintfl (EUCAINFO, "deleted stale/corrupted blob %s\n", abb->id);
                            abb->store = NULL; // so it will get skipped on next iteration
                            blobs_deleted++;
                        }
                    } else {
                        logprintfl (EUCAWARN, "could not open blockblob %s (it may be in use)\n", abb->id);
                        abb->store = NULL; // so it will get skipped on next iteration
                        blobs_unopenable++;
                    }
                }
            }
            assert (iterations<11);

            if (to_delete == to_delete_prev) // could not delete anything new this iteration
                break;
            to_delete_prev = to_delete;
            if (to_delete == 0)
                break;
        }
        
        if (num_blobs>0)
            logprintfl (EUCAINFO, "%s: examined %d blob(s) in %d iteration(s): "
                        "deleted %d, failed on %d + %d, failed to open %d\n", 
                        bs->path, num_blobs, iterations,
                        blobs_deleted, to_delete_prev, blobs_undeletable, blobs_unopenable);
    }
 free:
    if (bbs)
        free_bbs (bbs);
    
    return ret;
}

int blobstore_search ( blobstore * bs, const char * regex, blockblob_meta ** results )
{
    blockblob_meta * head = NULL;
    blockblob * bbs = NULL;
    int ret = 0;
    regex_t re;

    if (regcomp (&re, regex, REG_NOSUB) != 0) {
        ERR (BLOBSTORE_ERROR_UNKNOWN, "failed to parse search regular expression");
        return -1;
    }

    if (blobstore_lock(bs, BLOBSTORE_LOCK_TIMEOUT_USEC)==-1) { // lock it so we can traverse blobstore safely
        ERR (BLOBSTORE_ERROR_UNKNOWN, "failed to lock the blobstore");
        ret = -1;
        goto free;
    }
    
    // put existing items in the blobstore into a LL
    _blobstore_errno = BLOBSTORE_ERROR_OK;
    bbs = scan_blobstore (bs, NULL);
    if (bbs==NULL) {
        if (_blobstore_errno != BLOBSTORE_ERROR_OK) {
            ret = -1;
            goto free;
        }
    }
    
    // run through LL, looking for matches
    unsigned int num_blobs = 0;
    unsigned int blobs_matched = 0;
    blockblob_meta * prev = NULL;
    for (blockblob * abb = bbs; abb; abb=abb->next) {
        num_blobs++;
        if (regexec (&re, abb->id, 0, NULL, 0) != 0)
            continue;
        blobs_matched++;

        blockblob_meta * bm = calloc (1, sizeof (blockblob_meta));
        if (bm==NULL) {
            ERR (BLOBSTORE_ERROR_NOMEM, NULL);
            ret = -1;
            goto free;
        }

        safe_strncpy (bm->id, abb->id, sizeof (bm->id));
        bm->bs = bs;
        bm->size_bytes = abb->size_bytes;
        bm->in_use = abb->in_use;
        bm->is_hollow = abb->is_hollow;
        bm->last_accessed = abb->last_accessed;
        bm->last_modified = abb->last_modified;
        if (head==NULL) {
            head = bm;
        } else {
            prev->next = bm;
            bm->prev = prev;
        }
        prev = bm;
    }

    * results = head;
    ret = blobs_matched;

 free:
    regfree (&re); // free the regular expression
    if (bbs)
        free_bbs (bbs); // free the blockblobs LL returned by the search function

    if (blobstore_unlock (bs)==-1) {
        ERR (BLOBSTORE_ERROR_UNKNOWN, "failed to unlock the blobstore");
        ret = -1;
    }    

    if (ret < 0) { // there were problems, so free the partial linked list, if any
        for (blockblob_meta * bm = head; bm;) {
            blockblob_meta * next = bm->next;
            free (bm);
            bm = next;
        }
    }
    
    return ret;
}

int blobstore_delete_regex (blobstore * bs, const char * regex)
{
    blockblob_meta * matches = NULL;
    int found  = blobstore_search (bs, regex, &matches);
    int left_to_delete = found;
    int deleted;
    do { // iterate multiple times in case there are dependencies (TODO: unify with _fsck's iteration code?)
        deleted = 0; // deleted in this round
        for (blockblob_meta * bm = matches; bm; bm=bm->next) {
            blockblob * bb = blockblob_open (bs, bm->id, 0, 0, NULL, BLOBSTORE_FIND_TIMEOUT_USEC);
            if (bb!=NULL) {
                if (bb->in_use & BLOCKBLOB_STATUS_MAPPED) {
                    // mapped blobs have children, thus cannot be deleted at this iteration
                    blockblob_close (bb);
                    continue;
                }
                if (blockblob_delete (bb, BLOBSTORE_DELETE_TIMEOUT_USEC, 0)==-1) {
                    blockblob_close (bb);
                } else {
                    deleted++;
                }
            }
        }
    } while (deleted && (left_to_delete-=deleted));
    
    // free the search results
    for (blockblob_meta * bm = matches; bm;) {
        blockblob_meta * next = bm->next;
        free (bm);
        bm = next;
    }

    return (left_to_delete==0)?(found):(-1);
}

blockblob * blockblob_open ( blobstore * bs,
                             const char * id, // can be NULL if creating, in which case blobstore will pick a random ID
                             unsigned long long size_bytes, // on create: reserve this size; on open: verify the size, unless set to 0
                             unsigned int flags, // BLOBSTORE_FLAG_CREAT | BLOBSTORE_FLAG_EXCL - same semantcs as for open() flags, BLOBSTORE_FLAG_HOLLOW - when creating
                             const char * sig, // if non-NULL, on create sig is recorded, on open it is verified
                             unsigned long long timeout_usec ) // maximum wait, in microseconds
{
    long long size_blocks = round_up_sec (size_bytes) / 512;
    if (flags & ~(BLOBSTORE_FLAG_CREAT | BLOBSTORE_FLAG_EXCL | BLOBSTORE_FLAG_HOLLOW)) {
        ERR (BLOBSTORE_ERROR_INVAL, "only _CREAT, _EXCL, & _HOLLOW flags are allowed");
        return NULL;
    }
    if (id==NULL && !(flags & BLOBSTORE_FLAG_CREAT)) {
        ERR (BLOBSTORE_ERROR_INVAL, "NULL id is only allowed with _CREAT");
        return NULL;
    }
    if (size_blocks==0 && (flags & BLOBSTORE_FLAG_CREAT)) {
        ERR (BLOBSTORE_ERROR_INVAL, "size_blocks can be 0 only without _CREAT");
        return NULL;
    }
    if (size_blocks!=0 && (flags & BLOBSTORE_FLAG_CREAT) && (size_blocks > bs->limit_blocks) && !(flags && BLOBSTORE_FLAG_HOLLOW)) {
        ERR (BLOBSTORE_ERROR_NOSPC, NULL);
        return NULL;
    }

    logprintfl (EUCADEBUG2, "{%u} blockblob_open: opening blob id=%s flags=%d timeout=%lld\n", (unsigned int)pthread_self(), id, flags, timeout_usec);

    blockblob * bbs = NULL; // a temp LL of blockblobs, used for computing free space and for purging
    blockblob * bb = calloc (1, sizeof (blockblob));
    if (bb==NULL) {
        ERR (BLOBSTORE_ERROR_NOMEM, NULL);
        goto out;
    }
    
    bb->store = bs;
    if (id) {
        safe_strncpy (bb->id, id, sizeof(bb->id));
    } else {
        gen_id (bb->id, sizeof(bb->id));
    }
    bb->fd_lock = -1;
    bb->fd_blocks = -1;
    bb->size_bytes = size_bytes;
    set_blockblob_metadata_path (BLOCKBLOB_PATH_BLOCKS, bs, bb->id, bb->blocks_path, sizeof (bb->blocks_path));

    int blobstore_locked = 0;
    if (blobstore_lock(bs, timeout_usec)==-1) { // lock it so we can create blob's file atomically
        goto free; // failed to obtain a lock on the blobstore
    } else {
        blobstore_locked = 1;
    }
    int created_directory = ensure_blockblob_metadata_path (bs, bb->id); // TODO: maybe don't create directories needlessly if flags==0?
    if (created_directory==-1) {
        PROPAGATE_ERR (BLOBSTORE_ERROR_UNKNOWN);
        goto unlock;
    }
    if (blobstore_unlock(bs)==-1) {
        ERR (BLOBSTORE_ERROR_UNKNOWN, "failed to unlock the blobstore");
        goto free;
    }
    blobstore_locked = 0;

    int created_blob = 0;
    char lpath [PATH_MAX];
    set_blockblob_metadata_path (BLOCKBLOB_PATH_LOCK, bs, bb->id, lpath, sizeof (lpath));
    bb->fd_lock = open_and_lock (lpath, flags | BLOBSTORE_FLAG_RDWR, timeout_usec, BLOBSTORE_FILE_PERM); // blobs are always opened with exclusive write access
    if (bb->fd_lock == -1) { // failed to open/create and lock the blockblob
        goto clean;
    }
    char thread_id [512];
    snprintf (thread_id, sizeof (thread_id), "%d/%u", getpid(), (unsigned int)pthread_self());
    write (bb->fd_lock, thread_id, strlen (thread_id));

    // convert BLOBSTORE_* flags into standard Posix open() flags and open/create the blocks file
    int o_flags = 0;
    if (flags & BLOBSTORE_FLAG_RDONLY) {
        o_flags |= O_RDONLY;
    } else if ((flags & BLOBSTORE_FLAG_RDWR) ||
               (flags & BLOBSTORE_FLAG_CREAT)) {
        o_flags |= O_RDWR;
        if (flags & BLOBSTORE_FLAG_CREAT) {
            o_flags |= O_CREAT;
            // intentionally ignore _EXCL supplied without _CREAT
            if (flags & BLOBSTORE_FLAG_EXCL) 
                o_flags |= O_EXCL;
        }
    }
    bb->fd_blocks = open (bb->blocks_path, o_flags, BLOBSTORE_FILE_PERM);
    if (bb->fd_blocks == -1) { // failed to open/create the content file
        PROPAGATE_ERR (BLOBSTORE_ERROR_UNKNOWN);
        goto clean;
    }
    
    struct stat sb;
    if (fstat (bb->fd_blocks, &sb)==-1) {
        goto clean;
    }
        
    if (sb.st_size == 0) { // new blob
        created_blob = 1;
        
        if (blobstore_lock(bs, timeout_usec)==-1) { // lock it so we can traverse blobstore safely
            goto clean; // failed to obtain a lock on the blobstore
        } else {
            blobstore_locked = 1;
        }
        
        // put existing items in the blobstore into a LL
        _blobstore_errno = BLOBSTORE_ERROR_OK;
        bbs = scan_blobstore (bs, bb);
        if (bbs==NULL) {
            if (_blobstore_errno != BLOBSTORE_ERROR_OK) {
                goto clean;
            }
        }

        // a bit of a hack: HOLLOW blobs skip the blobstore limit check upon creation
        if (flags & BLOBSTORE_FLAG_HOLLOW) {
            bb->is_hollow = TRUE;
            if (write_blockblob_metadata_path (BLOCKBLOB_PATH_HOLLOW, bs, bb->id, "this blob is hollow\n"))
                goto clean;

        } else { // enforce blobstore limits

            // analyze the LL, calculating sizes
            long long blocks_unlocked = 0;
            long long blocks_locked = 0;
            unsigned int num_blobs = 0;
            for (blockblob * abb = bbs; abb; abb=abb->next) {
                long long abb_size_blocks = round_up_sec (abb->size_bytes) / 512;
                if (abb->is_hollow)
                    abb_size_blocks = 0;
                if (abb->in_use & BLOCKBLOB_STATUS_OPENED) {
                    blocks_locked += abb_size_blocks; // these can't be purged if we need space (TODO: look into recursive purging of unused references?)
                } else {
                    blocks_unlocked += abb_size_blocks; // these potentially can be purged, unless they are depended on by locked ones
                }
                num_blobs++;
            }
            
            long long blocks_free = bs->limit_blocks - (blocks_unlocked + blocks_locked);
            if (blocks_free < size_blocks) {
                if (!(bs->revocation_policy==BLOBSTORE_REVOCATION_LRU) // not allowed to purge
                    ||
                    (blocks_free+blocks_unlocked) < size_blocks) { // not enough purgeable material
                    ERR (BLOBSTORE_ERROR_NOSPC, NULL);
                    goto clean;
                } 
                long long blocks_needed = size_blocks-blocks_free;
                _err_off(); // do not care about errors duing purging
                long long blocks_freed = purge_blockblobs_lru (bs, bbs, blocks_needed);
                _err_on();
                if (blocks_freed < blocks_needed) {
                    ERR (BLOBSTORE_ERROR_NOSPC, "could not purge enough from cache");
                    goto clean;
                }
            }
        }

        if (lseek (bb->fd_blocks, size_bytes - 1, SEEK_CUR) == (off_t)-1) { // create a file with a hole
            PROPAGATE_ERR (BLOBSTORE_ERROR_UNKNOWN);
            goto clean;
        }
        if (write (bb->fd_blocks, zero_buf, 1) != (ssize_t)1) {
            PROPAGATE_ERR (BLOBSTORE_ERROR_UNKNOWN);
            goto clean;
        }
        if (sig)
            if (write_blockblob_metadata_path (BLOCKBLOB_PATH_SIG, bs, bb->id, sig)) {
                goto clean; 
            }
        bb->snapshot_type = BLOBSTORE_SNAPSHOT_NONE; // just created, so not a snapshot
        
        if (blobstore_unlock(bs)==-1) {
            ERR (BLOBSTORE_ERROR_UNKNOWN, "failed to unlock the blobstore");
        }
        blobstore_locked = 0;
        
    } else { // blob existed
        
        char buf [BLOBSTORE_SIG_MAX];
        
        if (bb->size_bytes==0) { // find out the size from the file size
            bb->size_bytes = sb.st_size;
        } else if (bb->size_bytes != sb.st_size) { // verify the size specified by the user
            ERR (BLOBSTORE_ERROR_SIGNATURE, "size of the existing blockblob does not match");
            goto clean;
        }
        
        // determine whether this blob is a map of another,
        // in which case the blocks are backing and should
        // not be accessed directly
        if (read_blockblob_metadata_path (BLOCKBLOB_PATH_DM, bs, bb->id, buf, sizeof (buf)) > 0) {
            bb->snapshot_type = BLOBSTORE_SNAPSHOT_DM;
        } else {
            bb->snapshot_type = BLOBSTORE_SNAPSHOT_NONE;
        }
        
        // check if its hollow
        if (read_blockblob_metadata_path (BLOCKBLOB_PATH_HOLLOW, bs, bb->id, buf, sizeof (buf)) != -1) {
            bb->is_hollow = TRUE;
        }

        if (sig) { // check the signature, if there
            int sig_size;
            if ((sig_size=read_blockblob_metadata_path (BLOCKBLOB_PATH_SIG, bs, bb->id, buf, sizeof (buf)))!=strlen(sig)
                ||
                (strncmp (sig, buf, sig_size) != 0)) {
                ERR (BLOBSTORE_ERROR_SIGNATURE, NULL);
                goto clean;
            }
        }

        // check its in-use status
        bb->in_use = check_in_use (bs, bb->id, 0);
    }
    
    { // create a loopback device, if there isn't a valid one already (this may happen whether the blob is new or old)
        char lo_dev [PATH_MAX] = "";
        struct stat sb;

        _err_off(); // do not care if loopback file does not exist
        read_blockblob_metadata_path (BLOCKBLOB_PATH_LOOPBACK, bs, bb->id, lo_dev, sizeof (lo_dev));
        _err_on();
        if ((strlen (lo_dev) < 1) // nothing in .loopback file
            || (stat (lo_dev, &sb) == -1) // something in .loopback that does not exist
            || (!S_ISBLK(sb.st_mode))) {  // something in .loopback that is not block device
            
            if (diskutil_loop (bb->blocks_path, 0, lo_dev, sizeof (lo_dev))) {
                ERR (BLOBSTORE_ERROR_UNKNOWN, "failed to obtain a loopback device for a blockblob");
                goto clean;
            }
            write_blockblob_metadata_path (BLOCKBLOB_PATH_LOOPBACK, bs, bb->id, lo_dev);
        }
    }
    
    set_device_path (bb); // read .dm and .loopback and set bb->device_path accordingly
    
    goto out; // all is well
    
 clean:
    {
        int saved_errno = _blobstore_errno; // save it because close_and_unlock() or delete_blockblob_files() may reset it
        if (bb->fd_lock!=-1) {
            ftruncate (bb->fd_lock, 0);
            close_and_unlock (bb->fd_lock); 
        }
        if (bb->fd_blocks!=-1) {
            close (bb->fd_blocks);
        }
        if (created_directory || created_blob) { // only delete disk state if we created it
            delete_blockblob_files (bs, bb->id);
        }
        if (saved_errno) {
            _blobstore_errno = saved_errno;
        }
    }

 unlock:
    {
        int saved_errno = _blobstore_errno;
        if (blobstore_locked && blobstore_unlock (bs)==-1) {
            ERR (BLOBSTORE_ERROR_UNKNOWN, "failed to unlock the blobstore");
            if (saved_errno) {
                _blobstore_errno = saved_errno;
            }
        }
    }

 free:
    if (bb) {
        free (bb);
        bb = NULL;

    }

 out:
    logprintfl (EUCADEBUG2, "{%u} blockblob_open: done with blob id=%s ret=%012lx\n", (unsigned int)pthread_self(), id, bb);
    if (bb==NULL) {
        logprintfl (EUCADEBUG2, "{%u} blockblob_open: errno=%d msg=%s\n", (unsigned int)pthread_self(), _blobstore_errno, blobstore_get_last_msg());
    }
    
    free_bbs (bbs);
    return bb;
}

static int loop_remove ( blobstore * bs, const char * bb_id) 
{
    char path [PATH_MAX] = "";
    int ret = 0;

    _err_off(); // do not care if loopback file does not exist
    read_blockblob_metadata_path (BLOCKBLOB_PATH_LOOPBACK, bs, bb_id, path, sizeof (path)); // loads path of /dev/loop?
    _err_on();
    
    if (strlen (path)) {
        if (diskutil_unloop (path)) {
            ERR (BLOBSTORE_ERROR_UNKNOWN, "failed to remove loopback device for blockblob");
            ret = -1;
        } else {
            set_blockblob_metadata_path (BLOCKBLOB_PATH_LOOPBACK, bs, bb_id, path, sizeof(path)); // load path of .../loopback file itself
            unlink (path);
        }
    }

    return ret;
}

// releases the blob locks, allowing others to open() it, and frees the blockblob handle
int blockblob_close ( blockblob * bb )
{
    if (bb==NULL) {
        ERR (BLOBSTORE_ERROR_INVAL,NULL);
        return -1;
    }
    int ret = 0;
    logprintfl (EUCADEBUG2, "{%u} blockblob_close: closing blob id=%s\n", (unsigned int)pthread_self(), bb->id);

    // do not remove /dev/loop* if it is used by device mapper 
    // (we do not care about BLOCKBLOB_STATUS_OPENED because 
    // it should be only this thread that has the blob open)
    int in_use = check_in_use (bb->store, bb->id, 0);
    if ( !(in_use & (BLOCKBLOB_STATUS_MAPPED|BLOCKBLOB_STATUS_BACKED)) ) {
        ret = loop_remove (bb->store, bb->id);
    }
    ret |= close (bb->fd_blocks);
    ftruncate (bb->fd_lock, 0);
    ret |= close_and_unlock (bb->fd_lock);
    free (bb); // we free the blob regardless of whether closing succeeds or not
    return ret;
}

#ifdef _UNIT_TEST // only used by the unit test for now
static int dm_suspend_resume (const char * dev_name)
{
    char cmd [1024];

    snprintf (cmd, sizeof (cmd), "%s %s suspend %s", helpers_path [ROOTWRAP], helpers_path [DMSETUP], dev_name);
    int status = system (cmd);
    if (status == -1 || WEXITSTATUS(status) != 0) {
        ERR (BLOBSTORE_ERROR_UNKNOWN, "failed to suspend device with 'dmsetup'");
        return -1;
    }
    snprintf (cmd, sizeof (cmd), "%s %s resume %s", helpers_path [ROOTWRAP], helpers_path [DMSETUP], dev_name);
    status = system (cmd);
    if (status == -1 || WEXITSTATUS(status) != 0) {
        ERR (BLOBSTORE_ERROR_UNKNOWN, "failed to resume device with 'dmsetup'");
        return -1;
    }
    return 0;
}
#endif // _UNIT_TEST

static int dm_check_device (const char * dev_name)
{
    // see if the device exists
    char dm_path [MAX_DM_PATH];
    snprintf (dm_path, sizeof (dm_path), DM_PATH "%s", dev_name);
    return check_path (dm_path); // we do not use check_block() because /dev/mapper/... entries can be sym links
}

static int dm_delete_device (const char * dev_name)
{
    int retries = 1;
    int ret = 0;

    // see if the device to delete exists
    char dm_path [MAX_DM_PATH];
    snprintf (dm_path, sizeof (dm_path), DM_PATH "%s", dev_name);
    errno = 0;
    if (check_path (dm_path) && errno==ENOENT) // we do not use check_block() because /dev/mapper/... entries can be sym links
        return 0;

    char cmd [1024];
    snprintf (cmd, sizeof (cmd), "%s %s remove %s", helpers_path [ROOTWRAP], helpers_path [DMSETUP], dev_name);

 try_again:
    myprintf (EUCAINFO, "removing device %s (retries=%d)\n", dev_name, retries);
    int status = system (cmd);
    if (status == -1 || WEXITSTATUS(status) != 0) {
        if (retries--) {
            usleep (100);
            goto try_again;
        }
        ERR (BLOBSTORE_ERROR_UNKNOWN, "failed to remove device mapper device with 'dmsetup'");
        ret = -1;
    }
    return ret;
}

static int dm_delete_devices (char * dev_names[], int size)
{
    if (size<1) return 0;
    int ret = 0;

    // construct list of device names in the order that they should be removed
    int devices = 0;
    char ** dev_names_removable = calloc (size, sizeof(char *));
    if (dev_names_removable==NULL) {
        ERR (BLOBSTORE_ERROR_NOMEM, NULL);
        return -1;
    }
    for (int i=size-1; i>=0; i--) {
        char * name = dev_names [i];
        int seen = 0;
        for (int j=i+1; j<size; j++) {
            if (!strcmp (name, dev_names [j])) {
                seen = 1;
                break;
            }
        }
        if (!seen) {
            dev_names_removable [devices++] = name;
        }
    }

    // run through devices and remove them
    for (int i=0; i<devices; i++) {

        // some of these devices may have children devices that were created
        // by GNU parted for each of the partitions inside; here we look for
        // those devices and remove them so the main device is not 'busy'.
        for (int j=1; j<10; j++) {
            char name_p [1024]; // device mapper name of a potential partition entry
            char path_p [1024]; // path to the device mapper file
            // just append 'pN' to the name, e.g., sda -> sdap1
            snprintf (name_p, sizeof (name_p), "%sp%d", dev_names_removable [i], j);
            snprintf (path_p, sizeof (path_p), DM_FORMAT, name_p);
            if (check_path(path_p)==0) {
                dm_delete_device (name_p);
            }
        }
        ret = dm_delete_device (dev_names_removable [i]);
    }
    free (dev_names_removable);

    return ret;
}

static int dm_create_devices (char * dev_names[], char * dm_tables[], int size)
{
    int i;

    for (i=0; i<size; i++) { // create devices one by one
        myprintf (EUCAINFO, "creating device %s\n", dev_names [i]);
        
        pid_t cpid = fork();
        if (cpid<0) { // fork error
            PROPAGATE_ERR (BLOBSTORE_ERROR_UNKNOWN);
            goto cleanup;

        } else if (cpid==0) { // child process - runs `dmsetup` using system()
            char tmpfile [MAX_PATH];
            bzero (tmpfile, sizeof(tmpfile));
            snprintf (tmpfile, sizeof(tmpfile)-1, "/tmp/dmsetup.XXXXXX");
            int fd = safe_mkstemp (tmpfile);
            if (fd >= 0) {
                int tot = 0;
                int rbytes = write (fd, dm_tables[i], strlen(dm_tables[i]));
                if (rbytes != strlen (dm_tables[i])) { // if write error
                    logprintfl (EUCAERROR, "{%u} error: dm_create_devices: write returned number of bytes != write buffer: %d/%d\n", 
                                (unsigned int)pthread_self(), rbytes, strlen(dm_tables[i]));
                    unlink (tmpfile);
                    exit (1);
                }
                close (fd);

            } else { // couldn't get fd
                logprintfl (EUCAERROR, "{%u} error: dm_create_devices: couldn't open temporary file %s: %s\n", (unsigned int)pthread_self(), tmpfile, strerror (errno));
                unlink (tmpfile);
                exit (1);
            }

            // invoke `dmsetup create ...`
            char cmd [MAX_PATH];
            snprintf (cmd, sizeof(cmd)-1, "%s %s create %s %s", helpers_path[ROOTWRAP], helpers_path[DMSETUP], dev_names[i], tmpfile);
            int rc = system (cmd);
            rc = rc>>8;
            unlink (tmpfile);
            exit (rc); // pass back dmsetup's return code

        } else { // parent - waits for child, reacts to status
            int status;
            int rc = timewait (cpid, &status, BLOBSTORE_DMSETUP_TIMEOUT_SEC);
            if (rc <= 0) {
                logprintfl (EUCAERROR, "{%u} error: dm_create_devices: bad exit from dmsetup child: %d\n", (unsigned int)pthread_self(), rc);
                PROPAGATE_ERR (BLOBSTORE_ERROR_UNKNOWN);
                goto cleanup;
            }
            if (WEXITSTATUS(status) != 0) {
                ERR (BLOBSTORE_ERROR_UNKNOWN, "failed to set up device mapper table with 'dmsetup'");
                myprintf (EUCAINFO, "{%u} command: %s %s create %s\n", (unsigned int)pthread_self(), helpers_path [ROOTWRAP], helpers_path [DMSETUP], dev_names[i]);
                myprintf (EUCAINFO, "{%u} input: %s", (unsigned int)pthread_self(), dm_tables [i]);
                goto cleanup;
            }

        }

        char dm_path [MAX_DM_PATH];
        snprintf (dm_path, sizeof (dm_path), DM_PATH "%s", dev_names[i]);
        if (diskutil_ch (dm_path, EUCALYPTUS_ADMIN, NULL, BLOBSTORE_FILE_PERM) != OK) {
            ERR (BLOBSTORE_ERROR_UNKNOWN, "failed to change permissions on the device mapper file\n");
            goto cleanup;
        }
    }

    return 0;
 cleanup:
    _err_off();
    dm_delete_devices (dev_names, i+1);
    _err_on();
    return -1;
}

static char * dm_get_zero (void)
{
    static char dev_zero [] = DM_PATH EUCA_ZERO; 
    
    struct stat sb;
    int tried = 0;
    while (stat (dev_zero, &sb)==-1) {
        if (tried) {
            ERR (BLOBSTORE_ERROR_UNKNOWN, "failed to create blockblob zero block device");
            return NULL;
        }
        
        char * dm_tables [1] = { "0 " EUCA_ZERO_SIZE " zero" };
        char * dm_names  [1] = { EUCA_ZERO };
        dm_create_devices (dm_names, dm_tables, 1);
        
        tried = 1;
    }

    if (!S_ISBLK(sb.st_mode)) {
        ERR (BLOBSTORE_ERROR_UNKNOWN, "blockblob zero is not a block device");
        return NULL;
    }
    
    return dev_zero;
}

static int blockblob_check (const blockblob * bb)
{
    char ** array = NULL;
    int array_size = 0;
    int err = 0;
    _err_off(); // do not care if metadata files do not exist

    // check on dm devices listed in .dm of this blob, if any
    if (read_array_blockblob_metadata_path (BLOCKBLOB_PATH_DM, bb->store, bb->id, &array, &array_size)!=-1) {
        for (int i=0; i<array_size; i++) {
            if (dm_check_device (array [i]))
                err++;
            free (array[i]);
        }        
        free (array);
    }
    
    // check on the loop device listed in .loopback of the blob, if any
    char lo_dev [PATH_MAX] = "";
    read_blockblob_metadata_path (BLOCKBLOB_PATH_LOOPBACK, bb->store, bb->id, lo_dev, sizeof (lo_dev));
    if (strlen (lo_dev) > 0) {
        struct stat sb;
        if (stat (lo_dev, &sb) == -1) {
            err++;
        } else if (!S_ISBLK(sb.st_mode)) {
            err++;
        } else if (diskutil_loop_check (bb->blocks_path, lo_dev)) {
            err++;
        }
    }

    if (get_stale_refs (bb, NULL)>0)
        err++;
    
    _err_on();
    return err;
}

static int delete_blob_state (blockblob * bb, long long timeout_usec, char do_force)
{
    blobstore * bs = bb->store;
    char ** array = NULL;
    int array_size = 0;
    int ret = 0;

    // delete dm devices listed in .dm of this blob
    if (read_array_blockblob_metadata_path (BLOCKBLOB_PATH_DM, bb->store, bb->id, &array, &array_size)==-1
        ||
        dm_delete_devices (array, array_size)==-1) {
        if (!do_force) {
            ret = -1;
            goto free;
        }
    }
    for (int i=0; i<array_size; i++) {
        free (array[i]);
    }
    if (array)
        free (array);
    array_size = 0;
    array = NULL;
    
    // Read in .deps (blobs that this blob depends on),
    // so as to update their .refs (blobs depending on them).
    if (read_array_blockblob_metadata_path (BLOCKBLOB_PATH_DEPS, bb->store, bb->id, &array, &array_size)==-1) {
        ret = -1;
        if (!do_force) {
            ret = -1;
            goto free;
        }
    }
    char my_ref [BLOBSTORE_MAX_PATH+MAX_DM_NAME+1];
    snprintf (my_ref, sizeof (my_ref), "%s %s", bb->store->path, bb->id);
    for (int i=0; i<array_size; i++) {
        char * store_path = strtok (array [i], " ");
        char * blob_id    = strtok (NULL, " "); // the remaining entries in array[i] are ignored
        
        if (strlen (store_path)<1 || strlen (blob_id)<1)
            continue; // TODO: print a warning about store/blob corruption?
        
        blobstore * dep_bs = bs;
        if (strcmp (bs->path, store_path)) { // if deleting reference in a different blobstore
            // need to open it
            dep_bs = blobstore_open (store_path, 0, BLOBSTORE_FLAG_CREAT, BLOBSTORE_FORMAT_ANY, BLOBSTORE_REVOCATION_ANY, BLOBSTORE_SNAPSHOT_ANY);
            if (dep_bs == NULL)
                continue; // TODO: print a warning about store/blob corruption?
            if (blobstore_lock (dep_bs, timeout_usec)==-1) { // lock this (different) blobstore, too, so .refs are updated atomically
                blobstore_close (dep_bs); 
                continue; // TODO: print a warning about store/blob corruption?
            }
        }
        
        // update .refs file on each of the dependencies
        if (update_entry_blockblob_metadata_path (BLOCKBLOB_PATH_REFS, dep_bs, blob_id, my_ref, 1)==-1) {
            // TODO: print a warning about store/blob corruption?
        }
        
        if (!check_in_use(dep_bs, blob_id, 0)) {
            loop_remove (dep_bs, blob_id); // TODO: do we care about errors?
        }
        if (dep_bs != bs) {
            blobstore_unlock (dep_bs);
            blobstore_close (dep_bs);
        }
    }

    // remove the loopback entry for this blob
    if (loop_remove (bs, bb->id) == -1) {
        ret = -1;
    }

    // remove the files, data and metadata, for of this blob
    if (delete_blockblob_files (bs, bb->id) < 1) {
        ret = -1;
    }

 free:
    for (int i=0; i<array_size; i++) {
        free (array[i]);
    }
    if (array)
        free (array);
    
    return ret;
}


// if no outside references to the blob exist, and blob is not protected, 
// deletes the blob and its metadata
// 
// returns 0 if cleanup was successful and frees the blockblob handle
// returns -1 otherwise, and DOES NOT free the blockblob handle
// (so that it can be closed and freed with blockblob_close) 
int blockblob_delete ( blockblob * bb, long long timeout_usec, char do_force )
{
    if (bb==NULL) {
        ERR (BLOBSTORE_ERROR_INVAL,NULL);
        return -1;
    }
    blobstore * bs = bb->store;
    int ret = 0;
    if (blobstore_lock (bs, timeout_usec)==-1) { // lock it so we can traverse it
        return -1; // failed to obtain a lock on the blobstore
    }
    
    // do not delete the blob if it is used by another one
    bb->in_use = check_in_use (bs, bb->id, 0); // update in_use status
    if (!do_force && bb->in_use & ~(BLOCKBLOB_STATUS_OPENED|BLOCKBLOB_STATUS_BACKED)) { // in use other than opened (by this thread) or backed
        ERR (BLOBSTORE_ERROR_AGAIN, NULL);
        ret = -1;
    } else {
        ret = delete_blob_state (bb, timeout_usec, do_force); // do the bulk of the cleanup

        // close the open file descriptors
        ftruncate (bb->fd_lock, 0);
        if (close_and_unlock (bb->fd_lock) == -1) {
            ret = -1;
        } else {
            bb->fd_lock = 0; // TODO: needed? maybe -1?
        }
        if (close (bb->fd_blocks) == -1) {
            ret = -1;
        } else {
            bb->fd_blocks = 0; // TODO: needed? maybe -1? 
        }
        
        // free the blob struct if everything above was OK
        if (ret==0) {
            free (bb);
        }
    }
    
    int saved_errno = 0;
    saved_errno = _blobstore_errno; // save it because blobstore_unlock may overwrite it
    if (blobstore_unlock (bs)==-1) {
        ERR (BLOBSTORE_ERROR_UNKNOWN, "failed to unlock the blobstore");
    }
    if (saved_errno) {
        _blobstore_errno = saved_errno;
    }
    
    return ret;
}

static int verify_bb ( const blockblob * bb, unsigned long long min_size_bytes ) 
{
    if (bb->fd_lock==-1) {
        ERR (BLOBSTORE_ERROR_INVAL, "blockblob lock involved in operation is not open");
        return -1;
    }
    if (bb->fd_blocks==-1) {
        ERR (BLOBSTORE_ERROR_INVAL, "blockblob involved in operation is not open");
        return -1;
    }
    struct stat sb;
    if (fstat (bb->fd_blocks, &sb)==-1) {
        PROPAGATE_ERR (BLOBSTORE_ERROR_NOENT);
        return -1;
    }
    if (sb.st_size < bb->size_bytes) {
        ERR (BLOBSTORE_ERROR_UNKNOWN, "blockblob involved in operation has backing of unexpected size");
        return -1;
    }
    if (sb.st_size < min_size_bytes) {
        ERR (BLOBSTORE_ERROR_INVAL, "blockblob involved in operation has backing that is too small");
        return -1;
    }
    if (stat (bb->device_path, &sb)==-1) {
        PROPAGATE_ERR (BLOBSTORE_ERROR_NOENT);
        return -1;
    }
    if (!S_ISBLK(sb.st_mode)) {
        ERR (BLOBSTORE_ERROR_INVAL, "blockblob involved in operation is missing a loopback block device");
        return -1;
    }
    return 0;
}

int blockblob_copy ( blockblob * src_bb, // source blob to copy data from
                     unsigned long long src_offset_bytes, // start offset in source
                     blockblob * dst_bb, // destination blob to copy data to
                     unsigned long long dst_offset_bytes, // start offset in destination
                     unsigned long long len_bytes) // 0 = copy until EOF of source
{
    int ret = 0;

    if (src_bb==NULL || dst_bb==NULL) {
        ERR (BLOBSTORE_ERROR_INVAL, "blockblob pointer is NULL");
        return -1;
    }

    long long copy_len_bytes = len_bytes;
    if (copy_len_bytes==0) {
        copy_len_bytes = src_bb->size_bytes - src_offset_bytes;
    }
    if (copy_len_bytes<1) {
        ERR (BLOBSTORE_ERROR_INVAL, "copy source offset outside of range");
        return -1;
    }

    // make sure both source and destination blobs are in good shape and big enough
    if (verify_bb (src_bb, src_offset_bytes + copy_len_bytes) ||
        verify_bb (dst_bb, dst_offset_bytes + copy_len_bytes)) {
        return -1;
    }

    // determine the largest acceptable block size for dd, all the way down to a byte possibly
    int granularity = 4096;
    while (src_offset_bytes % granularity || dst_offset_bytes % granularity || copy_len_bytes % granularity) {
        granularity /= 2;
    }
    
    // do the copy (with block devices dd will silently omit to copy bytes outside the block boundary, so we use paths for uncloned blobs)
    const char * src_path = (src_bb->snapshot_type == BLOBSTORE_SNAPSHOT_DM)?(blockblob_get_dev (src_bb)):(blockblob_get_file (src_bb));
    const char * dst_path = (dst_bb->snapshot_type == BLOBSTORE_SNAPSHOT_DM)?(blockblob_get_dev (dst_bb)):(blockblob_get_file (dst_bb));
    mode_t old_umask = umask (~BLOBSTORE_FILE_PERM);
    int error = diskutil_dd2 (src_path, dst_path, granularity, copy_len_bytes/granularity, dst_offset_bytes/granularity, src_offset_bytes/granularity);
    umask (old_umask);
    if (error) {
        ERR (BLOBSTORE_ERROR_INVAL, "failed to copy a section");
        return -1;
    }

    return ret;
}

int blockblob_clone ( blockblob * bb, // destination blob, which blocks may be used as backing
                      const blockmap * map, // map of blocks from other blobs/devices to be copied/mapped/snapshotted
                      unsigned int map_size ) // size of the map []
{
    int ret = 0;

    if (bb==NULL) {
        ERR (BLOBSTORE_ERROR_INVAL, "blockblob pointer is NULL");
        return -1;
    }
    if (map==NULL || map_size<1 || map_size>MAX_BLOCKMAP_SIZE) {
        ERR (BLOBSTORE_ERROR_INVAL, "invalid blockbmap or its size");
        return -1;
    }
    long long bb_size_blocks = round_down_sec (bb->size_bytes) / 512; // dmsetup will not map partial blocks, so we conservatively round down

    // verify dependencies (block devices present, blob sizes make sense, zero device present)
    char * zero_dev = NULL;
    for (int i=0; i<map_size; i++) {
        const blockmap * m = map + i;

        if (m->relation_type!=BLOBSTORE_COPY && bb->store->snapshot_policy!=BLOBSTORE_SNAPSHOT_DM) {
            ERR (BLOBSTORE_ERROR_INVAL, "relation type is incompatible with snapshot policy");
            return -1;
        }

        switch (m->source_type) {
        case BLOBSTORE_DEVICE: {
            const char * path = m->source.device_path;
            if (path==NULL) {
                ERR (BLOBSTORE_ERROR_INVAL, "one of the device paths is NULL");
                return -1;
            }
            struct stat sb;
            if (stat (path, &sb)==-1) {
                PROPAGATE_ERR (BLOBSTORE_ERROR_NOENT);
                return -1;
            }
            if (!S_ISBLK(sb.st_mode)) {
                ERR (BLOBSTORE_ERROR_INVAL, "one of the device paths is not a block device");
                return -1;
            }
            break;
        }
        case BLOBSTORE_BLOCKBLOB: {
            const blockblob * sbb = m->source.blob;
            if (sbb==NULL) {
                ERR (BLOBSTORE_ERROR_INVAL, "one of the source blockblob pointers is NULL");
                return -1;
            }
            long long sbb_size_blocks = round_down_sec (sbb->size_bytes) / 512; // dmsetup will not map partial blocks, so we conservatively round down
            if (verify_bb (sbb, sbb_size_blocks)) {
                return -1;
            }
            if (sbb_size_blocks < (m->first_block_src + m->len_blocks)) {
                ERR (BLOBSTORE_ERROR_INVAL, "one of the source blockblobs is too small for the map");
                return -1;
            }
            if (bb_size_blocks < (m->first_block_dst + m->len_blocks)) {
                ERR (BLOBSTORE_ERROR_INVAL, "the destination blockblob is too small for the map");
                return -1;
            }
            if (m->relation_type==BLOBSTORE_SNAPSHOT && m->len_blocks < MIN_BLOCKS_SNAPSHOT) {
                ERR (BLOBSTORE_ERROR_INVAL, "snapshot size is too small");
                return -1;
            }
            break;
        } 
        case BLOBSTORE_ZERO:
            zero_dev = dm_get_zero ();
            if (zero_dev == NULL) {
                return -1;
            }

            break;
        default:
            ERR (BLOBSTORE_ERROR_INVAL, "invalid map entry type");
            return -1;
        }
    }

    // compute the base name of the device mapper device
    char dm_base [MAX_DM_LINE];
    snprintf (dm_base, sizeof(dm_base), "euca-%s", bb->id);
    for (char * c = dm_base; * c != '\0'; c++)
        if ( * c == '/' ) // if the ID has slashes,
            * c = '-'; // replace them with hyphens

    int devices = 0;
    int mapped_or_snapshotted = 0;
    char buf [MAX_DM_LINE];
    char * main_dm_table = NULL;
    char ** dev_names = calloc (map_size*4+1, sizeof(char *)); // for device mapper dev names we will create
    if (dev_names==NULL) {
        ERR (BLOBSTORE_ERROR_NOMEM, NULL);
        return -1;
    }
    char ** dm_tables = calloc (map_size*4+1, sizeof(char *)); // for device mapper tables 
    if (dm_tables==NULL) {
        ERR (BLOBSTORE_ERROR_NOMEM, NULL);
        free(dev_names);
        return -1;
    }

    // either does copies or computes the device mapper tables
    for (int i=0; i<map_size; i++) {
        const blockmap * m = map + i;
        const blockblob * sbb = m->source.blob;
        const char * dev;

        switch (m->source_type) {
        case BLOBSTORE_DEVICE:
            dev = m->source.device_path; 
            break;
        case BLOBSTORE_BLOCKBLOB:
            dev = m->source.blob->device_path; 
            break;
        case BLOBSTORE_ZERO:
            dev = zero_dev;
            break;
        default:
            ERR (BLOBSTORE_ERROR_INVAL, "invalid device map source type");
            ret = -1;
            goto free;
        } 

        long long first_block_src = m->first_block_src;
        switch (m->relation_type) {
        case BLOBSTORE_COPY:
            // do the copy
            if (diskutil_dd2 (dev, bb->device_path, 512, m->len_blocks, m->first_block_dst, m->first_block_src)) {
                ERR (BLOBSTORE_ERROR_INVAL, "failed to copy a section");
                ret = -1;
                goto free;
            }
            // append to the main dm table (we do this here even if we never end up using the device mapper because all segments were copied)
            snprintf (buf, sizeof(buf), "%lld %lld linear %s %lld\n", m->first_block_dst, m->len_blocks, bb->device_path, m->first_block_dst);
            main_dm_table = strdupcat (main_dm_table, buf);
            break;

        case BLOBSTORE_SNAPSHOT: {
            int granularity = 16; // coarser granularity does not work
            while (m->len_blocks % granularity) { // do we need to do this?
                granularity /= 2;
            }

            // with a linear map, create a backing device for the snapshot
            snprintf (buf, sizeof(buf), "%s-p%d-back", dm_base, i);
            dev_names [devices] = strdup (buf);
            char * backing_dev = dev_names [devices];
            snprintf (buf, sizeof(buf), "0 %lld linear %s %lld\n", m->len_blocks, bb->device_path, m->first_block_dst);
            dm_tables [devices] = strdup (buf);
            devices++;

            // if there is an offset in the source device, create another map (since snapshots cannot be done at offsets)
            const char * snapshotted_dev = dev;
            if (m->first_block_src > 0 && m->source_type != BLOBSTORE_ZERO) {
                snprintf (buf, sizeof(buf), "%s-p%d-real", dm_base, i);
                dev_names [devices] = strdup (buf);
                snapshotted_dev = dev_names [devices];
                snprintf (buf, sizeof(buf), "0 %lld linear %s %lld\n", m->len_blocks, ((dev) ? dev : 0), m->first_block_src);
                dm_tables [devices] = strdup (buf);
                devices++;
            }

            // take a snapshot of the source
            snprintf (buf, sizeof(buf), "%s-p%d-snap", dm_base, i);
            dev_names [devices] = strdup (buf);
            dev = dev_names [devices];
            // We use 'n' for a non-persistent snapshot, which will not persist across a reboot.
            // With 'p' we could get a persistent snapshot at the cost of 0.2-3.0% overhead in
            // disk space, depending on chunksize [1-16], but then we would need to rebuild 
            // device mapper entries and change space management to accommodate the overhead.
            snprintf (buf, sizeof(buf), "0 %lld snapshot %s%s " DM_PATH "%s n %d\n", m->len_blocks, snapshotted_dev[0]=='e'?DM_PATH:"", snapshotted_dev, backing_dev, granularity);
            dm_tables [devices] = strdup (buf);
            devices++;
            
            first_block_src = 0; // for snapshots the mapping goes from the -snap device at offset 0
            // yes, fall through
        }

        case BLOBSTORE_MAP:
            // append to the main dm table
            snprintf (buf, sizeof(buf), "%lld %lld linear %s%s %lld\n", m->first_block_dst, m->len_blocks, dev[0]=='e'?DM_PATH:"", dev, first_block_src);
            main_dm_table = strdupcat (main_dm_table, buf);
            mapped_or_snapshotted++;
            break;

        default:
            ERR (BLOBSTORE_ERROR_INVAL, "invalid device map source type");
            ret = -1;
            goto free;
        }
    }

    if (mapped_or_snapshotted) { // we must use the device mapper
        safe_strncpy (bb->dm_name, dm_base, sizeof(bb->dm_name));
        dev_names [devices] = strdup (dm_base);
        dm_tables [devices] = main_dm_table;
        devices++;

        // change device_path from loopback to the device-mapper path
        snprintf (bb->device_path, sizeof(bb->device_path), DM_FORMAT, dm_base);

        if (dm_create_devices (dev_names, dm_tables, devices)) {
            ret = -1;
            goto free;
        }

        // record new devices in .dm of this blob
        if (write_array_blockblob_metadata_path (BLOCKBLOB_PATH_DM, bb->store, bb->id, dev_names, devices)==-1) {
            ret = -1;
            goto cleanup;
        }
        bb->snapshot_type = BLOBSTORE_SNAPSHOT_DM; // remember that blobstore uses device mapper

        // update .refs on dependencies and create .deps for this blob
        char my_ref [BLOBSTORE_MAX_PATH+MAX_DM_NAME+1];
        snprintf (my_ref, sizeof (my_ref), "%s %s", bb->store->path, bb->id); // TODO: use store ID to proof against moving blobstore?
        for (int i=0; i<map_size; i++) {
            const blockmap * m = map + i;
            const blockblob * sbb = m->source.blob;

            if (m->source_type != BLOBSTORE_BLOCKBLOB) // only blobstores have references
                continue;

            if (m->relation_type == BLOBSTORE_COPY) // copies do not create references
                continue;

            if (blobstore_lock (sbb->store, BLOBSTORE_LOCK_TIMEOUT_USEC)==-1) { // lock the source blobstore so the .refs are updated atomically
                logprintfl (EUCAERROR, "{%u} error: timed out on a blobstore lock while attempting to update .refs\n", (unsigned int)pthread_self());
                ret = -1;
                goto cleanup; // TODO: remove .refs entries from this batch that succeeded, if any?
            }

            // update .refs
            if (update_entry_blockblob_metadata_path (BLOCKBLOB_PATH_REFS, sbb->store, sbb->id, my_ref, 0)==-1) {
                ret = -1;
                goto cleanup; // TODO: remove .refs entries from this batch that succeeded, if any?
            }
            
            if (blobstore_unlock (sbb->store)==-1) {
                ret = -1;
                goto cleanup; // TODO: remove .refs entries from this batch that succeeded, if any?
            }

            // record the dependency in .deps (redundant entries will be filtered out)
            char dep_ref [BLOBSTORE_MAX_PATH+MAX_DM_NAME+1];
            snprintf (dep_ref, sizeof (dep_ref), "%s %s %s %llu %llu", 
                      sbb->store->path, sbb->id, blobstore_relation_type_name[m->relation_type], m->first_block_dst, m->len_blocks);
            if (update_entry_blockblob_metadata_path (BLOCKBLOB_PATH_DEPS, bb->store, bb->id, dep_ref, 0)==-1) {
                ret = -1;
                goto cleanup; // ditto
            }
        }
    } else {
        free (main_dm_table);
    }

    goto free;

    int saved_errno;
 cleanup: // this is failure cleanup code path

    saved_errno = _blobstore_errno; // save it because dm_delete_devices may overwrite it
    logprintfl (EUCAERROR, "error: blockblob_clone: %s (%d)\n", blobstore_get_last_msg(), _blobstore_errno);
    
    // remove dm devices that may have been created
    if (dm_delete_devices (dev_names, devices)==0) {
        
        // remove the .dm file so that others do not 
        // needlessly attempt to remove dm devices later
        char path [PATH_MAX];
        set_blockblob_metadata_path (BLOCKBLOB_PATH_DM, bb->store, bb->id, path, sizeof (path));
        unlink (path);
    }
    _blobstore_errno = saved_errno;

 free:
    for (int i=0; i<devices; i++) {
        free (dev_names[i]);
        free (dm_tables[i]);
    }
    free (dev_names);
    free (dm_tables);

    return ret;
}

// returns a block device pointing to the blob
const char * blockblob_get_dev ( blockblob * bb )
{
    if (bb==NULL) {
        ERR (BLOBSTORE_ERROR_INVAL,NULL);
        return NULL;
    }
    return bb->device_path;
}

// returns a path to the file containg the blob, but only if snapshot_type!=DM
const char * blockblob_get_file ( blockblob * bb )
{
    if (bb==NULL) {
        ERR (BLOBSTORE_ERROR_INVAL,NULL);
        return NULL;
    }
    if (bb->snapshot_type==BLOBSTORE_SNAPSHOT_DM) {
        ERR (BLOBSTORE_ERROR_INVAL, "file access only supported for uncloned blockblobs");
        return NULL;
    }
    return bb->blocks_path;
}

// size of blob in blocks
unsigned long long blockblob_get_size_blocks ( blockblob * bb)
{
    if (bb==NULL) {
        ERR (BLOBSTORE_ERROR_INVAL,NULL);
        return 0;
    }
    return round_up_sec (bb->size_bytes) / 512;
}

// size of blob in bytes
unsigned long long blockblob_get_size_bytes ( blockblob * bb)
{
    if (bb==NULL) {
        ERR (BLOBSTORE_ERROR_INVAL,NULL);
        return 0;
    }
    return bb->size_bytes;
}

/////////////////////////////////////////////// unit testing code ///////////////////////////////////////////////////

#ifdef _UNIT_TEST

#define F1 "/tmp/blobstore_test_1"
#define F2 "/tmp/blobstore_test_2"
#define F3 "/tmp/blobstore_test_3"

static char * _farray [] = { F1, F2, F3 };

#define _UNEXPECTED printf ("======================> UNEXPECTED RESULT (errors=%d)!!!\n", ++errors);

#define _CHKMETA(ST,RE) snprintf (entry_path, sizeof(entry_path), "%s/%s", bs->path, ST); \
    if (RE!=typeof_blockblob_metadata_path (bs, entry_path, blob_id, sizeof(blob_id))) _UNEXPECTED;

#define _OPEN(FD,FI,FL,TI,RE) _blobstore_errno=0;                       \
    printf ("%d: open (" FI " flags=%d timeout=%d)", getpid(), FL, TI); \
    FD=open_and_lock(FI,FL,TI,BLOBSTORE_FILE_PERM);                    \
    printf ("=%d errno=%d '%s'\n", FD, _blobstore_errno, blobstore_get_error_str(_blobstore_errno)); \
    if ((FD==-1) && (_blobstore_errno==0)) printf ("======================> UNSET errno ON ERROR (errors=%d)!!!\n", ++errors); \
    else if ((RE==-1 && FD!=-1) || (RE==0 && FD<0)) _UNEXPECTED;

#define _CLOS(FD,FI) ret=close_and_unlock(FD); \
    printf ("%d: close (%d " FI ")=%d\n", getpid(), FD, ret); 

#define _PARENT_WAITS \
            int status, ret; \
            printf ("waiting for child pid=%d\n", pid); \
            ret = wait (&status); \
            printf ("waited for child pid=%d ret=%d\n", ret, WEXITSTATUS(status)); \
            errors += WEXITSTATUS(status);

#define _R BLOBSTORE_FLAG_RDONLY
#define _W BLOBSTORE_FLAG_RDWR
#define _C BLOBSTORE_FLAG_CREAT|BLOBSTORE_FLAG_EXCL|BLOBSTORE_FLAG_RDWR
#define _CBB BLOBSTORE_FLAG_CREAT|BLOBSTORE_FLAG_EXCL

#define B1 "BLOCKBLOB-01"
#define B2 "BLOCKBLOB-02"
#define B3 "BLOCKBLOB-03"
#define B4 "BLOCKBLOB-04"
//#define B2 "FOO/BLOCKBLOB-02"
//#define B3 "FOO/BAR/BLOCKBLOB-03"
//#define B4 "FOO/BAR/BAZ/BLOCKBLOB-04"
#define B5 "BLOCKBLOB-05"
#define B6 "BLOCKBLOB-06"

#define _OPENBB(BB,ID,SI,SG,FL,TI,RE) _blobstore_errno=0;               \
    printf ("%d: bb_open (%s size=%d flags=%d timeout=%d)", getpid(), (ID==NULL)?("null"):(ID), SI, FL, TI); \
    BB=blockblob_open (bs, ID, (SI)*512, FL, SG, TI);                   \
    printf ("=%s errno=%d '%s'\n", (BB==NULL)?("NULL"):("OK"), _blobstore_errno, blobstore_get_error_str(_blobstore_errno)); \
    if ((BB==NULL) && (_blobstore_errno==0)) printf ("======================> UNSET errno ON ERROR (errors=%d)!!!\n", ++errors); \
    else if ((RE==-1 && BB!=NULL) || (RE==0 && BB==NULL)) _UNEXPECTED;
// same as _OPENBB but accepts bytes rather than blocks
#define _OPENBBb(BB,ID,SI,SG,FL,TI,RE) _blobstore_errno=0;               \
    printf ("%d: bb_open (%s size=%d flags=%d timeout=%d)", getpid(), (ID==NULL)?("null"):(ID), SI, FL, TI); \
    BB=blockblob_open (bs, ID, SI, FL, SG, TI); \
    printf ("=%s errno=%d '%s'\n", (BB==NULL)?("NULL"):("OK"), _blobstore_errno, blobstore_get_error_str(_blobstore_errno)); \
    if ((BB==NULL) && (_blobstore_errno==0)) printf ("======================> UNSET errno ON ERROR (errors=%d)!!!\n", ++errors); \
    else if ((RE==-1 && BB!=NULL) || (RE==0 && BB==NULL)) _UNEXPECTED;

#define _SEARCH(PATTERN,RE) results = NULL;                             \
    printf ("%d: bs_search (pattern=%s)", getpid(), PATTERN);           \
    nresults = blobstore_search (bs, PATTERN, &results);                \
    printf ("=%d (expected %d) errno=%d '%s'\n", nresults, RE, _blobstore_errno, blobstore_get_error_str(_blobstore_errno)); \
    if ((nresults<0) && (_blobstore_errno==0)) printf ("======================> UNSET errno ON ERROR (errors=%d)!!!\n", ++errors); \
    else if (RE!=nresults) _UNEXPECTED;                                 \
    for (blockblob_meta * bm = results; bm;) {                          \
        blockblob_meta * next = bm->next;                               \
        free (bm);                                                      \
        bm = next;                                                      \
    }

#define _CLOSBB(BB,ID) ret=blockblob_close(BB); \
    printf("%d: bb_close (%lu %s)=%d errno=%d '%s'\n", getpid(), (unsigned long)BB, (ID==NULL)?("null"):(ID), ret, _blobstore_errno, blobstore_get_error_str(_blobstore_errno));

#define _DELEBB(BB,ID,RE) ret=blockblob_delete(BB, 3000, 0);            \
    printf("%d: bb_delete (%lu %s)=%d errno=%d '%s'\n", getpid(), (unsigned long)BB, (ID==NULL)?("null"):(ID), ret, _blobstore_errno, blobstore_get_error_str(_blobstore_errno)); \
    if (ret!=RE) _UNEXPECTED;

#define _CLONBB(BB,ID,MP,RE) _blobstore_errno=0; \
    printf ("%d: bb_clone (%s map=%lu)", getpid(), (ID==NULL)?("null"):(ID), (unsigned long)MP); \
    ret=blockblob_clone(BB,MP,sizeof(MP)/sizeof(blockmap));    \
    printf ("=%d errno=%d '%s'\n", ret, _blobstore_errno, blobstore_get_error_str(_blobstore_errno)); \
    if ((ret==-1) && (_blobstore_errno==0)) printf ("======================> UNSET errno ON ERROR (errors=%d)!!!\n", ++errors); \
    else if (RE!=ret) _UNEXPECTED;

#define _COPYBB(SBB,SO,DBB,DO,LEN,RE) _blobstore_errno=0; \
    printf ("%d: bb_copy (%s to %s)", getpid(), SBB->id, DBB->id); \
    ret=blockblob_copy(SBB,SO,DBB,DO,LEN); \
    printf ("=%d errno=%d '%s'\n", ret, _blobstore_errno, blobstore_get_error_str(_blobstore_errno)); \
    if ((ret==-1) && (_blobstore_errno==0)) printf ("======================> UNSET errno ON ERROR (errors=%d)!!!\n", ++errors); \
    else if (RE!=ret) _UNEXPECTED;

#define BS_SIZE 30
#define BB_SIZE 10
#define CBB_SIZE 32
#define STRESS_BS_SIZE 100000
#define STRESS_MIN_BB  64
#define STRESS_BLOBS   10

static void _fill_blob (blockblob * bb, char c, int use_file)
{
    const char * path;
    if (use_file) {
        path = blockblob_get_file (bb);
    } else {
        path = blockblob_get_dev (bb);
    }

    char buf [1];
    buf [0] = c;

    printf ("filling out with dummy data %s\n", path);
    int fd = open (path, O_WRONLY);
    int failed_bytes = 0;
    if (fd!=-1) {
        for (int i=0; i<bb->size_bytes; i++) {
            if (write (fd, buf, 1)!=1)
                failed_bytes++;
        }
    }
    if (failed_bytes) {
        printf ("WARNING: failed to fill %d byte(s) to path %s\n", failed_bytes, path);
    }
    if(fd >= 0) {
        fsync (fd);
        close (fd);
    }
}

static blobstore * create_teststore (int size_blocks, const char * base, const char * name, blobstore_format_t format, blobstore_revocation_t revocation, blobstore_snapshot_t snapshot)
{
    static int ts = 0;
    static int counter = 0;

    if (ts==0) {
        ts = ((int)time(NULL))-1292630988;
        //ts = (((int)time(NULL))<<24)>>24;
    }

    char bs_path [PATH_MAX];
    snprintf (bs_path, sizeof (bs_path), "%s/test_blobstore_%05d_%s_%03d", base, ts, name, counter++);
    if (mkdir (bs_path, BLOBSTORE_DIRECTORY_PERM) == -1) {
        printf ("failed to create %s\n", bs_path);
        return NULL;
    }
    printf ("created %s\n", bs_path);
    blobstore * bs = blobstore_open (bs_path, size_blocks, BLOBSTORE_FLAG_CREAT, format, revocation, snapshot);
    if (bs==NULL) {
        printf ("ERROR: %s\n", blobstore_get_error_str(blobstore_get_error()));
        return NULL;
    }
    return bs;
}

static int write_byte (blockblob *bb, int seek, char c)
{
    const char * dev = blockblob_get_dev (bb);
    int fd = open (dev, O_WRONLY);
    if (fd==-1) { 
        printf ("ERROR: failed to open the blockblob dev %s\n", dev);
        return -1;
    }
    if (lseek (fd, seek, SEEK_SET)==-1) {
        printf ("ERROR: failed to lseek in blockblob dev %s\n", dev);
        close (fd);
        return -1;
    }
    if (write (fd, &c, 1)!=1) {
        printf ("ERROR: failed to write to blockblob dev %s\n", dev);
        close (fd);
        return -1;
    }
    fsync (fd);
    close (fd);

    return 0;
}

static char read_byte (blockblob *bb, int seek)
{
    const char * dev = blockblob_get_dev (bb);
    int fd = open (dev, O_RDONLY);
    if (fd==-1) { 
        printf ("ERROR: failed to open the blockblob dev %s\n", dev);
        return -1;
    }
    if (lseek (fd, seek, SEEK_SET)==-1) {
        printf ("ERROR: failed to lseek in blockblob dev %s\n", dev);
        close (fd);
        return -1;
    }
    char buf [1];
    if (read (fd, buf, 1)!=1) {
        printf ("ERROR: failed to write to blockblob dev %s\n", dev);
        close (fd);
        return -1;
    }
    close (fd);

    return buf [0];
}

static int do_clone_stresstest (const char * base, const char * name, blobstore_format_t format, blobstore_revocation_t revocation, blobstore_snapshot_t snapshot)
{
    int ret;
    int errors = 0;
    printf ("commencing cloning stress-test...\n");

    blobstore * bs1 = create_teststore (STRESS_BS_SIZE, base, name, BLOBSTORE_FORMAT_DIRECTORY, BLOBSTORE_REVOCATION_NONE, BLOBSTORE_SNAPSHOT_DM);
    if (bs1==NULL) { errors++; goto done; }
    blobstore * bs2 = create_teststore (STRESS_BS_SIZE, base, name, BLOBSTORE_FORMAT_DIRECTORY, BLOBSTORE_REVOCATION_LRU, BLOBSTORE_SNAPSHOT_DM);
    if (bs2==NULL) { 
        errors++; 
        goto done; 
    }

    blockblob * bbs1 [STRESS_BLOBS];
    long long bbs1_sizes [STRESS_BLOBS];
    blockblob * bbs2 [STRESS_BLOBS*2];
    long long bbs2_sizes [STRESS_BLOBS*2];

    // calculate sizes
    long long avg = STRESS_BS_SIZE / STRESS_BLOBS;
    if (avg < STRESS_MIN_BB*2) {
        printf ("ERROR: average blob size %lld for stress test is too small (<%d)\n", avg, STRESS_MIN_BB*2);
        errors++;
        goto done;
    }
    for (int i=0; i<STRESS_BLOBS; i++) { 
        bbs1_sizes [i] = avg;
        bbs1 [i] = NULL;
        bbs2 [i] = NULL;
        bbs2 [i+STRESS_BLOBS] = NULL;
    }
    for (int i=0; i<STRESS_BLOBS*3; i++) { // run over the array a few times
        int j = i % (STRESS_BLOBS/2); // modify pairs from array
        int k = j + (STRESS_BLOBS/2);
#define MIN(a,b) a>b?b:a
        long long max_delta = MIN(bbs1_sizes[j]-STRESS_MIN_BB, bbs1_sizes[k]-STRESS_MIN_BB);
        long long delta = max_delta*(((double)random()/RAND_MAX)-0.5);
        bbs1_sizes [j] -= delta;
        bbs2_sizes [j] = bbs1_sizes [j]/2;
        bbs2_sizes [j+STRESS_BLOBS] = bbs1_sizes [j] - bbs1_sizes [j]/2;

        bbs1_sizes [k] += delta;
        bbs2_sizes [k] = bbs1_sizes [k]/2;
        bbs2_sizes [k+STRESS_BLOBS] = bbs1_sizes [k] - bbs1_sizes [k]/2;
    }
    long long bbs1_totals = 0;
    for (int i=0; i<STRESS_BLOBS; i++) {
        bbs1_totals += bbs1_sizes [i];
        long long pair = bbs2_sizes [i] +  bbs2_sizes [i+STRESS_BLOBS];
        assert (pair==bbs1_sizes [i]);
        printf ("%lld ", bbs1_sizes [i]);
    }
    assert (bbs1_totals==STRESS_BS_SIZE);
    printf ("\n");

    // fill the stores
    for (int i=0; i<STRESS_BLOBS; i++) {
#define _OPENERR(BS,BB,BBSIZE)                                          \
        BB = blockblob_open (BS, NULL, BBSIZE*512, BLOBSTORE_FLAG_CREAT | BLOBSTORE_FLAG_EXCL, NULL, 1000); \
        if (BB == NULL) {                                               \
            printf ("ERROR: failed to create blockblob i=%d\n", i);       \
            errors++;                                                   \
            goto drain;                                                 \
        }
        printf ("allocating slot %d\n", i);
        _OPENERR(bs1,bbs1[i],bbs1_sizes[i]);
        _OPENERR(bs2,bbs2[i],bbs2_sizes[i]);
        _OPENERR(bs2,bbs2[i+STRESS_BLOBS],bbs2_sizes[i+STRESS_BLOBS]);
        write_byte (bbs2[i+STRESS_BLOBS], 0, 'b'); // write a byte into beginning of blob that will be snapshotted
        blockmap map [] = {
            {BLOBSTORE_MAP,      BLOBSTORE_BLOCKBLOB, {blob:bbs2[i]},              0, 0, bbs2_sizes[i]},
            {BLOBSTORE_SNAPSHOT, BLOBSTORE_BLOCKBLOB, {blob:bbs2[i+STRESS_BLOBS]}, 0, bbs2_sizes[i], bbs2_sizes[i+STRESS_BLOBS]},
        };        
        if (blockblob_clone (bbs1[i], map, 2)==-1) {
            printf ("ERROR: failed to clone on iteration %i\n", i);
            errors++;
            goto drain;
        }
        // verify that mapping works
        write_byte (bbs2[i], bbs2_sizes[i]*512-1, 'a'); // write a byte into the end of the blob that is being mapped
        dm_suspend_resume (bbs1[i]->dm_name);
        char c1 = read_byte (bbs1[i], bbs2_sizes[i]*512-1); // read that byte back via bbs1
        char c2 = read_byte (bbs1[i], bbs2_sizes[i]*512); // read the byte written before the snapshot
        if (c1!='a' || c2!='b') {
            printf ("ERROR: clone verification failed (c1=='%c', c2=='%c')\n", c1, c2);
            errors++;
            goto drain;
        }
    }
    
    // induce churn in stores
    for (int k=0; k<STRESS_BLOBS*1; k++) {
        usleep (100);
        // randomly free a few random blobs
        int to_free = (int)((STRESS_BLOBS/2)*((double)random()/RAND_MAX));
        printf ("will free %d random blobs\n", to_free);
        for (int j=0; j<to_free; j++) {
            int i = (int)((STRESS_BLOBS-1)*((double)random()/RAND_MAX));
            if (bbs1 [i] != NULL) {
                printf ("freeing slot %d\n", i);
#define _DELWARN(BB) if (BB && blockblob_delete (BB, 1000, 0) == -1) { printf ("WARNING: failed to delete blockblob %s i=%d\n", BB->id, i); } BB=NULL
                _DELWARN(bbs1[i]);
                blockblob_close (bbs2[i]); // so it can be purged with LRU 
                bbs2[i] = NULL;
                blockblob_close (bbs2[i+STRESS_BLOBS]); // so it can be purged with LRU 
                bbs2[i+STRESS_BLOBS]= NULL;
            }
        }
        
        // re-allocate those sizes
        for (int i=0; i<STRESS_BLOBS; i++) {
            if (bbs1 [i]!=NULL)
                continue;
            printf ("allocating slot %d\n", i);
            _OPENERR(bs1,bbs1[i],bbs1_sizes[i]);
            _OPENERR(bs2,bbs2[i],bbs2_sizes[i]);
            _OPENERR(bs2,bbs2[i+STRESS_BLOBS],bbs2_sizes[i+STRESS_BLOBS]);
            write_byte (bbs2[i+STRESS_BLOBS], 0, 'b'); // write a byte into beginning of blob that will be snapshotted
            blockmap map [] = {
                {BLOBSTORE_MAP,      BLOBSTORE_BLOCKBLOB, {blob:bbs2[i]},              0, 0, bbs2_sizes[i]},
                {BLOBSTORE_SNAPSHOT, BLOBSTORE_BLOCKBLOB, {blob:bbs2[i+STRESS_BLOBS]}, 0, bbs2_sizes[i], bbs2_sizes[i+STRESS_BLOBS]},
            };        
            if (blockblob_clone (bbs1[i], map, 2)==-1) {
                printf ("ERROR: failed to clone on iteration %i\n", i);
                errors++;
                goto drain;
            }
            // verify that mapping works
            write_byte (bbs2[i], bbs2_sizes[i]*512-1, 'a'); // write a byte into the end of the blob that is being mapped
            dm_suspend_resume (bbs1[i]->dm_name);
            char c1 = read_byte (bbs1[i], bbs2_sizes[i]*512-1); // read that byte back via bbs1
            char c2 = read_byte (bbs1[i], bbs2_sizes[i]*512); // read the byte written before the snapshot
            if (c1!='a' || c2!='b') {
                printf ("ERROR: clone verification failed (c1=='%c', c2=='%c')\n", c1, c2);
                errors++;
                goto drain;
            }
        }
    }

    // drain the stores
 drain:
    printf ("resting before draining...\n");
    sleep (1); 
    for (int i=0; i<STRESS_BLOBS; i++) {
        printf ("freeing slot %d\n", i);
        _DELWARN(bbs1[i]);
        _DELWARN(bbs2[i]);
        _DELWARN(bbs2[i+STRESS_BLOBS]);
    }

    printf ("completed cloning stress-test\n");
 done:
    if (bs1 != NULL) blobstore_close (bs1);
    if (bs2 != NULL) blobstore_close (bs2);
    return errors;
}

static int check_destination (blockblob * bb4, char * op)
{
    int errors = 0;
    const char * dev = blockblob_get_dev (bb4);
    if (dev!=NULL) {
        int fd = open (dev, O_RDONLY);
        if (fd != -1) {
            for (int i=1; i<4; i++) {
                for (int j=0; j<CBB_SIZE; j++) {
                    char buf [512];
                    int r = read (fd, buf, sizeof (buf));
                    if (r < 1) {
                        printf ("ERROR: failed to read bock device %s\n", dev);
                        errors++;
                        goto stop_comparing;
                    }
                    if (buf [0] != '0' + i) {
                        printf ("ERROR: block device %s has unexpected data ('%c' (%d) != '%c')\n", dev, buf [0], buf [0], '0' + i);
                        errors++;
                        goto stop_comparing;
                    }
                }
            }
        stop_comparing:
            close (fd);
        } else {
            printf ("ERROR: failed to open block device %s for the %s\n", dev, op);
            errors++;
        }
    } else {
        printf ("ERROR: failed to get a block device for the %s\n", op);
        errors++;
    }

    return errors;
}

static int do_copy_test (const char * base, const char * name)
{
    int ret;
    int errors = 0;
    printf ("commencing copy test\n");

    blobstore * bs = create_teststore (CBB_SIZE*7, base, name, BLOBSTORE_FORMAT_DIRECTORY, BLOBSTORE_REVOCATION_ANY, BLOBSTORE_SNAPSHOT_ANY);
    if (bs==NULL) { errors++; goto done; }

    blockblob * bb1, * bb2, * bb3, * bb4;

    // these are to be copied to another
    _OPENBBb(bb1,B1,CBB_SIZE*512*7+1,NULL,_CBB,0,-1); // too big for bs
    if (errors) goto done;
    _OPENBBb(bb1,B1,CBB_SIZE*512,NULL,_CBB,0,0); // bs size: 1
    _fill_blob (bb1, '1', TRUE);
    _OPENBBb(bb2,B2,CBB_SIZE*512+1,NULL,_CBB,0,0); // bs size: 3
    _fill_blob (bb2, '2', TRUE);
    _OPENBBb(bb3,B3,CBB_SIZE*512-2,NULL,_CBB,0,0); // bs size: 4
    _fill_blob (bb3, '3', TRUE);

    // this is to be the destination of the copy
    _OPENBB(bb4,B4,CBB_SIZE*3,NULL,_CBB,0,0); // bs size: 7
    _COPYBB(bb1,0,bb4,0,0,0);  // check that len=0 works and that right block size is chosen
    _COPYBB(bb2,0,bb4,CBB_SIZE*512,CBB_SIZE*512+1,0);
    _COPYBB(bb3,0,bb4,CBB_SIZE*512*2,CBB_SIZE*512-2,0);
    _COPYBB(bb3,0,bb4,CBB_SIZE*512*3-2,2,0);
    _COPYBB(bb3,0,bb4,CBB_SIZE*512*2,CBB_SIZE*512,-1); // source is too small
    _COPYBB(bb3,2,bb4,CBB_SIZE*512*2,CBB_SIZE*512,-1); // source is too small
    _COPYBB(bb3,0,bb4,CBB_SIZE*512*3-1,2,-1); // destination is too small

    // see if copy worked
    errors += check_destination (bb4, "copy");

    _DELEBB(bb1,B1,0);
    _DELEBB(bb2,B2,0);
    _DELEBB(bb3,B3,0);
    _DELEBB(bb4,B4,0);
    blobstore_close (bs);

    printf ("completed copy test\n");
 done:
    return errors;
}

static int do_clone_test (const char * base, const char * name, blobstore_format_t format, blobstore_revocation_t revocation, blobstore_snapshot_t snapshot, int copy_or_snapshot)
{
    int ret;
    int errors = 0;
    printf ("commencing cloning test\n");

    blobstore * bs = create_teststore (CBB_SIZE*6, base, name, BLOBSTORE_FORMAT_DIRECTORY, BLOBSTORE_REVOCATION_ANY, BLOBSTORE_SNAPSHOT_ANY);
    if (bs==NULL) { errors++; goto done; }

    blockblob * bb1, * bb2, * bb3, * bb4, * bb5, * bb6;

    // these are to be mapped to others
    _OPENBB(bb1,B1,CBB_SIZE,NULL,_CBB,0,0); // bs size: 1
    _fill_blob (bb1, '1', FALSE);
    _OPENBB(bb2,B2,CBB_SIZE,NULL,_CBB,0,0); // bs size: 2
    _fill_blob (bb2, '2', FALSE);
    _OPENBB(bb3,B3,CBB_SIZE,NULL,_CBB,0,0); // bs size: 3
    _fill_blob (bb3, '3', FALSE);

    // these are to be clones
    _OPENBB(bb4,B4,CBB_SIZE*3,NULL,_CBB,0,0); // bs size: 6
    blockmap bm1 [] = { 
        {BLOBSTORE_MAP, BLOBSTORE_BLOCKBLOB, {blob:bb1}, 0, 0, CBB_SIZE},
        {BLOBSTORE_COPY, BLOBSTORE_BLOCKBLOB, {blob:bb2}, 0, CBB_SIZE, CBB_SIZE},
        {BLOBSTORE_SNAPSHOT, BLOBSTORE_BLOCKBLOB, {blob:bb3}, 0, CBB_SIZE*2, CBB_SIZE},
    };
    _CLONBB(bb4,B4,bm1,0);

    // see if cloning worked
    errors += check_destination (bb4, "clone");

    _DELEBB(bb1,B1,-1); // referenced, not deletable
    _DELEBB(bb2,B2,0); // not referenced, deletable
    _DELEBB(bb3,B3,-1); // referenced, not deletable
    _CLOSBB(bb3,B3);
    _CLOSBB(bb4,B4);
    _DELEBB(bb1,B1,-1); // still referenced, not deletable
    _OPENBB(bb4,B4,0,NULL,0,0,0); // re-open so we can delete it
    _DELEBB(bb4,B4,0); // delete #4
    _DELEBB(bb1,B1,0); // now it should work
    _OPENBB(bb3,B3,0,NULL,0,0,0); // re-open so we can map it

    // open a second blobstore to test cross-references
    blobstore * bs2 = create_teststore (CBB_SIZE*6, base, name, BLOBSTORE_FORMAT_DIRECTORY, BLOBSTORE_REVOCATION_ANY, BLOBSTORE_SNAPSHOT_ANY);
    if (bs2==NULL) {
        errors++; 
        goto done; 
    }
    bb5 = blockblob_open (bs2, B5, CBB_SIZE*3*512, BLOBSTORE_FLAG_CREAT, NULL, 0);
    if (bb5==NULL) {
        errors++;
        goto done;
    }

    blockmap bm2 [] = { 
        {copy_or_snapshot, BLOBSTORE_BLOCKBLOB, {blob:bb3}, 0, 0, CBB_SIZE},
        {copy_or_snapshot, BLOBSTORE_ZERO,      {blob:NULL}, 0, CBB_SIZE, CBB_SIZE},
        //        {copy_or_snapshot,      BLOBSTORE_DEVICE,    {device_path:"/dev/sda2"}, 0, CBB_SIZE*2, CBB_SIZE}
    };
    _CLONBB(bb5,B5,bm2,0);

    if (copy_or_snapshot==BLOBSTORE_SNAPSHOT) {
        _DELEBB(bb3,B3,-1); // referenced, so not deletable
        _CLOSBB(bb3,B3);
        _OPENBB(bb3,B3,0,NULL,0,0,0); // re-open so we can try to delete it
        _DELEBB(bb3,B3,-1); // ditto
        _CLOSBB(bb3,B3);
        sleep (1); // otherwise the next delete occasionally fails with 'device busy'
    } else {
        _DELEBB(bb3,B3,0); // NOT referenced in case of _COPY, thus deletable
    }
    _DELEBB(bb5,B5,0); // delete #5
    if (copy_or_snapshot==BLOBSTORE_SNAPSHOT) {
        _OPENBB(bb3,B3,0,NULL,0,0,0); // re-open so we can finally delete it
        _DELEBB(bb3,B3,0); // should work now
    }

    blobstore_close (bs);
    blobstore_close (bs2);

    printf ("completed cloning test\n");
 done:
    return errors;
}

static int do_metadata_test (const char * base, const char * name)
{
    int ret;
    int errors = 0;

    printf ("\nrunning do_metadata_test()\n");
    
    blobstore * bs = create_teststore (BS_SIZE, base, name, BLOBSTORE_FORMAT_FILES, BLOBSTORE_REVOCATION_ANY, BLOBSTORE_SNAPSHOT_ANY);
    if (bs==NULL) { errors++; goto done; }

    char blob_id [MAX_PATH];
    char entry_path [MAX_PATH];
    _CHKMETA("foo",0);
    _CHKMETA(".dm",0);
    _CHKMETA(".loopback",0);
    _CHKMETA(".sig",0);
    _CHKMETA(".refs",0);
    _CHKMETA(".dmfoo",0);
    _CHKMETA("foo.blocks",BLOCKBLOB_PATH_BLOCKS);
    _CHKMETA("foo.dm",BLOCKBLOB_PATH_DM);
    _CHKMETA("foo.loopback",BLOCKBLOB_PATH_LOOPBACK);
    _CHKMETA("foo.sig",BLOCKBLOB_PATH_SIG);
    _CHKMETA("foo.refs",BLOCKBLOB_PATH_REFS);
    _CHKMETA("foo.dm.foo.dm",BLOCKBLOB_PATH_DM);
    _CHKMETA("foo/dm/dm.foo.loopback",BLOCKBLOB_PATH_LOOPBACK);
    _CHKMETA("foo/dm/dm.dm.sig",BLOCKBLOB_PATH_SIG);
    _CHKMETA("foo/dm/dm.dm.dm.refs",BLOCKBLOB_PATH_REFS);
    _CHKMETA(".dm.dm",BLOCKBLOB_PATH_DM);
    _CHKMETA(".foo.dm",BLOCKBLOB_PATH_DM);
    blobstore_close (bs);

    bs = create_teststore (BS_SIZE, base, name, BLOBSTORE_FORMAT_DIRECTORY, BLOBSTORE_REVOCATION_ANY, BLOBSTORE_SNAPSHOT_ANY);
    if (bs==NULL) { errors++; goto done; }
    _CHKMETA("foo",0);
    _CHKMETA(".dm",0);
    _CHKMETA(".loopback",0);
    _CHKMETA(".sig",0);
    _CHKMETA(".refs",0);
    _CHKMETA(".dmfoo",0);
    _CHKMETA("foo/blocks",BLOCKBLOB_PATH_BLOCKS);
    _CHKMETA("foo/dm",BLOCKBLOB_PATH_DM);
    _CHKMETA("foo/loopback",BLOCKBLOB_PATH_LOOPBACK);
    _CHKMETA("foo/sig",BLOCKBLOB_PATH_SIG);
    _CHKMETA("foo/refs",BLOCKBLOB_PATH_REFS);
    _CHKMETA("foo.dm.foo/dm",BLOCKBLOB_PATH_DM);
    _CHKMETA("foo/dm/dm.foo/loopback",BLOCKBLOB_PATH_LOOPBACK);
    _CHKMETA("foo/dm/dm.dm/sig",BLOCKBLOB_PATH_SIG);
    _CHKMETA("foo/dm/dm.dm.dm/refs",BLOCKBLOB_PATH_REFS);
    _CHKMETA(".dm/dm",BLOCKBLOB_PATH_DM);
    _CHKMETA(".foo/dm",BLOCKBLOB_PATH_DM);
    if (errors) {
        blobstore_close (bs);
        return errors;
    }

    printf ("\ntesting metadata manipulation\n");

    blockblob * bb1;
    _OPENBB(bb1,B1,BB_SIZE,NULL,_CBB,0,0); // bs size: 10
    if (bb1==NULL) return 1; // so test does not SEGFAULT when run as non-root

    int t = 1;
    char ** array;
    int array_size;
    char buf [1024];
    bzero (buf, sizeof(buf));
#define _BADMETACMD { printf ("UNEXPECTED RESULT LINE %d (errors=%d, errno=%d %s)\n", t, errors++, _blobstore_errno, blobstore_get_error_str(_blobstore_errno)); } t++
#define _STR1 "teststringtwo"
#define _STR2 "test\nstring\none\n"
    /* 1 */ if (read_blockblob_metadata_path  (BLOCKBLOB_PATH_SIG, bs, bb1->id, buf, sizeof(buf)) != -1) _BADMETACMD; // open nonexisting file
    if (write_blockblob_metadata_path (BLOCKBLOB_PATH_SIG, bs, bb1->id, "") != 0) _BADMETACMD; // delete nonexisting file
    if (write_blockblob_metadata_path (BLOCKBLOB_PATH_SIG, bs, bb1->id, _STR1) != 0) _BADMETACMD; // write new file
    if (write_blockblob_metadata_path (BLOCKBLOB_PATH_SIG, bs, bb1->id, _STR2) != 0) _BADMETACMD; // overwrite file
    /* 5 */ if (read_blockblob_metadata_path  (BLOCKBLOB_PATH_SIG, bs, bb1->id, buf, sizeof(buf)) != strlen(_STR2)) _BADMETACMD; // read file
    if (strcmp (buf, _STR2)) _BADMETACMD;
    if (read_array_blockblob_metadata_path (BLOCKBLOB_PATH_SIG, bs, bb1->id, &array, &array_size) != 0) _BADMETACMD; // read file line-by-line
    if (array_size!=3) _BADMETACMD;
    for (int i=0; i<array_size; i++) {
        free (array [i]);
    }
    free (array);
    if (update_entry_blockblob_metadata_path (BLOCKBLOB_PATH_SIG, bs, bb1->id, "test", 1) != 0) _BADMETACMD; // delete first line
    /* 10 */ if (update_entry_blockblob_metadata_path (BLOCKBLOB_PATH_SIG, bs, bb1->id, "one", 1) != 0) _BADMETACMD; // delete last line
    if (update_entry_blockblob_metadata_path (BLOCKBLOB_PATH_SIG, bs, bb1->id, "string", 1) != 0) _BADMETACMD; // delete only line
    if (write_blockblob_metadata_path (BLOCKBLOB_PATH_SIG, bs, bb1->id, "") != 0) _BADMETACMD; // delete existing file
    if (read_blockblob_metadata_path  (BLOCKBLOB_PATH_SIG, bs, bb1->id, buf, sizeof(buf)) != -1) _BADMETACMD; // open nonexisting file
    if (update_entry_blockblob_metadata_path (BLOCKBLOB_PATH_SIG, bs, bb1->id, "test", 0) != 0) _BADMETACMD; // add first line
    /* 15 */ if (update_entry_blockblob_metadata_path (BLOCKBLOB_PATH_SIG, bs, bb1->id, "string", 0) != 0) _BADMETACMD; // add second line
    if (update_entry_blockblob_metadata_path (BLOCKBLOB_PATH_SIG, bs, bb1->id, "one", 0) != 0) _BADMETACMD; // add third line
    if (read_blockblob_metadata_path  (BLOCKBLOB_PATH_SIG, bs, bb1->id, buf, sizeof(buf)) != strlen(_STR2)) _BADMETACMD; // read file
    if (strcmp (buf, _STR2)) _BADMETACMD;
    _CLOSBB(bb1,B1);

    blobstore_close (bs);
    printf ("completed metadata test\n");
 done:
    return errors;
}

static int do_blobstore_test (const char * base, const char * name, blobstore_format_t format, blobstore_revocation_t revocation)
{
    int ret;
    int errors = 0;

    printf ("\ntesting blockblob creation (name=%s, format=%d, revocation=%d)\n", name, format, revocation);

    blobstore * bs = create_teststore (BS_SIZE, base, name, format, revocation, BLOBSTORE_SNAPSHOT_ANY);
    if (bs==NULL) { errors++; goto done; }

    blockblob * bb1, * bb2, * bb3, * bb4, * bb5, * bb6;
    _OPENBB(bb1,NULL,0,NULL,_CBB,0,-1); // creating with size=0,
    _OPENBB(bb1,NULL,BS_SIZE+1,NULL,_CBB,0,-1); // too big for blobstore
    _OPENBB(bb1,NULL,BB_SIZE,NULL,_CBB|BLOBSTORE_FLAG_RDWR,0,-1); // bad flag

    // create bb 1, 2, and 3
    _OPENBB(bb1,B2,BB_SIZE,NULL,_CBB,0,0); // bs size: 10
    sleep(1); // to ensure mod time of bb1 and bb2 is different
    _OPENBB(bb2,B3,BB_SIZE,"sig",_CBB,0,0); // bs size: 20
    _OPENBB(bb3,B1,BB_SIZE,B1,_CBB,0,0); // bs size: 30

    // test search
    blockblob_meta * results;
    int nresults;
    _SEARCH("\\)invalid-regular-expression",-1);
    _SEARCH("foobar",0);
    _SEARCH(B2,1);
    _SEARCH("BLOCKBLOB.*",3);
        
    _OPENBB(bb4,NULL,BB_SIZE,B1,0,0,-1); // null ID without create
    _OPENBB(bb4,B1,BB_SIZE+1,B1,0,0,-1); // wrong size
    _OPENBB(bb4,B1,BB_SIZE,"foo",0,0,-1); // wrong sig
    _OPENBB(bb4,NULL,BB_SIZE,NULL,_CBB,0,-1); // blobstore full, all blobs in use
    _CLOSBB(bb1,NULL);
    _CLOSBB(bb2,NULL);
    if (revocation == BLOBSTORE_REVOCATION_LRU) {
        printf ("=== starting revocation sub-test\n");
        _OPENBB(bb4,NULL,BB_SIZE,NULL,_CBB,0,0); // blobstore full, 2 blobs purgeable
        _OPENBB(bb5,B2,0,B2,0,0,-1); // should not exist due to purging
        _OPENBB(bb5,NULL,BB_SIZE,NULL,_CBB,0,0); // blobstore full, 1 blob purgeable
        _OPENBB(bb6,NULL,BB_SIZE,NULL,_CBB,0,-1); // blobstore full, nothing purgeable
        _CLOSBB(bb4,NULL);
        _OPENBB(bb4,NULL,BB_SIZE,NULL,_CBB,0,0); // blobstore full, 1 blob purgeable
        _CLOSBB(bb4,NULL);
        _CLOSBB(bb5,NULL);
        _OPENBB(bb6,B2,BB_SIZE*2,NULL,_CBB,0,0); // blobstore full, 2 blobs purgeable
        _CLOSBB(bb6,NULL);
        printf ("=== done with revocation sub-test\n");
    } else {
        printf ("=== starting no-revocation sub-test\n");
        _OPENBB(bb4,NULL,BB_SIZE,NULL,_CBB,0,-1); // blobstore full, cannot purge
        _OPENBB(bb2,B3,0,NULL,0,0,0); // open existing with any size (0)
        _DELEBB(bb2,B3,0);
        _OPENBB(bb1,B2,BB_SIZE,NULL,0,0,0); // open existing with the right size
        _DELEBB(bb1,B2,0);
        _OPENBB(bb6,B2,BB_SIZE*2,NULL,_CBB,0,0); // blobstore has room for 20
        _CLOSBB(bb6,B2);
        printf ("=== done with no-revocation sub-test\n");
    }
    _CLOSBB(bb3,B1);
    _OPENBB(bb3,B1,BB_SIZE,B1,0,0,0); // open existing with the right size
    _CLOSBB(bb3,B1);
    _OPENBB(bb3,B1,0,B1,0,0,0); // open existing with any size (0)
    _DELEBB(bb3,B1,0); // delete it
    _OPENBB(bb3,B1,0,B1,0,0,-1); // open non-existining one

    blobstore_close (bs);

    printf ("completed blobstore test (name=%s)\n", name);
 done:
    return errors;
}

#define LOCK_CYCLES 3
#define COMPETITIVE_PARTICIPANTS 3
#define COMPETITIVE_ITERATIONS 30
#define COMPETITIVE_PAUSE_USEC 5
#define COMPETITIVE_TIMEOUT_USEC 3000000L

static void * competitor_function (void * ptr)
{
    long long timeout_usec = * (long long *) ptr;
    int errors = 0;
    int timeouts = 0;
    int successes = 0;
    int nfiles = (sizeof(_farray)/sizeof(char *));

    printf ("%u/%u: competitor running with timeout=%lld\n", (unsigned int)pthread_self(), (int)getpid(), timeout_usec);
    int * fsuccesses = calloc (nfiles, sizeof (int));
    
    for (int i=0; i<COMPETITIVE_ITERATIONS; i++) {
        int findex = (int)(nfiles*((double)random()/RAND_MAX)); // pick random file
        int fd = open_and_lock (_farray[findex], _C, 0, BLOBSTORE_FILE_PERM);
        if (fd!=-1) {
            printf ("%u/%u: created test lock %d\n", (unsigned int)pthread_self(), (int)getpid(), findex);
        } else {
            if (_blobstore_errno != BLOBSTORE_ERROR_EXIST && // it is OK if file already exists
                _blobstore_errno != BLOBSTORE_ERROR_AGAIN) { // it is OK to lose the race for the lock
                errors++;
                continue;
            }
        }
        
        int flags = (i%2)?(_W):(_R); // alternate read and write locks
        if (fd==-1) 
            fd = open_and_lock (_farray[findex], flags, timeout_usec, BLOBSTORE_FILE_PERM);
        
        if (fd!=-1) {
            printf ("%u/%u: opened test lock %d (fd=%d %s)\n", (unsigned int)pthread_self(), (int)getpid(), findex, fd, _farray[findex]);
            fsuccesses [findex] ++;
            usleep (COMPETITIVE_PAUSE_USEC);
            close_and_unlock (fd);
        } else {
            if (_blobstore_errno != BLOBSTORE_ERROR_AGAIN) { // it is OK to lose the race for the lock
                printf ("%u/%u: error opening lock %d (%s)\n", (unsigned int)pthread_self(), (int)getpid(), findex, blobstore_get_last_msg());
                errors++;
            } else {
                printf ("%u/%u: timed out on lock %d (fd=%d %s)\n", (unsigned int)pthread_self(), (int)getpid(), findex, fd, _farray[findex]);
                timeouts++;
                if (timeout_usec>0) // we shouldn't time out with a non-zero timeout
                    errors++;
            }
        }
    }
    
    for (int findex=0; findex<nfiles; findex++) {
        if (fsuccesses [findex] == 0) {
            printf ("%u/%u: ERROR: no successes for %d\n", (unsigned int)pthread_self(), (int)getpid(), findex);
            errors++;
        } else {
            printf ("%u/%u: file %d successes %d\n", (unsigned int)pthread_self(), (int)getpid(), findex, fsuccesses [findex]);
            successes += fsuccesses [findex];
        }
    }
    free (fsuccesses);

    printf ("%u/%u: successes=%d errors=%d timeouts=%d\n", (unsigned int)pthread_self(), (int)getpid(), successes, errors, timeouts);

    * (long long *) ptr = errors;
    return NULL;
}

static void * thread_function (void * ptr)
{
    printf ("this is a thread\n");
    int pid, ret, errors = 0;
    int fd1, fd2, fd3;

    _OPEN(fd2,F2,_W,0,-1);
    _OPEN(fd1,F1,_R,0,0);
    _CLOS(fd1,F1);
    _OPEN(fd3,F3,_W,0,0);
    * (int *) ptr = fd3;
    return NULL;
}

int do_file_lock_test (void)
{
    int pid, ret, errors = 0;
    int fd1, fd2, fd3;

    for (int lc=0; lc<LOCK_CYCLES; lc++) {
        printf ("\nintra-process locks cycle=%d\n", lc);

        _OPEN(fd1,F1,_W,300,-1);
        _OPEN(fd1,F1,_R,300,-1); 
        _OPEN(fd2,F1,_C,0,0);
        _OPEN(fd1,F1,_C,0,-1);
        _OPEN(fd1,F1,_W,300,-1);
        _OPEN(fd1,F1,_R,300,-1); 
        _CLOS(fd2,F1);
        _OPEN(fd2,F1,_R,0,0);
        _OPEN(fd1,F1,_W,300,-1);
        _OPEN(fd1,F1,_R,300,0);
        _OPEN(fd3,F1,_R,300,0);
        _CLOS(fd3,F1);
        _CLOS(fd2,F1);
        _CLOS(fd1,F1);
        _OPEN(fd1,F1,_W,300,0);
        _OPEN(fd2,F2,_C,0,0);
        _OPEN(fd3,F3,_C,0,0);
        _CLOS(fd2,F2);
        _CLOS(fd3,F3);
        _CLOS(fd1,F1);
        remove (F1);
        remove (F2);

        printf ("opening maximum number of descriptors\n");
        int fd [BLOBSTORE_MAX_CONCURRENT];
        for (int j=0; j<BLOBSTORE_MAX_CONCURRENT; j++) {
            fd [j] = open_and_lock (F3, _R, 0, BLOBSTORE_FILE_PERM);
            if (fd [j] == -1) {
                _UNEXPECTED;
                printf ("opened %d descriptors (max is %d)\n", j+1, BLOBSTORE_MAX_CONCURRENT);
            }
        }
        _OPEN(fd3,F3,_R,0,-1);
        for (int j=0; j<BLOBSTORE_MAX_CONCURRENT; j++) {
            if (close_and_unlock (fd [(j+9)%BLOBSTORE_MAX_CONCURRENT]) == -1) { // close them in different order
                _UNEXPECTED;
            }
        }
        remove (F3);

        // highly concurrent test that involves creating and then opening
        // three blobs (F1, F2, F3) from several threads over many iterations
        for (long long t=0L; t<=COMPETITIVE_TIMEOUT_USEC; t+=COMPETITIVE_TIMEOUT_USEC) { // do once without timeout, then once with timeout
            printf ("spawning %d competing threads timeout=%lld\n", COMPETITIVE_PARTICIPANTS, t);
            pthread_t threads [COMPETITIVE_PARTICIPANTS];
            long long thread_par [COMPETITIVE_PARTICIPANTS];
            int thread_par_sum = 0;
            for (int j=0; j<COMPETITIVE_PARTICIPANTS; j++) {
                thread_par [j] = t; // pass timeout to thread
                pthread_create (&threads[j], NULL, competitor_function, (void *)&thread_par[j]);
            }
            for (int j=0; j<COMPETITIVE_PARTICIPANTS; j++) {
                pthread_join (threads[j], NULL);
                thread_par_sum += (int)thread_par [j];
            }
            printf ("waited for all competing threads (returned sum=%d) timeout=%lld\n", thread_par_sum, t);
            remove (F1);
            remove (F2);
            remove (F3);
            errors += thread_par_sum;
        }
    }

    for (int lc=0; lc<LOCK_CYCLES; lc++) {
        printf ("\ninter-process locks cycle=%d\n", lc);
        _OPEN(fd1,F1,_W,300,-1);
        _OPEN(fd1,F1,_R,300,-1); 
        _OPEN(fd1,F1,_C,0,0);
        fflush (stdout);
        fflush (stderr);

        pid = fork();
        if (pid) {
            _PARENT_WAITS;
        } else {
            errors = 0;
            close_and_unlock (fd1);
            _OPEN(fd1,F1,_C,0,-1);
            _OPEN(fd1,F1,_W,600000,-1);
            _OPEN(fd1,F1,_R,600000,-1); 
            _OPEN(fd1,F2,_C,0,0); // test unlocking upon exit
            _OPEN(fd2,F3,_C,0,0);
            _CLOS(fd2,F3);
            _OPEN(fd2,F3,_W,0,0); // test unlocking upon exit
            fflush (stdout);
            _exit (errors);
        }
        _CLOS(fd1,F1);
        _OPEN(fd2,F2,_R,0,0);
        _OPEN(fd3,F3,_W,0,0);
        fflush (stdout);
        fflush (stderr);
        pid = fork();
        if (pid) {
            _PARENT_WAITS;
        } else {
            errors = 0;
            close_and_unlock (fd2);
            close_and_unlock (fd3);
            _OPEN(fd2,F2,_W,30000,-1);
            _OPEN(fd2,F2,_R,0,0);
            _OPEN(fd3,F2,_W,30000,-1);
            _OPEN(fd3,F3,_W,30000,-1);
            fflush (stdout);
            _exit (errors);
        }
        _CLOS(fd3,F3);
        _CLOS(fd2,F2);
        _OPEN(fd3,F3,_W,0,0);
        _CLOS(fd3,F3);

        fflush (stdout);
        fflush (stderr);
        pid = fork();
        if (pid) {
            _PARENT_WAITS;
        } else {
            _OPEN(fd2,F2,_W,0,0);
            fflush (stdout);
            pid = * (int *)0; // crash!
        }
        _OPEN(fd2,F2,_W,0,0);
        _OPEN(fd1,F1,_R,0,0);
        pthread_t thread;
        int fd_thread;
        pthread_create (&thread, NULL, thread_function, (void *)&fd_thread);
        pthread_join (thread, NULL);
        printf ("waited for thread (returned fd=%d)\n", fd_thread);
        _OPEN(fd3,F3,_R,3000,-1);
        _OPEN(fd3,F3,_W,3000,-1);
        _CLOS(fd_thread,F3);
        _OPEN(fd3,F3,_R,3000,0);
        _CLOS(fd3,F3);
        _CLOS(fd2,F2);
        _CLOS(fd1,F1);
        remove (F1);
        remove (F2);
        remove (F3);

        // highly concurrent test that involves creating and then opening
        // three blobs (F1, F2, F3) from several processes over many iterations
        for (long long t=0L; t<=COMPETITIVE_TIMEOUT_USEC; t+=COMPETITIVE_TIMEOUT_USEC) { // do once without timeout, then once with timeout
            printf ("spawning %d competing processes timeout=%lld\n", COMPETITIVE_PARTICIPANTS, t);
            int pids [COMPETITIVE_PARTICIPANTS];
            int proc_ret_sum = 0;
            fflush (stdout);
            fflush (stderr);
            for (int i=0; i<COMPETITIVE_PARTICIPANTS; i++) {
                pids [i] = fork();
                if (pids [i] == 0) { // child
                    long long ret = t;
                    competitor_function (&ret);
                    _exit ((int)ret);
                }
            }
            for (int i=0; i<COMPETITIVE_PARTICIPANTS; i++) {
                int status;
                waitpid (pids[i], &status, 0);
                proc_ret_sum += WEXITSTATUS(status);
            }
            fflush (stdout);
            fflush (stderr);
            printf ("waited for all competing processes (returned sum=%d) timeout=%lld\n", proc_ret_sum, t);
            remove (F1);
            remove (F2);
            remove (F3);
            errors += proc_ret_sum;
        }
    }
    return errors;
}

static void dummy_err_fn (const char * msg) { }

int main (int argc, char ** argv)
{
    int errors = 0;
    char cwd [1024];
    getcwd (cwd, sizeof (cwd));
    srandom (time(NULL));

    logfile (NULL, EUCADEBUG2, 4);
    blobstore_set_error_function (dummy_err_fn);    

    // if an argument is specified, it is treated as a blob name to create 
    // this allows two simultaneous invocations of test_blobstore to compete
    // for the same blob so as to test the inter-process locks manually
    if (argc > 1) {
        blobstore * bs = blobstore_open (".", 1000, BLOBSTORE_FLAG_CREAT, BLOBSTORE_FORMAT_FILES, BLOBSTORE_REVOCATION_ANY, BLOBSTORE_SNAPSHOT_ANY);
        if (bs==NULL) {
            printf ("ERROR: when opening blobstore: %s\n", blobstore_get_error_str(blobstore_get_error()));
            return 1;
        }
        char * id = argv [1];
        printf ("---------> opening blob %s\n", id);
        blockblob * bb = blockblob_open (bs, id, 20, BLOBSTORE_FLAG_CREAT, NULL, 1000);
        if (bb==NULL) {
            printf ("ERROR: when opening blockblob: %s\n", blobstore_get_error_str(blobstore_get_error()));
            return 1;
        }

        printf ("---------> writing to %s\n", blockblob_get_file(bb));
        int fd = open (blockblob_get_file(bb), O_RDWR);
        assert (fd>=0);
        char buf [32];
        bzero (buf, sizeof (buf));
        snprintf (buf, sizeof (buf), "%lld\n", (long long)time(NULL));
        write (fd, buf, strlen (buf));
        close (fd);

        printf ("---------> sleeping while holding blob %s\n", id);
        sleep (15);
        printf ("----------> closing blob %s\n", id);
        blockblob_close (bb);
        blobstore_close (bs);
        return 0;
    }

    printf ("testing blobstore.c\n");

    errors += do_file_lock_test ();
    if (errors) goto done; // no point in doing blobstore test if above isn't working

    errors += do_metadata_test (cwd, "directory-meta");
    if (errors) goto done; // no point in doing blobstore test if above isn't working

    errors += do_blobstore_test (cwd, "directory-norevoc", BLOBSTORE_FORMAT_DIRECTORY,   BLOBSTORE_REVOCATION_NONE);
    if (errors) goto done; // no point in continuing blobstore test if above isn't working

    errors += do_blobstore_test (cwd, "lru-directory", BLOBSTORE_FORMAT_DIRECTORY,   BLOBSTORE_REVOCATION_LRU);
    if (errors) goto done; // no point in continuing blobstore test if above isn't working

    errors += do_blobstore_test (cwd, "lru-visible", BLOBSTORE_FORMAT_FILES, BLOBSTORE_REVOCATION_LRU);
    if (errors) goto done; // no point in doing copy test if above isn't working

    errors += do_copy_test (cwd, "copy");
    if (errors) goto done; // no point in doing clone test if above isn't working

    errors += do_clone_test (cwd, "clone-with-snapshot", BLOBSTORE_FORMAT_DIRECTORY, BLOBSTORE_REVOCATION_LRU, BLOBSTORE_SNAPSHOT_DM, BLOBSTORE_SNAPSHOT);
    if (errors) goto done; // no point in doing clone stress test test if above isn't working

    errors += do_clone_test (cwd, "clone-with-copy", BLOBSTORE_FORMAT_DIRECTORY, BLOBSTORE_REVOCATION_LRU, BLOBSTORE_SNAPSHOT_DM, BLOBSTORE_COPY);
    if (errors) goto done; // no point in doing clone stress test test if above isn't working

    errors += do_clone_stresstest (cwd, "clonestress", BLOBSTORE_FORMAT_DIRECTORY, BLOBSTORE_REVOCATION_LRU, BLOBSTORE_SNAPSHOT_DM);
    if (errors) goto done; // no point in continuing

 done:
    printf ("done testing blobstore.c (errors=%d)\n", errors);
    blobstore_cleanup();
    exit(errors);
}
#endif // _UNIT_TEST

/////////////////////////////////////////////// command-line client code ///////////////////////////////////////////////////

#ifdef _EUCA_BLOBS

#include "map.h"

#define USAGE "Usage: euca-blobs [cache=... work=...] command [param1] [param2]...\n"
#define HELP  "\n"\
    "\thelp\t\t- print this help message\n"\
    "\tlist\t\t- list blobs in work and cache\n"\
    "\tdelete [id]\t- delete blob with\n"
#define MAX_ARGS 5

static char show_debug = FALSE;
static char show_extras = FALSE;
static char show_children = FALSE;
static char show_parents = FALSE;
static char * euca_home = NULL;
static char * work_path = NULL;
static char * cache_path = NULL;
static blobstore * work_bs = NULL;
static blobstore * cache_bs = NULL;
static map * blob_map;

static void bs_errors (const char * msg) { 
    if (show_debug)
        fprintf (stderr, "{%u} blobstore: %s", (unsigned int)pthread_self(), msg);
}

static int open_blobstore (const char * path, blobstore ** bs, const char * name)
{
    if (path!=NULL) {
        blobstore_set_error_function ( &bs_errors ); 

        * bs = blobstore_open (path, 0, BLOBSTORE_FLAG_RDWR, BLOBSTORE_FORMAT_ANY, BLOBSTORE_REVOCATION_ANY, BLOBSTORE_SNAPSHOT_ANY);
        if (* bs == NULL) {
            fprintf (stderr, "failed to open %s blobstore in '%s': %s\n", name, path, blobstore_get_error_str(blobstore_get_error()));
            exit (1);
        }
        if (show_debug)
            fprintf (stderr, "opened %s blobstore in %s\n", name, path);
        return 1;
    }
    return 0;
}

static int open_blobstores ()
{
    int opened = 0;
    opened += open_blobstore (work_path, &work_bs, "work");
    opened += open_blobstore (cache_path, &cache_bs, "cache");
    return opened;
}

static void close_blobstores ()
{
    if (work_bs!=NULL)
        blobstore_close (work_bs);
    if (cache_bs!=NULL)
        blobstore_close (cache_bs);
}

static int do_list_bs (blobstore * bs, const char * regex)
{
    const char match_all_regex [] = ".*";
    const char * actual_regex = regex;
    if (actual_regex == NULL)
        actual_regex = match_all_regex;

    blockblob_meta * matches = NULL;
    int found = blobstore_search (bs, actual_regex, &matches);
    if (found < 0) {
        fprintf (stderr, "error: %s\n", blobstore_get_error_str(blobstore_get_error()));
    }
    for (blockblob_meta * bm = matches; bm; bm=bm->next) {
        char uid [MAX_PATH];
        snprintf (uid, sizeof (uid), "%s/%s", bs->path, bm->id);
        map_set (blob_map, uid, bm);
    }

    return found;
}

static void print_tree (const char * prefix, blockblob_meta * bm, blockblob_path_t type)
{
    char ** array = NULL;
    int array_size = 0;
    if (read_array_blockblob_metadata_path (type, bm->bs, bm->id, &array, &array_size)==-1)
        return;
        
    for (int i=0; i<array_size; i++) {
        char * child_store_path = strtok (array [i], " ");
        char * child_blob_id    = strtok (NULL, " "); // the remaining entries in array[i] are ignored
        char child_uid [MAX_PATH];

        if (strlen (child_store_path)<1 || strlen (child_blob_id)<1)
            continue;
        snprintf (child_uid, sizeof (child_uid), "%s/%s", child_store_path, child_blob_id);
        fprintf (stdout, "%s%s\n", prefix, child_uid);

        char next_prefix [25];
        snprintf (next_prefix, sizeof (next_prefix), "%s\t", prefix);
        blockblob_meta * child_bm = map_get (blob_map, child_uid);
        if (child_bm==NULL) {
            fprintf (stdout, "%s?????\n", next_prefix);
        } else {
            print_tree (next_prefix, child_bm, type);
        }
        free (array[i]);
    }
    if (array)
        free (array);
}

static int do_list (const char * regex)
{
    int total_found = 0;

    if (work_bs) {
        int found = do_list_bs (work_bs, regex);
        if (found>0)
            total_found += found;
    }
    if (cache_bs) {
        int found = do_list_bs (cache_bs, regex);
        if (found>0)
            total_found += found;
    }

    if (total_found > 0) {
        for (map * mp=blob_map; mp; mp=mp->next) {
            blockblob_meta * bm = (blockblob_meta *)mp->val;

            char loop_dev [100] = "";
            if (read_blockblob_metadata_path (BLOCKBLOB_PATH_LOOPBACK, bm->bs, bm->id, loop_dev, sizeof (loop_dev))==OK) {
                
            }
            char extras [100] = "";
            if (show_extras) {
                snprintf (extras, sizeof (extras), "%c%c%c %9llu %s",
                          (bm->in_use & BLOCKBLOB_STATUS_OPENED) ? ('o') : ('-'), // o = open
                          (bm->in_use & BLOCKBLOB_STATUS_BACKED) ? ('p') : ('-'), // p = has parents
                          (bm->in_use & BLOCKBLOB_STATUS_MAPPED) ? ('c') : ('-'), // c = has children
                          bm->size_bytes / 512L, // size is in sectors
                          ctime (&(bm->last_modified)));
                extras [strlen (extras)-1] = ' '; // remove the newline from date
            }
            fprintf (stdout, "%s%s\n", extras, mp->key);
            if (show_parents) {
                print_tree ("                           depends on: ", bm, BLOCKBLOB_PATH_DEPS);
            }
            if (show_children) {
                print_tree ("                          depended by: ", bm, BLOCKBLOB_PATH_REFS);
            }
        }
    }

    return total_found;
}

static int do_delete (const char * id)
{
    return 0;
}

static void usage (const char * msg)
{
    if (msg!=NULL)
        fprintf (stderr, "error: %s\n", msg);

    fprintf (stderr, USAGE);

    if (msg==NULL)
        fprintf (stderr, "Try 'euca-blobs help' for list of commands\n");

    exit (1);
}

static void set_global_parameter (char * key, char * val)
{
    if (strcmp (key, "work")==0) {
        work_path = val;
    } else if (strcmp (key, "cache")==0) {
        cache_path = val;
    } else if (strcmp (key, "debug")==0) {
        show_debug = parse_boolean (val);
    } else {
        fprintf (stderr, "unknown global parameter '%s'", key);
        exit (1);
    }
}

int main (int argc, char * argv[])
{
    char * command = NULL;
    char * args [MAX_ARGS]; bzero (args, sizeof (args));
    int nargs = 0;

    while ( *(++argv) ) {
        char * eq = strstr(*argv, "="); // global params have '='s
        
        if (eq==NULL) { // it's a command or its arguments
            if (command==NULL) {
                command = * argv;
            } else {
                if (nargs==MAX_ARGS) {
                    fprintf (stderr, "error: too many arguments for command '%s'\n", command);
                    exit (1);
                }
                args [nargs++] = * argv;
            }
        } else { // this is a parameter
            if (strlen (eq) == 1)
                usage ("parameters must have non-empty values");
            * eq = '\0'; // split key from value
            if (strlen (* argv) == 1)
                usage ("parameters must have non-empty names");
            char * key = * argv;
            char * val = eq + 1;
            if (key==NULL || val==NULL)
                usage ("syntax error in parameters");
            if (key[0]=='-') key++; // skip '-' if any
            if (key[0]=='-') key++; // skip second '-' if any

            if (command==NULL) { // without a preceding command => global parameter
                set_global_parameter (key, val);
                continue;
            } else {
                usage ("unexpected parameters after the command");
            }
        }
    }

    if (command==NULL)
        usage (NULL);

    if (show_debug)
        logfile (NULL, EUCADEBUG, 4);
    else
        logfile (NULL, EUCAWARN, 4);

    if (work_path==NULL || cache_path==NULL) {
        // use $EUCALYPTUS env var if available
        char euca_root [] = "";
        euca_home = getenv (EUCALYPTUS_ENV_VAR_NAME);
        if (!euca_home) {
            fprintf (stderr, "warning: env variable $EUCALYPTUS is not set, assuming root\n");
            euca_home = euca_root;
        }
        
        char euca_confs [2][MAX_PATH];
        snprintf (euca_confs [0], sizeof (euca_confs [0]), EUCALYPTUS_CONF_LOCATION, euca_home);
        snprintf (euca_confs [1], sizeof (euca_confs [1]), EUCALYPTUS_CONF_OVERRIDE_LOCATION, euca_home);
        char * instance_path = getConfString(euca_confs, 2, INSTANCE_PATH);
        if (instance_path == NULL) {
            char path [MAX_PATH];
            snprintf (path, sizeof (path), "%s/var/lib/eucalyptus/instances", euca_home);
            instance_path = strdup (path);
            fprintf (stderr, "warning: failed to obtain %s from eucalyptus.conf, will try '%s'\n", INSTANCE_PATH, instance_path);
        }

        if (work_path==NULL) {
            char def_work_path [MAX_PATH];  
            snprintf (def_work_path, sizeof (def_work_path), "%s/work", instance_path);
            work_path = strdup (def_work_path);
        }
        if (cache_path==NULL) {
            char def_cache_path [MAX_PATH]; 
            snprintf (def_cache_path, sizeof (def_cache_path), "%s/cache", instance_path);
            cache_path = strdup (def_cache_path);
        }
        free (instance_path);
    }

    blob_map = map_create (100);
    int ret = 0;
    if (strcmp (command, "help") == 0) {
        fprintf (stderr, USAGE);
        fprintf (stderr, HELP);

    } else if (strcmp (command, "list") == 0) {
        open_blobstores ();
        char * regexp = NULL;
        for (int i = 0; i<nargs; i++) {
            regexp = args [i];
            if (regexp==NULL || regexp[0]!='-' || regexp[1]=='\0')
                break;
            switch (regexp[1]) {
            case 'l': show_extras = TRUE; break;
            case 'p': show_parents = TRUE; break;
            case 'c': show_children = TRUE; break;
            default:
                fprintf (stderr, "error: unknown flag '-%c'\n", regexp[1]);
                exit (1);
            }
            regexp = NULL;
        }
        int found = do_list (regexp); // argument may be NULL
        if (show_debug)
            fprintf (stderr, "found %d blobs\n", found);
        close_blobstores ();

    } else if (strcmp (command, "delete") == 0) {
        if (nargs!=1) {
            fprintf (stderr, "error: command 'delete' requires one parameter: id of the blob to delete\n");
            exit (1);
        }
        open_blobstores ();
        ret = do_delete (args [0]);
        close_blobstores ();

    } else {
        usage ("unknown command");
    }

    exit (ret);
}

#endif // _EUCA_BLOBS
