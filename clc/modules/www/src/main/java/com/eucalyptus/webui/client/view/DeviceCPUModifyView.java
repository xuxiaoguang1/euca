package com.eucalyptus.webui.client.view;

import com.google.gwt.user.client.ui.IsWidget;

public interface DeviceCPUModifyView extends IsWidget {
	
	public void setPresenter(Presenter presenter);
	
	public void popup(int cpu_id, String cpu_name, String cpu_desc, int cpu_total, int cs_used, String server_name);
	
	public interface Presenter {
		
		public boolean onOK(int cpu_id, String cpu_desc, int cpu_total, int cs_used);
		
	}

}
