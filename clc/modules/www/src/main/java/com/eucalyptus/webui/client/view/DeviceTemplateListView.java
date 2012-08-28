package com.eucalyptus.webui.client.view;

import java.util.ArrayList;

import com.eucalyptus.webui.client.service.SearchResult;
import com.eucalyptus.webui.shared.resource.VMImageType;
import com.eucalyptus.webui.shared.user.UserApp;
import com.google.gwt.user.client.ui.IsWidget;

public interface DeviceTemplateListView extends IsWidget, CanDisplaySearchResult, Clearable, SelectionController {
	void setPresenter( Presenter presenter );
	void display(SearchResult searchResult);
	void setVMImageTypeList(ArrayList<VMImageType> vmTypeList);
	
	public interface Presenter extends SearchRangeChangeHandler, MultiSelectionChangeHandler, KnowsPageSize {
		void doCreateUserApp(UserApp userApp);
	}
}
