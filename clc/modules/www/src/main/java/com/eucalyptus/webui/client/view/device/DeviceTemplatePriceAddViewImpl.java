package com.eucalyptus.webui.client.view.device;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DoubleBox;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.uibinder.client.UiField;

public class DeviceTemplatePriceAddViewImpl extends DialogBox implements DeviceTemplatePriceAddView {

	private static DeviceTemplatePriceAddViewImplUiBinder uiBinder = GWT.create(DeviceTemplatePriceAddViewImplUiBinder.class);
	
	interface DeviceTemplatePriceAddViewImplUiBinder extends UiBinder<Widget, DeviceTemplatePriceAddViewImpl> {
	}
	
	@UiField ListBox templateName;
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
	
	public DeviceTemplatePriceAddViewImpl() {
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
		templateName.addChangeHandler(new ChangeHandler() {

			@Override
			public void onChange(ChangeEvent event) {
				int index = templateName.getSelectedIndex();
				if (index != -1) {
					presenter.lookupTemplateDetailByName(templateName.getItemText(index));
				}
			}
			
		});
		center();
		hide();
	}
	
	private DeviceTemplatePriceAddView.Presenter presenter;

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}
	
	private int template_id;
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
	
	private void resetTemplateDetail(boolean all) {
		cpuName.setText("");
		memSize.setText("");
		diskSize.setText("");
		bwSize.setText("");
		if (all) {
			templatePriceDesc.setText("");
			cpuPrice.setText("");
			memPrice.setText("");
			diskPrice.setText("");
			bwPrice.setText("");
			totalPrice.setText("");
		}
	}
	
	@Override
	public void popup() {
		templateName.clear();
		resetTemplateDetail(true);
		presenter.lookupTemplateList();
		show();
	}
	
	@Override
	public void setTemplateList(List<String> template_name_list) {
		templateName.clear();
		resetTemplateDetail(true);
		if (template_name_list != null && !template_name_list.isEmpty()) {
			for (String template_name : template_name_list) {
				templateName.addItem(template_name);
			}
			templateName.setSelectedIndex(0);
			presenter.lookupTemplateDetailByName(templateName.getItemText(0));
		}
	}
	
	@Override
	public void setTemplateDetails(int template_id, String template_name, String cpu_name, int ncpus, double mem_size, double disk_size, double bw_size) {
		int index = templateName.getSelectedIndex();
		if (index != -1 && templateName.getItemText(index).equals(template_name)) {
			this.template_id = template_id;
			this.ncpus = ncpus;
			this.mem_size = mem_size;
			this.disk_size = disk_size;
			this.bw_size = bw_size;
			StringBuilder sb = new StringBuilder();
	        sb.append("CPU: ").append(cpu_name).append(" x ").append(ncpus);
			cpuName.setText(sb.toString());
			memSize.setValue(mem_size);
			diskSize.setValue(disk_size);
			bwSize.setValue(bw_size);
			updateTotalPrice();
		}
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
    	if (presenter.onOK(template_id, getTemplatePriceDesc(),
    			getCPUPrice(), getMemPrice(), getDiskPrice(), getBWPrice())) {
    		hide();
    	}
    }
    
	@UiHandler("buttonCancel")
	void handleButtonCancel(ClickEvent event) {
		hide();
	}
    
}
