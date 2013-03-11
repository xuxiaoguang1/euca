package com.eucalyptus.webui.client.view;

import java.util.Map;

import com.eucalyptus.webui.shared.resource.device.status.IPType;
import com.google.gwt.user.client.ui.IsWidget;

public interface DeviceIPServiceAddView extends IsWidget {
    
    public void setPresenter(Presenter presenter);
    
    public void popup();
    
    public void setAccountNames(Map<String, Integer> account_map);
    
    public void setUserNames(int account_id, Map<String, Integer> user_map);
    
    public interface Presenter {
        
        public boolean onOK(String is_desc, IPType ip_type, int count, int user_id);
        
        public void lookupAccountNames();
        
        public void lookupUserNamesByAccountID(int account_id);
        
    }
    
}
