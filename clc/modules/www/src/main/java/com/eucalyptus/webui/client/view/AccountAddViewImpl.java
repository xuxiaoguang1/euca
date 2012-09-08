package com.eucalyptus.webui.client.view;

import com.eucalyptus.webui.client.ClientFactory;
import com.eucalyptus.webui.client.service.LanguageSelection;
import com.eucalyptus.webui.client.view.FooterView.StatusType;
import com.eucalyptus.webui.shared.checker.InvalidValueException;
import com.eucalyptus.webui.shared.checker.ValueCheckerFactory;
import com.eucalyptus.webui.shared.user.AccountInfo;
import com.eucalyptus.webui.shared.user.EnumState;
import com.google.common.base.Strings;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.RadioButton;

public class AccountAddViewImpl extends DialogBox implements AccountAddView {

	private static AccountAddViewImplUiBinder uiBinder = GWT
			.create(AccountAddViewImplUiBinder.class);
	@UiField Button okButton;
	@UiField Button cancleButton;
	@UiField TextBox idInput;
	@UiField TextBox nameInput;
	@UiField TextBox emailInput;
	@UiField TextArea descriptionInput;
	@UiField RadioButton accountStateNormalRadio;
	@UiField RadioButton accountStatePauseRadio;
	@UiField RadioButton accountStateBanRadio;
	
	Presenter presenter;
	ClientFactory clientFactory;
	
	private static String[] ACCOUNT_ADD_VIEW_CAPTION = {"Add account", "增加账户"};

	interface AccountAddViewImplUiBinder extends
			UiBinder<Widget, AccountAddViewImpl> {
	}

	public AccountAddViewImpl() {
		setWidget(uiBinder.createAndBindUi(this));
		
		this.idInput.setVisible(false);
		
		accountStateNormalRadio.setTitle(EnumState.NORMAL.toString());
		accountStatePauseRadio.setTitle(EnumState.PAUSE.toString());
		accountStateBanRadio.setTitle(EnumState.BAN.toString());
		
		this.accountStateNormalRadio.setValue(true);
		
		setGlassEnabled( true );
	}

	public AccountAddViewImpl(String firstName) {
		setWidget(uiBinder.createAndBindUi(this));
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
		Document.get().getElementById(nameInput.getName()).focus();
	}

	@Override
	public void display(ClientFactory clientFactory) {
		// TODO Auto-generated method stub
		this.clientFactory = clientFactory;
		
		int lan = LanguageSelection.instance().getCurLanguage().ordinal();
		
		setText(ACCOUNT_ADD_VIEW_CAPTION[lan]);
		
		clearInputField();
		center();
		show();
	}

	@Override
	public void setPresenter(Presenter presenter) {
		// TODO Auto-generated method stub
		this.presenter = presenter;
	}
	
	@Override
	public void setAccount(AccountInfo account) {
		// TODO Auto-generated method stub
		this.idInput.setText(Integer.toString(account.getId()));
		this.nameInput.setText(account.getName());
		this.emailInput.setText(account.getEmail());
		this.descriptionInput.setText(account.getDescription());
		
		this.accountStateNormalRadio.setValue(false);
		this.accountStatePauseRadio.setValue(false);
		this.accountStateBanRadio.setValue(false);
		
		switch (account.getState()) {
		case NORMAL:
			this.accountStateNormalRadio.setValue(true);
			this.accountStatePauseRadio.setValue(false);
			this.accountStateBanRadio.setValue(false);
			
			break;
		
		case PAUSE:
			this.accountStateNormalRadio.setValue(false);
			this.accountStatePauseRadio.setValue(true);
			this.accountStateBanRadio.setValue(false);
			
			break;
		
		case BAN:
			this.accountStateNormalRadio.setValue(false);
			this.accountStatePauseRadio.setValue(false);
			this.accountStateBanRadio.setValue(true);
		
			break;
		}
	}

	@UiHandler("okButton")
	void onOkButtonClick(ClickEvent event) {
		try {
			AccountInfo account = checkValues();
			hide();
			
			if (account != null) {
				presenter.process(account);
			}
		}
		catch (InvalidValueException e) {
	    	clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.ERROR, e.getMessage(), FooterView.DEFAULT_STATUS_CLEAR_DELAY );
	    }		
	}
	
	@UiHandler("cancleButton")
	void onCancleButtonClick(ClickEvent event) {
		clearInputField();
		hide();
	}
	
	private void clearInputField() {
		this.nameInput.setText("");
		this.nameInput.setText("");
		this.descriptionInput.setText("");
		
		this.accountStateNormalRadio.setValue(true);
		this.accountStateBanRadio.setValue(false);
		this.accountStatePauseRadio.setValue(false);
	}
	
	private AccountInfo checkValues( ) throws InvalidValueException {
		
	    String name = nameInput.getValue();
	    if (Strings.isNullOrEmpty(name)) {
	    	return null;
	    }
	    
	    try {
	    	String email = ValueCheckerFactory.createEmailChecker().check(this.emailInput.getText());
	    	
	    	String description = descriptionInput.getValue();
	    
		    AccountInfo account = new AccountInfo();
		    
		    String accountId = this.idInput.getValue();
		    
		    if (!Strings.isNullOrEmpty(accountId))
		    	account.setId(Integer.parseInt(accountId));
		    
		    account.setName(name);
		    account.setEmail(email);
		    account.setDescription(description);
		    
		    if (this.accountStateNormalRadio.getValue() == true)
		    	account.setState(EnumState.NORMAL);
		    else if (this.accountStatePauseRadio.getValue() == true)
		    	account.setState(EnumState.PAUSE);
		    else if (this.accountStateBanRadio.getValue() == true)
		    	account.setState(EnumState.BAN);
		    else
		    	account.setState(EnumState.NONE);
		    
		    return account;
	    }
	    catch (InvalidValueException e) {
	    	throw e;
	    }
	}
}
