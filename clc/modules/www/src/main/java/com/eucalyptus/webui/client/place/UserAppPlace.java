package com.eucalyptus.webui.client.place;

import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class UserAppPlace extends SearchPlace {

  public UserAppPlace( String search ) {
    super( search );
  }

  @Prefix( "user_app" )
  public static class Tokenizer implements PlaceTokenizer<UserAppPlace> {

    @Override
    public UserAppPlace getPlace( String search ) {
      return new UserAppPlace( search );
    }

    @Override
    public String getToken( UserAppPlace place ) {
      return place.getSearch( );
    }
    
  }
  
}
