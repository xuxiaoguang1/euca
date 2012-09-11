package com.eucalyptus.webui.client.view;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.uibinder.client.UiField;

public class DeviceRoomModifyViewImpl extends DialogBox implements DeviceRoomModifyView {

	private static DeviceRoomModifyViewImplUiBinder uiBinder = GWT.create(DeviceRoomModifyViewImplUiBinder.class);
	
	interface DeviceRoomModifyViewImplUiBinder extends UiBinder<Widget, DeviceRoomModifyViewImpl> {
	}
	
	@UiField TextBox roomName;
	@UiField TextArea roomDesc;

	public DeviceRoomModifyViewImpl() {
		super(false);
		setWidget(uiBinder.createAndBindUi(this));
		center();
		hide();
	}
	
	private DeviceRoomModifyView.Presenter presenter;

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}
	
	private int room_id;
	
	@Override
	public void popup(int room_id, String room_name, String room_desc) {
		this.room_id = room_id;
		roomName.setText(room_name);
		roomDesc.setText(room_desc);
		show();
	}
	
	private String getRoomDesc() {
		String room_desc = roomDesc.getValue();
		if (room_desc == null) {
			return "";
		}
		return room_desc;
	}

	@UiHandler("buttonOK")
	void handleButtonOK(ClickEvent event) {
		if (presenter.onOK(room_id, getRoomDesc())) {
			hide();
		}
	}
	
	@UiHandler("buttonCancel")
	void handleButtonCancel(ClickEvent event) {
		hide();
	}

}
