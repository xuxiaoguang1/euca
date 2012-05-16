/*******************************************************************************
 * Copyright (c) 2009  Eucalyptus Systems, Inc.
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, only version 3 of the License.
 * 
 * 
 *  This file is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 *  for more details.
 * 
 *  You should have received a copy of the GNU General Public License along
 *  with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 *  Please contact Eucalyptus Systems, Inc., 130 Castilian
 *  Dr., Goleta, CA 93101 USA or visit <http://www.eucalyptus.com/licenses/>
 *  if you need additional information or have any questions.
 * 
 *  This file may incorporate work covered under the following copyright and
 *  permission notice:
 * 
 *    Software License Agreement (BSD License)
 * 
 *    Copyright (c) 2008, Regents of the University of California
 *    All rights reserved.
 * 
 *    Redistribution and use of this software in source and binary forms, with
 *    or without modification, are permitted provided that the following
 *    conditions are met:
 * 
 *      Redistributions of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 * 
 *      Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 * 
 *    THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 *    IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 *    TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 *    PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
 *    OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 *    EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 *    PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 *    PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 *    LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 *    NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *    SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. USERS OF
 *    THIS SOFTWARE ACKNOWLEDGE THE POSSIBLE PRESENCE OF OTHER OPEN SOURCE
 *    LICENSED MATERIAL, COPYRIGHTED MATERIAL OR PATENTED MATERIAL IN THIS
 *    SOFTWARE, AND IF ANY SUCH MATERIAL IS DISCOVERED THE PARTY DISCOVERING
 *    IT MAY INFORM DR. RICH WOLSKI AT THE UNIVERSITY OF CALIFORNIA, SANTA
 *    BARBARA WHO WILL THEN ASCERTAIN THE MOST APPROPRIATE REMEDY, WHICH IN
 *    THE REGENTS' DISCRETION MAY INCLUDE, WITHOUT LIMITATION, REPLACEMENT
 *    OF THE CODE SO IDENTIFIED, LICENSING OF THE CODE SO IDENTIFIED, OR
 *    WITHDRAWAL OF THE CODE CAPABILITY TO THE EXTENT NEEDED TO COMPLY WITH
 *    ANY SUCH LICENSES OR RIGHTS.
 *******************************************************************************
 * @author chris grzegorczyk <grze@eucalyptus.com>
 */

package com.eucalyptus.keys;

import javax.persistence.Column;
import javax.persistence.Lob;
import javax.persistence.PersistenceContext;
import javax.persistence.Table;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Entity;
import com.eucalyptus.auth.principal.AccountFullName;
import com.eucalyptus.auth.principal.Principals;
import com.eucalyptus.auth.principal.UserFullName;
import com.eucalyptus.cloud.CloudMetadata.KeyPairMetadata;
import com.eucalyptus.cloud.UserMetadata;
import com.eucalyptus.component.ComponentIds;
import com.eucalyptus.component.id.Eucalyptus;
import com.eucalyptus.util.FullName;
import com.eucalyptus.util.OwnerFullName;

@Entity
@javax.persistence.Entity
@PersistenceContext( name = "eucalyptus_cloud" )
@Table( name = "metadata_keypairs" )
@Cache( usage = CacheConcurrencyStrategy.TRANSACTIONAL )
public class SshKeyPair extends UserMetadata<SshKeyPair.State> implements KeyPairMetadata {
  enum State {
    available, removing
  }
  
  @Lob
  @Column( name = "metadata_keypair_public_key" )
  private String publicKey;
  @Column( name = "metadata_keypair_finger_print" )
  private String fingerPrint;
  
  SshKeyPair( ) {}
  
  SshKeyPair( OwnerFullName userFullName, String keyName ) {
    super( userFullName, keyName );
  }
  
  SshKeyPair( OwnerFullName user ) {
    super( user );
  }
  
  SshKeyPair( OwnerFullName user, String keyName, String publicKey, String fingerPrint ) {
    this( user, keyName );
    this.publicKey = publicKey;
    this.fingerPrint = fingerPrint;
  }
  
  public String getPublicKey( ) {
    return this.publicKey;
  }
  
  public void setPublicKey( String publicKey ) {
    this.publicKey = publicKey;
  }
  
  public String getFingerPrint( ) {
    return this.fingerPrint;
  }
  
  public void setFingerPrint( String fingerPrint ) {
    this.fingerPrint = fingerPrint;
  }
  
  @Override
  public String toString( ) {
    return String.format( "SshKeyPair:%s:fingerPrint=%s", this.getUniqueName( ), this.fingerPrint );
  }
  
  @Override
  public String getPartition( ) {
    return ComponentIds.lookup( Eucalyptus.class ).name( );
  }
  
  @Override
  public FullName getFullName( ) {
    return FullName.create.vendor( "euca" )
                          .region( ComponentIds.lookup( Eucalyptus.class ).name( ) )
                          .namespace( this.getOwnerAccountNumber( ) )
                          .relativeId( "keypair", this.getDisplayName( ) );
  }
  
  static SshKeyPair noKey( ) {
    return new SshKeyPair( Principals.nobodyFullName( ), "nokey", "", "" );
  }

  public static SshKeyPair named( OwnerFullName ownerFullName, String keyName ) {
    return new SshKeyPair( ownerFullName, keyName );
  }
  
  public static SshKeyPair create( UserFullName userFullName, String keyName ) {
    return new SshKeyPair( userFullName, keyName );
  }

  public static SshKeyPair withPublicKey( OwnerFullName ownerFullName, String keyValue ) {
    return new SshKeyPair( ownerFullName, null, keyValue, null );
  }

  public static SshKeyPair withPublicKey( OwnerFullName ownerFullName, String keyName, String keyValue ) {
    return new SshKeyPair( ownerFullName, keyName, keyValue, null );
  }
}
