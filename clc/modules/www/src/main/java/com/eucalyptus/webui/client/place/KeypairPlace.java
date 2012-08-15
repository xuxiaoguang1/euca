package com.eucalyptus.webui.client.place;

import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class KeypairPlace extends SearchPlace {

  public KeypairPlace( String search ) {
    super( search );
  }

  @Prefix( "keypair" )
  public static class Tokenizer implements PlaceTokenizer<KeypairPlace> {

    @Override
    public KeypairPlace getPlace( String search ) {
      return new KeypairPlace( search );
    }

    @Override
    public String getToken( KeypairPlace place ) {
      return place.getSearch( );
    }
    
  }
  
}
