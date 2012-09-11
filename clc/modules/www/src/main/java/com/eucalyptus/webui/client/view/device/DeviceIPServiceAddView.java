package com.eucalyptus.webui.client.view.device;

import java.util.Date;
import java.util.List;

import com.eucalyptus.webui.client.service.SearchResultRow;
import com.google.gwt.user.client.ui.IsWidget;

public interface DeviceIPServiceAddView extends IsWidget {
	
	void setPresenter(Presenter presenter);
	
	void setValue(SearchResultRow row, Date starttime, Date endtime, String state);
	
	void setAccountList(List<String> accountList);
	
	void setUserList(String account, List<String> userList);
	
	void setVMList(String account, String user, List<String> list);
	
	void clearCache();
	
	public interface Presenter {
		
		boolean onOK(SearchResultRow row, String account, String user, String vmMark, 
				Date starttime, Date endtime, String state);
		
		void lookupAccounts();
		
		void lookupUserByAccount(String account);
		
		void lookupVMsByUser(String account, String user);
		
		void onCancel();
		
	}

}
