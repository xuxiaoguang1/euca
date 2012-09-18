package com.eucalyptus.webui.shared.resource.device;

import java.io.Serializable;
import java.util.Date;

public class MemoryInfo implements Serializable {

    private static final long serialVersionUID = -6701344100995593117L;
    
    public int memory_id;
    public String memory_name;
    public String memory_desc;
    public long memory_total;
    public Date memory_creationtime;
    public Date memory_modifiedtime;
    public int server_id;
    
    public MemoryInfo() {
    }
    
    public MemoryInfo(int memory_id, String memory_name, String memory_desc, long memory_total, Date memory_creationtime, Date memory_modifiedtime, int server_id) {
        this.memory_id = memory_id;
        this.memory_name = memory_name;
        this.memory_desc = memory_desc;
        this.memory_total = memory_total;
        this.memory_creationtime = memory_creationtime;
        this.memory_modifiedtime = memory_modifiedtime;
        this.server_id = server_id;
    }
    
}
