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

public class DeviceServiceModifyViewImpl extends DialogBox implements DeviceServiceModifyView {

	private static DeviceServiceModifyViewImplUiBinder uiBinder = GWT.create(DeviceServiceModifyViewImplUiBinder.class);

	interface DeviceServiceModifyViewImplUiBinder extends UiBinder<Widget, DeviceServiceModifyViewImpl> {
	}

	private DeviceServiceModifyView.Presenter presenter;

	public DeviceServiceModifyViewImpl() {
		super(false);
		setWidget(uiBinder.createAndBindUi(this));
		this.setPopupPosition(300, 100);
	}

	@Override
	public void setPresenter(DeviceServiceModifyView.Presenter presenter) {
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
	@UiField
	ListBox stateList;

	private Date starttime;

	private SearchResultRow selected;
	private DeviceServiceDatePicker picker;

	@Override
	public void setValue(SearchResultRow row, Date starttime, Date date, String[] stateValueList, int stateSelected) {
		if (picker == null) {
			picker = new DeviceServiceDatePicker(datePicker, yearList, monthList, dayList);
		}
		this.starttime = picker.safeValue(starttime);
		picker.setDate(picker.safeValue(date));

		stateList.clear();
		for (int i = 0; i < stateValueList.length; i ++) {
			stateList.addItem(stateValueList[i]);
		}
		stateList.setSelectedIndex(stateSelected);
		
		selected = row;
		show();
	}

	@Override
	public void setValue(SearchResultRow row, Date starttime, Date date, String[] stateValueList, String state) {
		int selected = -1;
		for (int i = 0; i < stateValueList.length; i ++) {
			if (stateValueList[i].equals(state)) {
				selected = i;
				break;
			}
		}
		setValue(row, starttime, date, stateValueList, selected);
	}

	@UiHandler("buttonOK")
	void handleButtonOK(ClickEvent event) {
		Date endtime = picker.getValue();
		String state = stateList.getItemText(stateList.getSelectedIndex());
		if (presenter.onOK(selected, starttime, endtime, state)) {
			this.hide();
		}
	}

	@UiHandler("buttonCancel")
	void handleButtonCancel(ClickEvent event) {
		this.hide();
		presenter.onCancel();
	}

}
