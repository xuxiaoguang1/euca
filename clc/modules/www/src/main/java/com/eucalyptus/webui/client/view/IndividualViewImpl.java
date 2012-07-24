package com.eucalyptus.webui.client.view;

import com.eucalyptus.webui.shared.user.LoginUserProfile;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.event.dom.client.ClickEvent;

public class IndividualViewImpl extends Composite implements IndividualView {

	private static IndividualViewImplUiBinder uiBinder = GWT
			.create(IndividualViewImplUiBinder.class);
	@UiField TextBox inputName;
	@UiField TextBox inputMobile;
	@UiField TextBox inputEmail;
	
	@UiField TextBox inputOldPwd;
	@UiField TextBox inputNewPwd;
	@UiField TextBox inputNewPwdAgain;
	
	@UiField Button buttonInfo;
	@UiField Button buttonPwd;

	interface IndividualViewImplUiBinder extends
			UiBinder<Widget, IndividualViewImpl> {
	}

	public IndividualViewImpl() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	@Override
	public void setPresenter(Presenter presenter) {
		// TODO Auto-generated method stub
		this.presenter = presenter;
	}
	
	@Override
	public void setLoginUserProfile(LoginUserProfile profile) {
		// TODO Auto-generated method stub
		this.inputName.setText(profile.getUserTitle());
		this.inputMobile.setText(profile.getUserMobile());
		this.inputEmail.setText(profile.getUserEmail());
		
		this.profile = profile;
	}
	
	private Presenter presenter;

	@UiHandler("buttonInfo")
	void onButtonInfoClick(ClickEvent event) {		
		this.presenter.onUpdateInfo(this.inputName.getValue(), this.inputMobile.getValue(), this.inputEmail.getValue());
	}
	
	@UiHandler("buttonPwd")
	void onButtonPwdClick(ClickEvent event) {
		this.presenter.onChangePwd(this.inputOldPwd.getValue(), this.inputNewPwd.getValue(), this.inputNewPwdAgain.getValue());
	}
	
	LoginUserProfile profile;

	@Override
	public void clearPwd() {
		// TODO Auto-generated method stub
		this.inputOldPwd.setValue(null);
		this.inputNewPwd.setValue(null);
		this.inputNewPwdAgain.setValue(null);
	}
}
