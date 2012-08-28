package com.eucalyptus.webui.client.view;

import java.util.ArrayList;

import com.eucalyptus.webui.shared.user.AccountInfo;
import com.eucalyptus.webui.shared.user.EnumState;
import com.eucalyptus.webui.shared.user.EnumUserType;
import com.eucalyptus.webui.shared.user.UserInfo;
import com.google.common.base.Strings;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Label;

public class UserAddViewImpl extends DialogBox implements UserAddView {

	private static UserAddViewImplUiBinder uiBinder = GWT
			.create(UserAddViewImplUiBinder.class);
	@UiField TextBox nameInput;
	@UiField TextBox mobileInput;
	@UiField TextBox emailInput;
	@UiField TextBox titleInput;
	@UiField TextBox userIDInput;
	
	@UiField Button okButton;
	@UiField Button cancleButton;
	
	@UiField RadioButton userTypeAdminRadio;
	@UiField RadioButton userTypeUserRadio;
	
	@UiField RadioButton userStateNormalRadio;
	@UiField RadioButton userStatePauseRadio;
	@UiField RadioButton userStateBanRadio;
	@UiField ListBox accountCombo;
	@UiField Label accountLable;
	
	
	EnumUserType userType = EnumUserType.USER;
	EnumState userState = EnumState.NORMAL;
	
	Presenter presenter;
	
	private static String[] USER_ADD_VIEW_CAPTION = {"Add users", "增加用户"};
	
	interface UserAddViewImplUiBinder extends UiBinder<Widget, UserAddViewImpl> {
	}

	public UserAddViewImpl() {
		setWidget(uiBinder.createAndBindUi(this));
		
		this.userIDInput.setVisible(false);
		
		userTypeAdminRadio.setTitle(EnumUserType.ADMIN.toString());
		userTypeUserRadio.setTitle(EnumUserType.USER.toString());
		
		this.userType = EnumUserType.USER;
		userTypeUserRadio.setValue(true);
		
		userStateNormalRadio.setTitle(EnumState.NORMAL.toString());
		userStatePauseRadio.setTitle(EnumState.PAUSE.toString());
		userStateBanRadio.setTitle(EnumState.BAN.toString());
		
		this.userState = EnumState.NORMAL;
		userStateNormalRadio.setValue(true);
		
		setText(USER_ADD_VIEW_CAPTION[1]);
		
		setGlassEnabled( true );
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
		Document.get( ).getElementById( nameInput.getName() ).focus( );
	}
	
	@Override
	public void display() {
		// TODO Auto-generated method stub
		clearInputField();
		
		this.center();
		this.show();
	}
	

	@Override
	public void setAccountsInfo(ArrayList<AccountInfo> accounts) {
		// TODO Auto-generated method stub
		if (accounts == null)
			return;
		
		this.accountLable.setVisible(true);
		this.accountCombo.setVisible(true);
		for (AccountInfo account : accounts) {
			this.accountCombo.addItem(account.getName(), Integer.valueOf(account.getId()).toString());
		}
	}
	
