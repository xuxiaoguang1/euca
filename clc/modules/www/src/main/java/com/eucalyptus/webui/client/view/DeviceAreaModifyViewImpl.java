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

public class DeviceAreaModifyViewImpl extends DialogBox implements DeviceAreaModifyView {

	private static DeviceAreaModifyViewImplUiBinder uiBinder = GWT.create(DeviceAreaModifyViewImplUiBinder.class);
	
	interface DeviceAreaModifyViewImplUiBinder extends UiBinder<Widget, DeviceAreaModifyViewImpl> {
	}
	
	@UiField TextBox areaName;
	@UiField TextArea areaDesc;

	public DeviceAreaModifyViewImpl() {
		super(false);
		setWidget(uiBinder.createAndBindUi(this));
		center();
		hide();
	}
	
	private DeviceAreaModifyView.Presenter presenter;

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}
	
	private int area_id;
	
	@Override
	public void popup(int area_id, String area_name, String area_desc) {
		this.area_id = area_id;
		areaName.setText(area_name);
		areaDesc.setText(area_desc);
		show();
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
		if (presenter.onOK(area_id, getAreaDesc())) {
			hide();
		}
	}
	
	@UiHandler("buttonCancel")
	void handleButtonCancel(ClickEvent event) {
		hide();
	}

}
