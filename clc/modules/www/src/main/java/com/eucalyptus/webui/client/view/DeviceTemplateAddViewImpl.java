package com.eucalyptus.webui.client.view;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.uibinder.client.UiField;

public class DeviceTemplateAddViewImpl extends DialogBox implements DeviceTemplateAddView {

	private static DeviceTemplateServiceAddViewImplUiBinder uiBinder = GWT.create(DeviceTemplateServiceAddViewImplUiBinder.class);
	
	interface DeviceTemplateServiceAddViewImplUiBinder extends UiBinder<Widget, DeviceTemplateAddViewImpl> {
	}
	
	@UiField
	TextBox markText;
	@UiField
	TextBox cpuText;
	@UiField
	TextBox memText;
	@UiField
	TextBox diskText;
	@UiField
	TextBox bwText;
	@UiField
	TextBox imageText;

	public DeviceTemplateAddViewImpl() {
		super(false);
		setWidget(uiBinder.createAndBindUi(this));
	}

	private DeviceTemplateAddView.Presenter presenter;

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public void popup() {
		markText.setText("");
		cpuText.setText("");
		memText.setText("");
		diskText.setText("");
		bwText.setText("");
		imageText.setText("");
		show();
	}

	@UiHandler("buttonOK")
	void handleButtonOK(ClickEvent event) {
		if (presenter.onOK(markText.getText(), cpuText.getText(), memText.getText(),
				diskText.getText(), bwText.getText(), imageText.getText())) {
			this.hide();
		}
	}
	
	@UiHandler("buttonCancel")
	void handleButtonCancel(ClickEvent event) {
		this.hide();
	}

}
