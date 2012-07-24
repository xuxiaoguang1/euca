package com.eucalyptus.webui.client.place;

import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class DeviceBWPlace extends SearchPlace {
	
	public DeviceBWPlace(String search) {
		super(search);
	}
	
	@Prefix("device_bw")
	public static class Tokenizer implements PlaceTokenizer<DeviceBWPlace> {
		
		@Override
		public DeviceBWPlace getPlace(String search) {
			return new DeviceBWPlace(search);
		}
		
		@Override
        public String getToken(DeviceBWPlace place) {
			return place.getSearch();
        }
		
	}

}
