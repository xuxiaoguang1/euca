package com.eucalyptus.webui.client.view;

import java.util.Date;

import com.eucalyptus.webui.client.view.DeviceDateBox.Handler;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.LongBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class DeviceDiskServiceModifyViewImpl extends DialogBox implements DeviceDiskServiceModifyView {
	
	private static DeviceDiskServiceModifyViewImplUiBinder uiBinder = GWT.create(DeviceDiskServiceModifyViewImplUiBinder.class);
	
	interface DeviceDiskServiceModifyViewImplUiBinder extends UiBinder<Widget, DeviceDiskServiceModifyViewImpl> {
	}
	
	@UiField TextBox serverName;
	@UiField TextBox diskName;
	@UiField TextBox accountName;
	@UiField TextBox userName;
	@UiField TextArea diskDesc;
	@UiField DeviceDateBox dateBegin;
	@UiField DeviceDateBox dateEnd;
	@UiField LongBox diskUsed;
	@UiField IntegerBox dateLife;
	
	private DevicePopupPanel popup = new DevicePopupPanel();
		
	public DeviceDiskServiceModifyViewImpl() {
		super(false);
		setWidget(uiBinder.createAndBindUi(this));
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
	
	private String getDiskDesc() {
		return getInputText(diskDesc);
	}
	
	private String getInputText(TextArea textarea) {
		String text = textarea.getText();
		if (text == null) {
			return "";
		}
		return text;
	}
	
	private DeviceDiskServiceModifyView.Presenter presenter;
	
	@Override
    public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
    }
	
    private int getLife(Date starttime, Date endtime) {
    	final long div = 1000L * 24 * 3600;
    	long start = starttime.getTime() / div, end = endtime.getTime() / div;
    	return start <= end ? (int)(start - end) + 1 : 0;
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
	
	private int ds_id;
	
	@Override
	public void popup(int ds_id, String disk_name, String ds_desc, long ds_used, String ds_starttime, String ds_endtime, String server_name, String account_name, String user_name) {
		this.ds_id = ds_id;
		serverName.setText(server_name);
		diskName.setText(disk_name);
		diskDesc.setText(ds_desc);
		dateBegin.getTextBox().setText(ds_starttime);
		dateEnd.getTextBox().setText(ds_endtime);
		diskUsed.setValue(ds_used);
		accountName.setText(account_name);
		userName.setText(user_name);
		updateDateLife();
		show();
	}

	@UiHandler("buttonOK")
	void handleButtonOK(ClickEvent event) {
		if (presenter.onOK(ds_id, getDiskDesc(), dateBegin.getText(), dateEnd.getText())) {
			hide();
		}
	}
	
	@UiHandler("buttonCancel")
	void handleButtonCancel(ClickEvent event) {
		hide();
	}
	
}
