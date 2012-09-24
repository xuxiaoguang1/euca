package com.eucalyptus.webui.client.view;

import java.util.ArrayList;

import com.google.gwt.user.client.ui.IsWidget;

public interface GroupAddingUserListView extends IsWidget, CanDisplaySearchResult, Clearable, SelectionController {
	void setPresenter( Presenter presenter );
	void close();
	
	public interface Presenter extends SearchRangeChangeHandler, MultiSelectionChangeHandler, KnowsPageSize {
		void process(ArrayList<String> userIds);
	}
}
