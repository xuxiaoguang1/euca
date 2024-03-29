package com.eucalyptus.webui.client.view;

import com.eucalyptus.webui.shared.user.LoginUserProfile;
import com.google.gwt.user.client.ui.IsWidget;

public interface UserView extends IsWidget, CanDisplaySearchResult, Clearable, SelectionController {
    
  void setPresenter( Presenter presenter );
  void updateLoginUserProfile(LoginUserProfile curUser);
  
  public interface Presenter extends SearchRangeChangeHandler, MultiSelectionChangeHandler, KnowsPageSize, SearchTableDoubleClickHandler {
	void onAddUser();
	void onModifyUser();
    void onDeleteUsers();
    void onPauseUsers();
    void onResumeUses();
    void onBanUsers();
    void onAddToGroups( );
    void onRemoveFromGroups( );
    void onAddPolicy( );
    void onAddKey( );
    void onAddCert( );
  }

}
