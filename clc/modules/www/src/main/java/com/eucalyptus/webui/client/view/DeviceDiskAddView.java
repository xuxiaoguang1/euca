package com.eucalyptus.webui.client.view;

import java.util.Map;

import com.google.gwt.user.client.ui.IsWidget;

public interface DeviceDiskAddView extends IsWidget {
	
	public void setPresenter(Presenter presenter);
	
	public void popup();
	
    public void setAreaNames(Map<String, Integer> area_map);
    
    public void setRoomNames(int area_id, Map<String, Integer> room_map);
    
    public void setCabinetNames(int room_id, Map<String, Integer> cabinet_map);
    
    public void setServerNames(int cabinet_id, Map<String, Integer> server_map);
	
	public interface Presenter {
		
		public boolean onOK(String disk_name, String disk_desc, long disk_size, int server_id);
		
        public void lookupAreaNames();
        
        public void lookupRoomNamesByAreaID(int area_id);
        
        public void lookupCabinetNamesByRoomID(int room_id);
        
        public void lookupServerNamesByCabinetID(int cabinet_id);
		
	}
	
}
