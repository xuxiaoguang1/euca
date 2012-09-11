package com.eucalyptus.webui.client.view;

import com.google.gwt.user.client.ui.IsWidget;

public interface DeviceTemplatePriceModifyView extends IsWidget {
	
	void setPresenter(Presenter presenter);
	
	void popup(int template_price_id, String template_name, String template_price_desc, 
	        String cpu_name, int ncpus, double cpu_price,
	        double mem_size, double mem_price, double disk_size, double disk_price, double bw_size, double bw_price);
	        
	public interface Presenter {
	    
	    boolean onOK(int template_price_id, String template_price_desc, String cpu_price, String mem_price, String disk_price, String bw_price);
		
	}

}
