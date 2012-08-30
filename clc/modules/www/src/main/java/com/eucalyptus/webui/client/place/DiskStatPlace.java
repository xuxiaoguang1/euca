package com.eucalyptus.webui.client.place;

import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class DiskStatPlace extends SearchPlace {
  
  public DiskStatPlace( String search ) {
    super( search );
  }
  
  @Prefix( "disk_stat" )
  public static class Tokenizer implements PlaceTokenizer<DiskStatPlace> {

    @Override
    public DiskStatPlace getPlace( String token ) {
      return new DiskStatPlace( token );
    }

    @Override
    public String getToken( DiskStatPlace place ) {
      return place.getSearch( );
    }
    
  }
  
}
