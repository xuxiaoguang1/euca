package com.eucalyptus.webui.client.view;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.LongBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class DeviceMemoryModifyViewImpl extends DialogBox implements DeviceMemoryModifyView {
    
    private static DeviceMemoryModifyViewImplUiBinder uiBinder = GWT.create(DeviceMemoryModifyViewImplUiBinder.class);
    
    interface DeviceMemoryModifyViewImplUiBinder extends UiBinder<Widget, DeviceMemoryModifyViewImpl> {
    }
    
    @UiField TextBox serverName;
    @UiField TextArea memDesc;
    @UiField LongBox memSize;

    public DeviceMemoryModifyViewImpl() {
        super(false);
        setWidget(uiBinder.createAndBindUi(this));
        center();
        hide();
    }
    
    private String getMemoryDesc() {
        return getInputText(memDesc);
    }
    
    private long getMemorySize() {
        return memSize.getValue();
    }
    
    private String getInputText(TextArea textarea) {
        String text = textarea.getText();
        if (text == null) {
            return "";
        }
        return text;
    }
    
    private DeviceMemoryModifyView.Presenter presenter;
    
    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }
    
    private int mem_id;
    private long ms_used;

    @Override
    public void popup(int mem_id, String mem_desc, long mem_size, long ms_used, String server_name) {
        this.mem_id = mem_id;
        this.ms_used = ms_used;
        memDesc.setValue(mem_desc);
        memSize.setValue(mem_size);
        serverName.setValue(server_name);
        show();
    }
    
    @UiHandler("buttonOK")
    void handleButtonOK(ClickEvent event) {
        if (presenter.onOK(mem_id, getMemoryDesc(), getMemorySize(), ms_used)) {
            hide();
        }
    }
    
    @UiHandler("buttonCancel")
    void handleButtonCancel(ClickEvent event) {
        hide();
    }
    
}
