package com.eucalyptus.webui.client.view;

import com.google.gwt.user.client.ui.IsWidget;

public interface DeviceCabinetModifyView extends IsWidget {
	
	public void setPresenter(Presenter presenter);
	
	public void popup(int room_id, String room_name, String room_desc);
	
	public interface Presenter {
		
		public boolean onOK(int room_id, String room_desc);
		
	}

}
