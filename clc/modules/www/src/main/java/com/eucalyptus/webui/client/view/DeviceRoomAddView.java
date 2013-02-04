package com.eucalyptus.webui.client.view;

import java.util.Map;

import com.google.gwt.user.client.ui.IsWidget;

public interface DeviceRoomAddView extends IsWidget {
	
	void setPresenter(Presenter presenter);
	
	void popup();
	
	void setAreaNames(Map<String, Integer> area_map);
	
	public interface Presenter {
		
		boolean onOK(String room_name, String room_desc, int area_id);
		
		void lookupAreaNames();
		
	}

}
