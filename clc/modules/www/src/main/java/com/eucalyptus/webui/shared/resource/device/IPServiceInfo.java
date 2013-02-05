package com.eucalyptus.webui.shared.resource.device;

import java.io.Serializable;
import java.util.Date;

import com.eucalyptus.webui.shared.resource.device.status.IPState;
import com.eucalyptus.webui.shared.resource.device.status.IPType;

public class IPServiceInfo implements Serializable {

    private static final long serialVersionUID = -6509974246624205484L;
    
    public int ip_id;
    public String ip_addr;
    public String ip_desc;
    public IPType ip_type;
    public Date ip_creationtime;
    public Date ip_modifiedtime;
    public String is_desc;
    public Date is_starttime;
    public Date is_endtime;
    public IPState is_state;
    public Date is_creationtime;
    public Date is_modifiedtime;
    public int user_id;
    
    public IPServiceInfo() {
    }
    
    public IPServiceInfo(int ip_id, String ip_addr, String ip_desc, IPType ip_type, Date ip_creationtime, Date ip_modifiedtime, String is_desc, Date is_starttime, Date is_endtime, IPState is_state, Date is_creationtime, Date is_modifiedtime, int user_id) {
    	this.ip_id = ip_id;
        this.ip_addr = ip_addr;
        this.ip_desc = ip_desc;
        this.ip_type = ip_type;
        this.ip_creationtime = ip_creationtime;
        this.ip_modifiedtime = ip_modifiedtime;
        this.is_desc = is_desc;
        this.is_starttime = is_starttime;
        this.is_endtime = is_endtime;
        this.is_state = is_state;
        this.is_creationtime = is_creationtime;
        this.is_modifiedtime = is_modifiedtime;
        this.ip_id = ip_id;
        this.user_id = user_id;
    }
    
}
