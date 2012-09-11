package com.eucalyptus.webui.client.place.device;

import com.eucalyptus.webui.client.place.SearchPlace;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class DeviceRoomPlace extends SearchPlace {
	
	public DeviceRoomPlace(String search) {
		super(search);
	}
	
	@Prefix("device_room")
	public static class Tokenizer implements PlaceTokenizer<DeviceRoomPlace> {
		
		@Override
		public DeviceRoomPlace getPlace(String search) {
			return new DeviceRoomPlace(search);
		}
		
		@Override
        public String getToken(DeviceRoomPlace place) {
			return place.getSearch();
        }
		
	}

}
