package com.eucalyptus.webui.client.view;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.uibinder.client.UiField;

public class DeviceCabinetModifyViewImpl extends DialogBox implements DeviceCabinetModifyView {

	private static DeviceCabinetModifyViewImplUiBinder uiBinder = GWT.create(DeviceCabinetModifyViewImplUiBinder.class);
	
	interface DeviceCabinetModifyViewImplUiBinder extends UiBinder<Widget, DeviceCabinetModifyViewImpl> {
	}
	
	@UiField TextBox cabinetName;
	@UiField TextArea cabinetDesc;

	public DeviceCabinetModifyViewImpl() {
		super(false);
		setWidget(uiBinder.createAndBindUi(this));
		center();
		hide();
	}
	
	private DeviceCabinetModifyView.Presenter presenter;

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}
	
	private int cabinet_id;
	
	@Override
	public void popup(int cabinet_id, String cabinet_name, String cabinet_desc) {
		this.cabinet_id = cabinet_id;
		cabinetName.setText(cabinet_name);
		cabinetDesc.setText(cabinet_desc);
		show();
	}
	
	private String getCabinetDesc() {
		String cabinet_desc = cabinetDesc.getValue();
		if (cabinet_desc == null) {
			return "";
		}
		return cabinet_desc;
	}

	@UiHandler("buttonOK")
	void handleButtonOK(ClickEvent event) {
		if (presenter.onOK(cabinet_id, getCabinetDesc())) {
			hide();
		}
	}
	
	@UiHandler("buttonCancel")
	void handleButtonCancel(ClickEvent event) {
		hide();
	}

}
