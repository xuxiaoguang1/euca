package com.eucalyptus.webui.client.view;

import com.google.gwt.user.client.ui.IsWidget;

public interface DeviceDiskModifyView extends IsWidget {
	
	public void setPresenter(Presenter presenter);
	
	public void popup(int disk_id, String disk_name, String disk_desc, long disk_size, long ds_used, String server_name);
	
	public interface Presenter {
		
		public boolean onOK(int disk_id, String disk_desc, long disk_size, long ds_used);
		
	}

}
