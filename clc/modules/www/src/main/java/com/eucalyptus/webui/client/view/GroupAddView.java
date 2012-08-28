package com.eucalyptus.webui.client.view;

import java.util.ArrayList;

import com.eucalyptus.webui.shared.user.AccountInfo;
import com.eucalyptus.webui.shared.user.GroupInfo;

public interface GroupAddView {
	void setFocus( );
	void display();
	void setAccountsInfo(ArrayList<AccountInfo> accounts);
	
	void setPresenter( Presenter presenter );
	
	void setGroup(GroupInfo group);
	
	public interface Presenter {
	    void process( GroupInfo group );
	}
}
