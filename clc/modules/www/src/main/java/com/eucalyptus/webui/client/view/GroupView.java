package com.eucalyptus.webui.client.view;

import com.google.gwt.user.client.ui.IsWidget;

public interface GroupView extends IsWidget, CanDisplaySearchResult, Clearable, SelectionController {
    
  void setPresenter( Presenter presenter );
  
  public interface Presenter extends SearchRangeChangeHandler, MultiSelectionChangeHandler, KnowsPageSize, SearchTableDoubleClickHandler {
	void onAddGroup();
	void onModifyGroup();
    void onDeleteGroup();
    
    void showGroupDetails();
    
    void onPauseGroup();
    void onResumeGroup();
    void onBanGroup();
  }
  
}
