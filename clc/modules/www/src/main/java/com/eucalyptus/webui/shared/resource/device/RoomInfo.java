package com.eucalyptus.webui.shared.resource.device;

import java.io.Serializable;
import java.util.Date;

public class RoomInfo implements Serializable {

    private static final long serialVersionUID = 2867495750741331535L;
    
    public int room_id;
    public String room_name;
    public String room_desc;
    public Date room_creationtime;
    public Date room_modifiedtime;
    public int area_id;
    
    public RoomInfo() {
    }
    
    public RoomInfo(int room_id, String room_name, String room_desc, Date room_creationtime, Date room_modifiedtime, int area_id) {
        this.room_id = room_id;
        this.room_name = room_name;
        this.room_desc = room_desc;
        this.room_creationtime = room_creationtime;
        this.room_modifiedtime = room_modifiedtime;
        this.area_id = area_id;
    }
    
}
