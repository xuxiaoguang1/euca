package com.eucalyptus.webui.client.view;

import java.util.List;

public interface RunInstanceView {
	void setFocus( );
	void display();
	
	void setPresenter( Presenter presenter );
	public interface Presenter {
	    void processRun(String image, String keypair, String vmtype, String group);
	    List<String> getImages();
	    List<String> getKeypairs();
	}
}
