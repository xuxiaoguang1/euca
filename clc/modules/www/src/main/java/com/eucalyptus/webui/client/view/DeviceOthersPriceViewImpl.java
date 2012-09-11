package com.eucalyptus.webui.client.view;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.DoubleBox;

public class DeviceOthersPriceViewImpl extends Composite implements DeviceOthersPriceView {
	
	private static DeviceOthersPriceViewImplUiBinder uiBinder = GWT.create(DeviceOthersPriceViewImplUiBinder.class);
	
	@UiField DoubleBox memoryPrice;
	@UiField DoubleBox diskPrice;
	@UiField DoubleBox bandwidthPrice;
	@UiField TextArea memoryPriceDesc;
	@UiField TextArea diskPriceDesc;
	@UiField TextArea bandwidthPriceDesc;
	@UiField Label memoryPriceModifiedtime;
	@UiField Label diskPriceModifiedtime;
	@UiField Label bandwidthPriceModifiedtime;
	@UiField Anchor memoryPriceModify;
	@UiField Anchor diskPriceModify;
	@UiField Anchor bandwidthPriceModify;
    
    interface DeviceOthersPriceViewImplUiBinder extends UiBinder<Widget, DeviceOthersPriceViewImpl> {
    }
    
    class Column {
	    
	    DoubleBox price;
	    TextArea price_desc;
	    Label price_modifiedtime;
	    Anchor buttonModify;
	    
	    public Column(DoubleBox price, TextArea price_desc, Label price_modifiedtime, Anchor buttonModify) {
	        this.price = price;
	        this.price_desc = price_desc;
	        this.price_modifiedtime = price_modifiedtime;
	        this.buttonModify = buttonModify;
	        setValue(0, "", "");
	    }
	    
	    
	    public void setValue(double price, String price_desc, String price_modifiedtime) {
	        if (price_desc == null) {
	            price_desc = "";
	        }
	        if (price_modifiedtime == null) {
	            price_modifiedtime = "";
	        }
	        this.price.setValue(price);
	        this.price_desc.setValue(price_desc);
	        this.price_modifiedtime.setText(price_modifiedtime);
	    }
	    
	}
	
	private Presenter presenter;
	
	private Column memoryColumn;
	private Column diskColumn;
	private Column bandwidthColumn;
	
	public DeviceOthersPriceViewImpl() {
		initWidget(uiBinder.createAndBindUi(this));
		memoryColumn = new Column(memoryPrice, memoryPriceDesc, memoryPriceModifiedtime, memoryPriceModify);
		diskColumn = new Column(diskPrice, diskPriceDesc, diskPriceModifiedtime, diskPriceModify);
		bandwidthColumn = new Column(bandwidthPrice, bandwidthPriceDesc, bandwidthPriceModifiedtime, bandwidthPriceModify);
	}
	
	public boolean isEmpty(String s) {
		return s == null || s.length() == 0;
	}
	
	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}
	
    @Override
    public void setMemoryPrice(double price, String price_desc, String price_modifiedtime) {
        memoryColumn.setValue(price, price_desc, price_modifiedtime);
    }

    @Override
    public void setDiskPrice(double price, String price_desc, String price_modifiedtime) {
        diskColumn.setValue(price, price_desc, price_modifiedtime);
    }

    @Override
    public void setBandwidthPrice(double price, String price_desc, String price_modifiedtime) {
        bandwidthColumn.setValue(price, price_desc, price_modifiedtime);
    }
    
    @UiHandler("memoryPriceModify")
    void handleMemoryPriceModify(ClickEvent event) {
    	presenter.onModifyMemoryPrice(memoryColumn.price.getText(), memoryColumn.price_desc.getText());
    }
    
    @UiHandler("diskPriceModify")
    void handleDiskPriceModify(ClickEvent event) {
    	presenter.onModifyDiskPrice(diskColumn.price.getText(), diskColumn.price_desc.getText());
    }
    
    @UiHandler("bandwidthPriceModify")
    void handleBandwidthPriceModify(ClickEvent event) {
    	presenter.onModifyBandwidthPrice(bandwidthColumn.price.getText(), bandwidthColumn.price_desc.getText());
    }
    
}
