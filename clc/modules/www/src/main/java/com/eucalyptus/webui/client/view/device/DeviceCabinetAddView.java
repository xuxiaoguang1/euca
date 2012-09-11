package com.eucalyptus.webui.client.view.device;

import java.util.Collection;

import com.google.gwt.user.client.ui.IsWidget;

public interface DeviceCabinetAddView extends IsWidget {
	
	void setPresenter(Presenter presenter);
	
	void popup();
	
	void setAreaNameList(Collection<String> area_name_list);
	
	void setRoomNameList(String area_name, Collection<String> room_name_list);
	
	public interface Presenter {
		
		boolean onOK(String cabinet_name, String cabinet_desc, String room_name);
		
		void lookupAreaNames();
		
		void lookupRoomNamesByAreaName(String area_name);
		
	}

}
