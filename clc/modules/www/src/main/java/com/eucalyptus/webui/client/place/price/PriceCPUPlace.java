package com.eucalyptus.webui.client.place.price;

import com.eucalyptus.webui.client.place.SearchPlace;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class PriceCPUPlace extends SearchPlace {
	
	public PriceCPUPlace(String search) {
		super(search);
	}
	
	@Prefix("price_cpu")
	public static class Tokenizer implements PlaceTokenizer<PriceCPUPlace> {
		
		@Override
		public PriceCPUPlace getPlace(String search) {
			return new PriceCPUPlace(search);
		}
		
		@Override
        public String getToken(PriceCPUPlace place) {
			return place.getSearch();
        }
		
	}

}
