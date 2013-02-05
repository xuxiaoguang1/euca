package com.eucalyptus.webui.shared.resource.device;

import java.io.Serializable;
import java.util.Date;

public class AreaInfo implements Serializable {
    
    private static final long serialVersionUID = -5800197263355717258L;
    
    public int area_id;
    public String area_name;
    public String area_desc;
    public Date area_creationtime;
    public Date area_modifiedtime;
    
    public AreaInfo() {
        /* do nothing */
    }
    
    public AreaInfo(int area_id, String area_name, String area_desc, Date area_creationtime, Date area_modifiedtime) {
        this.area_id = area_id;
        this.area_name = area_name;
        this.area_desc = area_desc;
        this.area_creationtime = area_creationtime;
        this.area_modifiedtime = area_modifiedtime;
    }
    
}
