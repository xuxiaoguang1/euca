package com.eucalyptus.webui.client.view;

import java.util.LinkedList;
import java.util.List;

import com.eucalyptus.webui.shared.resource.device.status.ServerState;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class DeviceServerModifyViewImpl extends DialogBox implements DeviceServerModifyView {

	private static DeviceServerModifyViewImplUiBinder uiBinder = GWT.create(DeviceServerModifyViewImplUiBinder.class);

	interface DeviceServerModifyViewImplUiBinder extends UiBinder<Widget, DeviceServerModifyViewImpl> {
	}
	
	@UiField TextBox serverName;
	@UiField TextArea serverDesc;
	@UiField TextBox serverIP;
	@UiField IntegerBox serverBW;
	@UiField ListBox serverStateList;
	
	private ServerState[] serverStateValue = new ServerState[]{ServerState.INUSE, ServerState.STOP, ServerState.ERROR};

	public DeviceServerModifyViewImpl() {
		super(false);
		setWidget(uiBinder.createAndBindUi(this));
		center();
		hide();
	}
	
	private String getServerDesc() {
		return getInputText(serverDesc);
	}
	
	private String getServerIP() {
		return getInputText(serverIP);
	}
	
	private String getServerBW() {
		return getInputText(serverBW);
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
	
	private String getInputText(IntegerBox textbox) {
		String text = textbox.getText();
		if (text == null) {
			return "";
		}
		return text;
	}
	
	private DeviceServerModifyView.Presenter presenter;

	@Override
	public void setPresenter(DeviceServerModifyView.Presenter presenter) {
		this.presenter = presenter;
	}
	
	private int server_id;
	
	@Override
	public void popup(int server_id, String server_name, String server_desc, String server_ip, String server_bw, ServerState server_state) {
		this.server_id = server_id;
		serverName.setText(server_name);
		serverDesc.setText(server_desc);
		serverIP.setText(server_ip);
		serverBW.setText(server_bw);
		List<String> values = new LinkedList<String>();
		for (int i = 0; i < serverStateValue.length; i ++) {
			values.add(serverStateValue[i].toString());
		}
		setListBox(serverStateList, values);
		for (int i = 0; i < serverStateValue.length; i ++) {
			if (server_state == serverStateValue[i]) {
				serverStateList.setSelectedIndex(i);
			}
		}
		show();
	}
	
	@UiHandler("buttonOK")
	void handleButtonOK(ClickEvent event) {
		if (presenter.onOK(server_id, getServerDesc(), getServerIP(), getServerBW(),
				serverStateValue[serverStateList.getSelectedIndex()])) {
			hide();
		}
	}

	@UiHandler("buttonCancel")
	void handleButtonCancel(ClickEvent event) {
		hide();
	}
	
    private void setListBox(ListBox listbox, List<String> values) {
    	listbox.clear();
    	if (values != null && !values.isEmpty()) {
	    	for (String value : values) {
	    		listbox.addItem(value);
	    	}
	    	listbox.setSelectedIndex(0);
    	}
    }
    
}
