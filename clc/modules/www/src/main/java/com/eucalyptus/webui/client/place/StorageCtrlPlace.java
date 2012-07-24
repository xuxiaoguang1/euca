package com.eucalyptus.webui.client.place;

import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class StorageCtrlPlace extends SearchPlace {

  public StorageCtrlPlace( String search ) {
    super( search );
  }

  @Prefix( "storageCtrl" )
  public static class Tokenizer implements PlaceTokenizer<StorageCtrlPlace> {

    @Override
    public StorageCtrlPlace getPlace( String search ) {
      return new StorageCtrlPlace( search );
    }

    @Override
    public String getToken( StorageCtrlPlace place ) {
      return place.getSearch( );
    }
    
  }
  
}
