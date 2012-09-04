package com.eucalyptus.webui.client.place;

import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class IpPermissionPlace extends SearchPlace {

  public IpPermissionPlace( String search ) {
    super( search );
  }

  @Prefix( "ipPermission" )
  public static class Tokenizer implements PlaceTokenizer<IpPermissionPlace> {

    @Override
    public IpPermissionPlace getPlace( String search ) {
      return new IpPermissionPlace( search );
    }

    @Override
    public String getToken( IpPermissionPlace place ) {
      return place.getSearch( );
    }
    
  }
  
}
