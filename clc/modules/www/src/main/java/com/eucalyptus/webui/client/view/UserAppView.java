package com.eucalyptus.webui.client.view;

import java.util.ArrayList;

import com.eucalyptus.webui.shared.user.LoginUserProfile;
import com.eucalyptus.webui.shared.user.UserAppStateCount;
import com.google.gwt.user.client.ui.IsWidget;

public interface UserAppView extends IsWidget, CanDisplaySearchResult, Clearable, SelectionController {
	
	void setPresenter( Presenter presenter );
	void setCellClickProc(SearchTableCellClickHandler clickHandler);
	void displayCtrl(LoginUserProfile curUser);
	void updateCountInfo(ArrayList<UserAppStateCount> countInfo);
	
	public interface Presenter extends SearchRangeChangeHandler, MultiSelectionChangeHandler, KnowsPageSize {
	    void onApproveUserApp();
	    void onRejectUserApp();
	    
	    void onCreateUserApp();
	    void onDeleteUserApp();
	    
	    void onShowAllApps();
	    void onApprovedApps();
	    void onRejectedApps();
	    void onApplyingApps();
	}
}
