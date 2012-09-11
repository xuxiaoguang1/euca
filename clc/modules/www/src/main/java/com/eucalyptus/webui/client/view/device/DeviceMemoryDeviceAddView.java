package com.eucalyptus.webui.client.view.device;

import java.io.Serializable;
import java.util.List;

import com.google.gwt.user.client.ui.IsWidget;

public interface DeviceMemoryDeviceAddView extends IsWidget {
	
	void setPresenter(Presenter presenter);
	
	void popup();
	
	void setDevicesInfo(DataCache cache);
	
	void clearCache();
	
	public interface Presenter {
		
		boolean onOK(String serverMark, String name, long total, int num);
		
		void lookupDevicesInfo();
		
	}
	
	public class DataCache implements Serializable {
		
        private static final long serialVersionUID = 5644742281760151360L;

		public List<String> serverNameList;
        
        public List<String> serverMarkList;

		public List<String> memoryNameList;
		
	}

}
