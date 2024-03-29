package com.eucalyptus.webui.client.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.eucalyptus.webui.shared.resource.device.status.IPType;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;

public class DeviceIPServiceAddViewImpl extends DialogBox implements DeviceIPServiceAddView {
    
    private static DeviceIPServiceAddViewImplUiBinder uiBinder = GWT.create(DeviceIPServiceAddViewImplUiBinder.class);
    
    interface DeviceIPServiceAddViewImplUiBinder extends UiBinder<Widget, DeviceIPServiceAddViewImpl> {
    }

    @UiField ListBox accountNameList;
    @UiField ListBox userNameList;
    @UiField TextArea ipDesc;
    @UiField ListBox ipCount;
    @UiField ListBox ipType;
    
    private Map<String, Integer> accountMap = new HashMap<String, Integer>();
    private Map<String, Integer> userMap = new HashMap<String, Integer>();
    
    public DeviceIPServiceAddViewImpl() {
        super(false);
        setWidget(uiBinder.createAndBindUi(this));
        accountNameList.addChangeHandler(new ChangeHandler() {

            @Override
            public void onChange(ChangeEvent event) {
                int account_id = getAccountID();
                if (account_id != -1) {
                    presenter.lookupUserNamesByAccountID(account_id);
                }
            }
            
        });
        for (int i = 1; i <= 128; i ++) {
            ipCount.addItem(Integer.toString(i));
        }
        ipType.addItem(IPType.PUBLIC.toString());
        ipType.addItem(IPType.PRIVATE.toString());
        center();
        hide();
    }
    
    @Override
    public void setAccountNames(Map<String, Integer> account_map) {
        accountNameList.clear();
        userNameList.clear();
        accountMap.clear();
        userMap.clear();
        if (account_map != null && !account_map.isEmpty()) {
            List<String> list = new ArrayList<String>(account_map.keySet());
            Collections.sort(list);
            for (String account_name : list) {
                accountNameList.addItem(account_name);
            }
            accountNameList.setSelectedIndex(0);
            accountMap = account_map;
            int account_id = getAccountID();
            if (account_id != -1) {
                presenter.lookupUserNamesByAccountID(account_id);
            }
        }
    }
    
    @Override
    public void setUserNames(int account_id, Map<String, Integer> user_map) {
        if (getAccountID() == account_id) {
            userNameList.clear();
            userMap.clear();
            if (user_map != null && !user_map.isEmpty()) {
                List<String> list = new ArrayList<String>(user_map.keySet());
                Collections.sort(list);
                for (String user_name : list) {
                    userNameList.addItem(user_name);
                }
                userNameList.setSelectedIndex(0);
                userMap = user_map;
            }
        }
    }
    
    private String getIPDesc() {
        return getInputText(ipDesc);
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
    
    private int getAccountID() {
        return getID(accountMap, getSelectedText(accountNameList));
    }
    
    private int getUserID() {
        return getID(userMap, getSelectedText(userNameList));
    }
    
    private int getIPCount() {
        return ipCount.getSelectedIndex() + 1;
    }
    
    private IPType getIPType() {
        int index = ipType.getSelectedIndex();
        switch (index) {
        case 0: return IPType.PUBLIC;
        case 1: return IPType.PRIVATE;
        default: return null;
        }
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
    
    private DeviceIPServiceAddView.Presenter presenter;
    
    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }
    
    @Override
    public void popup() {
        ipType.setSelectedIndex(-1);
        ipCount.setSelectedIndex(0);
        ipDesc.setValue("");
        accountNameList.clear();
        userNameList.clear();
        presenter.lookupAccountNames();
        show();
    }

    @UiHandler("buttonOK")
    void handleButtonOK(ClickEvent event) {
        if (presenter.onOK(getIPDesc(), getIPType(), getIPCount(), getUserID())) {
            hide();
        }
    }
    
    @UiHandler("buttonCancel")
    void handleButtonCancel(ClickEvent event) {
        hide();
    }
    
}
