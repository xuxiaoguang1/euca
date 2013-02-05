package com.eucalyptus.webui.client.view;

import java.util.Map;

import com.google.gwt.user.client.ui.IsWidget;

public interface DeviceCPUAddView extends IsWidget {
	
	public void setPresenter(Presenter presenter);
	
	public void popup();
	
    public void setAreaNames(Map<String, Integer> area_map);
    
    public void setRoomNames(int area_id, Map<String, Integer> room_map);
    
    public void setCabinetNames(int room_id, Map<String, Integer> cabinet_map);
    
    public void setServerNames(int cabinet_id, Map<String, Integer> server_map);
	
	public interface Presenter {
		
		public boolean onOK(String cpu_name, String cpu_desc, int cpu_total, String cpu_vendor, String cpu_model, double cpu_ghz, double cpu_cache, int server_id);
		
		public void lookupAreaNames();
        
        public void lookupRoomNamesByAreaID(int area_id);
        
        public void lookupCabinetNamesByRoomID(int room_id);
        
        public void lookupServerNamesByCabinetID(int cabinet_id);
		
	}
	
}
