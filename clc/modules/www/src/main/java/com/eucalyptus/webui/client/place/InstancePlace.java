package com.eucalyptus.webui.client.place;

import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class InstancePlace extends SearchPlace {
  
  public InstancePlace( String search ) {
    super( search );
  }
  
  @Prefix( "instance" )
  public static class Tokenizer implements PlaceTokenizer<InstancePlace> {

    @Override
    public InstancePlace getPlace( String token ) {
      return new InstancePlace( token );
    }

    @Override
    public String getToken( InstancePlace place ) {
      return place.getSearch( );
    }
    
  }
  
}
