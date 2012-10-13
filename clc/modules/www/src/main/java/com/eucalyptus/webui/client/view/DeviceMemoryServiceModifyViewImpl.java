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

public class DeviceMemoryServiceModifyViewImpl extends DialogBox implements DeviceMemoryServiceModifyView {
	
	private static DeviceMemoryServiceModifyViewImplUiBinder uiBinder = GWT.create(DeviceMemoryServiceModifyViewImplUiBinder.class);
	
	interface DeviceMemoryServiceModifyViewImplUiBinder extends UiBinder<Widget, DeviceMemoryServiceModifyViewImpl> {
	}
	
	@UiField TextBox serverName;
	@UiField TextBox memoryName;
	@UiField TextBox accountName;
	@UiField TextBox userName;
	@UiField TextArea memoryDesc;
	@UiField DeviceDateBox dateBegin;
	@UiField DeviceDateBox dateEnd;
	@UiField LongBox memoryUsed;
	@UiField IntegerBox dateLife;
	
	private DevicePopupPanel popup = new DevicePopupPanel();
		
	public DeviceMemoryServiceModifyViewImpl() {
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
	
	private String getMemoryDesc() {
		return getInputText(memoryDesc);
	}
	
	private String getInputText(TextArea textarea) {
		String text = textarea.getText();
		if (text == null) {
			return "";
		}
		return text;
	}
	
	private DeviceMemoryServiceModifyView.Presenter presenter;
	
	@Override
    public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
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
	
	private int ms_id;
	
	@Override
	public void popup(int ms_id, String memory_name, String ms_desc, long ms_used, String ms_starttime, String ms_endtime, String server_name, String account_name, String user_name) {
		this.ms_id = ms_id;
		serverName.setText(server_name);
		memoryName.setText(memory_name);
		memoryDesc.setText(ms_desc);
		dateBegin.getTextBox().setText(ms_starttime);
		dateEnd.getTextBox().setText(ms_endtime);
		memoryUsed.setValue(ms_used);
		accountName.setText(account_name);
		userName.setText(user_name);
		updateDateLife();
		show();
	}

	@UiHandler("buttonOK")
	void handleButtonOK(ClickEvent event) {
		if (presenter.onOK(ms_id, getMemoryDesc(), dateBegin.getText(), dateEnd.getText())) {
			hide();
		}
	}
	
	@UiHandler("buttonCancel")
	void handleButtonCancel(ClickEvent event) {
		hide();
	}
	
}
