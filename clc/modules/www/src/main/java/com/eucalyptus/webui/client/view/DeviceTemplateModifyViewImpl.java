package com.eucalyptus.webui.client.view;

import com.eucalyptus.webui.client.service.SearchResultRow;
import com.eucalyptus.webui.server.DeviceTemplateServiceProcImpl;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.uibinder.client.UiField;

public class DeviceTemplateModifyViewImpl extends DialogBox implements DeviceTemplateModifyView {

	private static DeviceTemplateModifyViewImplUiBinder uiBinder = GWT.create(DeviceTemplateModifyViewImplUiBinder.class);
	
	interface DeviceTemplateModifyViewImplUiBinder extends UiBinder<Widget, DeviceTemplateModifyViewImpl> {
	}
	
	@UiField
	Label markLabel;
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

	public DeviceTemplateModifyViewImpl() {
		super(false);
		setWidget(uiBinder.createAndBindUi(this));
	}

	private DeviceTemplateModifyView.Presenter presenter;

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}
	
	SearchResultRow selected;

	@Override
	public void popup(SearchResultRow row) {
		selected = row;
		markLabel.setText(selected.getField(DeviceTemplateServiceProcImpl.TABLE_COL_INDEX_MARK));
		cpuText.setText(selected.getField(DeviceTemplateServiceProcImpl.TABLE_COL_INDEX_CPU));
		memText.setText(selected.getField(DeviceTemplateServiceProcImpl.TABLE_COL_INDEX_MEM));
		diskText.setText(selected.getField(DeviceTemplateServiceProcImpl.TABLE_COL_INDEX_DISK));
		bwText.setText(selected.getField(DeviceTemplateServiceProcImpl.TABLE_COL_INDEX_BW));
		imageText.setText(selected.getField(DeviceTemplateServiceProcImpl.TABLE_COL_INDEX_IMAGE));
		show();
	}

	@UiHandler("buttonOK")
	void handleButtonOK(ClickEvent event) {
		if (presenter.onOK(selected, cpuText.getText(), memText.getText(),
				diskText.getText(), bwText.getText(), imageText.getText())) {
			this.hide();
		}
	}
	
	@UiHandler("buttonCancel")
	void handleButtonCancel(ClickEvent event) {
		presenter.onCancel();
		this.hide();
	}

}
