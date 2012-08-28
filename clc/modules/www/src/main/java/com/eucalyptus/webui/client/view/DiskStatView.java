package com.eucalyptus.webui.client.view;

import com.google.gwt.user.client.ui.IsWidget;

public interface DiskStatView extends IsWidget, CanDisplaySearchResult {

	void setPresenter(Presenter presenter);

	public interface Presenter extends SearchRangeChangeHandler, KnowsPageSize{


	}

}
