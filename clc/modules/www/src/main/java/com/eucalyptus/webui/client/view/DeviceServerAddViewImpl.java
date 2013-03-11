package com.eucalyptus.webui.client.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.eucalyptus.webui.shared.resource.device.status.ServerState;
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
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class DeviceServerAddViewImpl extends DialogBox implements DeviceServerAddView {
    
    private static DeviceServerAddViewImplUiBinder uiBinder = GWT.create(DeviceServerAddViewImplUiBinder.class);
    
    interface DeviceServerAddViewImplUiBinder extends UiBinder<Widget, DeviceServerAddViewImpl> {
    }
    
    @UiField ListBox areaNameList;
    @UiField ListBox roomNameList;
    @UiField ListBox cabinetNameList;
    
    @UiField TextBox serverName;
    @UiField TextBox serverEuca;
    @UiField TextArea serverDesc;
    @UiField TextBox serverIP;
    @UiField IntegerBox serverBW;
    @UiField ListBox serverStateList;
    
    private Map<String, Integer> areaMap = new HashMap<String, Integer>();
    private Map<String, Integer> roomMap = new HashMap<String, Integer>();
    private Map<String, Integer> cabinetMap = new HashMap<String, Integer>();
        
    private ServerState[] serverStateValue = new ServerState[]{ServerState.INUSE, ServerState.STOP, ServerState.ERROR};

    public DeviceServerAddViewImpl() {
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
        roomNameList.addChangeHandler(new ChangeHandler() {

            @Override
            public void onChange(ChangeEvent event) {
                int room_id = getRoomID();
                if (room_id != -1) {
                    presenter.lookupCabinetNamesByRoomID(room_id);
                }
            }
            
        });
        center();
        hide();
    }
    
    @Override
    public void setAreaNames(Map<String, Integer> area_map) {
        areaNameList.clear();
        roomNameList.clear();
        cabinetNameList.clear();
        areaMap.clear();
        roomMap.clear();
        cabinetMap.clear();
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
            cabinetNameList.clear();
            roomMap.clear();
            cabinetMap.clear();
            if (room_map != null && !room_map.isEmpty()) {
                List<String> list = new ArrayList<String>(room_map.keySet());
                Collections.sort(list);
                for (String room_name : list) {
                    roomNameList.addItem(room_name);
                }
                roomNameList.setSelectedIndex(0);
                roomMap = room_map;
                int room_id = getRoomID();
                if (room_id != -1) {
                    presenter.lookupCabinetNamesByRoomID(room_id);
                }
            }
        }
    }
    
    @Override
    public void setCabinetNames(int room_id, Map<String, Integer> cabinet_map) {
        if (getRoomID() == room_id) {
            cabinetNameList.clear();
            cabinetMap.clear();
            if (cabinet_map != null && !cabinet_map.isEmpty()) {
                List<String> list = new ArrayList<String>(cabinet_map.keySet());
                Collections.sort(list);
                for (String cabinet_name : list) {
                    cabinetNameList.addItem(cabinet_name);
                }
                cabinetNameList.setSelectedIndex(0);
                cabinetMap = cabinet_map;
            }
        }
    }
    
    private String getServerName() {
        return getInputText(serverName);
    }
    
    private String getServerDesc() {
        return getInputText(serverDesc);
    }
    
    private String getServerEuca() {
        return getInputText(serverEuca);
    }
    
    private String getServerIP() {
        return getInputText(serverIP);
    }
    
    private String getServerBW() {
        return getInputText(serverBW);
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
    
    private int getCabinetID() {
        return getID(cabinetMap, getSelectedText(cabinetNameList));
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
    
    private String getSelectedText(ListBox listbox) {
        int index = listbox.getSelectedIndex();
        if (index == -1) {
            return "";
        }
        return listbox.getItemText(index);
    }
    
    private DeviceServerAddView.Presenter presenter;
    
    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }
    
    @Override
    public void popup() {
        serverName.setText("");
        serverDesc.setText("");
        serverEuca.setText("");
        serverIP.setText("");
        serverBW.setText("");
        List<String> values = new LinkedList<String>();
        for (int i = 0; i < serverStateValue.length; i ++) {
            values.add(serverStateValue[i].toString());
        }
        setListBox(serverStateList, values);
        areaNameList.clear();
        roomNameList.clear();
        cabinetNameList.clear();
        presenter.lookupAreaNames();
        show();
    }

    @UiHandler("buttonOK")
    void handleButtonOK(ClickEvent event) {
        if (presenter.onOK(getServerName(), getServerDesc(), getServerEuca(), getServerIP(), getServerBW(), serverStateValue[serverStateList.getSelectedIndex()], getCabinetID())) {
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
