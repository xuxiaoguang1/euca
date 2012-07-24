package com.eucalyptus.webui.client.view;

import java.util.ArrayList;

import com.eucalyptus.webui.shared.user.GroupInfo;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.event.dom.client.ClickEvent;

public class GroupListViewImpl extends DialogBox implements GroupListView {

	private static GroupListViewImplUiBinder uiBinder = GWT
			.create(GroupListViewImplUiBinder.class);
	@UiField Label lableGrouName;
	@UiField Button buttonOk;
	@UiField Button buttonCancle;
	@UiField ListBox comboGroupList;
	
	private Presenter presenter; 
	
	private static String[] GROUP_LIST_VIEW_CAPTION = {"Group list", "组列表"};

	interface GroupListViewImplUiBinder extends
			UiBinder<Widget, GroupListViewImpl> {
	}

	public GroupListViewImpl() {
		setWidget(uiBinder.createAndBindUi(this));
		setGlassEnabled( true );
		
		setText(GROUP_LIST_VIEW_CAPTION[1]);
	}

	@Override
	public Widget asWidget() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setPresenter(Presenter presenter) {
		// TODO Auto-generated method stub
		this.presenter = presenter;
	}

	@Override
	public void display() {
		// TODO Auto-generated method stub
		this.center();
		this.show();
	}
	@UiHandler("buttonOk")
	void onButtonOkClick(ClickEvent event) {
		hide();
		
		int selectedIndex = this.comboGroupList.getSelectedIndex();
		this.presenter.doAddUserToGroup(Integer.valueOf(this.comboGroupList.getValue(selectedIndex)));
	}
	@UiHandler("buttonCancle")
	void onButtonCancleClick(ClickEvent event) {
		hide();
	}

	@Override
	public void setGroupList(ArrayList<GroupInfo> groupList) {
		// TODO Auto-generated method stub
		
		if (groupList == null)
			return;
		
		this.comboGroupList.clear();
		
		for (GroupInfo group : groupList) {
			this.comboGroupList.addItem(group.getName(), Integer.valueOf(group.getId()).toString());
		}
	}
}