	@Override
	public void setUser(UserInfo user) {
		// TODO Auto-generated method stub
		this.userIDInput.setText(Integer.toString(user.getId()));
		this.nameInput.setText(user.getName());
		this.titleInput.setText(user.getTitle());
		this.mobileInput.setText(user.getMobile());
		this.emailInput.setText(user.getEmail());
		
		this.userTypeAdminRadio.setValue(false);
		this.userTypeUserRadio.setValue(false);
		
		switch (user.getType()) {
		case ADMIN:
			this.userTypeAdminRadio.setValue(true);
			this.userTypeUserRadio.setValue(false);
		break;
		
		case USER:
			this.userTypeAdminRadio.setValue(false);
			this.userTypeUserRadio.setValue(true);
		break;
		}
		
		this.userStateNormalRadio.setValue(false);
		this.userStatePauseRadio.setValue(false);
		this.userStateBanRadio.setValue(false);
		
		switch (user.getState()) {
		case NORMAL:
			this.userStateNormalRadio.setValue(true);
			this.userStatePauseRadio.setValue(false);
			this.userStateBanRadio.setValue(false);
			
			break;
		
		case PAUSE:
			this.userStateNormalRadio.setValue(false);
			this.userStatePauseRadio.setValue(true);
			this.userStateBanRadio.setValue(false);
			
			break;
		
		case BAN:
			this.userStateNormalRadio.setValue(false);
			this.userStatePauseRadio.setValue(false);
			this.userStateBanRadio.setValue(true);
		
			break;
		}
		
		String accountId = Integer.toString(user.getAccountId());
		
		int itemCount = this.accountCombo.getItemCount();
		
		for (int i=0; i<itemCount; i++) {
			if (this.accountCombo.getValue(i).equals(accountId)) {
				this.accountCombo.setSelectedIndex(i);
				break;
			}
		}
	}
	
	private void clearInputField() {
		this.accountCombo.clear();
		this.accountLable.setVisible(false);
		this.accountCombo.setVisible(false);
		
		this.userIDInput.setText("");
		this.nameInput.setText("");
		this.titleInput.setText("");
		this.emailInput.setText("");
		this.mobileInput.setText("");
	}
	
	@Override
	public void setPresenter(Presenter presenter) {
		// TODO Auto-generated method stub
		this.presenter = presenter;
	}
	
	@UiHandler("okButton")
	void onBUTTON_OKClick(ClickEvent event) {
		
		UserInfo user = checkValues();
		
		if (user != null) {
			presenter.process(user);
		}
		
		hide();
	}

	@UiHandler("cancleButton")
	void onBUTTON_CANCLEClick(ClickEvent event) {
		hide();
	}
	
	private UserInfo checkValues( ) {
		
	    String name = nameInput.getValue();
	    if (Strings.isNullOrEmpty(name)) {
	    	return null;
	    }
	    
	    String title = titleInput.getValue();
	    if (Strings.isNullOrEmpty(title)) {
	    	return null;
	    }
	    
	    String mobile = mobileInput.getValue();
	    if (Strings.isNullOrEmpty(mobile)) {
	    	return null;
	    }
	    
	    String email = emailInput.getValue();
	    if (Strings.isNullOrEmpty(email)) {
	    	return null;
	    }
	    
	    String accountId = null;
	    
	    if (this.accountCombo.isVisible())
	    	accountId = this.accountCombo.getValue(this.accountCombo.getSelectedIndex());
	    
	    UserInfo user = new UserInfo();
	    
	    String userId = this.userIDInput.getText();
	    if (!Strings.isNullOrEmpty(userId))
	    	user.setId(Integer.valueOf(userId));
	    
	    user.setName(name);
	    user.setTitle(title);
	    user.setMobile(mobile);
	    user.setEmail(email);
	    user.setType(userType);
	    user.setState(userState);
	    
	    if (accountId != null)
	    	user.setAccountId(Integer.valueOf(accountId));
	    
	    return user;
	}
	
	@UiHandler("userTypeAdminRadio")
	void onuserTypeAdminRadioClick(ClickEvent event) {
		userType = EnumUserType.ADMIN;
	}
	@UiHandler("userTypeUserRadio")
	void onuserTypeUserRadioClick(ClickEvent event) {
		userType = EnumUserType.USER;
	}
	
	@UiHandler("userStateNormalRadio")
	void onuserStateNormalRadioClick(ClickEvent event) {
		userState = EnumState.NORMAL;
	}
	@UiHandler("userStatePauseRadio")
	void onuserStatePauseRadioClick(ClickEvent event) {
		userState = EnumState.PAUSE;
	}
	@UiHandler("userStateBanRadio")
	void onuserStateBanRadioClick(ClickEvent event) {
		userState = EnumState.BAN;
	}
}
