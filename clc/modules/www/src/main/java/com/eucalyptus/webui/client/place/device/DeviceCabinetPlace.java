package com.eucalyptus.webui.client.place.device;

import com.eucalyptus.webui.client.place.SearchPlace;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class DeviceCabinetPlace extends SearchPlace {
	
	public DeviceCabinetPlace(String search) {
		super(search);
	}
	
	@Prefix("device_cabinet")
	public static class Tokenizer implements PlaceTokenizer<DeviceCabinetPlace> {
		
		@Override
		public DeviceCabinetPlace getPlace(String search) {
			return new DeviceCabinetPlace(search);
		}
		
		@Override
        public String getToken(DeviceCabinetPlace place) {
			return place.getSearch();
        }
		
	}

}
