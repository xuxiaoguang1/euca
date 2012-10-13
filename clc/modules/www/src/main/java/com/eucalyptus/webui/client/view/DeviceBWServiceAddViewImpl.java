package com.eucalyptus.webui.client.view;

import java.util.Date;
import java.util.List;

import com.eucalyptus.webui.client.view.DeviceDateBox.Handler;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;

public class DeviceBWServiceAddViewImpl extends DialogBox implements DeviceBWServiceAddView {
	
	private static DeviceBWServiceAddViewImplUiBinder uiBinder = GWT.create(DeviceBWServiceAddViewImplUiBinder.class);
	
	interface DeviceBWServiceAddViewImplUiBinder extends UiBinder<Widget, DeviceBWServiceAddViewImpl> {
	}

	@UiField ListBox ipAddrList;
	@UiField ListBox accountNameList;
	@UiField ListBox userNameList;
	@UiField TextArea bwDesc;
	@UiField IntegerBox bwMax;
	@UiField DeviceDateBox dateBegin;
	@UiField DeviceDateBox dateEnd;
	@UiField IntegerBox dateLife;
	
	private DevicePopupPanel popup = new DevicePopupPanel();
		
	public DeviceBWServiceAddViewImpl() {
		super(false);
		setWidget(uiBinder.createAndBindUi(this));
		accountNameList.addChangeHandler(new ChangeHandler() {

			@Override
			public void onChange(ChangeEvent event) {
				String account_name = getAccountName();
				if (!isEmpty(account_name)) {
					presenter.lookupUserNamesByAccountName(account_name);
				}
			}
			
		});
		userNameList.addChangeHandler(new ChangeHandler() {

			@Override
			public void onChange(ChangeEvent event) {
				presenter.lookupAddrByUserName(getAccountName(), getUserName());
			}
			
		});
		for (final DeviceDateBox dateBox : new DeviceDateBox[]{dateBegin, dateEnd}) {
			dateBox.setErrorHandler(new Handler() {

				@Override
				public void onErrorHappens() {
					updateDateLife();
					int x = dateBox.getAbsoluteLeft();
		            int y = dateBox.getAbsoluteTop() + dateBox.getOffsetHeight();
					popup.setHTML(x, y, "15EM", "3EM", DeviceDateBox.getDateErrorHTML(dateBox));
				}

				@Override
				public void onValueChanged() {
					updateDateLife();
	            	int x = dateBox.getAbsoluteLeft();
		            int y = dateBox.getAbsoluteTop() + dateBox.getOffsetHeight();
	                DeviceDateBox pair;
	                pair = (dateBox != dateBegin ? dateBegin : dateEnd);
	                if (!pair.hasError()) {
	                	Date date0 = dateBegin.getValue(), date1 = dateEnd.getValue();
	                	if (date0 != null && date1 != null) {
	                		if (date0.getTime() > date1.getTime()) {
	                			popup.setHTML(x, y, "12EM", "2EM", DeviceDateBox.getDateErrorHTML(dateBegin, dateEnd));
	                			return;
	                		}
	                	}
	                }
				}
			});
		}
		center();
		hide();
	}
	
	private String getAccountName() {
		return getSelectedText(accountNameList); 
	}
	
	private String getUserName() {
		return getSelectedText(userNameList); 
	}
	
	private String getIPAddr() {
		return getSelectedText(ipAddrList);
	}
	
	private String getBWDesc() {
		return getInputText(bwDesc);
	}
	
	private String getBWMax() {
		return getInputText(bwMax);
	}
	
	private String getInputText(IntegerBox textbox) {
		String text = textbox.getText();
		if (text == null) {
			return "";
		}
		return text;
	}
	
	private String getInputText(TextArea textarea) {
		String text = textarea.getText();
		if (text == null) {
			return "";
		}
		return text;
	}

	private String getSelectedText(ListBox listbox) {
	    int index = listbox.getSelectedIndex();
	    if (index == -1) {
	    	return "";
	    }
	    return listbox.getItemText(index);
	}
	
	private boolean isEmpty(String s) {
	    return s == null || s.length() == 0;
	}
	
    private int getLife(Date starttime, Date endtime) {
    	final long div = 1000L * 24 * 3600;
    	long start = starttime.getTime() / div, end = endtime.getTime() / div;
    	return start <= end ? (int)(end - start) + 1 : 0;
    }
	
	public void updateDateLife() {
		dateLife.setText("");
		try {
			Date starttime = DeviceDateBox.parse(dateBegin.getText());
			Date endtime = DeviceDateBox.parse(dateEnd.getText());
			int days1 = getLife(starttime, endtime);
			int days2 = getLife(new Date(), endtime);
			if (days1 < days2) {
				dateLife.setText(Integer.toString(days1));
			}
			else {
				dateLife.setText(Integer.toString(days1) + "/" + Integer.toString(days2));
			}
		}
		catch (Exception e) {
		}
	}
	
	private DeviceBWServiceAddView.Presenter presenter;
	
	@Override
    public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
    }
	
	@Override
	public void popup() {
		bwMax.setValue(0);
		bwDesc.setText("");
		dateBegin.setValue(new Date());
		dateEnd.getTextBox().setText("");
		ipAddrList.clear();
		accountNameList.clear();
		userNameList.clear();
		presenter.lookupAccountNames();
		updateDateLife();
		show();
    }

	@UiHandler("buttonOK")
	void handleButtonOK(ClickEvent event) {
		if (presenter.onOK(getIPAddr(), getBWDesc(), getBWMax(), dateBegin.getText(), dateEnd.getText())) {
			hide();
		}
	}
	
	@UiHandler("buttonCancel")
	void handleButtonCancel(ClickEvent event) {
		hide();
	}
	
    @Override
    public void setAccountNameList(List<String> account_name_list) {
    	setListBox(accountNameList, account_name_list, true);
    	setListBox(userNameList, null, true);
    	setListBox(ipAddrList, null, false);
    	String account_name = getAccountName();
        if (!isEmpty(account_name)) {
        	presenter.lookupUserNamesByAccountName(account_name);
        }
        else {
        	presenter.lookupAddrByUserName("", "");
        }
    }
    
    @Override
    public void setUserNameList(String account_name, List<String> user_name_list) {
        if (getAccountName().equals(account_name)) {
	        setListBox(userNameList, user_name_list, true);
	        presenter.lookupAddrByUserName(account_name, getUserName());
        }
    }
    
	@Override
	public void setIPAddrList(List<String> ip_addr_list, String account_name, String user_name) {
		if (getAccountName().equals(account_name) && getUserName().equals(user_name)) {
			setListBox(ipAddrList, ip_addr_list, false);
		}
	}
	
    private void setListBox(ListBox listbox, List<String> values, boolean hasEmpty) {
    	listbox.clear();
    	if (values != null && !values.isEmpty()) {
	    	for (String value : values) {
	    		listbox.addItem(value);
	    	}
	    	listbox.setSelectedIndex(hasEmpty ? -1 : 0);
    	}
    }

}
