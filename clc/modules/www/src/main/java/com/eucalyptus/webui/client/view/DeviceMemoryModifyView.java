package com.eucalyptus.webui.client.view;

import com.google.gwt.user.client.ui.IsWidget;

public interface DeviceMemoryModifyView extends IsWidget {
	
	public void setPresenter(Presenter presenter);
	
	public void popup(int memory_id, String memory_name, String memory_desc, long memory_size, String server_name);
	
	public interface Presenter {
		
		public boolean onOK(int memory_id, String memory_desc, String memory_size);
		
	}

}
