package com.eucalyptus.webui.shared.resource.device;

import java.io.Serializable;
import java.util.Date;

public class CPUPriceInfo implements Serializable {

    private static final long serialVersionUID = 163042748632284245L;
    
    public final int cp_id;
    public final String cpu_name;
    public final String cp_desc;
    public final double cp_price;
    public final Date cp_creationtime;
    public final Date cp_modifiedtime;
    
    public CPUPriceInfo(int cp_id, String cpu_name, String cp_desc, double cp_price, Date cp_creationtime, Date cp_modifiedtime) {
        this.cp_id = cp_id;
        this.cpu_name = cpu_name;
        this.cp_desc = cp_desc;
        this.cp_price = cp_price;
        this.cp_creationtime = cp_creationtime;
        this.cp_modifiedtime = cp_modifiedtime;
    }
    
}
