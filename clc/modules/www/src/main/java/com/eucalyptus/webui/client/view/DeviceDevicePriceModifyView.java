package com.eucalyptus.webui.client.view;

import com.google.gwt.user.client.ui.IsWidget;

public interface DeviceDevicePriceModifyView extends IsWidget {
	
	void popup(String title, String price_unit, String op_desc, double op_price, Presenter presenter);
	
	public interface Presenter {
	    
	    boolean onOK(String op_desc, double op_price);
		
	}

}
