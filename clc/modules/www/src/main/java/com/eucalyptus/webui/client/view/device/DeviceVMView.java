package com.eucalyptus.webui.client.view.device;

import com.eucalyptus.webui.client.view.CanDisplaySearchResult;
import com.eucalyptus.webui.client.view.Clearable;
import com.eucalyptus.webui.client.view.KnowsPageSize;
import com.eucalyptus.webui.client.view.MultiSelectionChangeHandler;
import com.eucalyptus.webui.client.view.SearchRangeChangeHandler;
import com.eucalyptus.webui.client.view.SelectionController;
import com.google.gwt.user.client.ui.IsWidget;

public interface DeviceVMView extends IsWidget, CanDisplaySearchResult, Clearable, SelectionController {

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
