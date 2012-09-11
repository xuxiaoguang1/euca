package com.eucalyptus.webui.client.view;

import java.util.Collection;

import com.google.gwt.user.client.ui.IsWidget;

public interface DeviceCPUPriceAddView extends IsWidget {
	
	void setPresenter(Presenter presenter);
	
	void popup();
	
	void setCPUNameList(Collection<String> cpu_name_list);
	
	public interface Presenter {
		
		boolean onOK(String cpu_name, String cpu_price_desc, String cpu_price);
		
		void lookupCPUNames();
		
	}

}
