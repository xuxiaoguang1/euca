package com.eucalyptus.webui.client.view.device;

import com.google.gwt.user.client.ui.IsWidget;

public interface DeviceCabinetModifyView extends IsWidget {
	
	void setPresenter(Presenter presenter);
	
	void popup(int room_id, String room_name, String room_desc);
	
	public interface Presenter {
		
		boolean onOK(int room_id, String room_desc);
		
	}

}
