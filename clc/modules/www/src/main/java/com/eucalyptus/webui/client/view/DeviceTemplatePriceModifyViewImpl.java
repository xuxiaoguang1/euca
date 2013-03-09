package com.eucalyptus.webui.client.view;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DoubleBox;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.LongBox;
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
	@UiField IntegerBox cpuSize;
	@UiField DoubleBox cpuPrice;
	@UiField LongBox memTotal;
	@UiField DoubleBox memPrice;
	@UiField LongBox diskTotal;
	@UiField DoubleBox diskPrice;
	@UiField IntegerBox bwSize;
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
	private double mem_total;
	private double disk_total;
	private double bw_size;
	
	private void updateTotalPrice() {
        try {
            double sum = 0;
            double value;
            if ((value = cpuPrice.getValue()) < 0 || ncpus < 0) {
                totalPrice.setText("INVALID VALUE");
                return;
            }
            sum += value * ncpus;
            if ((value = memPrice.getValue()) < 0 || mem_total < 0) {
                totalPrice.setText("INVALID VALUE");
                return;
            }
            sum += value * mem_total;
            if ((value = diskPrice.getValue()) < 0 || disk_total < 0) {
                totalPrice.setText("INVALID VALUE");
                return;
            }
            sum += value * disk_total;
            if ((value = bwPrice.getValue()) < 0 || bw_size < 0) {
                totalPrice.setText("INVALID VALUE");
                return;
            }
            sum += value * bw_size;
            totalPrice.setValue(sum);
        }
        catch (Exception e) {
            totalPrice.setText("INVALID VALUE");
        }
	}

    @Override
    public void popup(int tp_id, String template_name, String tp_desc,
            int ncpus, double tp_cpu, long mem_total, double tp_mem, long disk_total, double tp_disk,
            int bw_size, double bw_price) {
        this.template_price_id = tp_id;
        this.ncpus = ncpus;
        this.mem_total = mem_total;
        this.disk_total = disk_total;
        this.bw_size = bw_size;
        templateName.setText(template_name);
        templatePriceDesc.setText(tp_desc);
        cpuSize.setValue(ncpus);
        cpuPrice.setValue(tp_cpu);
        memTotal.setValue(mem_total);
        memPrice.setValue(tp_mem);
        diskTotal.setValue(disk_total);
        diskPrice.setValue(tp_disk);
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
    
    private double getCPUPrice() {
        return cpuPrice.getValue();
    }
    
    private double getMemPrice() {
    	return memPrice.getValue();
    }

    private double getDiskPrice() {
    	return diskPrice.getValue();
    }
    
    private double getBWPrice() {
    	return bwPrice.getValue();
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
