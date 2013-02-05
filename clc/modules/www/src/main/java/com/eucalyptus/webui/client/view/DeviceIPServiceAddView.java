package com.eucalyptus.webui.client.view;

import java.util.Date;
import java.util.Map;

import com.google.gwt.user.client.ui.IsWidget;

public interface DeviceIPServiceAddView extends IsWidget {
	
	public void setPresenter(Presenter presenter);
	
	public void popup(int ip_id, String ip_addr);
	
	public void setAccountNames(Map<String, Integer> account_map);
    
    public void setUserNames(int account_id, Map<String, Integer> user_map);
	
	public interface Presenter {
		
		public boolean onOK(int ip_id, String is_desc, Date is_starttime, Date is_endtime, int user_id);
		
		public void lookupAccountNames();
		
		public void lookupUserNamesByAccountID(int account_id);
		
	}
	
}
