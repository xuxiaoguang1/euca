package com.eucalyptus.webui.client.view;

import java.util.Date;

import com.google.gwt.user.client.ui.IsWidget;

public interface DeviceOthersPriceView extends IsWidget {
	
	public void setPresenter(Presenter presenter);
	
	public void setMemoryPrice(String op_desc, double op_price, Date op_modifiedtime);
	
	public void setDiskPrice(String op_desc, double op_price, Date op_modifiedtime);
	
	public void setBWPrice(String op_desc, double op_price, Date op_modifiedtime);
	
	public interface Presenter {
		
		public void onModifyMemoryPrice(String op_desc, double op_price);
		
		public void onModifyDiskPrice(String op_desc, double op_price);
		
		public void onModifyBWPrice(String op_desc, double op_price);
		
	}
	
}
