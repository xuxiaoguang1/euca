package com.eucalyptus.webui.client.view;

import java.util.Date;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.DoubleBox;

public class DeviceDevicePriceViewImpl extends Composite implements DeviceDevicePriceView {
	
	private static DeviceDevicePriceViewImplUiBinder uiBinder = GWT.create(DeviceDevicePriceViewImplUiBinder.class);
	
	@UiField DoubleBox cpuPrice;
	@UiField DoubleBox memoryPrice;
	@UiField DoubleBox diskPrice;
	@UiField DoubleBox bandwidthPrice;
	@UiField TextBox cpuPriceDesc;
	@UiField TextBox memoryPriceDesc;
	@UiField TextBox diskPriceDesc;
	@UiField TextBox bandwidthPriceDesc;
	@UiField Label cpuPriceModifiedtime;
	@UiField Label memoryPriceModifiedtime;
	@UiField Label diskPriceModifiedtime;
	@UiField Label bandwidthPriceModifiedtime;
	@UiField Anchor cpuPriceModify;
	@UiField Anchor memoryPriceModify;
	@UiField Anchor diskPriceModify;
	@UiField Anchor bandwidthPriceModify;
    
    interface DeviceDevicePriceViewImplUiBinder extends UiBinder<Widget, DeviceDevicePriceViewImpl> {
    }
    
    private DateTimeFormat format = DateTimeFormat.getFormat("yyyy-MM-dd");
    
    class Column {
	    
	    DoubleBox price;
	    TextBox price_desc;
	    Label price_modifiedtime;
	    Anchor buttonModify;
	    
	    public Column(DoubleBox price, TextBox price_desc, Label price_modifiedtime, Anchor buttonModify) {
	        this.price = price;
	        this.price_desc = price_desc;
	        this.price_modifiedtime = price_modifiedtime;
	        this.buttonModify = buttonModify;
	        setValue("", 0, null);
	    }
	    
	    
	    public void setValue(String op_desc, double op_price, Date op_modifiedtime) {
	        if (op_desc == null) {
	            op_desc = "";
	        }
	        this.price_desc.setText(op_desc);
	        this.price.setValue(op_price);
	        if (op_modifiedtime == null) {
	            this.price_modifiedtime.setText("");
	        }
	        else {
	            this.price_modifiedtime.setText(format.format(op_modifiedtime));
	        }
	    }
	    
	}
	
	private Presenter presenter;
	
	private Column cpuColumn;
	private Column memoryColumn;
	private Column diskColumn;
	private Column bandwidthColumn;
	
	public DeviceDevicePriceViewImpl() {
		initWidget(uiBinder.createAndBindUi(this));
		cpuColumn = new Column(cpuPrice, cpuPriceDesc, cpuPriceModifiedtime, cpuPriceModify);
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
    public void setCPUPrice(String op_desc, double op_price, Date op_modifiedtime) {
        cpuColumn.setValue(op_desc, op_price, op_modifiedtime);
    }
	
    @Override
    public void setMemoryPrice(String op_desc, double op_price, Date op_modifiedtime) {
        memoryColumn.setValue(op_desc, op_price, op_modifiedtime);
    }

    @Override
    public void setDiskPrice(String op_desc, double op_price, Date op_modifiedtime) {
        diskColumn.setValue(op_desc, op_price, op_modifiedtime);
    }

    @Override
    public void setBWPrice(String op_desc, double op_price, Date op_modifiedtime) {
        bandwidthColumn.setValue(op_desc, op_price, op_modifiedtime);
    }
    
    @UiHandler("cpuPriceModify")
    void handleCPUPriceModify(ClickEvent event) {
        presenter.onModifyCPUPrice(cpuColumn.price_desc.getText(), cpuColumn.price.getValue());
    }
    
    @UiHandler("memoryPriceModify")
    void handleMemoryPriceModify(ClickEvent event) {
    	presenter.onModifyMemoryPrice(memoryColumn.price_desc.getText(), memoryColumn.price.getValue());
    }
    
    @UiHandler("diskPriceModify")
    void handleDiskPriceModify(ClickEvent event) {
    	presenter.onModifyDiskPrice(diskColumn.price_desc.getText(), diskColumn.price.getValue());
    }
    
    @UiHandler("bandwidthPriceModify")
    void handleBandwidthPriceModify(ClickEvent event) {
    	presenter.onModifyBWPrice(bandwidthColumn.price_desc.getText(), bandwidthColumn.price.getValue());
    }
    
}
