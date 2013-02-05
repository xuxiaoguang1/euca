package com.eucalyptus.webui.client.view;

import java.util.Date;

import com.google.gwt.user.client.ui.IsWidget;

public interface DeviceMemoryServiceModifyView extends IsWidget {
	
	public void setPresenter(Presenter presenter);
	
	public void popup(int ms_id, String mem_name, String ms_desc, long ms_reserved, long ms_used, Date ms_starttime, Date ms_endtime, String server_name, String account_name, String user_name);
	
	public interface Presenter {
		
		public boolean onOK(int ms_id, String ms_desc, long ms_reserved, long ms_used, Date ms_starttime, Date ms_endtime);
		
	}
	
}