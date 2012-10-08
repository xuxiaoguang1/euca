package com.eucalyptus.webui.shared.resource.device;

import java.io.Serializable;
import java.util.Date;

import com.eucalyptus.webui.shared.resource.device.status.IPType;

public class IPInfo implements Serializable {

    private static final long serialVersionUID = 7122386390408761109L;
    
    public int ip_id;
    public String ip_addr;
    public String ip_desc;
    public IPType ip_type;
    public Date ip_creationtime;
    public Date ip_modifiedtime;
    
    public IPInfo() {
    }
    
    public IPInfo(int ip_id, String ip_addr, String ip_desc, IPType ip_type, Date ip_creationtime, Date ip_modifiedtime) {
        this.ip_id = ip_id;
        this.ip_addr = ip_addr;
        this.ip_desc = ip_desc;
        this.ip_type = ip_type;
        this.ip_creationtime = ip_creationtime;
        this.ip_modifiedtime = ip_modifiedtime;
    }
    
}
