package com.eucalyptus.webui.client.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	
	private Map<String, Integer> areaMap = new HashMap<String, Integer>();
	
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
	
    @Override
    public void setAreaNames(Map<String, Integer> area_map) {
        areaNameList.clear();
        areaMap.clear();
        if (area_map != null && !area_map.isEmpty()) {
            List<String> list = new ArrayList<String>(area_map.keySet());
            Collections.sort(list);
            for (String area_name : list) {
                areaNameList.addItem(area_name);
            }
            areaNameList.setSelectedIndex(0);
            areaMap.putAll(area_map);
        }
    }
	
	private String getRoomName() {
		return getInputText(roomName);
	}
	
	private String getRoomDesc() {
		return getInputText(roomDesc);
	}
	
	private int getAreaID() {
	    String area_name = getSelectedText(areaNameList);
	    if (area_name == null || area_name.isEmpty()) {
	        return -1;
	    }
	    Integer id = areaMap.get(area_name);
	    if (id == null) {
	        return -1;
	    }
	    return id;
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
		if (presenter.onOK(getRoomName(), getRoomDesc(), getAreaID())) {
			hide();
		}
	}
	
	@UiHandler("buttonCancel")
	void handleButtonCancel(ClickEvent event) {
		hide();
	}

}
