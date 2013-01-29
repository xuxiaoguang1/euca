package com.eucalyptus.webui.client.view;

import java.util.Map;

import com.google.gwt.user.client.ui.IsWidget;

public interface DeviceCabinetAddView extends IsWidget {
	
	public void setPresenter(Presenter presenter);
	
	public void popup();
	
	public void setAreaNames(Map<String, Integer> area_map);
	
	public void setRoomNames(int area_id, Map<String, Integer> room_map);
	
	public interface Presenter {
		
		public boolean onOK(String cabinet_name, String cabinet_desc, int room_id);
		
		public void lookupAreaNames();
		
		public void lookupRoomNamesByAreaID(int area_id);
		
	}

}
