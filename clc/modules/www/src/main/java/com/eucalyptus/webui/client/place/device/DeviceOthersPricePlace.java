package com.eucalyptus.webui.client.place.device;

import com.eucalyptus.webui.client.place.SearchPlace;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class DeviceOthersPricePlace extends SearchPlace {
	
	public DeviceOthersPricePlace(String search) {
		super(search);
	}
	
	@Prefix("device_others_price")
	public static class Tokenizer implements PlaceTokenizer<DeviceOthersPricePlace> {
		
		@Override
		public DeviceOthersPricePlace getPlace(String search) {
			return new DeviceOthersPricePlace(search);
		}
		
		@Override
        public String getToken(DeviceOthersPricePlace place) {
			return place.getSearch();
        }
		
	}

}
