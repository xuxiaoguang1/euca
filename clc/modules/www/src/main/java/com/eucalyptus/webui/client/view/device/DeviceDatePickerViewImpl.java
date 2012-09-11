package com.eucalyptus.webui.client.view.device;

import java.util.Date;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.DatePicker;
import com.google.gwt.user.client.ui.ListBox;

public class DeviceDatePickerViewImpl extends DialogBox implements DeviceDatePickerView {

	private static DeviceDatePickerViewImplUiBinder uiBinder = GWT.create(DeviceDatePickerViewImplUiBinder.class);

	interface DeviceDatePickerViewImplUiBinder extends UiBinder<Widget, DeviceDatePickerViewImpl> {
	}
	
	private Presenter presenter;
	
	public DeviceDatePickerViewImpl() {
		this(null);
	}
	
	public DeviceDatePickerViewImpl(Presenter presenter) {
		super(false);
		setWidget(uiBinder.createAndBindUi(this));
		this.setPopupPosition(300, 100);
		this.presenter = presenter;
	}

	@UiField
	DatePicker datePicker;
	@UiField
	ListBox yearList;
	@UiField
	ListBox monthList;
	@UiField
	ListBox dayList;

	private DeviceServiceDatePicker picker;
	private Label label;

	@Override
	public void setValue(Label label) {
		if (picker == null) {
			picker = new DeviceServiceDatePicker(datePicker, yearList, monthList, dayList);
		}
		this.label = label;

		String text = label.getText();
		Date date = null;
		if (text != null) {
			date = DeviceServiceDatePicker.parse(text);
		}
		if (date == null) {
			date = new Date();
		}
		picker.setDate(picker.safeValue(date));

		show();
	}

	@UiHandler("buttonOK")
	void handleButtonOk(ClickEvent event) {
		label.setText(DeviceServiceDatePicker.format(picker.getValue()));
		this.hide();
		if (presenter != null) {
			presenter.onOK();
		}
	}

	@UiHandler("buttonCancel")
	void handleButtonCancel(ClickEvent event) {
		this.hide();
		if (presenter != null) {
			presenter.onCancel();
		}
	}
	
}
