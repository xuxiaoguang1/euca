package com.eucalyptus.webui.shared.resource.device;

import java.io.Serializable;
import java.util.Date;

import com.eucalyptus.webui.shared.resource.device.status.DiskState;

public class DiskServiceInfo implements Serializable {

    private static final long serialVersionUID = -2042295766843796262L;
    
    public int ds_id;
    public String ds_desc;
    public long ds_used;
    public DiskState disk_state;
    public Date ds_creationtime;
    public Date ds_modifiedtime;
    public int disk_id;
    public int user_id;
    
    public DiskServiceInfo() {
    }
    
    public DiskServiceInfo(int ds_id, String ds_desc, long ds_used, DiskState disk_state, Date ds_creationtime, Date ds_modifiedtime, int disk_id, int user_id) {
        this.ds_id = ds_id;
        this.ds_desc = ds_desc;
        this.ds_used = ds_used;
        this.disk_state = disk_state;
        this.ds_creationtime = ds_creationtime;
        this.ds_modifiedtime = ds_modifiedtime;
        this.disk_id = disk_id;
        this.user_id = user_id;
    }
    
}
