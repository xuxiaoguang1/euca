package com.eucalyptus.webui.client.place;

import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class SecurityGroupPlace extends SearchPlace {

  public SecurityGroupPlace( String search ) {
    super( search );
  }

  @Prefix( "securityGroup" )
  public static class Tokenizer implements PlaceTokenizer<SecurityGroupPlace> {

    @Override
    public SecurityGroupPlace getPlace( String search ) {
      return new SecurityGroupPlace( search );
    }

    @Override
    public String getToken( SecurityGroupPlace place ) {
      return place.getSearch( );
    }
    
  }
  
}
