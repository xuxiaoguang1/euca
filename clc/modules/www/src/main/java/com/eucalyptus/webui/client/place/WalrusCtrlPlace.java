package com.eucalyptus.webui.client.place;

import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class WalrusCtrlPlace extends SearchPlace {

  public WalrusCtrlPlace( String search ) {
    super( search );
  }

  @Prefix( "walrusCtrl" )
  public static class Tokenizer implements PlaceTokenizer<WalrusCtrlPlace> {

    @Override
    public WalrusCtrlPlace getPlace( String search ) {
      return new WalrusCtrlPlace( search );
    }

    @Override
    public String getToken( WalrusCtrlPlace place ) {
      return place.getSearch( );
    }
    
  }
  
}
