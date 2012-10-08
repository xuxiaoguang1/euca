package com.eucalyptus.webui.client.view;

import com.google.gwt.user.client.ui.IsWidget;

public interface DeviceTemplateModifyView extends IsWidget {
	
	public void setPresenter(Presenter presenter);
	
	public void popup(int template_id, String template_name, String template_desc, String template_cpu, int template_ncpus, String template_mem, String template_disk, String template_bw, String template_image);
	
	public interface Presenter {
		
		public boolean onOK(int template_id, String template_desc, String template_cpu, int template_ncpus, String template_mem, String template_disk, String template_bw, String template_image);
		
	}
	
}
