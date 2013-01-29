package com.eucalyptus.webui.client.view;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.LongBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class DeviceTemplateAddViewImpl extends DialogBox implements DeviceTemplateAddView {
	
	private static DeviceTemplateAddViewImplUiBinder uiBinder = GWT.create(DeviceTemplateAddViewImplUiBinder.class);
	
	interface DeviceTemplateAddViewImplUiBinder extends UiBinder<Widget, DeviceTemplateAddViewImpl> {
	}
	
	@UiField TextBox templateName;
	@UiField TextArea templateDesc;
	@UiField ListBox templateCPU;
	@UiField ListBox templateNCPUs;
	@UiField LongBox templateMem;
	@UiField LongBox templateDisk;
	@UiField IntegerBox templateBW;
	@UiField TextBox templateImage;
	
	public DeviceTemplateAddViewImpl() {
		super(false);
		setWidget(uiBinder.createAndBindUi(this));
        for (int i = 1; i <= 64; i ++) {
        	templateNCPUs.addItem(Integer.toString(i));
        }
		center();
		hide();
	}
	
	private String getTemplateName() {
		return getInputText(templateName);
	}
	
	private String getTemplateDesc() {
		return getInputText(templateDesc);
	}
	
	private String getTemplateCPU() {
		return getInputText(templateCPU);
	}
	
	private int getTemplateNCPUs() {
		return templateNCPUs.getSelectedIndex() + 1;
	}
	
	private long getTemplateMem() {
		return templateMem.getValue();
	}
	
	private long getTemplateDisk() {
		return templateDisk.getValue();
	}
	
	private int getTemplateBW() {
		return templateBW.getValue();
	}
	
	private String getTemplateImage() {
		return getInputText(templateImage);
	}
	
	private String getInputText(TextBox textbox) {
		String text = textbox.getText();
		if (text == null) {
			return "";
		}
		return text;
	}
	
	private String getInputText(ListBox textbox) {
	    String text = textbox.getValue(textbox.getSelectedIndex());
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

	private DeviceTemplateAddView.Presenter presenter;
	
	@Override
    public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
    }
	
	@Override
	public void popup() {
		templateName.setValue("");
		templateDesc.setValue("");
		templateCPU.clear();
		templateNCPUs.setSelectedIndex(0);
		templateMem.setValue(0L);;
		templateDisk.setValue(0L);;
		templateBW.setValue(0);;
		templateImage.setValue("");;
		show();
    }

	@UiHandler("buttonOK")
	void handleButtonOK(ClickEvent event) {
		if (presenter.onOK(getTemplateName(), getTemplateDesc(), getTemplateCPU(), getTemplateNCPUs(), getTemplateMem(), getTemplateDisk(), getTemplateBW(), getTemplateImage())) {
			hide();
		}
	}
	
	@UiHandler("buttonCancel")
	void handleButtonCancel(ClickEvent event) {
		hide();
	}
	
	@Override
	public void setCPUNameList(List<String> cpu_name_list) {
		
	}
	
}
