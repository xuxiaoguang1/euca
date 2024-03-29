package com.eucalyptus.webui.client.view;

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

public class DeviceTemplateModifyViewImpl extends DialogBox implements DeviceTemplateModifyView {
	
	private static DeviceTemplateModifyViewImplUiBinder uiBinder = GWT.create(DeviceTemplateModifyViewImplUiBinder.class);
	
	interface DeviceTemplateModifyViewImplUiBinder extends UiBinder<Widget, DeviceTemplateModifyViewImpl> {
	}
	
	@UiField TextBox templateName;
	@UiField TextArea templateDesc;
	@UiField ListBox templateNCPUs;
	@UiField LongBox templateMem;
	@UiField LongBox templateDisk;
	@UiField IntegerBox templateBW;
	
	public DeviceTemplateModifyViewImpl() {
		super(false);
		setWidget(uiBinder.createAndBindUi(this));
        for (int i = 1; i <= 64; i ++) {
        	templateNCPUs.addItem(Integer.toString(i));
        }
		center();
		hide();
	}
	
	private String getTemplateDesc() {
		return getInputText(templateDesc);
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
	
	private String getInputText(TextArea textarea) {
		String text = textarea.getText();
		if (text == null) {
			return "";
		}
		return text;
	}

	private DeviceTemplateModifyView.Presenter presenter;
	
	@Override
    public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
    }
	
	private int template_id;
	
	@Override
	public void popup(int template_id, String template_name, String template_desc, int template_ncpus, long template_mem, long template_disk, int template_bw) {
		this.template_id = template_id;
		templateName.setText(template_name);
		templateDesc.setText(template_desc);
		templateNCPUs.setSelectedIndex(template_ncpus - 1);
		templateMem.setValue(template_mem);;
		templateDisk.setValue(template_disk);;
		templateBW.setValue(template_bw);;
		show();
    }

	@UiHandler("buttonOK")
	void handleButtonOK(ClickEvent event) {
		if (presenter.onOK(template_id, getTemplateDesc(), getTemplateNCPUs(), getTemplateMem(), getTemplateDisk(), getTemplateBW())) {
			hide();
		}
	}
	
	@UiHandler("buttonCancel")
	void handleButtonCancel(ClickEvent event) {
		hide();
	}
	
}
