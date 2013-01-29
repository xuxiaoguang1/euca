package com.eucalyptus.webui.client.view;

import com.eucalyptus.webui.shared.resource.device.status.ServerState;
import com.google.gwt.user.client.ui.IsWidget;

public interface DeviceServerModifyView extends IsWidget {
	
	public void setPresenter(Presenter presenter);
	
	public void popup(int server_id, String server_name, String server_desc, String server_ip, int server_bw, ServerState server_state);
	
	public interface Presenter {
		
		public boolean onOK(int server_id, String server_desc, String server_ip, String server_bw);
		
	}

}
