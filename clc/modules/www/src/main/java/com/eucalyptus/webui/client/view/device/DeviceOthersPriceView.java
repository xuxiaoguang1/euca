package com.eucalyptus.webui.client.view.device;

import com.google.gwt.user.client.ui.IsWidget;

public interface DeviceOthersPriceView extends IsWidget {
	
	public void setPresenter(Presenter presenter);
	
	public void setMemoryPrice(double price, String price_desc, String price_modifiedtime);
	
	public void setDiskPrice(double price, String price_desc, String price_modifiedtime);
	
	public void setBandwidthPrice(double price, String price_desc, String price_modifiedtime);
	
	public interface Presenter {
		
		public void onModifyMemoryPrice(String price, String price_decs);
		
		public void onModifyDiskPrice(String price, String price_decs);
		
		public void onModifyBandwidthPrice(String price, String price_decs);
		
	}
	
}
