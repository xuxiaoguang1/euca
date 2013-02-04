package com.eucalyptus.webui.client.view;

import com.eucalyptus.webui.shared.resource.device.status.IPType;
import com.google.gwt.user.client.ui.IsWidget;

public interface DeviceIPAddView extends IsWidget {
	
	public void setPresenter(Presenter presenter);
	
	public void popup();
	
	public interface Presenter {
		
		public boolean onOK(String ip_addr, String ip_desc, IPType ip_type);
		
	}
	
}
