package com.eucalyptus.webui.client.view.device;

import com.google.gwt.user.client.ui.IsWidget;

public interface DeviceAreaModifyView extends IsWidget {
	
	void setPresenter(Presenter presenter);
	
	void popup(int area_id, String area_name, String area_desc);
	
	public interface Presenter {
		
		boolean onOK(int area_id, String area_desc);
		
	}

}
