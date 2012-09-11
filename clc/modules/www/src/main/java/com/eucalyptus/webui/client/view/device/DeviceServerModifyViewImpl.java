package com.eucalyptus.webui.client.view.device;

import com.eucalyptus.webui.client.activity.device.DeviceServerActivity.ServerState;
import com.eucalyptus.webui.client.service.SearchResultRow;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Widget;

public class DeviceServerModifyViewImpl extends DialogBox implements DeviceServerModifyView {

	private static DeviceServerModifyViewImplUiBinder uiBinder = GWT.create(DeviceServerModifyViewImplUiBinder.class);

	interface DeviceServerModifyViewImplUiBinder extends UiBinder<Widget, DeviceServerModifyViewImpl> {
	}

	private DeviceServerModifyView.Presenter presenter;

	public DeviceServerModifyViewImpl() {
		super(false);
		setWidget(uiBinder.createAndBindUi(this));
		this.setPopupPosition(300, 100);
	}

	@Override
	public void setPresenter(DeviceServerModifyView.Presenter presenter) {
		this.presenter = presenter;
	}

	private SearchResultRow selected;
	
	@UiField
	Button buttonStart;
	@UiField
	Button buttonStop;
	@UiField
	Button buttonError;
	
	@Override
	public void popup(SearchResultRow row, int state) {
		selected = row;
		buttonStart.setEnabled(state != ServerState.INUSE.getValue());
		buttonStop.setEnabled(state != ServerState.STOP.getValue());
		buttonError.setEnabled(state != ServerState.ERROR.getValue());
		show();
	}
	
	@UiHandler("buttonCancel")
	void handleButtonCancel(ClickEvent event) {
		this.hide();
		presenter.onCancel();
	}

	@UiHandler("buttonStart")
	void handleButtonStart(ClickEvent event) {
		this.hide();
		presenter.onOK(selected, ServerState.INUSE.getValue());
	}
	
	@UiHandler("buttonStop")
	void handleButtonStop(ClickEvent event) {
		this.hide();
		presenter.onOK(selected, ServerState.STOP.getValue());
	}
	
	@UiHandler("buttonError")
	void handleButtonError(ClickEvent event) {
		this.hide();
		presenter.onOK(selected, ServerState.ERROR.getValue());
	}

}
