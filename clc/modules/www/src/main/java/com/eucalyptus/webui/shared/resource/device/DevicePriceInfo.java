package com.eucalyptus.webui.shared.resource.device;

import java.io.Serializable;
import java.util.Date;

public class DevicePriceInfo implements Serializable {

    private static final long serialVersionUID = -8523479379621055990L;
    
    public String op_name;
    public String op_desc;
    public double op_price;
    public Date op_modifiedtime;
    
    public DevicePriceInfo() {
        /* do nothing */
    }
    
    public DevicePriceInfo(String op_name, String op_desc, double op_price, Date op_modifiedtime) {
        this.op_name = op_name;
        this.op_desc = op_desc;
        this.op_price = op_price;
        this.op_modifiedtime = op_modifiedtime;
    }
    
}
