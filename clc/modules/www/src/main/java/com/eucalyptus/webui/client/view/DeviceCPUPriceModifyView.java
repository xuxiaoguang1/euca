package com.eucalyptus.webui.client.view;

import com.google.gwt.user.client.ui.IsWidget;

public interface DeviceCPUPriceModifyView extends IsWidget {
	
	void setPresenter(Presenter presenter);
	
	void popup(int cpu_price_id, String cpu_name, String cpu_price_desc, double cpu_price);
	
	public interface Presenter {
		
		boolean onOK(int cpu_price_id, String cpu_price_desc, String price);
		
	}

}
