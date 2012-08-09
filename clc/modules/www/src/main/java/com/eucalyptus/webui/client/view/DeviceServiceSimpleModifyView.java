package com.eucalyptus.webui.client.view;

import java.util.Date;

import com.eucalyptus.webui.client.service.SearchResultRow;
import com.google.gwt.user.client.ui.IsWidget;

public interface DeviceServiceSimpleModifyView extends IsWidget {
	
	void setPresenter(Presenter presenter);
	
	void setValue(SearchResultRow row, Date starttime, Date date);
	
	public interface Presenter {
		
		boolean onOK(SearchResultRow row, Date starttime, Date endtime);
		
		void onCancel();
		
	}

}
