package com.eucalyptus.webui.client.view;

import com.eucalyptus.webui.shared.resource.device.status.IPType;
import com.google.gwt.user.client.ui.IsWidget;

public interface DeviceIPModifyView extends IsWidget {
	
	public void setPresenter(Presenter presenter);
	
	public void popup(int ip_id, String ip_addr, String ip_desc, IPType ip_type);
	
	public interface Presenter {
		
		public boolean onOK(int ip_id, String ip_desc, IPType ip_type);
		
	}

}
