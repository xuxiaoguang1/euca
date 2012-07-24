package com.eucalyptus.webui.client.place;

import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class DeviceCPUPlace extends SearchPlace {
	
	public DeviceCPUPlace(String search) {
		super(search);
	}
	
	@Prefix("device_cpu")
	public static class Tokenizer implements PlaceTokenizer<DeviceCPUPlace> {
		
		@Override
		public DeviceCPUPlace getPlace(String search) {
			return new DeviceCPUPlace(search);
		}
		
		@Override
        public String getToken(DeviceCPUPlace place) {
			return place.getSearch();
        }
		
	}

}
