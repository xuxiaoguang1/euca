package com.eucalyptus.webui.client.view;

import java.util.Date;
import java.util.Map;

import com.google.gwt.user.client.ui.IsWidget;

public interface DeviceMemoryServiceAddView extends IsWidget {
	
	public void setPresenter(Presenter presenter);
	
	public void popup(int mem_id, String mem_name, long ms_reserved, String server_name);
	
	public void setAccountNames(Map<String, Integer> account_map);
    
    public void setUserNames(int account_id, Map<String, Integer> user_map);
	
	public interface Presenter {
		
	    public boolean onOK(int mem_id, String ms_desc, long ms_reserved, long ms_used, Date ms_starttime, Date ms_endtime, int user_id);
		
        public void lookupAccountNames();
        
        public void lookupUserNamesByAccountID(int account_id);
		
	}
	
}
