package com.eucalyptus.webui.client.view;

import java.util.List;

import com.eucalyptus.webui.shared.resource.device.status.ServerState;
import com.google.gwt.user.client.ui.IsWidget;

public interface DeviceServerAddView extends IsWidget {
	
	public void setPresenter(Presenter presenter);
	
	public void popup();
	
	public void setAreaNameList(List<String> area_name_list);
	
	public void setRoomNameList(String area_name, List<String> room_name_list);
	
	public void setCabinetNameList(String room_name, List<String> cabinet_name_list);
	
	public interface Presenter {
		
		public boolean onOK(String server_name, String server_desc, String server_ip, String server_bw, ServerState server_state, String cabinet_name);
		
		public void lookupAreaNames();
		
		public void lookupRoomNamesByAreaName(String area_name);
		
		public void lookupCabinetNamesByRoomName(String room_name);
		
	}
	
}
