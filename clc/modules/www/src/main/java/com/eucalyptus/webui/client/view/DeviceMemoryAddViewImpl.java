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
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.LongBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;

public class DeviceMemoryAddViewImpl extends DialogBox implements DeviceMemoryAddView {
    
    private static DeviceMemoryAddViewImplUiBinder uiBinder = GWT.create(DeviceMemoryAddViewImplUiBinder.class);
    
    interface DeviceMemoryAddViewImplUiBinder extends UiBinder<Widget, DeviceMemoryAddViewImpl> {
    }
    
    @UiField ListBox areaNameList;
    @UiField ListBox roomNameList;
    @UiField ListBox cabinetNameList;
    @UiField ListBox serverNameList;
    
    @UiField TextArea memDesc;
    @UiField LongBox memSize;
    
    private Map<String, Integer> areaMap = new HashMap<String, Integer>();
    private Map<String, Integer> roomMap = new HashMap<String, Integer>();
    private Map<String, Integer> cabinetMap = new HashMap<String, Integer>();
    private Map<String, Integer> serverMap = new HashMap<String, Integer>();
        
    public DeviceMemoryAddViewImpl() {
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
        cabinetNameList.addChangeHandler(new ChangeHandler() {

            @Override
            public void onChange(ChangeEvent event) {
                int cabinet_id = getCabinetID();
                if (cabinet_id != -1) {
                    presenter.lookupServerNamesByCabinetID(cabinet_id);
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
        serverNameList.clear();
        areaMap.clear();
        roomMap.clear();
        cabinetMap.clear();
        serverMap.clear();
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
            serverNameList.clear();
            roomMap.clear();
            cabinetMap.clear();
            serverMap.clear();
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
            serverNameList.clear();
            cabinetMap.clear();
            serverMap.clear();
            if (cabinet_map != null && !cabinet_map.isEmpty()) {
                List<String> list = new ArrayList<String>(cabinet_map.keySet());
                Collections.sort(list);
                for (String cabinet_name : list) {
                    cabinetNameList.addItem(cabinet_name);
                }
                cabinetNameList.setSelectedIndex(0);
                cabinetMap = cabinet_map;
                int cabinet_id = getCabinetID();
                if (cabinet_id != -1) {
                    presenter.lookupServerNamesByCabinetID(cabinet_id);
                }
            }
        }
    }

    @Override
    public void setServerNames(int cabinet_id, Map<String, Integer> server_map) {
        if (getCabinetID() == cabinet_id) {
            serverNameList.clear();
            serverMap.clear();
            if (server_map != null && !server_map.isEmpty()) {
                List<String> list = new ArrayList<String>(server_map.keySet());
                Collections.sort(list);
                for (String server_name : list) {
                    serverNameList.addItem(server_name);
                }
                serverNameList.setSelectedIndex(0);
                serverMap = server_map;
            }
        }
    }
    
    private String getMemoryDesc() {
        return getInputText(memDesc);
    }
    
    private long getMemorySize() {
        return memSize.getValue();
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
    
    private int getServerID() {
        return getID(serverMap, getSelectedText(serverNameList));
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
    
    private DeviceMemoryAddView.Presenter presenter;
    
    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }
    
    @Override
    public void popup() {
        memDesc.setValue("");
        memSize.setValue(0L);
        areaNameList.clear();
        roomNameList.clear();
        cabinetNameList.clear();
        serverNameList.clear();
        presenter.lookupAreaNames();
        show();
    }

    @UiHandler("buttonOK")
    void handleButtonOK(ClickEvent event) {
        if (presenter.onOK(getMemoryDesc(), getMemorySize(), getServerID())) {
            hide();
        }
    }
    
    @UiHandler("buttonCancel")
    void handleButtonCancel(ClickEvent event) {
        hide();
    }

}
