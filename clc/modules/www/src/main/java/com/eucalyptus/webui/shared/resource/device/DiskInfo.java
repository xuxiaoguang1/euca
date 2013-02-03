package com.eucalyptus.webui.shared.resource.device;

import java.io.Serializable;
import java.util.Date;

public class DiskInfo implements Serializable {

    private static final long serialVersionUID = -6701344100995593117L;
    
    public int disk_id;
    public String disk_name;
    public String disk_desc;
    public long disk_size;
    public long ds_reserved;
    public Date disk_creationtime;
    public Date disk_modifiedtime;
    public int server_id;
    
    public DiskInfo() {
    }
    
    public DiskInfo(int disk_id, String disk_name, String disk_desc, long disk_total, long ds_reserved, Date disk_creationtime, Date disk_modifiedtime, int server_id) {
        this.disk_id = disk_id;
        this.disk_name = disk_name;
        this.disk_desc = disk_desc;
        this.disk_size = disk_total;
        this.ds_reserved = ds_reserved;
        this.disk_creationtime = disk_creationtime;
        this.disk_modifiedtime = disk_modifiedtime;
        this.server_id = server_id;
    }
    
}
