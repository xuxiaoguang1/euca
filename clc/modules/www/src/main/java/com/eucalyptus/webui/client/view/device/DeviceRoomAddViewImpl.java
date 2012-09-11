package com.eucalyptus.webui.client.view.device;

import java.util.Collection;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.uibinder.client.UiField;

public class DeviceRoomAddViewImpl extends DialogBox implements DeviceRoomAddView {

	private static DeviceRoomAddViewImplUiBinder uiBinder = GWT.create(DeviceRoomAddViewImplUiBinder.class);
	
	interface DeviceRoomAddViewImplUiBinder extends UiBinder<Widget, DeviceRoomAddViewImpl> {
	}
	
	@UiField TextBox roomName;
	@UiField TextArea roomDesc;
	@UiField ListBox areaNameList;
	
	public DeviceRoomAddViewImpl() {
		super(false);
		setWidget(uiBinder.createAndBindUi(this));
		center();
		hide();
	}
	
	private DeviceRoomAddView.Presenter presenter;

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}
	
	@Override
	public void popup() {
		roomName.setValue("");
		roomDesc.setValue("");
		areaNameList.clear();
		presenter.lookupAreaNames();
		show();
	}
	
	public void setAreaNameList(Collection<String> area_name_list) {
		areaNameList.clear();
		if (area_name_list != null && !area_name_list.isEmpty()) {
			for (String area_name : area_name_list) {
				areaNameList.addItem(area_name);
			}
			areaNameList.setSelectedIndex(0);
		}
	}
	
	private String getRoomName() {
		String room_name = roomName.getValue();
		if (room_name == null) {
			return "";
		}
		return room_name;
	}
	
	private String getRoomDesc() {
		String room_desc = roomDesc.getValue();
		if (room_desc == null) {
			return "";
		}
		return room_desc;
	}
	
	private String getAreaName() {
		int index = areaNameList.getSelectedIndex();
		if (index == -1) {
			return "";
		}
		return areaNameList.getItemText(index);
	}

	@UiHandler("buttonOK")
	void handleButtonOK(ClickEvent event) {
		if (presenter.onOK(getRoomName(), getRoomDesc(), getAreaName())) {
			hide();
		}
	}
	
	@UiHandler("buttonCancel")
	void handleButtonCancel(ClickEvent event) {
		hide();
	}

}
