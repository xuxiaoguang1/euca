package com.eucalyptus.webui.client.view;

import java.util.List;

import com.google.gwt.user.client.ui.IsWidget;

public interface DeviceMemoryServiceAddView extends IsWidget {
	
	public void setPresenter(Presenter presenter);
	
	public void popup(int memory_id, String memory_name, String server_name, long ms_reserved);
	
	public void setAccountNameList(List<String> account_name_list);
	
	public void setUserNameList(String account_name, List<String> user_name_list);
	
	public interface Presenter {
		
		public boolean onOK(int memory_id, String ms_desc, String ms_used, String ms_starttime, String ms_endtime, String account_name, String user_name);
		
		public void lookupAccountNames();
		
		public void lookupUserNamesByAccountName(String account_name);
		
	}
	
}
