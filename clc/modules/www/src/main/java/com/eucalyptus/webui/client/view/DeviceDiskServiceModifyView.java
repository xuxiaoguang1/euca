package com.eucalyptus.webui.client.view;

import java.util.Date;

import com.google.gwt.user.client.ui.IsWidget;

public interface DeviceDiskServiceModifyView extends IsWidget {
	
	public void setPresenter(Presenter presenter);
	
	public void popup(int ds_id, String disk_name, String ds_desc, long ds_used, Date ds_starttime, Date ds_endtime, String server_name, String account_name, String user_name);
	
	public interface Presenter {
		
		public boolean onOK(int ds_id, String ds_desc, Date ds_starttime, Date ds_endtime);
		
	}
	
}
