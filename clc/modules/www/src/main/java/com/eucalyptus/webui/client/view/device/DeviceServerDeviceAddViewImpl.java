package com.eucalyptus.webui.client.view.device;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class DeviceServerDeviceAddViewImpl extends DialogBox implements DeviceServerDeviceAddView {
	
	private static DeviceServerDeviceAddViewImplUiBinder uiBinder = GWT.create(DeviceServerDeviceAddViewImplUiBinder.class);
	
	interface DeviceServerDeviceAddViewImplUiBinder extends UiBinder<Widget, DeviceServerDeviceAddViewImpl> {
	}
	
	public DeviceServerDeviceAddViewImpl() {
		super(false);
		setWidget(uiBinder.createAndBindUi(this));
	}
	
	private DeviceServerDeviceAddView.Presenter presenter;

	@Override
    public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
    }
	
	class IntegerBoxChangeHandler implements ChangeHandler {

		IntegerBox box;

		IntegerBoxChangeHandler(IntegerBox box) {
			this.box = box;
			this.box.setText(Integer.toString(0));
		}
		
		@Override
        public void onChange(ChangeEvent event) {
			String value = Integer.toString(intValue(box.getText()));
			if (!value.equals(box.getText())) {
				box.setText(value);
			}
        }
		
	}
	
	private boolean initialized = false;

	@Override
    public void popup(String[] stateValueList) {
		if (!initialized) {
			initialized = true;
			serverBW.addChangeHandler(new IntegerBoxChangeHandler(serverBW));
			serverState.clear();
			if (stateValueList != null && stateValueList.length != 0) {
				for (String s : stateValueList) {
					serverState.addItem(s);
				}
				serverState.setSelectedIndex(0);
			}
		}
		serverMark.setText("");
		serverName.setText("");
		serverConf.setText("");
		serverIP.setText("");
		serverRoom.setText("");
	    show();
    }
	
	@UiField TextBox serverMark;
	@UiField TextBox serverName;
	@UiField TextBox serverConf;
	@UiField TextBox serverIP;
	@UiField IntegerBox serverBW;
	@UiField TextBox serverRoom;
	@UiField ListBox serverState;

	private int intValue(String v) {
		try {
			return Integer.parseInt(v);
		}
		catch (Exception e) {
			return 0;
		}
	}
	
	private String getValue(ListBox box) {
		int index = box.getSelectedIndex();
		return index == -1 ? null : box.getItemText(index);
	}
	
	@UiHandler("buttonOK")
	void handleButtonOk(ClickEvent event) {
		String mark = serverMark.getText();
		String name = serverName.getText();
		String conf = serverConf.getText();
		String ip = serverIP.getText();
		String room = serverRoom.getText();
		String state = getValue(serverState);
		int bw = intValue(serverBW.getText());
		if (presenter.onOK(mark, name,conf, ip, bw, state, room)) {
			this.hide();
		}
	}
	
	@UiHandler("buttonCancel")
	void handleButtonCancel(ClickEvent event) {
		this.hide();
	}

}
