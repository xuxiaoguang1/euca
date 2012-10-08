package com.eucalyptus.webui.client.view;

import com.google.gwt.user.client.ui.IsWidget;

public interface DeviceCPUServiceModifyView extends IsWidget {
	
	public void setPresenter(Presenter presenter);
	
	public void popup(int cs_id, String cpu_name, String cs_desc, int cs_used, String cs_starttime, String cs_endtime, String server_name, String account_name, String user_name);
	
	public interface Presenter {
		
		public boolean onOK(int cs_id, String cs_desc, String cs_starttime, String cs_endtime);
		
	}
	
}
