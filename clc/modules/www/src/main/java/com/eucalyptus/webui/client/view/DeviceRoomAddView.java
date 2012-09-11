package com.eucalyptus.webui.client.view;

import java.util.Collection;

import com.google.gwt.user.client.ui.IsWidget;

public interface DeviceRoomAddView extends IsWidget {
	
	void setPresenter(Presenter presenter);
	
	void popup();
	
	void setAreaNameList(Collection<String> area_name_list);
	
	public interface Presenter {
		
		boolean onOK(String room_name, String room_desc, String area_name);
		
		void lookupAreaNames();
		
	}

}
