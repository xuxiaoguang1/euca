package com.eucalyptus.webui.client.view;

import java.util.Collection;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DoubleBox;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.uibinder.client.UiField;

public class DeviceCPUPriceAddViewImpl extends DialogBox implements DeviceCPUPriceAddView {

	private static DeviceCPUPriceAddViewImplUiBinder uiBinder = GWT.create(DeviceCPUPriceAddViewImplUiBinder.class);
	
	interface DeviceCPUPriceAddViewImplUiBinder extends UiBinder<Widget, DeviceCPUPriceAddViewImpl> {
	}
	
	@UiField ListBox cpuNameList;
	@UiField DoubleBox cpuPrice;
	@UiField TextArea cpuPriceDesc;
	
	public DeviceCPUPriceAddViewImpl() {
		super(false);
		setWidget(uiBinder.createAndBindUi(this));
		center();
		hide();
	}
	
	private DeviceCPUPriceAddView.Presenter presenter;

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}
	
	@Override
	public void popup() {
		cpuNameList.clear();
		cpuPrice.setText("");
		presenter.lookupCPUNames();
		show();
	}
	
	@Override
	public void setCPUNameList(Collection<String> cpu_name_list) {
		cpuNameList.clear();
		if (cpu_name_list != null && !cpu_name_list.isEmpty()) {
			for (String cpu_name : cpu_name_list) {
				cpuNameList.addItem(cpu_name);
			}
			cpuNameList.setSelectedIndex(0);
		}
	}

	public String getCPUName() {
		int index = cpuNameList.getSelectedIndex();
		if (index == -1) {
			return "";
		}
		return cpuNameList.getItemText(index);
	}
	
	public double getCPUPrice() {
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
		if (presenter.onOK(getCPUName(), getCPUPriceDesc(), getCPUPrice())) {
			hide();
		}
	}
	
	@UiHandler("buttonCancel")
	void handleButtonCancel(ClickEvent event) {
		hide();
	}

}
