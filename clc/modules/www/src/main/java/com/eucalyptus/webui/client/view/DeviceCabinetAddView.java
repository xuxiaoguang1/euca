package com.eucalyptus.webui.client.view;

import java.util.List;

import com.google.gwt.user.client.ui.IsWidget;

public interface DeviceCabinetAddView extends IsWidget {
	
	public void setPresenter(Presenter presenter);
	
	public void popup();
	
	public void setAreaNameList(List<String> area_name_list);
	
	public void setRoomNameList(String area_name, List<String> room_name_list);
	
	public interface Presenter {
		
		public boolean onOK(String cabinet_name, String cabinet_desc, String room_name);
		
		public void lookupAreaNames();
		
		public void lookupRoomNamesByAreaName(String area_name);
		
	}

}
