package com.eucalyptus.webui.client.view;

import com.eucalyptus.webui.shared.user.LoginUserProfile;
import com.google.gwt.user.client.ui.IsWidget;

public interface UserAppView extends IsWidget, CanDisplaySearchResult, Clearable, SelectionController {
	
	void setPresenter( Presenter presenter );
	void displayCtrl(LoginUserProfile curUser);
	
	public interface Presenter extends SearchRangeChangeHandler, MultiSelectionChangeHandler, KnowsPageSize {
	    void onApproveUserApp();
	    void onRejectUserApp();
	    
	    void onCreateUserApp();
	    void onDeleteUserApp();
	    
	    void onShowAllApps();
	    void onSolvedApps();
	    void onSolvingApps();
	    void onToSolveApps();
	}
}
