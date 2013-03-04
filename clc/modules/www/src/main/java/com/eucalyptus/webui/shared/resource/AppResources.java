package com.eucalyptus.webui.shared.resource;

import java.io.Serializable;

public class AppResources implements Serializable {
    
    private static final long serialVersionUID = 5656441028059332029L;
    
    public final int cs_id;
    public final int ms_id;
    public final int ds_id;
    
    public AppResources(int cs_id, int ms_id, int ds_id) {
        this.cs_id = cs_id;
        this.ms_id = ms_id;
        this.ds_id = ds_id;
    }
    
}
