package com.eucalyptus.webui.client.view;

import java.util.List;

import com.google.gwt.user.client.ui.IsWidget;

public interface DeviceCPUServiceAddView extends IsWidget {
	
	public void setPresenter(Presenter presenter);
	
	public void popup(int cpu_id, String cpu_name, String server_name, int cs_reserved);
	
	public void setAccountNameList(List<String> account_name_list);
	
	public void setUserNameList(String account_name, List<String> user_name_list);
	
	public interface Presenter {
		
		public boolean onOK(int cpu_id, String cs_desc, int cs_used, String cs_starttime, String cs_endtime, String account_name, String user_name);
		
		public void lookupAccountNames();
		
		public void lookupUserNamesByAccountName(String account_name);
		
	}
	
}
