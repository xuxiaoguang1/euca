package com.eucalyptus.webui.client.view;

import java.util.ArrayList;

import com.google.gwt.user.client.ui.IsWidget;

public interface UserListView extends IsWidget, CanDisplaySearchResult, Clearable, SelectionController {
	void setPresenter( Presenter presenter );
	void display();
	
	public interface Presenter extends SearchRangeChangeHandler, MultiSelectionChangeHandler, KnowsPageSize {
		void process(ArrayList<String> ids);
	}
}
