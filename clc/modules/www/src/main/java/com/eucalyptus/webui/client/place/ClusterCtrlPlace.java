package com.eucalyptus.webui.client.place;

import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class ClusterCtrlPlace extends SearchPlace {

  public ClusterCtrlPlace( String search ) {
    super( search );
  }

  @Prefix( "clusterCtrl" )
  public static class Tokenizer implements PlaceTokenizer<ClusterCtrlPlace> {

    @Override
    public ClusterCtrlPlace getPlace( String search ) {
      return new ClusterCtrlPlace( search );
    }

    @Override
    public String getToken( ClusterCtrlPlace place ) {
      return place.getSearch( );
    }
    
  }
  
}
