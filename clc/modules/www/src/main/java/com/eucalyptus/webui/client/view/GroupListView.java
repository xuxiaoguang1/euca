package com.eucalyptus.webui.client.view;

import java.util.ArrayList;

import com.eucalyptus.webui.shared.user.GroupInfo;
import com.google.gwt.user.client.ui.IsWidget;

public interface GroupListView extends IsWidget {
	void setPresenter( Presenter presenter );
	void setGroupList(ArrayList<GroupInfo> groupList);
	
	void display();
	
	public interface Presenter {
		void doAddUserToGroup(int groupId);
	}
}
