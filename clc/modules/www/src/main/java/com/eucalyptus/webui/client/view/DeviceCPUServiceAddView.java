package com.eucalyptus.webui.client.view;

import java.util.Date;
import java.util.Map;

import com.google.gwt.user.client.ui.IsWidget;

public interface DeviceCPUServiceAddView extends IsWidget {
	
	public void setPresenter(Presenter presenter);
	
	public void popup(int cpu_id, String cpu_name, int cs_reserved, String server_name);
	
	public void setAccountNames(Map<String, Integer> account_map);
    
    public void setUserNames(int account_id, Map<String, Integer> user_map);
	
	public interface Presenter {
		
		public boolean onOK(int cpu_id, String cs_desc, int cs_reserved, int cs_used, Date cs_starttime, Date cs_endtime, int user_id);
		
		public void lookupAccountNames();
		
		public void lookupUserNamesByAccountID(int account_id);
		
	}
	
}
