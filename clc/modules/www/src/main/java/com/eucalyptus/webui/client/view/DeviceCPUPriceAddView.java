package com.eucalyptus.webui.client.view;

import java.util.Collection;

import com.google.gwt.user.client.ui.IsWidget;

public interface DeviceCPUPriceAddView extends IsWidget {
	
	public void setPresenter(Presenter presenter);
	
	public void popup();
	
	void setCPUNameList(Collection<String> cpu_name_list);
	
	public interface Presenter {
		
		public boolean onOK(String cpu_name, String cp_desc, double cp_price);
		
		public void lookupCPUNames();
		
	}

}
