package com.eucalyptus.webui.client.view.device;

import java.util.List;

import com.google.gwt.user.client.ui.IsWidget;

public interface DeviceTemplatePriceAddView extends IsWidget {
	
	void setPresenter(Presenter presenter);
	
	void popup();
	
	void setTemplateList(List<String> template_name_list);
	
	void setTemplateDetails(int template_id, String template_name, String cpu_name, int ncpus, double mem_size, double disk_size, double bw_size);
	
	public interface Presenter {
		
		void lookupTemplateList();
		
		void lookupTemplateDetailByName(String template_name);
	    
	    boolean onOK(int template_id, String template_price_desc, String cpu_price, String mem_price, String disk_price, String bw_price);
		
	}

}
