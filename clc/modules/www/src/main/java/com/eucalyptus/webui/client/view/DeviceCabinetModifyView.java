package com.eucalyptus.webui.client.view;

import com.google.gwt.user.client.ui.IsWidget;

public interface DeviceCabinetModifyView extends IsWidget {
	
	public void setPresenter(Presenter presenter);
	
	public void popup(int cabinet_id, String cabinet_name, String cabinet_desc);
	
	public interface Presenter {
		
		public boolean onOK(int cabinet_id, String cabinet_desc);
		
	}

}
