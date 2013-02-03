package com.eucalyptus.webui.client.view;

import java.util.Date;

import com.eucalyptus.webui.client.activity.device.DeviceDate;
import com.eucalyptus.webui.client.view.DeviceDateBox.Handler;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.LongBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class DeviceMemoryServiceModifyViewImpl extends DialogBox implements DeviceMemoryServiceModifyView {
	
	private static DeviceMemoryServiceModifyViewImplUiBinder uiBinder = GWT.create(DeviceMemoryServiceModifyViewImplUiBinder.class);
	
	interface DeviceMemoryServiceModifyViewImplUiBinder extends UiBinder<Widget, DeviceMemoryServiceModifyViewImpl> {
	}
	
	@UiField TextBox serverName;
	@UiField TextBox memName;
	@UiField TextBox accountName;
	@UiField TextBox userName;
	@UiField TextArea msDesc;
	@UiField DeviceDateBox dateBegin;
	@UiField DeviceDateBox dateEnd;
	@UiField LongBox msUsed;
	@UiField TextBox dateLife;
	
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
	
	private String getMemoryDesc() {
		return getInputText(msDesc);
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
	
	private int ms_id;
	private long ms_reserved;
	private long ms_used;
	
	@Override
	public void popup(int ms_id, String mem_name, String ms_desc, long ms_reserved, long ms_used, Date ms_starttime, Date ms_endtime, String server_name, String account_name, String user_name) {
		this.ms_id = ms_id;
		this.ms_reserved = ms_reserved + ms_used;
		this.ms_used = ms_used;
		serverName.setValue(server_name);
		memName.setValue(mem_name);
		msDesc.setValue(ms_desc);
		dateBegin.setValue(ms_starttime);
		dateEnd.setValue(ms_endtime);
		msUsed.setValue(ms_used);
		accountName.setValue(account_name);
		userName.setValue(user_name);
		updateDateLife();
		show();
	}

	@UiHandler("buttonOK")
	void handleButtonOK(ClickEvent event) {
		if (presenter.onOK(ms_id, getMemoryDesc(), ms_reserved, ms_used, dateBegin.getValue(), dateEnd.getValue())) {
			hide();
		}
	}
	
	@UiHandler("buttonCancel")
	void handleButtonCancel(ClickEvent event) {
		hide();
	}
	
}
