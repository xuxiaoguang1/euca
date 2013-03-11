package com.eucalyptus.webui.shared.resource.device;

import java.io.Serializable;
import java.util.Date;

import com.eucalyptus.webui.shared.resource.device.status.IPState;
import com.eucalyptus.webui.shared.resource.device.status.IPType;

public class IPServiceInfo implements Serializable {

    private static final long serialVersionUID = -6509974246624205484L;
    
    public int ip_id;
    public String ip_addr;
    public IPType ip_type;
    public String is_desc;
    public IPState is_state;
    public Date is_creationtime;
    public Date is_modifiedtime;
    public int user_id;
    
    public IPServiceInfo() {
    }
    
    public IPServiceInfo(int ip_id, String ip_addr, IPType ip_type, String is_desc, IPState is_state, Date is_creationtime, Date is_modifiedtime, int user_id) {
        this.ip_id = ip_id;
        this.ip_addr = ip_addr;
        this.ip_type = ip_type;
        this.is_desc = is_desc;
        this.is_state = is_state;
        this.is_creationtime = is_creationtime;
        this.is_modifiedtime = is_modifiedtime;
        this.ip_id = ip_id;
        this.user_id = user_id;
    }
    
}
