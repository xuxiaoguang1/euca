package com.eucalyptus.webui.client.view;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.uibinder.client.UiField;

public class DeviceCabinetAddViewImpl extends DialogBox implements DeviceCabinetAddView {

	private static DeviceCabinetAddViewImplUiBinder uiBinder = GWT.create(DeviceCabinetAddViewImplUiBinder.class);
	
	interface DeviceCabinetAddViewImplUiBinder extends UiBinder<Widget, DeviceCabinetAddViewImpl> {
	}
	
	@UiField TextBox cabinetName;
	@UiField TextArea cabinetDesc;
	@UiField ListBox areaNameList;
	@UiField ListBox roomNameList;
	
	public DeviceCabinetAddViewImpl() {
		super(false);
		setWidget(uiBinder.createAndBindUi(this));
		areaNameList.addChangeHandler(new ChangeHandler() {

			@Override
			public void onChange(ChangeEvent event) {
				String area_name = getAreaName();
				if (!isEmpty(area_name)) {
					presenter.lookupRoomNamesByAreaName(area_name);
				}
			}
			
		});
		center();
		hide();
	}
	
	private boolean isEmpty(String s) {
		return s == null || s.length() == 0;
	}
	
	private DeviceCabinetAddView.Presenter presenter;

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}
	
	@Override
	public void popup() {
		cabinetName.setText("");
		cabinetDesc.setText("");
		areaNameList.clear();
		roomNameList.clear();
		presenter.lookupAreaNames();
		show();
	}
	
	@Override
	public void setAreaNameList(List<String> area_name_list) {
		areaNameList.clear();
		roomNameList.clear();
		if (area_name_list != null && !area_name_list.isEmpty()) {
			for (String area_name : area_name_list) {
				areaNameList.addItem(area_name);
			}
			areaNameList.setSelectedIndex(0);
			String area_name = getAreaName();
			if (!isEmpty(area_name)) {
				presenter.lookupRoomNamesByAreaName(getAreaName());
			}
		}
	}
	
	@Override
	public void setRoomNameList(String area_name, List<String> room_name_list) {
		if (getAreaName().equals(area_name)) {
			roomNameList.clear();
			if (room_name_list != null && !room_name_list.isEmpty()) {
				if (!room_name_list.isEmpty()) {
					for (String room_name : room_name_list) {
						roomNameList.addItem(room_name);
					}
					roomNameList.setSelectedIndex(0);
				}
			}
		}
	}
	
	private String getCabinetName() {
		return getInputText(cabinetName);
	}
	
	private String getCabinetDesc() {
		return getInputText(cabinetDesc);
	}
	
	private String getAreaName() {
		return getSelectedText(areaNameList);
	}
	
	private String getRoomName() {
		return getSelectedText(roomNameList);
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
		if (presenter.onOK(getCabinetName(), getCabinetDesc(), getRoomName())) {
			hide();
		}
	}
	
	@UiHandler("buttonCancel")
	void handleButtonCancel(ClickEvent event) {
		hide();
	}

}
