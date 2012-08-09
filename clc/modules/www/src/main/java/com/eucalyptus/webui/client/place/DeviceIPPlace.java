package com.eucalyptus.webui.client.place;

import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class DeviceIPPlace extends SearchPlace {
	
	public DeviceIPPlace(String search) {
		super(search);
	}
	
	@Prefix("device_ip")
	public static class Tokenizer implements PlaceTokenizer<DeviceIPPlace> {
		
		@Override
		public DeviceIPPlace getPlace(String search) {
			return new DeviceIPPlace(search);
		}
		
		@Override
        public String getToken(DeviceIPPlace place) {
			return place.getSearch();
        }
		
	}

}
