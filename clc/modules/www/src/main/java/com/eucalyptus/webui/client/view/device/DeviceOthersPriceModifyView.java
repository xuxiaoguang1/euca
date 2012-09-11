package com.eucalyptus.webui.client.view.device;

import com.google.gwt.user.client.ui.IsWidget;

public interface DeviceOthersPriceModifyView extends IsWidget {
	
	void popup(String title, String price_unit, String price, String price_desc, Presenter presenter);
	
	public interface Presenter {
	    
	    boolean onOK(String price, String price_desc);
		
	}

}
