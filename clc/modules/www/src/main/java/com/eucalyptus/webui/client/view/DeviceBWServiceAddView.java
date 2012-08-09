package com.eucalyptus.webui.client.view;

import java.util.Date;
import java.util.List;

import com.google.gwt.user.client.ui.IsWidget;

public interface DeviceBWServiceAddView extends IsWidget {
	
	void setPresenter(Presenter presenter);
	
	void popup();
	
	void setAccountList(List<String> accountList);
	
	void setUserList(String account, List<String> userList);
	
	void setIPList(String account, String user, List<String> ipList);
	
	public interface Presenter {
		
		boolean onOK(String account, String user, Date starttime, Date endtime, String ip, int bandwidth);
		
		void lookupAccounts();
		
		void lookupUserByAccount(String account);
		
		void lookupIPsByUser(String account, String user);
		
		void onCancel();
		
	}

}
