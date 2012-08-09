package com.eucalyptus.webui.client.view;

import java.util.Date;
import java.util.List;

import com.eucalyptus.webui.client.service.SearchResultRow;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.datepicker.client.DatePicker;
import com.google.gwt.user.client.ui.ListBox;

public class DeviceCPUServiceAddViewImpl extends DialogBox implements DeviceCPUServiceAddView {

	private static DeviceCPUServiceAddViewImplUiBinder uiBinder = GWT.create(DeviceCPUServiceAddViewImplUiBinder.class);
	
	interface DeviceCPUServiceAddViewImplUiBinder extends UiBinder<Widget, DeviceCPUServiceAddViewImpl> {
	}

	@UiField
	DatePicker datePickerStarttime;
	@UiField
	ListBox yearListStarttime;
	@UiField
	ListBox monthListStarttime;
	@UiField
	ListBox dayListStarttime;
	@UiField
	DatePicker datePickerEndtime;
	@UiField
	ListBox yearListEndtime;
	@UiField
	ListBox monthListEndtime;
	@UiField
	ListBox dayListEndtime;
	@UiField
	ListBox accountList;
	@UiField
	ListBox userList;
	@UiField
	ListBox stateList;

	public DeviceCPUServiceAddViewImpl(String[] stateValueList) {
		super(false);
		setWidget(uiBinder.createAndBindUi(this));
		for (String s : stateValueList) {
			stateList.addItem(s);
		}
	}

	private DeviceCPUServiceAddView.Presenter presenter;

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	private SearchResultRow selected;
	private DeviceServiceDatePicker pickerStarttime;
	private DeviceServiceDatePicker pickerEndtime;
	
	private static final Date today = new Date();
	
	@Override
	public void setValue(SearchResultRow row, Date starttime, Date endtime, String state) {
		if (pickerStarttime == null) {
			pickerStarttime = new DeviceServiceDatePicker(datePickerStarttime, yearListStarttime, monthListStarttime,
			        dayListStarttime);
			pickerStarttime.setDate(today);
		}
		if (starttime != null) {
			pickerStarttime.setDate(pickerStarttime.safeValue(starttime));
		}

		if (pickerEndtime == null) {
			pickerEndtime = new DeviceServiceDatePicker(datePickerEndtime, yearListEndtime, monthListEndtime,
			        dayListEndtime);
			pickerEndtime.setDate(today);
		}
		if (endtime != null) {
			pickerEndtime.setDate(pickerEndtime.safeValue(endtime));
		}
		
		if (state != null) {
			for (int i = 0; i < stateList.getItemCount(); i ++) {
				if (state.equals(stateList.getItemText(i))) {
					stateList.setSelectedIndex(i);
					break;
				}
			}
		}
		
		if (cache.getAccounts() == null) {
			presenter.lookupAccounts();
		}

		selected = row;
		show();
	}

	@UiHandler("accountList")
	void handleAccountListChange(ChangeEvent event) {
		String account = accountList.getItemText(accountList.getSelectedIndex());
		List<String> list = cache.getUsersByAccount(account);
		if (list == null) {
			presenter.lookupUserByAccount(account);
		}
		else {
			userList.clear();
			for (String s : list) {
				userList.addItem(s);
			}
		}
	}

	@Override
	public void setAccountList(List<String> list) {
		cache.setKey(list);
		accountList.clear();
		if (list.size() != 0) {
			for (String s : list) {
				accountList.addItem(s);
			}
			accountList.setSelectedIndex(0);
			presenter.lookupUserByAccount(accountList.getItemText(0));
		}
		userList.clear();
	}

	@Override
	public void setUserList(String account, List<String> list) {
		if (!cache.setValue(account, list)) {
			return;
		}
		int index = accountList.getSelectedIndex();
		if (index != -1 && accountList.getItemText(index).equals(account)) {
			userList.clear();
			if (list.size() != 0) {
				for (String s : list) {
					userList.addItem(s);
				}
				userList.setSelectedIndex(0);
			}
		}
	}

	private DeviceAccountsDataCache cache = new DeviceAccountsDataCache();

	@Override
	public void clearCache() {
		accountList.clear();
		userList.clear();
		cache = new DeviceAccountsDataCache();
	}
	
	@UiHandler("buttonOK")
	void handleButtonOK(ClickEvent event) {
		if (presenter.onOK(selected, getSelectedItem(accountList), getSelectedItem(userList),
				pickerStarttime.getValue(), pickerEndtime.getValue(), getSelectedItem(stateList))) {
			this.hide();
		}
	}
	
	private String getSelectedItem(ListBox box) {
		int index;
		if ((index = box.getSelectedIndex()) != -1) {
			return box.getItemText(index);
		}
		return null;
	}

	@UiHandler("buttonCancel")
	void handleButtonCancel(ClickEvent event) {
		this.hide();
		presenter.onCancel();
	}
	
}
