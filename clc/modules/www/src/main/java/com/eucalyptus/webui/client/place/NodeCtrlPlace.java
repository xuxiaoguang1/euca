package com.eucalyptus.webui.client.place;

import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class NodeCtrlPlace extends SearchPlace {

  public NodeCtrlPlace( String search ) {
    super( search );
  }

  @Prefix( "nodeCtrl" )
  public static class Tokenizer implements PlaceTokenizer<NodeCtrlPlace> {

    @Override
    public NodeCtrlPlace getPlace( String search ) {
      return new NodeCtrlPlace( search );
    }

    @Override
    public String getToken( NodeCtrlPlace place ) {
      return place.getSearch( );
    }
    
  }
  
}
