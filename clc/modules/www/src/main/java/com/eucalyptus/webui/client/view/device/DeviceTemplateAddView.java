package com.eucalyptus.webui.client.view.device;

import java.util.List;

import com.google.gwt.user.client.ui.IsWidget;

public interface DeviceTemplateAddView extends IsWidget {
	
	void setPresenter(Presenter presenter);
	
	void popup();
	
	void setCPUNameList(List<String> list);
	
	public interface Presenter {
		
		boolean onOK(String mark, String cpu, int ncpus, String mem, String disk, String bw, String image);
		
		void lookupCPUNames();
		
	}

}
