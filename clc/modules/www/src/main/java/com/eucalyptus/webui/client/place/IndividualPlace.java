package com.eucalyptus.webui.client.place;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class IndividualPlace extends Place {

  public IndividualPlace() {
  }

  @Prefix( "individual" )
  public static class Tokenizer implements PlaceTokenizer<IndividualPlace> {

    @Override
    public IndividualPlace getPlace( String token ) {
      return new IndividualPlace( );
    }

    @Override
    public String getToken( IndividualPlace place ) {
      return "";
    }
  }
  
}
