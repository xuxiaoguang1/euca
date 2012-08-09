package com.eucalyptus.webui.client.view;

import java.util.Date;

import com.eucalyptus.webui.client.service.SearchResultRow;
import com.google.gwt.user.client.ui.IsWidget;

public interface DeviceServiceModifyView extends IsWidget {
	
	void setPresenter(Presenter presenter);
	
	void setValue(SearchResultRow row, Date starttime, Date date, String[] stateValueList, int stateSelected);
	
	void setValue(SearchResultRow row, Date starttime, Date date, String[] stateValueList, String state);
	
	public interface Presenter {
		
		boolean onOK(SearchResultRow row, Date starttime, Date endtime, String state);
		
		void onCancel();
		
	}

}
