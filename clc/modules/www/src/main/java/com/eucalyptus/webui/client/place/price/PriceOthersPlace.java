package com.eucalyptus.webui.client.place.price;

import com.eucalyptus.webui.client.place.SearchPlace;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class PriceOthersPlace extends SearchPlace {
	
	public PriceOthersPlace(String search) {
		super(search);
	}
	
	@Prefix("price_others")
	public static class Tokenizer implements PlaceTokenizer<PriceOthersPlace> {
		
		@Override
		public PriceOthersPlace getPlace(String search) {
			return new PriceOthersPlace(search);
		}
		
		@Override
        public String getToken(PriceOthersPlace place) {
			return place.getSearch();
        }
		
	}

}
