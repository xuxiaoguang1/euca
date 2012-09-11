package com.eucalyptus.webui.client.view;

import java.util.Date;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.datepicker.client.DatePicker;

public class DeviceServiceDatePicker {
	
	DatePicker datePicker;
	ListBox yearList;
	ListBox monthList;
	ListBox dayList;
	
	public DeviceServiceDatePicker(DatePicker datePicker, ListBox yearList, ListBox monthList, ListBox dayList) {
		this.datePicker = datePicker;
		this.yearList = yearList;
		this.monthList = monthList;
		this.dayList = dayList;
		
		ValueChangeHandler<Date> handler0 = new ValueChangeHandler<Date>() {

			@Override
            public void onValueChange(ValueChangeEvent<Date> event) {
				onDatePickerChange();
            }
			
		};
		
		ChangeHandler handler1 = new ChangeHandler() {
			
			@Override
			public void onChange(ChangeEvent event) {
				onDateListChange();
			}
			
		};
		
		datePicker.addValueChangeHandler(handler0);
		yearList.addChangeHandler(handler1);
		monthList.addChangeHandler(handler1);
		dayList.addChangeHandler(handler1);
		
		for (int i = YEAR_MIN; i <= YEAR_MAX; i ++) {
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
	}
	
	private void onDatePickerChange() {
		Date date = datePicker.getValue();
		if (checkYear(date)) {
			selected = date;
			updateDateList();
		}
		else {
			datePicker.setValue(selected);
		}
	}
	
	private void onDateListChange() {
		int year = yearList.getSelectedIndex() + YEAR_MIN;
		int month = monthList.getSelectedIndex() + 1;
		updateDayList(year, month);
		int day = dayList.getSelectedIndex() + 1;
		selected = getDate(year, month, day);
		updateDatePicker();
	}
	
	private static final int YEAR_MIN = 2010;
	private static final int YEAR_MAX = 2030;
	
	private Date selected = new Date();
	
	private void updateDatePicker() {
		datePicker.setCurrentMonth(selected);
		datePicker.setValue(selected);
	}
	
	private void updateDateList() {
		int year = getYear(selected);
		int month = getMonth(selected);
		updateDayList(year, month);
		yearList.setSelectedIndex(year - YEAR_MIN);
		monthList.setSelectedIndex(month - 1);
		dayList.setSelectedIndex(getDay(selected) - 1);
	}
	
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
	
	private int getYear(Date date) {
		return Integer.parseInt(DateTimeFormat.getFormat("yyyy").format(date));
	}

	private int getMonth(Date date) {
		return Integer.parseInt(DateTimeFormat.getFormat("M").format(date));
	}

	private int getDay(Date date) {
		return Integer.parseInt(DateTimeFormat.getFormat("d").format(date));
	}
	
	private boolean checkYear(Date date) {
		int year = getYear(date);
		return year >= YEAR_MIN && year <= YEAR_MAX;
	}
	
	public Date safeValue(Date date) {
		if (date == null || !checkYear(date)) {
			return new Date();
		}
		return date;
	}
	
	public void setDate(Date date) {
		selected = date;
		updateDatePicker();
		updateDateList();
	}
	
	public Date getValue() {
		return selected;
	}
	
	public static String format(Date date) {
		return DateTimeFormat.getFormat("yyyy-MM-dd").format(date);
	}
	
	public static Date parse(String date) {
		return parse(date, "0");
	}
	
	public static final long DAY_MILLIS = 1L * 24 * 60 * 60 * 1000;
	
	public static Date parse(String date, String add) {
		try {
			Date result = DateTimeFormat.getFormat("yyyy-MM-dd").parse(date);
			return new Date(result.getTime() + DAY_MILLIS * Integer.parseInt(add));
		}
		catch (Exception e) {
			return null;
		}
	}

}
