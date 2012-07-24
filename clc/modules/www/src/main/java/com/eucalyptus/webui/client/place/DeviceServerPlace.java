package com.eucalyptus.webui.client.place;

import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class DeviceServerPlace extends SearchPlace {

	public DeviceServerPlace(String search) {
		super(search);
	}

	@Prefix("device_server")
	public static class Tokenizer implements PlaceTokenizer<DeviceServerPlace> {

		@Override
		public DeviceServerPlace getPlace(String search) {
			return new DeviceServerPlace(search);
		}

		@Override
		public String getToken(DeviceServerPlace place) {
			return place.getSearch();
		}

	}

}
