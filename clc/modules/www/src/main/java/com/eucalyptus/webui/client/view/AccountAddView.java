package com.eucalyptus.webui.client.view;

import com.eucalyptus.webui.client.ClientFactory;
import com.eucalyptus.webui.shared.user.AccountInfo;

public interface AccountAddView {
	void setFocus();
	void display(ClientFactory clientFactory);
	
	void setPresenter( Presenter presenter );
	
	void setAccount(AccountInfo account);
	
	public interface Presenter {
	    void process( AccountInfo account );
	}
}
