package com.eucalyptus.webui.client.view;

import java.util.Date;

import com.google.gwt.user.client.ui.IsWidget;

public interface DeviceServiceExtendView extends IsWidget {
	
	void setPresenter(Presenter presenter);
	
	void setValue(Date date, String[] stateValueList, int stateSelected);
	
	void setValue(Date date, String[] stateValueList, String state);
	
	public interface Presenter {
		
		void onOK(String endtime, String state);
		
		void onCancel();
		
	}

}
