package com.eucalyptus.webui.client.place.price;

import com.eucalyptus.webui.client.place.SearchPlace;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class PriceTemplatePlace extends SearchPlace {
	
	public PriceTemplatePlace(String search) {
		super(search);
	}
	
	@Prefix("price_template")
	public static class Tokenizer implements PlaceTokenizer<PriceTemplatePlace> {
		
		@Override
		public PriceTemplatePlace getPlace(String search) {
			return new PriceTemplatePlace(search);
		}
		
		@Override
        public String getToken(PriceTemplatePlace place) {
			return place.getSearch();
        }
		
	}

}
