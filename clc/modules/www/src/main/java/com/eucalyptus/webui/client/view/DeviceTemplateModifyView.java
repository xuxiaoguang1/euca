package com.eucalyptus.webui.client.view;

import com.eucalyptus.webui.client.service.SearchResultRow;
import com.google.gwt.user.client.ui.IsWidget;

public interface DeviceTemplateModifyView extends IsWidget {
	
	void setPresenter(Presenter presenter);
	
	void popup(SearchResultRow row);
	
	public interface Presenter {
		
		boolean onOK(SearchResultRow row, String cpu, String mem, String disk, String bw, String image);
		
		void onCancel();
		
	}

}
