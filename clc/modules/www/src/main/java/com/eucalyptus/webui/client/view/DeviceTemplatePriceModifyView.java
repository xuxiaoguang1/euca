package com.eucalyptus.webui.client.view;

import com.google.gwt.user.client.ui.IsWidget;

public interface DeviceTemplatePriceModifyView extends IsWidget {
	
	void setPresenter(Presenter presenter);
	
	void popup(int tp_id, String template_name, String tp_desc, int ncpus, double tp_cpu,
	        long mem_size, double tp_mem, long disk_size, double tp_disk, int bw_size, double tp_bw);
	        
	public interface Presenter {
	    
	    boolean onOK(int tp_id, String tp_desc, double tp_cpu, double tp_mem, double tp_disk, double tp_bw);
		
	}

}
