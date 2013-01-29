package com.eucalyptus.webui.client.view;

import java.util.List;

import com.google.gwt.user.client.ui.IsWidget;

public interface DeviceTemplateAddView extends IsWidget {
	
	public void setPresenter(Presenter presenter);
	
	public void popup();
	
	public void setCPUNameList(List<String> cpu_name_list);
	
	public interface Presenter {
		
		public boolean onOK(String template_name, String template_desc, String template_cpu, int template_ncpus, long template_mem, long template_disk, int template_bw, String template_image);
		
		public void lookupCPUNames();
		
	}
	
}
