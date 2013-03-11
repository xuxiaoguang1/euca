package com.eucalyptus.webui.client.view;

import java.util.Map;

import com.google.gwt.user.client.ui.IsWidget;

public interface DeviceBWServiceAddView extends IsWidget {
    
    public void setPresenter(Presenter presenter);
    
    public void popup();
    
    public void setAccountNames(Map<String, Integer> account_map);
    
    public void setUserNames(int account_id, Map<String, Integer> user_map);
    
    public void setIPs(int account_id, int user_id, Map<String, Integer> ip_map);
    
    public interface Presenter {
        
        public boolean onOK(String bs_desc, int bs_bw_max, int ip_id);
        
        public void lookupAccountNames();
        
        public void lookupUserNamesByAccountID(int account_id);
        
        public void lookupIPsWithoutBWService(int account_id, int user_id);
        
    }
    
}
