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
	
	private String getCPUGHz() {
		return getInputText(cpuGHz);
	}
	
	private String getCPUCache() {
		return getInputText(cpuCache);
	}
	
	private int getCPUTotal() {
		return numList.getSelectedIndex() + 1;
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
	
	private String getInputText(DoubleBox textbox) {
		String text = textbox.getText();
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

	@Override
	public void popup(int cpu_id, String cpu_name, String cpu_desc, int cpu_total, String cpu_vendor, String cpu_model, String cpu_ghz, String cpu_cache, String server_name) {
		this.cpu_id = cpu_id;
		cpuName.setText(cpu_name);
		cpuDesc.setText(cpu_desc);
		cpuVendor.setText(cpu_vendor);
		cpuModel.setText(cpu_model);
		cpuGHz.setText(cpu_ghz);
		cpuCache.setText(cpu_cache);
		serverName.setText(server_name);
		numList.clear();
		for (int i = 1; i <= Math.max(cpu_total, 64); i ++) {
			numList.addItem(Integer.toString(i));
		}
		numList.setSelectedIndex(cpu_total - 1);
		show();
	}
	
	@UiHandler("buttonOK")
	void handleButtonOK(ClickEvent event) {
		if (presenter.onOK(cpu_id, getCPUDesc(), getCPUTotal(), getCPUVendor(), getCPUModel(), getCPUGHz(), getCPUCache())) {
			hide();
		}
	}
	
	@UiHandler("buttonCancel")
	void handleButtonCancel(ClickEvent event) {
		hide();
	}
    
}
