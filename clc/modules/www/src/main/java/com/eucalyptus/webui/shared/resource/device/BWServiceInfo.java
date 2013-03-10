package com.eucalyptus.webui.shared.resource.device;

import java.io.Serializable;
import java.util.Date;

public class BWServiceInfo implements Serializable {

    private static final long serialVersionUID = 5019892709903157779L;
    
    public int bs_id;
    public String bs_desc;
    public Date bs_starttime;
    public Date bs_endtime;
    public int bs_bw;
    public int bs_bw_max;
    public Date bs_creationtime;
    public Date bs_modifiedtime;
    public int ip_id;

    public BWServiceInfo() {
    }
    
    public BWServiceInfo(int bs_id, String bs_desc, Date bs_starttime, Date bs_endtime, int bs_bw, int bs_bw_max, Date bs_creationtime, Date bs_modifiedtime, int ip_id) {
        this.bs_id = bs_id;
        this.bs_desc = bs_desc;
        this.bs_starttime = bs_starttime;
        this.bs_endtime = bs_endtime;
        this.bs_bw = bs_bw;
        this.bs_bw_max = bs_bw_max;
        this.bs_creationtime = bs_creationtime;
        this.bs_modifiedtime = bs_modifiedtime;
        this.ip_id = ip_id;
    }
    
}
