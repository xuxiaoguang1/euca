package com.eucalyptus.webui.client.place.device;

import com.eucalyptus.webui.client.place.SearchPlace;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class DeviceDevicePricePlace extends SearchPlace {
	
	public DeviceDevicePricePlace(String search) {
		super(search);
	}
	
	@Prefix("device_device_price")
	public static class Tokenizer implements PlaceTokenizer<DeviceDevicePricePlace> {
		
		@Override
		public DeviceDevicePricePlace getPlace(String search) {
			return new DeviceDevicePricePlace(search);
		}
		
		@Override
        public String getToken(DeviceDevicePricePlace place) {
			return place.getSearch();
        }
		
	}

}
