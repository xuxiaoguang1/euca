package com.eucalyptus.webui.client.view;

import java.util.List;
import java.util.Map;

import com.eucalyptus.webui.client.ClientFactory;
import com.eucalyptus.webui.shared.resource.VMImageType;
import com.eucalyptus.webui.shared.user.UserApp;

public interface UserAppAddView {
	void setPresenter( Presenter presenter );
	void setClientFactory(ClientFactory clientFactory);
	void setDeviceTemplates(Map<String, Integer> templates);
	void setVMImageTypeList(List<VMImageType> vmTypeList);
	void setKeyPairList(List<String> keyPairList);
	void setSecurityGroupList(List<String> securityGroupList);
	
	void display();
	
	public interface Presenter extends SearchRangeChangeHandler, MultiSelectionChangeHandler, KnowsPageSize {
		void doCreateUserApp(UserApp userApp);
	}
}
