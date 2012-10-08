package com.eucalyptus.webui.client.view;

import java.util.List;

import com.google.gwt.user.client.ui.IsWidget;

public interface DeviceDiskServiceAddView extends IsWidget {
	
	public void setPresenter(Presenter presenter);
	
	public void popup(int disk_id, String disk_name, String server_name, long ds_reserved);
	
	public void setAccountNameList(List<String> account_name_list);
	
	public void setUserNameList(String account_name, List<String> user_name_list);
	
	public interface Presenter {
		
		public boolean onOK(int disk_id, String ds_desc, String ds_used, String ds_starttime, String ds_endtime, String account_name, String user_name);
		
		public void lookupAccountNames();
		
		public void lookupUserNamesByAccountName(String account_name);
		
	}
	
}
