package com.eucalyptus.webui.client.place;

import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class DeviceDiskPlace extends SearchPlace {
	
	public DeviceDiskPlace(String search) {
		super(search);
	}
	
	@Prefix("device_disk")
	public static class Tokenizer implements PlaceTokenizer<DeviceDiskPlace> {
		
		@Override
		public DeviceDiskPlace getPlace(String search) {
			return new DeviceDiskPlace(search);
		}
		
		@Override
        public String getToken(DeviceDiskPlace place) {
			return place.getSearch();
        }
		
	}

}
