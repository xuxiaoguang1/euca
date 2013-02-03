package com.eucalyptus.webui.client.view;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DoubleBox;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class DeviceCPUModifyViewImpl extends DialogBox implements DeviceCPUModifyView {
	
	private static DeviceCPUModifyViewImplUiBinder uiBinder = GWT.create(DeviceCPUModifyViewImplUiBinder.class);
	
	interface DeviceCPUModifyViewImplUiBinder extends UiBinder<Widget, DeviceCPUModifyViewImpl> {
	}
	
	@UiField TextBox serverName;
	@UiField TextBox cpuName;
	@UiField TextArea cpuDesc;
	@UiField TextBox cpuVendor;
	@UiField TextBox cpuModel;
	@UiField DoubleBox cpuGHz;
	@UiField DoubleBox cpuCache;
	@UiField ListBox numList;

	public DeviceCPUModifyViewImpl() {
		super(false);
		setWidget(uiBinder.createAndBindUi(this));
		center();
		hide();
	}
	
	private String getCPUDesc() {
		return getInputText(cpuDesc);
	}
	
	private String getCPUVendor() {
		return getInputText(cpuVendor);
	}
	
	private String getCPUModel() {
		return getInputText(cpuModel);
	}
	
	private double getCPUGHz() {
		return cpuGHz.getValue();
	}
	
	private double getCPUCache() {
		return cpuCache.getValue();
	}
	
	private int getCPUTotal() {
		return numList.getSelectedIndex() + cpu_total_base;
	}
	
	private String getInputText(TextBox textbox) {
		String text = textbox.getText();
		if (text == null) {
			return "";
		}
		return text;
	}
	
	private String getInputText(TextArea textarea) {
		String text = textarea.getText();
		if (text == null) {
			return "";
		}
		return text;
	}
	
	private DeviceCPUModifyView.Presenter presenter;
	
	@Override
    public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
    }
	
	private int cpu_id;
	private int cpu_total_base;
	private int cs_used;

	@Override
	public void popup(int cpu_id, String cpu_name, String cpu_desc, int cpu_total, int cs_used, String cpu_vendor, String cpu_model, double cpu_ghz, double cpu_cache, String server_name) {
		this.cpu_id = cpu_id;
		this.cpu_total_base = Math.max(1, cs_used);
		this.cs_used = cs_used;
		cpuName.setValue(cpu_name);
		cpuDesc.setValue(cpu_desc);
		cpuVendor.setValue(cpu_vendor);
		cpuModel.setValue(cpu_model);
		cpuGHz.setValue(cpu_ghz);
		cpuCache.setValue(cpu_cache);
		serverName.setValue(server_name);
		numList.clear();
		for (int i = cpu_total_base; i <= Math.max(cpu_total, 64); i ++) {
			numList.addItem(Integer.toString(i));
		}
		numList.setSelectedIndex(cpu_total - cpu_total_base);
		show();
	}
	
	@UiHandler("buttonOK")
	void handleButtonOK(ClickEvent event) {
		if (presenter.onOK(cpu_id, getCPUDesc(), getCPUTotal(), cs_used, getCPUVendor(), getCPUModel(), getCPUGHz(), getCPUCache())) {
			hide();
		}
	}
	
	@UiHandler("buttonCancel")
	void handleButtonCancel(ClickEvent event) {
		hide();
	}
    
}
