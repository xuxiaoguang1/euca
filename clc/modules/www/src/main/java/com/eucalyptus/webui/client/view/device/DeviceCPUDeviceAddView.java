package com.eucalyptus.webui.client.view.device;

import java.io.Serializable;
import java.util.List;

import com.google.gwt.user.client.ui.IsWidget;

public interface DeviceCPUDeviceAddView extends IsWidget {
	
	void setPresenter(Presenter presenter);
	
	void popup();
	
	void setDevicesInfo(DataCache cache);
	
	void clearCache();
	
	public interface Presenter {
		
		boolean onOK(String serverMark, String name, String vendor, String model, double ghz, double cache, int num);
		
		void lookupDevicesInfo();
		
	}
	
	public class DataCache implements Serializable {
		
        private static final long serialVersionUID = 80419409300921832L;
        
        public List<String> serverNameList;
        
        public List<String> serverMarkList;

		public List<String> cpuNameList;
		
		public List<String> cpuVendorList;
		
		public List<String> cpuModelList;
		
	}

}
