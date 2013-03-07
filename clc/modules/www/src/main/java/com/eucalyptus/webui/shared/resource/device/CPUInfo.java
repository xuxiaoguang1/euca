package com.eucalyptus.webui.shared.resource.device;

import java.io.Serializable;
import java.util.Date;

public class CPUInfo implements Serializable {

    private static final long serialVersionUID = -8959658652658533444L;
    
    public int cpu_id;
    public String cpu_desc;
    public int cpu_total;
    public int cs_reserved;
    public Date cpu_creationtime;
    public Date cpu_modifiedtime;
    public int server_id;
    
    public CPUInfo() {
    }
    
    public CPUInfo(int cpu_id, String cpu_desc, int cpu_total, int cpu_reserved, Date cpu_creationtime, Date cpu_modifiedtime, int server_id) {
        this.cpu_id = cpu_id;
        this.cpu_desc = cpu_desc;
        this.cpu_total = cpu_total;
        this.cs_reserved = cpu_reserved;
        this.cpu_creationtime = cpu_creationtime;
        this.cpu_modifiedtime = cpu_modifiedtime;
        this.server_id = server_id;
    }

}
