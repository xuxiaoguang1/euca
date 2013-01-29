package com.eucalyptus.webui.client.view;

import java.util.Date;
import java.util.List;

import com.eucalyptus.webui.client.activity.device.DeviceDate;
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
import com.google.gwt.user.client.ui.TextBox;
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
	@UiField TextBox dateLife;
	
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
				else {
					userNameList.clear();
					presenter.lookupAddrByUserName(getAccountName(), getUserName());
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
					popup.setHTML(x, y, "30EM", "3EM", DeviceDateBox.getDateErrorHTML(dateBox));
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
	                			popup.setHTML(x, y, "20EM", "2EM", DeviceDateBox.getDateErrorHTML(dateBegin, dateEnd));
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
	
	private int getBWMax() {
		return bwMax.getValue();
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
	
	public void updateDateLife() {
		dateLife.setText("");
		try {
			if (!isEmpty(dateBegin.getText()) && !isEmpty(dateEnd.getText())) {
				int life = DeviceDate.calcLife(dateEnd.getText(), dateBegin.getText());
				if (life > 0) {
					int real = Math.max(0, Math.min(life, DeviceDate.calcLife(dateEnd.getText(), DeviceDate.today())));
					if (real != life) {
						dateLife.setText(Integer.toString(real) + "/" + Integer.toString(life));
					}
					else {
						dateLife.setText(Integer.toString(life));
					}
				}
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
		bwDesc.setValue("");
		dateBegin.setValue(new Date());
		dateEnd.setValue(new Date());
		ipAddrList.clear();
		accountNameList.clear();
		userNameList.clear();
		presenter.lookupAccountNames();
		updateDateLife();
		show();
    }

	@UiHandler("buttonOK")
	void handleButtonOK(ClickEvent event) {
		if (presenter.onOK(getIPAddr(), getBWDesc(), getBWMax(), dateBegin.getValue(), dateEnd.getValue())) {
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
    		if (hasEmpty) {
    			listbox.addItem("");
    		}
	    	for (String value : values) {
	    		listbox.addItem(value);
	    	}
	    	listbox.setSelectedIndex(0);
    	}
    }

}
