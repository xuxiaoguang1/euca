package com.eucalyptus.webui.client.view;

public interface GroupDetailView extends CanDisplaySearchResult {
  
  void setTitle( String title );
  
  void setPresenter( Presenter presenter );
  
  void setAccountId(int accountId);
  void setGroupId(int groupId);

  public interface Presenter extends SearchRangeChangeHandler, MultiSelectionChangeHandler, KnowsPageSize {
    void onAction( String key );
    void onHide( );
    
    void onAddUsers(int accountId, int groupId);
    void onRemoveUsers();
  }
}
