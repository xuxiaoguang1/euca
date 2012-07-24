package com.eucalyptus.webui.client.place;

import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class DeviceVMPlace extends SearchPlace {
	
	public DeviceVMPlace(String search) {
		super(search);
	}
	
	@Prefix("device_vm")
	public static class Tokenizer implements PlaceTokenizer<DeviceVMPlace> {
		
		@Override
		public DeviceVMPlace getPlace(String search) {
			return new DeviceVMPlace(search);
		}
		
		@Override
        public String getToken(DeviceVMPlace place) {
			return place.getSearch();
        }
		
	}

}
