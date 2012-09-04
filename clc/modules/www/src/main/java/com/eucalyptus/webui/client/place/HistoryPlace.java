package com.eucalyptus.webui.client.place;

import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class HistoryPlace extends SearchPlace {
  
  public HistoryPlace( String search ) {
    super( search );
  }
  
  @Prefix( "history" )
  public static class Tokenizer implements PlaceTokenizer<HistoryPlace> {

    @Override
    public HistoryPlace getPlace( String token ) {
      return new HistoryPlace( token );
    }

    @Override
    public String getToken( HistoryPlace place ) {
      return place.getSearch( );
    }
    
  }
  
}
