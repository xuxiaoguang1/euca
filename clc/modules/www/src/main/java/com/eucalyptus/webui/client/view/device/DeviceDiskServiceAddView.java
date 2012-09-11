package com.eucalyptus.webui.client.view.device;

import java.util.Date;
import java.util.List;

import com.eucalyptus.webui.client.service.SearchResultRow;
import com.google.gwt.user.client.ui.IsWidget;

public interface DeviceDiskServiceAddView extends IsWidget {
	
	void setPresenter(Presenter presenter);
	
	void setValue(SearchResultRow row, Date starttime, Date endtime, String state, String used);
	
	void setAccountList(List<String> accountList);
	
	void setUserList(String account, List<String> userList);
	
	void clearCache();
	
	public interface Presenter {
		
		boolean onOK(SearchResultRow row, String account, String user, Date starttime, Date endtime, String state,
				long used, long max);
		
		void lookupAccounts();
		
		void lookupUserByAccount(String account);
		
		void onCancel();
		
	}

}
