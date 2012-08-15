package com.eucalyptus.webui.client.view;

import com.google.gwt.user.client.ui.IsWidget;

public interface CertView extends IsWidget, CanDisplaySearchResult, Clearable, SelectionController {

	void setPresenter(Presenter presenter);

	public interface Presenter extends SearchRangeChangeHandler, MultiSelectionChangeHandler, KnowsPageSize {

		void onDeleteCert();

		void onActivateCert();

		void onDeactivateCert();

		void onRevokeCert();

		void onAuthorizeCert();

	}

}
