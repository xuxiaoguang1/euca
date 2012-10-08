package com.eucalyptus.webui.client.view;

import java.util.List;

import com.google.gwt.user.client.ui.IsWidget;

public interface DeviceDiskAddView extends IsWidget {
	
	public void setPresenter(Presenter presenter);
	
	public void popup();
	
	public void setAreaNameList(List<String> area_name_list);
	
	public void setRoomNameList(String area_name, List<String> room_name_list);
	
	public void setCabinetNameList(String room_name, List<String> cabinet_name_list);
	
	public void setServerNameList(String cabinet_name, List<String> server_name_list);
	
	public interface Presenter {
		
		public boolean onOK(String disk_name, String disk_desc, String disk_size, String server_name);
		
		public void lookupAreaNames();
		
		public void lookupRoomNamesByAreaName(String area_name);
		
		public void lookupCabinetNamesByRoomName(String room_name);
		
		public void lookupServerNameByCabinetName(String cabinet_name);
		
	}
	
}
