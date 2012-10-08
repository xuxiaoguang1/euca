package com.eucalyptus.webui.shared.resource.device;

import java.io.Serializable;
import java.util.Date;

import com.eucalyptus.webui.shared.resource.device.status.IPState;

public class IPServiceInfo implements Serializable {

    private static final long serialVersionUID = -6509974246624205484L;
    
    public int is_id;
    public String is_desc;
    public Date is_starttime;
    public Date is_endtime;
    public IPState is_state;
    public Date is_creationtime;
    public Date is_modifiedtime;
    public int ip_id;
    public int user_id;
    
    public IPServiceInfo() {
    }
    
    public IPServiceInfo(int is_id, String is_desc, Date is_starttime, Date is_endtime, IPState is_state, Date is_creationtime, Date is_modifiedtime, int ip_id, int user_id) {
        this.is_id = is_id;
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
