package com.eucalyptus.webui.client.view.device;

import java.util.ArrayList;

import com.eucalyptus.webui.client.service.SearchResult;
import com.eucalyptus.webui.client.view.CanDisplaySearchResult;
import com.eucalyptus.webui.client.view.Clearable;
import com.eucalyptus.webui.client.view.KnowsPageSize;
import com.eucalyptus.webui.client.view.MultiSelectionChangeHandler;
import com.eucalyptus.webui.client.view.SearchRangeChangeHandler;
import com.eucalyptus.webui.client.view.SelectionController;
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
