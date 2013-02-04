package com.eucalyptus.webui.client.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	
	private Map<String, Integer> areaMap = new HashMap<String, Integer>();
	private Map<String, Integer> roomMap = new HashMap<String, Integer>();
	
	public DeviceCabinetAddViewImpl() {
		super(false);
		setWidget(uiBinder.createAndBindUi(this));
		areaNameList.addChangeHandler(new ChangeHandler() {

			@Override
			public void onChange(ChangeEvent event) {
			    int area_id = getAreaID();
			    if (area_id != -1) {
			        presenter.lookupRoomNamesByAreaID(area_id);
			    }
			}
			
		});
		center();
		hide();
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
	public void setAreaNames(Map<String, Integer> area_map) {
	    areaNameList.clear();
        roomNameList.clear();
        areaMap.clear();
        roomMap.clear();
        if (area_map != null && !area_map.isEmpty()) {
            List<String> list = new ArrayList<String>(area_map.keySet());
            Collections.sort(list);
            for (String area_name : list) {
                areaNameList.addItem(area_name);
            }
            areaNameList.setSelectedIndex(0);
            areaMap = area_map;
            int area_id = getAreaID();
            if (area_id != -1) {
                presenter.lookupRoomNamesByAreaID(area_id);
            }
        }
	}
	
	@Override
	public void setRoomNames(int area_id, Map<String, Integer> room_map) {
	    if (getAreaID() == area_id) {
	        roomNameList.clear();
	        roomMap.clear();
	        if (room_map != null && !room_map.isEmpty()) {
	            List<String> list = new ArrayList<String>(room_map.keySet());
	            Collections.sort(list);
	            for (String room_name : list) {
	                roomNameList.addItem(room_name);
	            }
	            roomNameList.setSelectedIndex(0);
	            roomMap = room_map;
	        }
	    }
	}
	
	private String getCabinetName() {
		return getInputText(cabinetName);
	}
	
	private String getCabinetDesc() {
		return getInputText(cabinetDesc);
	}
	
	private int getID(Map<String, Integer> map, String name) {
	    if (name == null || name.isEmpty()) {
	        return -1;
	    }
	    Integer id = map.get(name);
	    if (id == null) {
	        return -1;
	    }
	    return id;
	}
	
	private int getAreaID() {
	    return getID(areaMap, getSelectedText(areaNameList));
	}
	
	private int getRoomID() {
	    return getID(roomMap, getSelectedText(roomNameList));
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
		if (presenter.onOK(getCabinetName(), getCabinetDesc(), getRoomID())) {
			hide();
		}
	}
	
	@UiHandler("buttonCancel")
	void handleButtonCancel(ClickEvent event) {
		hide();
	}

}
