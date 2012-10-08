package com.eucalyptus.webui.client.view;

import com.eucalyptus.webui.shared.resource.device.status.IPType;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class DeviceIPAddViewImpl extends DialogBox implements DeviceIPAddView {
	
	private static DeviceIPAddViewImplUiBinder uiBinder = GWT.create(DeviceIPAddViewImplUiBinder.class);
	
	interface DeviceIPAddViewImplUiBinder extends UiBinder<Widget, DeviceIPAddViewImpl> {
	}
	
	@UiField TextBox ipAddr;
	@UiField TextArea ipDesc;
	@UiField ListBox ipType;
		
	public DeviceIPAddViewImpl() {
		super(false);
		setWidget(uiBinder.createAndBindUi(this));
		ipType.addItem(IPType.PUBLIC.toString());
		ipType.addItem(IPType.PRIVATE.toString());
		center();
		hide();
	}
	
	private String getIPAddr() {
		return getInputText(ipAddr);
	}
	
	private String getIPDesc() {
		return getInputText(ipDesc);
	}
	
	private IPType getIPType() {
		int index = ipType.getSelectedIndex();
		switch (index) {
		case 0: return IPType.PUBLIC;
		case 1: return IPType.PRIVATE;
		default: return null;
		}
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
	
	private DeviceIPAddView.Presenter presenter;
	
	@Override
    public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
    }
	
	@Override
    public void popup() {
		ipAddr.setText("");
		ipDesc.setText("");
		ipType.setSelectedIndex(-1);
		show();
    }

	@UiHandler("buttonOK")
	void handleButtonOK(ClickEvent event) {
		if (presenter.onOK(getIPAddr(), getIPDesc(), getIPType())) {
			hide();
		}
	}
	
	@UiHandler("buttonCancel")
	void handleButtonCancel(ClickEvent event) {
		hide();
	}

}
