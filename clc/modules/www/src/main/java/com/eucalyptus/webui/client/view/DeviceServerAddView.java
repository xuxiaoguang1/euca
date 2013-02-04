package com.eucalyptus.webui.client.view;

import java.util.Map;

import com.eucalyptus.webui.shared.resource.device.status.ServerState;
import com.google.gwt.user.client.ui.IsWidget;

public interface DeviceServerAddView extends IsWidget {
	
	public void setPresenter(Presenter presenter);
	
	public void popup();
	
	public void setAreaNames(Map<String, Integer> area_map);
	    
	public void setRoomNames(int area_id, Map<String, Integer> room_map);
	
	public void setCabinetNames(int room_id, Map<String, Integer> cabinet_map);

	public interface Presenter {
		
		public boolean onOK(String server_name, String server_desc, String server_ip, String server_bw, ServerState server_state, int cabinet_id);
		
		public void lookupAreaNames();
	        
		public void lookupRoomNamesByAreaID(int area_id);
		
		public void lookupCabinetNamesByRoomID(int room_id);
		
	}
	
}
