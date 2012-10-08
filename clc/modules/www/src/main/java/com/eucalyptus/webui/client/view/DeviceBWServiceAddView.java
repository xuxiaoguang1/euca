package com.eucalyptus.webui.client.view;

import java.util.List;

import com.google.gwt.user.client.ui.IsWidget;

public interface DeviceBWServiceAddView extends IsWidget {
	
	public void setPresenter(Presenter presenter);
	
	public void popup();
	
	public void setAccountNameList(List<String> account_name_list);
	
	public void setUserNameList(String account_name, List<String> user_name_list);
	
	public void setIPAddrList(List<String> ip_addr_list, String account_name, String user_name);
	
	public interface Presenter {
		
		public boolean onOK(String ip_addr, String bs_desc, String bs_bw_max, String bs_starttime, String bs_endtime);
		
		public void lookupAccountNames();
		
		public void lookupUserNamesByAccountName(String account_name);
		
		public void lookupAddrByUserName(String account_name, String user_name);
		
	}
	
}
