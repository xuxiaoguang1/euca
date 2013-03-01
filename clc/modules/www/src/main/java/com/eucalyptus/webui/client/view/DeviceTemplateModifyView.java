package com.eucalyptus.webui.client.view;

import com.google.gwt.user.client.ui.IsWidget;

public interface DeviceTemplateModifyView extends IsWidget {
	
	public void setPresenter(Presenter presenter);
	
	public void popup(int template_id, String template_name, String template_desc, int template_ncpus, long template_mem, long template_disk, int template_bw);
	
	public interface Presenter {
		
		public boolean onOK(int template_id, String template_desc, int template_ncpus, long template_mem, long template_disk, int template_bw);
		
	}
	
}
