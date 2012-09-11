package com.eucalyptus.webui.client.view.device;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DoubleBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.uibinder.client.UiField;

public class DeviceCPUPriceModifyViewImpl extends DialogBox implements DeviceCPUPriceModifyView {

	private static DeviceCPUPriceModifyViewImplUiBinder uiBinder = GWT.create(DeviceCPUPriceModifyViewImplUiBinder.class);
	
	interface DeviceCPUPriceModifyViewImplUiBinder extends UiBinder<Widget, DeviceCPUPriceModifyViewImpl> {
	}
	
	@UiField TextBox cpuName;
	@UiField DoubleBox cpuPrice;
	@UiField TextArea cpuPriceDesc;

	public DeviceCPUPriceModifyViewImpl() {
		super(false);
		setWidget(uiBinder.createAndBindUi(this));
		center();
		hide();
	}
	
	private DeviceCPUPriceModifyView.Presenter presenter;

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}
	
	private int cpu_price_id;
	
	@Override
	public void popup(int cpu_price_id, String cpu_name, String cpu_price_desc, double cpu_price) {
		this.cpu_price_id = cpu_price_id;
		cpuName.setText(cpu_name);
		cpuPrice.setValue(cpu_price);
		cpuPriceDesc.setText(cpu_price_desc);
		show();
	}
	
	private String getCPUPriceValue() {
		String value = cpuPrice.getText();
		if (value == null) {
			return "";
		}
		return value;
	}
	
	private String getCPUPriceDesc() {
		String cpu_price_desc = cpuPriceDesc.getText();
		if (cpu_price_desc == null) {
			return "";
		}
		return cpu_price_desc;
	}

	@UiHandler("buttonOK")
	void handleButtonOK(ClickEvent event) {
		if (presenter.onOK(cpu_price_id, getCPUPriceDesc(), getCPUPriceValue())) {
			hide();
		}
	}
	
	@UiHandler("buttonCancel")
	void handleButtonCancel(ClickEvent event) {
		hide();
	}

}
