package com.eucalyptus.webui.client.view;

import com.google.common.base.Strings;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class CertAddViewImpl extends DialogBox implements CertAddView {

	private static CertAddViewImplUiBinder uiBinder = GWT
			.create(CertAddViewImplUiBinder.class);

	interface CertAddViewImplUiBinder extends UiBinder<Widget, CertAddViewImpl> {
	}
	
	@UiField
	TextBox pemInput;
	
	@UiField
	Button okButton;
	
	@UiField
	Button cancelButton;
	
	Presenter presenter;

	public CertAddViewImpl() {
		setWidget(uiBinder.createAndBindUi(this));
		setGlassEnabled(true);
	}
	
	@UiHandler("okButton")
	void onButtonOKClick(ClickEvent e) {
		String pem = pemInput.getText();
		if(!Strings.isNullOrEmpty(pem)){
			presenter.processAddCert(pem);
		}
		hide();
	}

	@UiHandler("cancelButton")
	void onButtonCancelClick(ClickEvent e) {
		hide();
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public void display(String caption) {
		this.setText(caption);
		pemInput.setText("");
		
		center( );
		show( );
	}

}
