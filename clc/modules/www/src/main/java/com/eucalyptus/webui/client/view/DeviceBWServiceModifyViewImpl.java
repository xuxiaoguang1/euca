package com.eucalyptus.webui.client.view;

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

public class DeviceBWServiceModifyViewImpl extends DialogBox implements DeviceBWServiceModifyView {
    
    private static DeviceBWServiceModifyViewImplUiBinder uiBinder = GWT.create(DeviceBWServiceModifyViewImplUiBinder.class);
    
    interface DeviceBWServiceModifyViewImplUiBinder extends UiBinder<Widget, DeviceBWServiceModifyViewImpl> {
    }

    @UiField TextBox ipAddr;
    @UiField TextBox accountName;
    @UiField TextBox userName;
    @UiField TextArea bwDesc;
    @UiField IntegerBox bwMax;
    
    public DeviceBWServiceModifyViewImpl() {
        super(false);
        setWidget(uiBinder.createAndBindUi(this));
        center();
        hide();
    }
    
    private String getBWDesc() {
        return getInputText(bwDesc);
    }
    
    private int getBWMax() {
        return bwMax.getValue();
    }
    
    private String getInputText(TextArea textarea) {
        String text = textarea.getText();
        if (text == null) {
            return "";
        }
        return text;
    }
    
    private DeviceBWServiceModifyView.Presenter presenter;
    
    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }
    
    private int bs_id;
    
    @Override
    public void popup(int bs_id, String ip_addr, String bs_desc, int bs_bw_max, String account_name, String user_name) {
        this.bs_id = bs_id;
        ipAddr.setValue(ip_addr);
        bwDesc.setValue(bs_desc);
        bwMax.setValue(bs_bw_max);
        accountName.setValue(account_name);
        userName.setValue(user_name);
        show();
    }

    @UiHandler("buttonOK")
    void handleButtonOK(ClickEvent event) {
        if (presenter.onOK(bs_id, getBWDesc(), getBWMax())) {
            hide();
        }
    }
    
    @UiHandler("buttonCancel")
    void handleButtonCancel(ClickEvent event) {
        hide();
    }
    
}
