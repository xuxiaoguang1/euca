package com.eucalyptus.webui.client.place.device;

import com.eucalyptus.webui.client.place.SearchPlace;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class DeviceCPUPricePlace extends SearchPlace {
	
	public DeviceCPUPricePlace(String search) {
		super(search);
	}
	
	@Prefix("device_cpu_price")
	public static class Tokenizer implements PlaceTokenizer<DeviceCPUPricePlace> {
		
		@Override
		public DeviceCPUPricePlace getPlace(String search) {
			return new DeviceCPUPricePlace(search);
		}
		
		@Override
        public String getToken(DeviceCPUPricePlace place) {
			return place.getSearch();
        }
		
	}

}
