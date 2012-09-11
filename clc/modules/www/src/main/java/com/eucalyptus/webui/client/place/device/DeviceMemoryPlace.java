package com.eucalyptus.webui.client.place.device;

import com.eucalyptus.webui.client.place.SearchPlace;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class DeviceMemoryPlace extends SearchPlace {
	
	public DeviceMemoryPlace(String search) {
		super(search);
	}
	
	@Prefix("device_memory")
	public static class Tokenizer implements PlaceTokenizer<DeviceMemoryPlace> {
		
		@Override
		public DeviceMemoryPlace getPlace(String search) {
			return new DeviceMemoryPlace(search);
		}
		
		@Override
        public String getToken(DeviceMemoryPlace place) {
			return place.getSearch();
        }
		
	}

}
