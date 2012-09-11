package com.eucalyptus.webui.client.place.device;

import com.eucalyptus.webui.client.place.SearchPlace;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class DeviceTemplatePricePlace extends SearchPlace {
	
	public DeviceTemplatePricePlace(String search) {
		super(search);
	}
	
	@Prefix("device_template_price")
	public static class Tokenizer implements PlaceTokenizer<DeviceTemplatePricePlace> {
		
		@Override
		public DeviceTemplatePricePlace getPlace(String search) {
			return new DeviceTemplatePricePlace(search);
		}
		
		@Override
        public String getToken(DeviceTemplatePricePlace place) {
			return place.getSearch();
        }
		
	}

}
