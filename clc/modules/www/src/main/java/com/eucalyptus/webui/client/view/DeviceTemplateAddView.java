package com.eucalyptus.webui.client.view;

import com.google.gwt.user.client.ui.IsWidget;

public interface DeviceTemplateAddView extends IsWidget {
	
	void setPresenter(Presenter presenter);
	
	void popup();
	
	public interface Presenter {
		
		boolean onOK(String mark, String cpu, String mem, String disk, String bw, String image);
		
	}

}
