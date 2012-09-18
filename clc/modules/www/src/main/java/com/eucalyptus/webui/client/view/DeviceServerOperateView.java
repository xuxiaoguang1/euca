package com.eucalyptus.webui.client.view;

import com.eucalyptus.webui.shared.resource.device.status.ServerState;
import com.google.gwt.user.client.ui.IsWidget;

public interface DeviceServerOperateView extends IsWidget {
	
	public void setPresenter(Presenter presenter);
	
	public void popup(int server_id, String server_name, ServerState server_state);
	
	public interface Presenter {
		
		public void onOK(int server_id, ServerState server_state);
		
	}

}
