package com.eucalyptus.webui.client.view;

import java.util.List;

import com.eucalyptus.webui.client.service.SearchResultRow;
import com.google.gwt.user.client.ui.IsWidget;

public interface DeviceTemplateModifyView extends IsWidget {
	
	void setPresenter(Presenter presenter);
	
	void popup(SearchResultRow row);
	
	void setCPUNameList(List<String> list);
	
	public interface Presenter {
		
		boolean onOK(SearchResultRow row, String cpu, int ncpus, String mem, String disk, String bw, String image);
		
		void lookupCPUNames();
		
		void onCancel();
		
	}

}
