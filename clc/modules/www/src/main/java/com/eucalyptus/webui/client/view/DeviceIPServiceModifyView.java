package com.eucalyptus.webui.client.view;

import com.google.gwt.user.client.ui.IsWidget;

public interface DeviceIPServiceModifyView extends IsWidget {
	
	public void setPresenter(Presenter presenter);
	
	public void popup(int is_id, String ip_addr, String is_desc, String is_starttime, String is_endtime, String account_name, String user_name);
	
	public interface Presenter {
		
		public boolean onOK(int is_id, String is_desc, String is_starttime, String is_endtime);
		
	}
	
}
