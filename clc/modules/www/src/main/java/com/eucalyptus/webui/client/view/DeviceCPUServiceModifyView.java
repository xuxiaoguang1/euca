package com.eucalyptus.webui.client.view;

import java.util.Date;

import com.google.gwt.user.client.ui.IsWidget;

public interface DeviceCPUServiceModifyView extends IsWidget {
	
	public void setPresenter(Presenter presenter);
	
	public void popup(int cs_id, String cpu_name, String cs_desc, int cs_reserved, int cs_used, Date cs_starttime, Date cs_endtime, String server_name, String account_name, String user_name);
	
	public interface Presenter {
		
		public boolean onOK(int cs_id, String cs_desc, int cs_reserved, int cs_used, Date cs_starttime, Date cs_endtime);
		
	}
	
}
