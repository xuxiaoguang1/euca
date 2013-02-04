package com.eucalyptus.webui.client.view;

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
	
	private int cp_id;
	
	@Override
	public void popup(int cp_id, String cpu_name, String cp_desc, double cp_price) {
		this.cp_id = cp_id;
		cpuName.setText(cpu_name);
		cpuPrice.setValue(cp_price);
		cpuPriceDesc.setText(cp_desc);
		show();
	}
	
	private double getCPUPriceValue() {
	    return cpuPrice.getValue();
	}
	
	private String getCPUPriceDesc() {
		String cp_desc = cpuPriceDesc.getText();
		if (cp_desc == null) {
			return "";
		}
		return cp_desc;
	}

	@UiHandler("buttonOK")
	void handleButtonOK(ClickEvent event) {
		if (presenter.onOK(cp_id, getCPUPriceDesc(), getCPUPriceValue())) {
			hide();
		}
	}
	
	@UiHandler("buttonCancel")
	void handleButtonCancel(ClickEvent event) {
		hide();
	}

}
