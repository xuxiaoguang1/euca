package com.eucalyptus.webui.client.view.device;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;

public interface DeviceDatePickerView extends IsWidget {
	
	void setValue(Label label);
	
	interface Presenter {
		
		void onOK();
		
		void onCancel();
		
	}

}
