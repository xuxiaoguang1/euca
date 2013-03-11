package com.eucalyptus.webui.client.view;

import com.google.gwt.user.client.ui.IsWidget;

public interface DeviceBWServiceModifyView extends IsWidget {
    
    public void setPresenter(Presenter presenter);
    
    public void popup(int bs_id, String ip_addr, String bs_desc, int bs_bw_max, String account_name, String user_name);
    
    public interface Presenter {
        
        public boolean onOK(int bs_id, String bs_desc, int bs_bw_max);
        
    }
    
}
