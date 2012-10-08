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
	@UiField TextBox memoryName;
	@UiField TextArea memoryDesc;
	@UiField LongBox memorySize;

	public DeviceMemoryModifyViewImpl() {
		super(false);
		setWidget(uiBinder.createAndBindUi(this));
		center();
		hide();
	}
	
	private String getMemoryDesc() {
		return getInputText(memoryDesc);
	}
	
	private String getMemorySize() {
		return getInputText(memorySize);
	}
	
	private String getInputText(TextArea textarea) {
		String text = textarea.getText();
		if (text == null) {
			return "";
		}
		return text;
	}
	
	private String getInputText(LongBox textbox) {
		String text = textbox.getText();
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
	
	private int memory_id;

	@Override
	public void popup(int memory_id, String memory_name, String memory_desc, long memory_size, String server_name) {
		this.memory_id = memory_id;
		memoryName.setText(memory_name);
		memoryDesc.setText(memory_desc);
		memorySize.setValue(memory_size);
		serverName.setText(server_name);
		show();
	}
	
	@UiHandler("buttonOK")
	void handleButtonOK(ClickEvent event) {
		if (presenter.onOK(memory_id, getMemoryDesc(), getMemorySize())) {
			hide();
		}
	}
	
	@UiHandler("buttonCancel")
	void handleButtonCancel(ClickEvent event) {
		hide();
	}
    
}
