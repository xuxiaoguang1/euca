package com.eucalyptus.webui.client.view;

import java.util.ArrayList;

import com.eucalyptus.webui.shared.user.AccountInfo;
import com.eucalyptus.webui.shared.user.EnumState;
import com.eucalyptus.webui.shared.user.GroupInfo;
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
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RadioButton;

public class GroupAddViewImpl extends DialogBox implements GroupAddView {

	private static GroupAddViewImplUiBinder uiBinder = GWT
			.create(GroupAddViewImplUiBinder.class);
	@UiField Button okButton;
	@UiField Button cancleButton;
	@UiField TextBox nameInput;
	@UiField TextArea descriptionInput;
	@UiField Label accountLable;
	@UiField ListBox accountCombo;
	@UiField RadioButton groupStateNormalRadio;
	@UiField RadioButton groupStatePauseRadio;
	@UiField RadioButton groupStateBanRadio;
	@UiField TextBox groupIdInput;
	
	Presenter presenter;
	
	private static String[] GROUP_ADD_VIEW_CAPTION = {"Add groups", "增加用户组"};

	interface GroupAddViewImplUiBinder extends
			UiBinder<Widget, GroupAddViewImpl> {
	}

	public GroupAddViewImpl() {
		setWidget(uiBinder.createAndBindUi(this));
		
		this.groupIdInput.setVisible(false);
		
		groupStateNormalRadio.setTitle(EnumState.NORMAL.toString());
		groupStatePauseRadio.setTitle(EnumState.PAUSE.toString());
		groupStateBanRadio.setTitle(EnumState.BAN.toString());
		
		this.groupStateNormalRadio.setValue(true);
		
		setText(GROUP_ADD_VIEW_CAPTION[1]);
		
		setGlassEnabled( true );
	}

	public GroupAddViewImpl(String firstName) {
		setWidget(uiBinder.createAndBindUi(this));
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
		Document.get().getElementById(nameInput.getName()).focus();
	}

	@Override
	public void display() {
		// TODO Auto-generated method stub
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
	public void setAccountsInfo(ArrayList<AccountInfo> accounts) {
		// TODO Auto-generated method stub
		if (accounts == null)
			return;
		this.accountCombo.clear();
				
		for (AccountInfo account : accounts) {
			this.accountCombo.addItem(account.getName(), Integer.valueOf(account.getId()).toString());
		}
		
		this.accountLable.setVisible(true);
		this.accountCombo.setVisible(true);
	}
	
	@Override
	public void setGroup(GroupInfo group) {
		// TODO Auto-generated method stub
		this.groupIdInput.setText(Integer.toString(group.getId()));
		this.nameInput.setText(group.getName());
		this.descriptionInput.setText(group.getDescription());
		
		this.groupStateNormalRadio.setValue(false);
		this.groupStatePauseRadio.setValue(false);
		this.groupStateBanRadio.setValue(false);
		
		switch (group.getState()) {
		case NORMAL:
			this.groupStateNormalRadio.setValue(true);
			this.groupStatePauseRadio.setValue(false);
			this.groupStateBanRadio.setValue(false);
			
			break;
		
		case PAUSE:
			this.groupStateNormalRadio.setValue(false);
			this.groupStatePauseRadio.setValue(true);
			this.groupStateBanRadio.setValue(false);
			
			break;
		
		case BAN:
			this.groupStateNormalRadio.setValue(false);
			this.groupStatePauseRadio.setValue(false);
			this.groupStateBanRadio.setValue(true);
		
			break;
			
		default:
			break;
		}
		
		String accountId = Integer.toString(group.getAccountId());
		
		int itemCount = this.accountCombo.getItemCount();
		
		for (int i=0; i<itemCount; i++) {
			if (this.accountCombo.getValue(i).equals(accountId)) {
				this.accountCombo.setSelectedIndex(i);
				break;
			}
		}
	}

	@UiHandler("okButton")
	void onOkButtonClick(ClickEvent event) {
		hide();
		
		GroupInfo group = checkValues();
		if (group != null) {
			presenter.process(group);
		}
	}
	
	@UiHandler("cancleButton")
	void onCancleButtonClick(ClickEvent event) {
		hide();
	}
	
	private void clearInputField() {
		this.accountLable.setVisible(false);
		this.accountCombo.setVisible(false);
		
		this.nameInput.setText("");
		this.descriptionInput.setText("");;
	}
	
	private GroupInfo checkValues( ) {
		
	    String name = nameInput.getValue();
	    if (Strings.isNullOrEmpty(name)) {
	    	return null;
	    }
	    
	    String description = descriptionInput.getValue();
	    
	    GroupInfo group = new GroupInfo();
	    
	    String groupId = this.groupIdInput.getValue();
	    
	    if (!Strings.isNullOrEmpty(groupId))
	    	group.setId(Integer.parseInt(groupId));
	    
	    group.setName(name);
	    group.setDescription(description);
	    
	    if (this.groupStateNormalRadio.getValue() == true)
	    	group.setState(EnumState.NORMAL);
	    else if (this.groupStatePauseRadio.getValue() == true)
	    	group.setState(EnumState.PAUSE);
	    else if (this.groupStateBanRadio.getValue() == true)
	    	group.setState(EnumState.BAN);
	    else
	    	group.setState(EnumState.NONE);
	    
	    String accountId = null;
	    
	    if (this.accountCombo.isVisible())
	    	accountId = this.accountCombo.getValue(this.accountCombo.getSelectedIndex());
	    
	    if (accountId != null)
	    	group.setAccountId(Integer.valueOf(accountId));
	    
	    return group;
	}
}
