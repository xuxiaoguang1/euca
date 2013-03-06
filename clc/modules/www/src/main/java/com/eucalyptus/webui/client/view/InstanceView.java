package com.eucalyptus.webui.client.view;

import com.google.gwt.user.client.ui.IsWidget;

public interface InstanceView extends IsWidget, CanDisplaySearchResult, Clearable, SelectionController {
    
  
  void setPresenter( Presenter presenter );
  void setCellClickProc(SearchTableCellClickHandler clickHandler);
  
  public interface Presenter extends SearchRangeChangeHandler, MultiSelectionChangeHandler, KnowsPageSize {
	  void onStartInstances( );
	  void onStopInstances( );
	  void onTerminateInstances( );
	  void onRunInstance( );
	  void onAssociateAddress( );
  }
  
}
