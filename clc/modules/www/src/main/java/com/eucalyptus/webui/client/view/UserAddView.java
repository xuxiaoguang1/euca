package com.eucalyptus.webui.client.view;

import java.util.ArrayList;

import com.eucalyptus.webui.shared.user.AccountInfo;
import com.eucalyptus.webui.shared.user.UserInfo;


public interface UserAddView {
	void setFocus();
	void display();
	void setAccountsInfo(ArrayList<AccountInfo> accounts);
	
	void setUser(UserInfo user);
	
	void setPresenter( Presenter presenter );
	
	public interface Presenter {
	    void process( UserInfo user );
	}
}
