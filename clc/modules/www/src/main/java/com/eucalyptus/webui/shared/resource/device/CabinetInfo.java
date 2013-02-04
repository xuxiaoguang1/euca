package com.eucalyptus.webui.shared.resource.device;

import java.io.Serializable;
import java.util.Date;

public class CabinetInfo implements Serializable {
    
    private static final long serialVersionUID = -5293003273493323915L;
    
    public int cabinet_id;
    public String cabinet_name;
    public String cabinet_desc;
    public Date cabinet_creationtime;
    public Date cabinet_modifiedtime;
    public int room_id;
    
    public CabinetInfo() {
        /* do nothing */
    }
    
    public CabinetInfo(int cabinet_id, String cabinet_name, String cabinet_desc, Date cabinet_creationtime, Date cabinet_modifiedtime, int room_id) {
        this.cabinet_id = cabinet_id;
        this.cabinet_name = cabinet_name;
        this.cabinet_desc = cabinet_desc;
        this.cabinet_creationtime = cabinet_creationtime;
        this.cabinet_modifiedtime = cabinet_modifiedtime;
        this.room_id = room_id;
    }
    
}
