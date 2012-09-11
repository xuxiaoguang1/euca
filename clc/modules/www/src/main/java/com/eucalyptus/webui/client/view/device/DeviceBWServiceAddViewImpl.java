package com.eucalyptus.webui.client.view.device;

import java.util.Date;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.LongBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.datepicker.client.DatePicker;
import com.google.gwt.user.client.ui.ListBox;

public class DeviceBWServiceAddViewImpl extends DialogBox implements DeviceBWServiceAddView {

	private static DeviceBWServiceAddViewImplUiBinder uiBinder = GWT.create(DeviceBWServiceAddViewImplUiBinder.class);
	
	interface DeviceBWServiceAddViewImplUiBinder extends UiBinder<Widget, DeviceBWServiceAddViewImpl> {
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
	ListBox ipList;
	@UiField
	LongBox bandwidthBox;

	public DeviceBWServiceAddViewImpl() {
		super(false);
		setWidget(uiBinder.createAndBindUi(this));
	}
	
	private DeviceBWServiceAddView.Presenter presenter;

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	private DeviceServiceDatePicker pickerStarttime;
	private DeviceServiceDatePicker pickerEndtime;
	
	private static final Date today = new Date();
	
	class LongBoxChangeHandler implements ChangeHandler {
		
		LongBox box;
		
		LongBoxChangeHandler(LongBox box) {
			this.box = box;
			box.setText(Long.toString(0));
		}

		@Override
        public void onChange(ChangeEvent event) {
			String value = Long.toString(longValue(box.getText()));
			if (!value.equals(box.getText())) {
				box.setText(value);
			}
        }
		
	}
	
	private Long longValue(String s) {
		try {
			return Long.parseLong(s);
		}
		catch (Exception e) {
			return (long)0;
		}
	}
	
	private boolean initialized = false;
	
	private void init() {
		if (initialized) {
			return;
		}
		initialized = true;
		bandwidthBox.addChangeHandler(new LongBoxChangeHandler(bandwidthBox));
	}
	
	@Override
	public void popup() {
		init();
		if (pickerStarttime == null) {
			pickerStarttime = new DeviceServiceDatePicker(datePickerStarttime, yearListStarttime, monthListStarttime,
			        dayListStarttime);
			pickerStarttime.setDate(today);
		}

		if (pickerEndtime == null) {
			pickerEndtime = new DeviceServiceDatePicker(datePickerEndtime, yearListEndtime, monthListEndtime,
			        dayListEndtime);
			pickerEndtime.setDate(today);
		}
		
		if (cache.getAccounts() == null) {
			presenter.lookupAccounts();
		}
		
		String account = getSelectedItem(accountList);
		if (account != null) {
			String user = getSelectedItem(userList);
			if (user != null) {
				presenter.lookupIPsByUser(account, user);
			}
		}
		
		bandwidthBox.setText(Long.toString(longValue(bandwidthBox.getText())));
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
			if (list.size() != 0) {
				for (String s : list) {
					userList.addItem(s);
				}
				userList.setSelectedIndex(0);
				presenter.lookupIPsByUser(account, userList.getItemText(0));
			}
		}
	}
	
	@UiHandler("userList")
	void handleUserListChange(ChangeEvent event) {
		String account = accountList.getItemText(accountList.getSelectedIndex());
		String user = userList.getItemText(userList.getSelectedIndex());
		presenter.lookupIPsByUser(account, user);
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
				presenter.lookupIPsByUser(account, userList.getItemText(0));
			}
		}
	}
	
	@Override
	public void setIPList(String account, String user, List<String> list) {
		int index;
		index = accountList.getSelectedIndex();
		if (index == -1 || !accountList.getItemText(index).equals(account)) {
			return;
		}
		index = userList.getSelectedIndex();
		if (index == -1 || !userList.getItemText(index).equals(user)) {
			return;
		}
		ipList.clear();
		if (list.size() != 0) {
			for (String s : list) {
				ipList.addItem(s);
			}
			ipList.setSelectedIndex(0);
		}
	}

	private DeviceAccountsDataCache cache = new DeviceAccountsDataCache();

	@UiHandler("buttonOK")
	void handleButtonOK(ClickEvent event) {
		if (presenter.onOK(getSelectedItem(accountList), getSelectedItem(userList),
				pickerStarttime.getValue(), pickerEndtime.getValue(), getSelectedItem(ipList),
				longValue(bandwidthBox.getText()))) {
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
