package com.eucalyptus.webui.client.view;

import com.eucalyptus.webui.shared.resource.device.status.ServerState;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class DeviceServerOperateViewImpl extends DialogBox implements DeviceServerOperateView {

	private static DeviceServerOperateViewImplUiBinder uiBinder = GWT.create(DeviceServerOperateViewImplUiBinder.class);

	interface DeviceServerOperateViewImplUiBinder extends UiBinder<Widget, DeviceServerOperateViewImpl> {
	}
	
	@UiField Button buttonStart;
	@UiField Button buttonStop;
	@UiField Button buttonConnect;
	@UiField Button buttonSecret;
	@UiField TextBox serverName;
	
	public DeviceServerOperateViewImpl() {
		super(false);
		setWidget(uiBinder.createAndBindUi(this));
		center();
		hide();
	}
	
	private DeviceServerOperateView.Presenter presenter;
	
	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	private int server_id;
	
	@Override
	public void popup(int server_id, String server_name, ServerState server_state) {
		this.server_id = server_id;
		serverName.setText(server_name);
		if (server_state == ServerState.INUSE) {
			buttonStart.setEnabled(false);
			buttonStop.setEnabled(true);
		}
		else if (server_state == ServerState.STOP) {
			buttonStart.setEnabled(true);
			buttonStop.setEnabled(false);
		}
		show();
	}
	
	@UiHandler("buttonStart")
	void handleButtonStart(ClickEvent event) {
		presenter.onOK(server_id, ServerState.INUSE);
		hide();
	}
	
	@UiHandler("buttonStop")
	void handleButtonStop(ClickEvent event) {
		presenter.onOK(server_id, ServerState.STOP);
		hide();
	}
	
	@UiHandler("buttonCancel")
	void handleButtonCancel(ClickEvent event) {
		hide();
	}
	
}
