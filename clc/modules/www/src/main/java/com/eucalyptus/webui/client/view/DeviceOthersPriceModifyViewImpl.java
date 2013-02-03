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

public class DeviceOthersPriceModifyViewImpl extends DialogBox implements DeviceOthersPriceModifyView {

	private static DeviceOthersPriceModifyViewImplUiBinder uiBinder = GWT.create(DeviceOthersPriceModifyViewImplUiBinder.class);
	
	interface DeviceOthersPriceModifyViewImplUiBinder extends UiBinder<Widget, DeviceOthersPriceModifyViewImpl> {
	}

	@UiField Label othersPriceTitle;
	@UiField Anchor othersPriceUnit;
	@UiField DoubleBox othersPrice;
	@UiField TextArea othersPriceDesc;

	public DeviceOthersPriceModifyViewImpl() {
		super(false);
		setWidget(uiBinder.createAndBindUi(this));
		center();
		hide();
	}
	
	private DeviceOthersPriceModifyView.Presenter presenter;
	
    @Override
    public void popup(String title, String price_unit, String op_desc, double op_price, Presenter presenter) {
        if (!isShowing()) {
        	othersPriceTitle.setText(title);
            othersPriceUnit.setText(price_unit);
            if (op_desc == null) {
                op_desc = "";
            }
            othersPriceDesc.setText(op_desc);
            othersPrice.setValue(op_price);
            this.presenter = presenter;
            show();
        }
    }
    
    private double getOthersPriceValue() {
        return othersPrice.getValue();
    }
    
    private String getOthersPriceDesc() {
        String text = othersPriceDesc.getText();
        if (text == null) {
            return "";
        }
        return text;
    }

	@UiHandler("buttonOK")
	void handleButtonOK(ClickEvent event) {
	    if (presenter.onOK(getOthersPriceDesc(), getOthersPriceValue())) {
	        hide();
	    }
	}
	
	@UiHandler("buttonCancel")
	void handleButtonCancel(ClickEvent event) {
		hide();
	}

}
