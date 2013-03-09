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

public class DeviceDiskModifyViewImpl extends DialogBox implements DeviceDiskModifyView {
    
    private static DeviceDiskModifyViewImplUiBinder uiBinder = GWT.create(DeviceDiskModifyViewImplUiBinder.class);
    
    interface DeviceDiskModifyViewImplUiBinder extends UiBinder<Widget, DeviceDiskModifyViewImpl> {
    }
    
    @UiField TextBox serverName;
    @UiField TextArea diskDesc;
    @UiField LongBox diskTotal;

    public DeviceDiskModifyViewImpl() {
        super(false);
        setWidget(uiBinder.createAndBindUi(this));
        center();
        hide();
    }
    
    private String getDiskDesc() {
        return getInputText(diskDesc);
    }
    
    private long getDiskSize() {
        return diskTotal.getValue();
    }
    
    private String getInputText(TextArea textarea) {
        String text = textarea.getText();
        if (text == null) {
            return "";
        }
        return text;
    }
    
    private DeviceDiskModifyView.Presenter presenter;
    
    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }
    
    private int disk_id;
    private long ds_used;

    @Override
    public void popup(int disk_id, String disk_desc, long disk_total, long ds_used, String server_name) {
        this.disk_id = disk_id;
        this.ds_used = ds_used;
        diskDesc.setValue(disk_desc);
        diskTotal.setValue(disk_total);
        serverName.setValue(server_name);
        show();
    }
    
    @UiHandler("buttonOK")
    void handleButtonOK(ClickEvent event) {
        if (presenter.onOK(disk_id, getDiskDesc(), getDiskSize(), ds_used)) {
            hide();
        }
    }
    
    @UiHandler("buttonCancel")
    void handleButtonCancel(ClickEvent event) {
        hide();
    }
    
}
