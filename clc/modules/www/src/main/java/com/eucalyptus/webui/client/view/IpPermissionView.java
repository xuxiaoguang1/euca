package com.eucalyptus.webui.client.view;

import com.google.gwt.user.client.ui.IsWidget;

public interface IpPermissionView extends IsWidget, CanDisplaySearchResult, Clearable, SelectionController {
    
  void setPresenter( Presenter presenter );
  
  public interface Presenter extends SearchRangeChangeHandler, MultiSelectionChangeHandler, KnowsPageSize {
    void onAddRule();
    void onDeleteRule();
  }
  
}
