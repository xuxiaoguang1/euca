package com.eucalyptus.webui.client.view;

import com.eucalyptus.webui.shared.resource.device.status.ServerState;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class DeviceServerModifyViewImpl extends DialogBox implements DeviceServerModifyView {

    private static DeviceServerModifyViewImplUiBinder uiBinder = GWT.create(DeviceServerModifyViewImplUiBinder.class);

    interface DeviceServerModifyViewImplUiBinder extends UiBinder<Widget, DeviceServerModifyViewImpl> {
    }
    
    @UiField TextBox serverName;
    @UiField TextBox serverEuca;
    @UiField TextArea serverDesc;
    @UiField TextBox serverIP;
    @UiField IntegerBox serverBW;
    @UiField TextBox serverState;
    
    public DeviceServerModifyViewImpl() {
        super(false);
        setWidget(uiBinder.createAndBindUi(this));
        center();
        hide();
    }
    
    private String getServerDesc() {
        return getInputText(serverDesc);
    }
    
    private String getServerIP() {
        return getInputText(serverIP);
    }
    
    private String getServerBW() {
        return getInputText(serverBW);
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
    
    private DeviceServerModifyView.Presenter presenter;

    @Override
    public void setPresenter(DeviceServerModifyView.Presenter presenter) {
        this.presenter = presenter;
    }
    
    private int server_id;
    
    @Override
    public void popup(int server_id, String server_name, String server_desc, String server_euca, String server_ip, int server_bw, ServerState server_state) {
        this.server_id = server_id;
        serverName.setText(server_name);
        serverDesc.setText(server_desc);
        serverEuca.setText(server_euca);
        serverIP.setText(server_ip);
        serverBW.setText(Integer.toString(server_bw));
        serverState.setText(server_state == null ? "" : server_state.toString());
        show();
    }
    
    @UiHandler("buttonOK")
    void handleButtonOK(ClickEvent event) {
        if (presenter.onOK(server_id, getServerDesc(), getServerIP(), getServerBW())) {
            hide();
        }
    }

    @UiHandler("buttonCancel")
    void handleButtonCancel(ClickEvent event) {
        hide();
    }
    
}
