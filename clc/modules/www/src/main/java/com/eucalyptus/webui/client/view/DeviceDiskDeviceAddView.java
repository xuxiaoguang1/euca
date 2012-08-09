package com.eucalyptus.webui.client.view;

import java.io.Serializable;
import java.util.List;

import com.google.gwt.user.client.ui.IsWidget;

public interface DeviceDiskDeviceAddView extends IsWidget {
	
	void setPresenter(Presenter presenter);
	
	void popup();
	
	void setDevicesInfo(DataCache cache);
	
	void clearCache();
	
	public interface Presenter {
		
		boolean onOK(String serverMark, String name, long total, int num);
		
		void lookupDevicesInfo();
		
	}
	
	public class DataCache implements Serializable {
		
        private static final long serialVersionUID = 341204709997119230L;

		public List<String> serverNameList;
        
        public List<String> serverMarkList;

		public List<String> diskNameList;
		
	}

}
