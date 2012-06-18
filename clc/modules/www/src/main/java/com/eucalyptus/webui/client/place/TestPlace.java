package com.eucalyptus.webui.client.place;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class TestPlace extends Place {
  
  public TestPlace( ) {
  }
  
  @Prefix( "test" )
  public static class Tokenizer implements PlaceTokenizer<TestPlace> {

    @Override
    public TestPlace getPlace( String token ) {
      return new TestPlace( );
    }

    @Override
    public String getToken( TestPlace place ) {
      return "";
    }
    
  }
  
}
