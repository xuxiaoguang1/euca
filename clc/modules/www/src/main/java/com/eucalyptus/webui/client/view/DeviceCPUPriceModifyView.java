package com.eucalyptus.webui.client.view;

import com.google.gwt.user.client.ui.IsWidget;

public interface DeviceCPUPriceModifyView extends IsWidget {
	
	public void setPresenter(Presenter presenter);
	
	public void popup(int cp_id, String cpu_name, String cp_desc, double cp_price);
	
	public interface Presenter {
		
		public boolean onOK(int cp_id, String cp_desc, double cp_price);
		
	}

}
