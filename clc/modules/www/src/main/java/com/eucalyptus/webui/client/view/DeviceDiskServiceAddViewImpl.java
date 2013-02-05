package com.eucalyptus.webui.client.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.LongBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class DeviceDiskServiceAddViewImpl extends DialogBox implements DeviceDiskServiceAddView {
	
	private static DeviceDiskServiceAddViewImplUiBinder uiBinder = GWT.create(DeviceDiskServiceAddViewImplUiBinder.class);
	
	interface DeviceDiskServiceAddViewImplUiBinder extends UiBinder<Widget, DeviceDiskServiceAddViewImpl> {
	}
	
	@UiField TextBox serverName;
	@UiField TextBox diskName;
	@UiField ListBox accountNameList;
	@UiField ListBox userNameList;
	@UiField TextArea dsDesc;
	@UiField LongBox dsUsed;
	@UiField DeviceDateBox dateBegin;
	@UiField DeviceDateBox dateEnd;
	@UiField TextBox dateLife;
	
	private Map<String, Integer> accountMap = new HashMap<String, Integer>();
    private Map<String, Integer> userMap = new HashMap<String, Integer>();
	
	private DevicePopupPanel popup = new DevicePopupPanel();
		
	public DeviceDiskServiceAddViewImpl() {
		super(false);
		setWidget(uiBinder.createAndBindUi(this));
		accountNameList.addChangeHandler(new ChangeHandler() {

            @Override
            public void onChange(ChangeEvent event) {
                int account_id = getAccountID();
                if (account_id != -1) {
                    presenter.lookupUserNamesByAccountID(account_id);
                }
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
	
    @Override
    public void setAccountNames(Map<String, Integer> account_map) {
        accountNameList.clear();
        userNameList.clear();
        accountMap.clear();
        userMap.clear();
        if (account_map != null && !account_map.isEmpty()) {
            List<String> list = new ArrayList<String>(account_map.keySet());
            Collections.sort(list);
            for (String account_name : list) {
                accountNameList.addItem(account_name);
            }
            accountNameList.setSelectedIndex(0);
            accountMap = account_map;
            int account_id = getAccountID();
            if (account_id != -1) {
                presenter.lookupUserNamesByAccountID(account_id);
            }
        }
    }
    
    @Override
    public void setUserNames(int account_id, Map<String, Integer> user_map) {
        if (getAccountID() == account_id) {
            userNameList.clear();
            userMap.clear();
            if (user_map != null && !user_map.isEmpty()) {
                List<String> list = new ArrayList<String>(user_map.keySet());
                Collections.sort(list);
                for (String user_name : list) {
                    userNameList.addItem(user_name);
                }
                userNameList.setSelectedIndex(0);
                userMap = user_map;
            }
        }
    }
	
	private String getDiskDesc() {
		return getInputText(dsDesc);
	}
	
	private long getDiskUsed() {
	    return dsUsed.getValue();
	}
	
    private int getID(Map<String, Integer> map, String name) {
        if (name == null || name.isEmpty()) {
            return -1;
        }
        Integer id = map.get(name);
        if (id == null) {
            return -1;
        }
        return id;
    }
    
    private int getAccountID() {
        return getID(accountMap, getSelectedText(accountNameList));
    }
    
    private int getUserID() {
        return getID(userMap, getSelectedText(userNameList));
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
	
	private DeviceDiskServiceAddView.Presenter presenter;
	
	@Override
    public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
    }
	
	private int disk_id;
	private long ds_reserved;
	
	@Override
	public void popup(int disk_id, String disk_name, long ds_reserved, String server_name) {
		this.disk_id = disk_id;
		this.ds_reserved = ds_reserved;
		serverName.setValue(server_name);
		diskName.setValue(disk_name);
		dsDesc.setValue("");
		dsUsed.setValue(ds_reserved);
		dateBegin.setValue(new Date());
		dateEnd.setValue(new Date());
		accountNameList.clear();
		userNameList.clear();
		presenter.lookupAccountNames();
		updateDateLife();
		show();
    }

	@UiHandler("buttonOK")
	void handleButtonOK(ClickEvent event) {
		if (presenter.onOK(disk_id, getDiskDesc(), ds_reserved, getDiskUsed(), dateBegin.getValue(), dateEnd.getValue(), getUserID())) {
			hide();
		}
	}
	
	@UiHandler("buttonCancel")
	void handleButtonCancel(ClickEvent event) {
		hide();
	}
	
}
