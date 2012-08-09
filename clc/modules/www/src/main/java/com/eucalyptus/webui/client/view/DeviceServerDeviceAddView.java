package com.eucalyptus.webui.client.view;

import com.google.gwt.user.client.ui.IsWidget;

public interface DeviceServerDeviceAddView extends IsWidget {
	
	void setPresenter(Presenter presenter);
	
	void popup(String[] stateValueList);
	
	public interface Presenter {
		
		boolean onOK(String mark, String name, String conf, String ip, int bw, String state, String room);
		
	}
	
}
