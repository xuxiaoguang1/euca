package com.eucalyptus.webui.client.view;

import java.util.List;

import com.google.gwt.user.client.ui.IsWidget;

public interface DeviceIPDeviceAddView extends IsWidget {
	
	void setPresenter(Presenter presenter);
	
	void popup();
	
	public interface Presenter {
		
		void onOK(List<String> publicList, List<String> privateList);
		
	}
	
}
