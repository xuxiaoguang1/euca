package com.eucalyptus.webui.client.place.device;

import com.eucalyptus.webui.client.place.SearchPlace;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class DeviceAreaPlace extends SearchPlace {
	
	public DeviceAreaPlace(String search) {
		super(search);
	}
	
	@Prefix("device_area")
	public static class Tokenizer implements PlaceTokenizer<DeviceAreaPlace> {
		
		@Override
		public DeviceAreaPlace getPlace(String search) {
			return new DeviceAreaPlace(search);
		}
		
		@Override
        public String getToken(DeviceAreaPlace place) {
			return place.getSearch();
        }
		
	}

}
