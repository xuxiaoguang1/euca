package com.eucalyptus.webui.client.view;

import com.google.gwt.user.client.ui.IsWidget;

public interface DeviceTemplateAddView extends IsWidget {
	
	public void setPresenter(Presenter presenter);
	
	public void popup();
	
	public interface Presenter {
		
		public boolean onOK(String template_name, String template_desc, String template_cpu, int template_ncpus, String template_mem, String template_disk, String template_bw, String template_image);
		
	}
	
}
