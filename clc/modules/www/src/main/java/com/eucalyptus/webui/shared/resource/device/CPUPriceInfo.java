package com.eucalyptus.webui.shared.resource.device;

import java.io.Serializable;
import java.util.Date;

public class CPUPriceInfo implements Serializable {

    private static final long serialVersionUID = 163042748632284245L;
    
    public int cp_id;
    public String cpu_name;
    public String cp_desc;
    public double cp_price;
    public Date cp_creationtime;
    public Date cp_modifiedtime;
    
    public CPUPriceInfo() {
        /* do nothing */
    }
    
    public CPUPriceInfo(int cp_id, String cpu_name, String cp_desc, double cp_price, Date cp_creationtime, Date cp_modifiedtime) {
        this.cp_id = cp_id;
        this.cpu_name = cpu_name;
        this.cp_desc = cp_desc;
        this.cp_price = cp_price;
        this.cp_creationtime = cp_creationtime;
        this.cp_modifiedtime = cp_modifiedtime;
    }
    
}
