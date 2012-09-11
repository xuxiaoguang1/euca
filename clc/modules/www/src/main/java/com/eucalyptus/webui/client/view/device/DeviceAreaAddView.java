package com.eucalyptus.webui.client.view.device;

import com.google.gwt.user.client.ui.IsWidget;

public interface DeviceAreaAddView extends IsWidget {
	
	void setPresenter(Presenter presenter);
	
	void popup();

	public interface Presenter {
		
		boolean onOK(String area_name, String area_desc);
		
	}

}
