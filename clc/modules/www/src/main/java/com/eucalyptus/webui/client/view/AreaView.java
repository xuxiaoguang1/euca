package com.eucalyptus.webui.client.view;

public interface AreaView {
	void setFocus( );
	void display();
	
	void setPresenter( Presenter presenter );
	public interface Presenter {
	    String getText();
	}
}
