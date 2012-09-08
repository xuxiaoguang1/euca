package com.eucalyptus.webui.client.view;

import java.util.List;

import com.eucalyptus.webui.client.service.SearchResult;
import com.eucalyptus.webui.shared.resource.VMImageType;
import com.eucalyptus.webui.shared.user.UserApp;
import com.google.gwt.user.client.ui.IsWidget;

public interface UserAppAddView extends IsWidget, CanDisplaySearchResult, Clearable, SelectionController {
	void setPresenter( Presenter presenter );
	void display(SearchResult searchResult);
	void setVMImageTypeList(List<VMImageType> vmTypeList);
	void setKeyPairList(List<String> keyPairList);
	void setSecurityGroupList(List<String> securityGroupList);
	
	public interface Presenter extends SearchRangeChangeHandler, MultiSelectionChangeHandler, KnowsPageSize {
		void doCreateUserApp(UserApp userApp);
	}
}
