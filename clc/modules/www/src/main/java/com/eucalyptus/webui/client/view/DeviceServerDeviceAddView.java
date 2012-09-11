package com.eucalyptus.webui.client.view;

import java.util.List;

import com.google.gwt.user.client.ui.IsWidget;

public interface DeviceServerDeviceAddView extends IsWidget {
	
	void setPresenter(Presenter presenter);
	
	void popup(String[] stateValueList);
	
	void setAreaNameList(List<String> area_name_list);
	
	void setRoomNameList(String area_name, List<String> room_name_list);
	
	void setCabinetNameList(String room_name, List<String> cabinet_name_list);
	
	public interface Presenter {
		
		boolean onOK(String mark, String name, String conf, String ip, int bw, String state, String cabinet_name);
		
		void lookupAreaNames();
		
		void lookupRoomNamesByAreaName(String area_name);
		
		void lookupCabinetNamesByRoomName(String room_name);
		
	}
	
}
