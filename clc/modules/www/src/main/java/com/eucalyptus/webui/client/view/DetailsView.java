package com.eucalyptus.webui.client.view;

import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.IsWidget;

public interface DetailsView extends IsWidget {

  HasWidgets.ForIsWidget getContentContainer( );
  
  public interface Controller {
	    void hideDetail( );    
  }
}
