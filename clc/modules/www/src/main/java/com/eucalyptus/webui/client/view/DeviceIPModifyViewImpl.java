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

public class DeviceIPModifyViewImpl extends DialogBox implements DeviceIPModifyView {
	
	private static DeviceIPModifyViewImplUiBinder uiBinder = GWT.create(DeviceIPModifyViewImplUiBinder.class);
	
	interface DeviceIPModifyViewImplUiBinder extends UiBinder<Widget, DeviceIPModifyViewImpl> {
	}
	
	@UiField TextBox ipAddr;
	@UiField TextArea ipDesc;
	@UiField ListBox ipType;

	public DeviceIPModifyViewImpl() {
		super(false);
		setWidget(uiBinder.createAndBindUi(this));
		ipType.addItem(IPType.PUBLIC.toString());
		ipType.addItem(IPType.PRIVATE.toString());
		center();
		hide();
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
		
	private String getInputText(TextArea textarea) {
		String text = textarea.getText();
		if (text == null) {
			return "";
		}
		return text;
	}
	
	private DeviceIPModifyView.Presenter presenter;
	
	@Override
    public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
    }
	
	private int ip_id;

	@Override
	public void popup(int ip_id, String ip_addr, String ip_desc, IPType ip_type) {
		this.ip_id = ip_id;
		ipAddr.setText(ip_addr);
		ipDesc.setText(ip_desc);
		if (ip_type == IPType.PUBLIC) {
			ipType.setSelectedIndex(0);
		}
		else if (ip_type == IPType.PRIVATE) {
			ipType.setSelectedIndex(1);
		}
		else {
			ipType.setSelectedIndex(-1);
		}
		show();
	}
	
	@UiHandler("buttonOK")
	void handleButtonOK(ClickEvent event) {
		if (presenter.onOK(ip_id, getIPDesc(), getIPType())) {
			hide();
		}
	}
	
	@UiHandler("buttonCancel")
	void handleButtonCancel(ClickEvent event) {
		hide();
	}

}
