package com.eucalyptus.webui.client.view;

import java.util.List;

import com.google.gwt.user.client.ui.IsWidget;

public interface DeviceIPServiceAddView extends IsWidget {
	
	public void setPresenter(Presenter presenter);
	
	public void popup(int ip_id, String ip_addr);
	
	public void setAccountNameList(List<String> account_name_list);
	
	public void setUserNameList(String account_name, List<String> user_name_list);
	
	public interface Presenter {
		
		public boolean onOK(int ip_id, String is_desc, String is_starttime, String is_endtime, String account_name, String user_name);
		
		public void lookupAccountNames();
		
		public void lookupUserNamesByAccountName(String account_name);
		
	}
	
}
