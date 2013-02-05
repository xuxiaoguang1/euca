package com.eucalyptus.webui.shared.resource.device;

import java.io.Serializable;
import java.util.Date;

public class MemoryInfo implements Serializable {

    private static final long serialVersionUID = -6701344100995593117L;
    
    public int mem_id;
    public String mem_name;
    public String mem_desc;
    public long mem_size;
    public long ms_reserved;
    public Date mem_creationtime;
    public Date mem_modifiedtime;
    public int server_id;
    
    public MemoryInfo() {
    }
    
    public MemoryInfo(int mem_id, String mem_name, String mem_desc, long mem_total, long ms_reserved, Date mem_creationtime, Date mem_modifiedtime, int server_id) {
        this.mem_id = mem_id;
        this.mem_name = mem_name;
        this.mem_desc = mem_desc;
        this.mem_size = mem_total;
        this.ms_reserved = ms_reserved;
        this.mem_creationtime = mem_creationtime;
        this.mem_modifiedtime = mem_modifiedtime;
        this.server_id = server_id;
    }
    
}
