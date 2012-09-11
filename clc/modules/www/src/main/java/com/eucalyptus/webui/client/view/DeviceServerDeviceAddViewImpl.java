package com.eucalyptus.webui.client.view;

import java.util.List;

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
        serverBW.addChangeHandler(new IntegerBoxChangeHandler(serverBW));
        serverState.clear();
 
        areaNameList.addChangeHandler(new ChangeHandler() {

            @Override
            public void onChange(ChangeEvent event) {
                String area_name = getAreaName();
                if (!isEmpty(area_name)) {
                    presenter.lookupRoomNamesByAreaName(area_name);
                }
            }
            
        });
        
        roomNameList.addChangeHandler(new ChangeHandler() {

            @Override
            public void onChange(ChangeEvent event) {
                String room_name = getRoomName();
                if (!isEmpty(room_name)) {
                    presenter.lookupCabinetNamesByRoomName(room_name);
                }
            }
            
        });
	}
	
	private String getAreaName() {
	    return getSelectedText(areaNameList);
	}
	
	private String getRoomName() {
	    return getSelectedText(roomNameList);
	}
	
	private String getCabinetName() {
	    return getSelectedText(cabinetNameList);
	}
	
	private String getSelectedText(ListBox listbox) {
	    int index = listbox.getSelectedIndex();
        if (index != -1) {
            return listbox.getItemText(index);
        }
        return "";
	}
	
	private boolean isEmpty(String s) {
	    return s == null || s.length() == 0;
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
	
	@Override
    public void popup(String[] stateValueList) {
	    serverState.clear();
	    if (stateValueList != null && stateValueList.length != 0) {
	        for (String s : stateValueList) {
	            serverState.addItem(s);
            }   
            serverState.setSelectedIndex(0);
        }
		serverMark.setText("");
		serverName.setText("");
		serverConf.setText("");
		serverIP.setText("");
		areaNameList.clear();
		roomNameList.clear();
		cabinetNameList.clear();
		presenter.lookupAreaNames();
	    show();
    }
	
	@UiField TextBox serverMark;
	@UiField TextBox serverName;
	@UiField TextBox serverConf;
	@UiField TextBox serverIP;
	@UiField IntegerBox serverBW;
	@UiField ListBox serverState;
	
	@UiField ListBox areaNameList;
    @UiField ListBox roomNameList;
    @UiField ListBox cabinetNameList;

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
		String state = getValue(serverState);
		int bw = intValue(serverBW.getText());
		if (presenter.onOK(mark, name,conf, ip, bw, state, getCabinetName())) {
			this.hide();
		}
	}
	
	@UiHandler("buttonCancel")
	void handleButtonCancel(ClickEvent event) {
		this.hide();
	}

    @Override
    public void setAreaNameList(List<String> area_name_list) {
        areaNameList.clear();
        roomNameList.clear();
        cabinetNameList.clear();
        if (area_name_list != null && !area_name_list.isEmpty()) {
            for (String area_name : area_name_list) {
                areaNameList.addItem(area_name);
            }
            areaNameList.setSelectedIndex(0);
            String area_name = getAreaName();
            if (!isEmpty(area_name)) {
                presenter.lookupRoomNamesByAreaName(area_name);
            }
        }
    }

    @Override
    public void setRoomNameList(String area_name, List<String> room_name_list) {
        if (getAreaName().equals(area_name)) {
            roomNameList.clear();
            cabinetNameList.clear();
            if (room_name_list != null && !room_name_list.isEmpty()) {
                for (String room_name : room_name_list) {
                    roomNameList.addItem(room_name);
                }
                roomNameList.setSelectedIndex(0);
                String room_name = getRoomName();
                if (!isEmpty(room_name)) {
                    presenter.lookupCabinetNamesByRoomName(room_name);
                }
            }
        }
    }

    @Override
    public void setCabinetNameList(String room_name, List<String> cabinet_name_list) {
        System.out.println("set cabinet name list");
        if (getRoomName().equals(room_name)) {
            cabinetNameList.clear();
            if (cabinet_name_list != null && !cabinet_name_list.isEmpty()) {
                for (String cabinet_name : cabinet_name_list) {
                    cabinetNameList.addItem(cabinet_name);
                }
                cabinetNameList.setSelectedIndex(0);
            }
        }
    }

}
