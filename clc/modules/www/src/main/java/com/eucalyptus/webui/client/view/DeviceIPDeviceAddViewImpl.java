package com.eucalyptus.webui.client.view;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.TextArea;

public class DeviceIPDeviceAddViewImpl extends DialogBox implements DeviceIPDeviceAddView {
	
	private static DeviceIPDeviceAddViewImplUiBinder uiBinder = GWT.create(DeviceIPDeviceAddViewImplUiBinder.class);
	
	
	interface DeviceIPDeviceAddViewImplUiBinder extends UiBinder<Widget, DeviceIPDeviceAddViewImpl> {
	}
	
	public DeviceIPDeviceAddViewImpl() {
		super(false);
		setWidget(uiBinder.createAndBindUi(this));
	}
	
	private DeviceIPDeviceAddView.Presenter presenter;

	@Override
    public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
    }
	
	@UiField TextArea publicArea;
	@UiField TextArea privateArea;
	
	@Override
    public void popup() {
		publicArea.setText("");
		privateArea.setText("");
	    show();
    }
	
	List<String> splitLines(String text) {
		List<String> list = new ArrayList<String>();
		String[] tmp = text.split("\n");
		for (String s : tmp) {
			if (s.length() != 0) {
				list.add(s);
			}
		}
		if (list.size() != 0) {
			return list;
		}
		return null;
	}
	
	@UiHandler("buttonOK")
	void handleButtonOk(ClickEvent event) {
		presenter.onOK(splitLines(publicArea.getText()), splitLines(privateArea.getText()));
		this.hide();
	}
	
	@UiHandler("buttonCancel")
	void handleButtonCancel(ClickEvent event) {
		this.hide();
	}

}
