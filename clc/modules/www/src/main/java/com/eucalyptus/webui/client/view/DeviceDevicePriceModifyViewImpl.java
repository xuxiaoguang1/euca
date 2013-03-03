package com.eucalyptus.webui.client.view;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DoubleBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.uibinder.client.UiField;

public class DeviceDevicePriceModifyViewImpl extends DialogBox implements DeviceDevicePriceModifyView {

	private static DeviceDevicePriceModifyViewImplUiBinder uiBinder = GWT.create(DeviceDevicePriceModifyViewImplUiBinder.class);
	
	interface DeviceDevicePriceModifyViewImplUiBinder extends UiBinder<Widget, DeviceDevicePriceModifyViewImpl> {
	}

	@UiField Label devicePriceTitle;
	@UiField Anchor devicePriceUnit;
	@UiField DoubleBox devicePrice;
	@UiField TextArea devicePriceDesc;

	public DeviceDevicePriceModifyViewImpl() {
		super(false);
		setWidget(uiBinder.createAndBindUi(this));
		center();
		hide();
	}
	
	private DeviceDevicePriceModifyView.Presenter presenter;
	
    @Override
    public void popup(String title, String price_unit, String op_desc, double op_price, Presenter presenter) {
        if (!isShowing()) {
        	devicePriceTitle.setText(title);
            devicePriceUnit.setText(price_unit);
            if (op_desc == null) {
                op_desc = "";
            }
            devicePriceDesc.setText(op_desc);
            devicePrice.setValue(op_price);
            this.presenter = presenter;
            show();
        }
    }
    
    private double getDevicePriceValue() {
        return devicePrice.getValue();
    }
    
    private String getDevicePriceDesc() {
        String text = devicePriceDesc.getText();
        if (text == null) {
            return "";
        }
        return text;
    }

	@UiHandler("buttonOK")
	void handleButtonOK(ClickEvent event) {
	    if (presenter.onOK(getDevicePriceDesc(), getDevicePriceValue())) {
	        hide();
	    }
	}
	
	@UiHandler("buttonCancel")
	void handleButtonCancel(ClickEvent event) {
		hide();
	}

}
