package com.eucalyptus.webui.shared.resource.device;

import java.io.Serializable;
import java.util.Date;

import com.eucalyptus.webui.shared.resource.device.status.MemoryState;

public class MemoryServiceInfo implements Serializable {

    private static final long serialVersionUID = -2042295766843796262L;
    
    public int ms_id;
    public String ms_desc;
    public long ms_used;
    public Date ms_starttime;
    public Date ms_endtime;
    public MemoryState memory_state;
    public Date ms_creationtime;
    public Date ms_modifiedtime;
    public int memory_id;
    public int user_id;
    
    public MemoryServiceInfo() {
    }
    
    public MemoryServiceInfo(int ms_id, String ms_desc, long ms_used, Date ms_starttime, Date ms_endtime, MemoryState memory_state, Date ms_creationtime, Date ms_modifiedtime, int memory_id, int user_id) {
        this.ms_id = ms_id;
        this.ms_desc = ms_desc;
        this.ms_used = ms_used;
        this.ms_starttime = ms_starttime;
        this.ms_endtime = ms_endtime;
        this.memory_state = memory_state;
        this.ms_creationtime = ms_creationtime;
        this.ms_modifiedtime = ms_modifiedtime;
        this.memory_id = memory_id;
        this.user_id = user_id;
    }
    
}
