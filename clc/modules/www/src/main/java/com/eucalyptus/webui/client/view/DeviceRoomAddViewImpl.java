package com.eucalyptus.webui.client.view;

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
		roomName.setText("");
		roomDesc.setText("");
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
		return getInputText(roomName);
	}
	
	private String getRoomDesc() {
		return getInputText(roomDesc);
	}
	
	private String getAreaName() {
		return getSelectedText(areaNameList);
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
	
	private String getSelectedText(ListBox listbox) {
	    int index = listbox.getSelectedIndex();
	    if (index == -1) {
	    	return "";
	    }
	    return listbox.getItemText(index);
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
