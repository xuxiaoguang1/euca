package com.eucalyptus.webui.client.place;

import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class MemoryStatPlace extends SearchPlace {
  
  public MemoryStatPlace( String search ) {
    super( search );
  }
  
  @Prefix( "memory_stat" )
  public static class Tokenizer implements PlaceTokenizer<MemoryStatPlace> {

    @Override
    public MemoryStatPlace getPlace( String token ) {
      return new MemoryStatPlace( token );
    }

    @Override
    public String getToken( MemoryStatPlace place ) {
      return place.getSearch( );
    }
    
  }
  
}
