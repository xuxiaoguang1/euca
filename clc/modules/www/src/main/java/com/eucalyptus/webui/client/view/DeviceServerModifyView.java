package com.eucalyptus.webui.client.view;

import com.eucalyptus.webui.client.service.SearchResultRow;
import com.google.gwt.user.client.ui.IsWidget;

public interface DeviceServerModifyView extends IsWidget {
	
	void setPresenter(Presenter presenter);
	
	void popup(SearchResultRow row, int state);
	
	public interface Presenter {
		
		void onOK(SearchResultRow row, int state);
		
		void onCancel();
		
	}

}
