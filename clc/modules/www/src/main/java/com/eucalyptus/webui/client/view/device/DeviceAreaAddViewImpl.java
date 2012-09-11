package com.eucalyptus.webui.client.view.device;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.uibinder.client.UiField;

public class DeviceAreaAddViewImpl extends DialogBox implements DeviceAreaAddView {

	private static DeviceAreaAddViewImplUiBinder uiBinder = GWT.create(DeviceAreaAddViewImplUiBinder.class);
	
	interface DeviceAreaAddViewImplUiBinder extends UiBinder<Widget, DeviceAreaAddViewImpl> {
	}
	
	@UiField TextBox areaName;
	@UiField TextArea areaDesc;
	
	public DeviceAreaAddViewImpl() {
		super(false);
		setWidget(uiBinder.createAndBindUi(this));
		center();
		hide();
	}
	
	private DeviceAreaAddView.Presenter presenter;

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}
	
	@Override
	public void popup() {
		areaName.setValue("");
		areaDesc.setValue("");
		show();
	}
	
	private String getAreaName() {
		String area_name = areaName.getValue();
		if (area_name == null) {
			return "";
		}
		return area_name;
	}
	
	private String getAreaDesc() {
		String area_desc = areaDesc.getValue();
		if (area_desc == null) {
			return "";
		}
		return area_desc;
	}

	@UiHandler("buttonOK")
	void handleButtonOK(ClickEvent event) {
		if (presenter.onOK(getAreaName(), getAreaDesc())) {
			hide();
		}
	}
	
	@UiHandler("buttonCancel")
	void handleButtonCancel(ClickEvent event) {
		hide();
	}

}
