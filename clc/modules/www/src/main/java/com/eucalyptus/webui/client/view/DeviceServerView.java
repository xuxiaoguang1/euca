package com.eucalyptus.webui.client.view;

import com.google.gwt.user.client.ui.IsWidget;

public interface DeviceServerView extends IsWidget, CanDisplaySearchResult, Clearable, SelectionController {

	void setPresenter(Presenter presenter);

	public interface Presenter extends SearchRangeChangeHandler, MultiSelectionChangeHandler, KnowsPageSize {
		
		void onAdd();
		
		void onSecretKey();
		
		void onPower();
		
		void onConnect();
		
		void onExtend();
		
		void onModify();
		
		void onDelete();
		
	}

}
