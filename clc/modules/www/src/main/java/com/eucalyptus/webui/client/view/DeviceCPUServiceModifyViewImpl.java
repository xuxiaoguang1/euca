package com.eucalyptus.webui.client.view;

import java.util.Date;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.DatePicker;
import com.google.gwt.user.client.ui.ListBox;

public class DeviceCPUServiceModifyViewImpl extends DialogBox implements DeviceServiceExtendView {

	private static DeviceCPUServiceModifyViewImplUiBinder uiBinder = GWT.create(DeviceCPUServiceModifyViewImplUiBinder.class);
	
	interface DeviceCPUServiceModifyViewImplUiBinder extends UiBinder<Widget, DeviceCPUServiceModifyViewImpl> {
	}
	
	private Presenter presenter;
	
	public DeviceCPUServiceModifyViewImpl() {
		super(false);
		setWidget(uiBinder.createAndBindUi(this));
		for (int i = YEAR_MIN; i<= YEAR_MAX; i ++) {
			yearList.addItem(Integer.toString(i));
		}
		for (int i = 1; i <= 12; i ++) {
			if (i < 10) {
				monthList.addItem("0" + Integer.toString(i));
			}
			else {
				monthList.addItem(Integer.toString(i));
			}
		}
		for (int i = 1; i <= 31; i ++) {
			if (i < 10) {
				dayList.addItem("0" + Integer.toString(i));
			}
			else {
				dayList.addItem(Integer.toString(i));
			}
		}
		this.setPopupPosition(300, 100);
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}
	
	@UiField DatePicker datePicker;
	@UiField ListBox yearList;
	@UiField ListBox monthList;
	@UiField ListBox dayList;
	@UiField ListBox stateList;
	
	private static final int YEAR_MIN = 2010;
	private static final int YEAR_MAX = 2030;
	
	private Date dateSelected = new Date();
	
	private void updateDayList(int year, int month) {
		for (int i = dayList.getItemCount(); i > 28 && getDate(year, month, i) == null; i --) {
			dayList.removeItem(i - 1);
		}
		for (int i = dayList.getItemCount() + 1; i <= 31 && getDate(year, month, i) != null; i ++) {
			dayList.addItem(Integer.toString(i));
		}
		if (dayList.getSelectedIndex() == -1) {
			dayList.setSelectedIndex(dayList.getItemCount() - 1);
		}
	}
	
	@UiHandler("yearList")
	void handleYearListChange(ChangeEvent event) {
		handleDateListChange();
	}
	
	@UiHandler("monthList")
	void handleMonthListChange(ChangeEvent event) {
		handleDateListChange();
	}
	
	@UiHandler("dayList")
	void handleDayListChange(ChangeEvent event) {
		handleDateListChange();
	}
	
	private void handleDateListChange() {
		int year = yearList.getSelectedIndex() + YEAR_MIN;
		int month = monthList.getSelectedIndex() + 1;
		updateDayList(year, month);
		int day = dayList.getSelectedIndex() + 1;
		dateSelected = getDate(year, month, day);
		updateDatePicker();
	}
	
	private void updateDatePicker() {
		datePicker.setCurrentMonth(dateSelected);
		datePicker.setValue(dateSelected);
	}
	
	private void updateDateList() {
		int year = getYear(dateSelected);
		int month = getMonth(dateSelected);
		updateDayList(year, month);
		yearList.setSelectedIndex(year - YEAR_MIN);
		monthList.setSelectedIndex(month - 1);
		dayList.setSelectedIndex(getDay(dateSelected) - 1);
	}
	
	public static Date getDate(String sdate, int add) {
		try {
			Date date = DateTimeFormat.getFormat("yyyy-MM-dd").parse(sdate);
			return new Date(date.getTime() + (long)add * 24 * 60 * 60 * 1000);
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private Date getDate(int year, int month, int day) {
		StringBuilder sb = new StringBuilder();
		sb.append(year);
		if (month < 10) {
			sb.append("0");
		}
		sb.append(month);
		if (day < 10) {
			sb.append("0");
		}
		sb.append(day);
		try {
			Date date = DateTimeFormat.getFormat("yyyyMMdd").parse(sb.toString());
			if (day == getDay(date) && month == getMonth(date) && year == getYear(date)) {
				return date;
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private String getSelectedDate() {
		return DateTimeFormat.getFormat("yyyy-MM-dd").format(dateSelected);
	}
	
	private boolean checkYear(Date date) {
		int year = getYear(date);
		return year >= YEAR_MIN && year <= YEAR_MAX;
	}
	
	@UiHandler("datePicker")
	void handleValueChange(ValueChangeEvent<Date> event) {
		Date date = event.getValue();
		if (checkYear(date)) {
			dateSelected = date;
			updateDateList();
		}
		else {
			datePicker.setValue(dateSelected);
		}
	}

	private int getYear(Date date) {
		return Integer.parseInt(DateTimeFormat.getFormat("yyyy").format(date));
	}
	
	private int getMonth(Date date) {
		return Integer.parseInt(DateTimeFormat.getFormat("M").format(date));
	}
	
	private int getDay(Date date) {
		return Integer.parseInt(DateTimeFormat.getFormat("d").format(date));
	}

	@Override
	public void setValue(Date date, String[] stateValueList, int stateSelected) {
		if (date == null || !checkYear(date)) {
			date = new Date();
		}
		dateSelected = date;
		updateDateList();
		updateDatePicker();
		
		stateList.clear();
		for (int i = 0; i < stateValueList.length; i ++) {
			stateList.addItem(stateValueList[i]);
		}
		stateList.setSelectedIndex(stateSelected);
		show();
	}
	
	@Override
	public void setValue(Date date, String[] stateValueList, String state) {
		int selected = -1;
		for (int i = 0; i < stateValueList.length; i ++) {
			if (stateValueList[i].equals(state)) {
				selected = i;
				break ;
			}
		}
		setValue(date, stateValueList, selected);
	}
	
	@UiHandler("stateList")
	void handleStateListChange(ChangeEvent event) {
		/* do nothing */
	}
	
	@UiHandler("buttonOK")
	void handleButtonOk(ClickEvent event) {
		this.hide();
		presenter.onOK(getSelectedDate(), stateList.getItemText(stateList.getSelectedIndex()));
	}
	
	@UiHandler("buttonCancel")
	void handleButtonCancel(ClickEvent event) {
		this.hide();
		presenter.onCancel();
	}
	
}
