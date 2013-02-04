package com.eucalyptus.webui.client.view;

import java.util.Map;

import com.google.gwt.user.client.ui.IsWidget;

public interface DeviceTemplatePriceAddView extends IsWidget {
	
	void setPresenter(Presenter presenter);
	
	void popup();
	
	void setTemplates(Map<String, Integer> template_map);
	
	void setTemplate(int template_id, String template_name, String cpu_name, int ncpus, long mem_size, long disk_size, int bw_size);
	
	public interface Presenter {
		
		void lookupTemplates();
		
		void lookupTemplate(int template_id);
		
	    boolean onOK(int template_id, String tp_desc, double tp_cpu, double tp_mem, double tp_disk, double tp_bw);
		
	}

}
