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
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class DeviceCPUServiceModifyViewImpl extends DialogBox implements DeviceCPUServiceModifyView {
	
	private static DeviceCPUServiceModifyViewImplUiBinder uiBinder = GWT.create(DeviceCPUServiceModifyViewImplUiBinder.class);
	
	interface DeviceCPUServiceModifyViewImplUiBinder extends UiBinder<Widget, DeviceCPUServiceModifyViewImpl> {
	}
	
	@UiField TextBox serverName;
	@UiField TextBox cpuName;
	@UiField TextBox accountName;
	@UiField TextBox userName;
	@UiField TextArea csDesc;
	@UiField DeviceDateBox dateBegin;
	@UiField DeviceDateBox dateEnd;
	@UiField IntegerBox csUsed;
	@UiField TextBox dateLife;
	
	private DevicePopupPanel popup = new DevicePopupPanel();
		
	public DeviceCPUServiceModifyViewImpl() {
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
	
	private String getCPUDesc() {
		return getInputText(csDesc);
	}
	
	private String getInputText(TextArea textarea) {
		String text = textarea.getText();
		if (text == null) {
			return "";
		}
		return text;
	}
	
	private DeviceCPUServiceModifyView.Presenter presenter;
	
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
	
	private int cs_id;
	private int cs_reserved;
	private int cs_used;
	
	@Override
	public void popup(int cs_id, String cpu_name, String cs_desc, int cs_reserved, int cs_used, Date cs_starttime, Date cs_endtime, String server_name, String account_name, String user_name) {
		this.cs_id = cs_id;
		this.cs_reserved = cs_reserved + cs_used;
		this.cs_used = cs_used;
		serverName.setValue(server_name);
		cpuName.setValue(cpu_name);
		csDesc.setValue(cs_desc);
		dateBegin.setValue(cs_starttime);
		dateEnd.setValue(cs_endtime);
		csUsed.setValue(cs_used);
		accountName.setValue(account_name);
		userName.setValue(user_name);
		updateDateLife();
		show();
	}

	@UiHandler("buttonOK")
	void handleButtonOK(ClickEvent event) {
		if (presenter.onOK(cs_id, getCPUDesc(), cs_reserved, cs_used, dateBegin.getValue(), dateEnd.getValue())) {
			hide();
		}
	}
	
	@UiHandler("buttonCancel")
	void handleButtonCancel(ClickEvent event) {
		hide();
	}
	
}