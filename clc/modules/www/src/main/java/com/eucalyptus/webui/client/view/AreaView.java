package com.eucalyptus.webui.client.view;

import java.util.List;

import com.eucalyptus.webui.shared.aws.ImageType;

public interface AreaView {
	void setFocus( );
	void display();
	
	void setPresenter( Presenter presenter );
	public interface Presenter {
	    String getText();
	}
}
