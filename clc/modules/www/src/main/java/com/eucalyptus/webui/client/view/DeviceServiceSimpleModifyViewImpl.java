package com.eucalyptus.webui.client.view;

import java.util.Date;

import com.eucalyptus.webui.client.service.SearchResultRow;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.DatePicker;
import com.google.gwt.user.client.ui.ListBox;

public class DeviceServiceSimpleModifyViewImpl extends DialogBox implements DeviceServiceSimpleModifyView {

	private static DeviceServiceModifyViewImplUiBinder uiBinder = GWT.create(DeviceServiceModifyViewImplUiBinder.class);

	interface DeviceServiceModifyViewImplUiBinder extends UiBinder<Widget, DeviceServiceSimpleModifyViewImpl> {
	}

	private DeviceServiceSimpleModifyView.Presenter presenter;

	public DeviceServiceSimpleModifyViewImpl() {
		super(false);
		setWidget(uiBinder.createAndBindUi(this));
		this.setPopupPosition(300, 100);
	}

	@Override
	public void setPresenter(DeviceServiceSimpleModifyView.Presenter presenter) {
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

	private Date starttime;

	private SearchResultRow selected;
	private DeviceServiceDatePicker picker;

	@Override
	public void setValue(SearchResultRow row, Date starttime, Date date) {
		if (picker == null) {
			picker = new DeviceServiceDatePicker(datePicker, yearList, monthList, dayList);
		}
		this.starttime = picker.safeValue(starttime);
		picker.setDate(picker.safeValue(date));

		selected = row;
		show();
	}

	@UiHandler("buttonOK")
	void handleButtonOK(ClickEvent event) {
		Date endtime = picker.getValue();
		if (presenter.onOK(selected, starttime, endtime)) {
			this.hide();
		}
	}

	@UiHandler("buttonCancel")
	void handleButtonCancel(ClickEvent event) {
		this.hide();
		presenter.onCancel();
	}

}
