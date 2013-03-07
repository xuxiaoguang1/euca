package com.eucalyptus.webui.client.view;

import com.google.gwt.user.client.ui.IsWidget;

public interface DeviceMemoryModifyView extends IsWidget {
    
    public void setPresenter(Presenter presenter);
    
    public void popup(int mem_id, String mem_desc, long mem_size, long ms_used, String server_name);
    
    public interface Presenter {
        
        public boolean onOK(int mem_id, String mem_desc, long mem_size, long ms_used);
        
    }

}
