package com.eucalyptus.webui.client.place;

import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class DeviceTemplatePlace extends SearchPlace {
	
	public DeviceTemplatePlace(String search) {
		super(search);
	}
	
	@Prefix("device_template")
	public static class Tokenizer implements PlaceTokenizer<DeviceTemplatePlace> {
		
		@Override
		public DeviceTemplatePlace getPlace(String search) {
			return new DeviceTemplatePlace(search);
		}
		
		@Override
        public String getToken(DeviceTemplatePlace place) {
			return place.getSearch();
        }
		
	}

}

