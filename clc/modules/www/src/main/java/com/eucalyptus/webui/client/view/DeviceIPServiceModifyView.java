package com.eucalyptus.webui.client.view;

import java.util.Date;

import com.google.gwt.user.client.ui.IsWidget;

public interface DeviceIPServiceModifyView extends IsWidget {
	
	public void setPresenter(Presenter presenter);
	
	public void popup(int ip_id, String ip_addr, String is_desc, Date is_starttime, Date is_endtime, String account_name, String user_name);
	
	public interface Presenter {
		
		public boolean onOK(int ip_id, String is_desc, Date is_starttime, Date is_endtime);
		
	}
	
}
