package com.eucalyptus.webui.client.view;

import com.eucalyptus.webui.client.service.SearchResult;
import com.google.gwt.user.client.ui.IsWidget;

public interface DeviceTemplateListView extends IsWidget, CanDisplaySearchResult, Clearable, SelectionController {
	void setPresenter( Presenter presenter );
	void display(SearchResult searchResult);
	
	public interface Presenter extends SearchRangeChangeHandler, MultiSelectionChangeHandler, KnowsPageSize {
		void doCreateUserApp(String templateId);
	}
}
