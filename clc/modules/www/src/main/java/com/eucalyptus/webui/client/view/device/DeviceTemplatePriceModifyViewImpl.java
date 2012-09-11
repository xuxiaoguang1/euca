package com.eucalyptus.webui.client.view.device;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DoubleBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.uibinder.client.UiField;

public class DeviceTemplatePriceModifyViewImpl extends DialogBox implements DeviceTemplatePriceModifyView {

	private static DeviceTemplatePriceModifyViewImplUiBinder uiBinder = GWT.create(DeviceTemplatePriceModifyViewImplUiBinder.class);
	
	interface DeviceTemplatePriceModifyViewImplUiBinder extends UiBinder<Widget, DeviceTemplatePriceModifyViewImpl> {
	}
	
	@UiField TextBox templateName;
	@UiField TextArea templatePriceDesc;
	@UiField TextBox cpuName;
	@UiField DoubleBox cpuPrice;
	@UiField DoubleBox memSize;
	@UiField DoubleBox memPrice;
	@UiField DoubleBox diskSize;
	@UiField DoubleBox diskPrice;
	@UiField DoubleBox bwSize;
	@UiField DoubleBox bwPrice;
	@UiField DoubleBox totalPrice;
	
	public DeviceTemplatePriceModifyViewImpl() {
		super(false);
		setWidget(uiBinder.createAndBindUi(this));
		ValueChangeHandler<Double> handler = new ValueChangeHandler<Double>() {

			@Override
			public void onValueChange(ValueChangeEvent<Double> event) {
				updateTotalPrice();
			}
			
		};
		cpuPrice.addValueChangeHandler(handler);
		memPrice.addValueChangeHandler(handler);
		diskPrice.addValueChangeHandler(handler);
		bwPrice.addValueChangeHandler(handler);
		center();
		hide();
	}
	
	private DeviceTemplatePriceModifyView.Presenter presenter;

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}
	
	private int template_price_id;
	private int ncpus;
	private double mem_size;
	private double disk_size;
	private double bw_size;
	
	private boolean isEmpty(String s) {
		return s == null || s.isEmpty();
	}
	
	private double getPrice(String text) {
		if (isEmpty(text)) {
			return 0;
		}
		return Double.parseDouble(text);
	}
	
	private void updateTotalPrice() {
		try {
			double total_price = getPrice(cpuPrice.getText()) * ncpus;
			total_price += getPrice(memPrice.getText()) * mem_size;
			total_price += getPrice(diskPrice.getText()) * disk_size;
			total_price += getPrice(bwPrice.getText()) * bw_size;
			totalPrice.setValue(total_price);
		}
		catch (Exception e) {
			totalPrice.setText("INVALID VALUE");
			e.printStackTrace();
		}
	}

    @Override
    public void popup(int template_price_id, String template_name, String template_price_desc, String cpu_name,
            int ncpus, double cpu_price, double mem_size, double mem_price, double disk_size, double disk_price,
            double bw_size, double bw_price) {
        this.template_price_id = template_price_id;
        this.ncpus = ncpus;
        this.mem_size = mem_size;
        this.disk_size = disk_size;
        this.bw_size = bw_size;
        templateName.setText(template_name);
        templatePriceDesc.setText(template_price_desc);
        StringBuilder sb = new StringBuilder();
        sb.append("CPU: ").append(cpu_name).append(" x ").append(ncpus);
        cpuName.setText(sb.toString());
        cpuPrice.setValue(cpu_price);
        memSize.setValue(mem_size);
        memPrice.setValue(mem_price);
        diskSize.setValue(disk_size);
        diskPrice.setValue(disk_price);
        bwSize.setValue(bw_size);
        bwPrice.setValue(bw_price);
        updateTotalPrice();
        show();
    }
    
    private String getTemplatePriceDesc() {
    	String text = templatePriceDesc.getText();
    	if (text == null) {
    		return "";
    	}
    	return text;
    }
    
    private String getCPUPrice() {
    	String value = cpuPrice.getText();
    	if (value == null) {
    		return "";
    	}
    	return value;
    }
    
    private String getMemPrice() {
    	String value = memPrice.getText();
    	if (value == null) {
    		return "";
    	}
    	return value;
    }

    private String getDiskPrice() {
    	String value = diskPrice.getText();
    	if (value == null) {
    		return "";
    	}
    	return value;
    }
    
    private String getBWPrice() {
    	String value = bwPrice.getText();
    	if (value == null) {
    		return "";
    	}
    	return value;
    }
    
    @UiHandler("buttonOK")
    void handleButtonOK(ClickEvent event) {
    	if (presenter.onOK(template_price_id, getTemplatePriceDesc(),
    			getCPUPrice(), getMemPrice(), getDiskPrice(), getBWPrice())) {
    		hide();
    	}
    }
    
	@UiHandler("buttonCancel")
	void handleButtonCancel(ClickEvent event) {
		hide();
	}
    
}
