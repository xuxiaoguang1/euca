package com.eucalyptus.webui.client.place;

import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class CPUStatPlace extends SearchPlace {
  
  public CPUStatPlace( String search ) {
    super( search );
  }
  
  @Prefix( "cpu_stat" )
  public static class Tokenizer implements PlaceTokenizer<CPUStatPlace> {

    @Override
    public CPUStatPlace getPlace( String token ) {
      return new CPUStatPlace( token );
    }

    @Override
    public String getToken( CPUStatPlace place ) {
      return place.getSearch( );
    }
    
  }
  
}
