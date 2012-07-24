package com.eucalyptus.webui.client.view;

import java.util.ArrayList;

public interface DialogInputViewTemp {

	void setPresenter( Presenter presenter );
	
	public interface Presenter {
	    void process(ArrayList<String> values );
	}
}
