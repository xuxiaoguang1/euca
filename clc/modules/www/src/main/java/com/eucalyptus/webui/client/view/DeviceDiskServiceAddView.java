package com.eucalyptus.webui.client.view;

import java.util.Date;
import java.util.Map;

import com.google.gwt.user.client.ui.IsWidget;

public interface DeviceDiskServiceAddView extends IsWidget {
	
	public void setPresenter(Presenter presenter);
	
	public void popup(int disk_id, String disk_name, long ds_reserved, String server_name);
	
    public void setAccountNames(Map<String, Integer> account_map);
    
    public void setUserNames(int account_id, Map<String, Integer> user_map);
	
	public interface Presenter {
		
		public boolean onOK(int disk_id, String ds_desc, long ds_reserved, long ds_used, Date ds_starttime, Date ds_endtime, int user_id);
		
		public void lookupAccountNames();
		
		public void lookupUserNamesByAccountID(int account_id);
		
	}
	
}
