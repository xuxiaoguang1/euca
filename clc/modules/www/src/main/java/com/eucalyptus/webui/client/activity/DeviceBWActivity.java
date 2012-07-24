package com.eucalyptus.webui.client.activity;

import java.util.ArrayList;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.eucalyptus.webui.client.ClientFactory;
import com.eucalyptus.webui.client.place.SearchPlace;
import com.eucalyptus.webui.client.service.EucalyptusServiceAsync;
import com.eucalyptus.webui.client.service.SearchRange;
import com.eucalyptus.webui.client.service.SearchResult;
import com.eucalyptus.webui.client.service.SearchResultRow;
import com.eucalyptus.webui.client.session.Session;
import com.eucalyptus.webui.client.view.DeviceBWView;
import com.eucalyptus.webui.client.view.HasValueWidget;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class DeviceBWActivity extends AbstractSearchActivity implements DeviceBWView.Presenter {

	private static final int LAN_SELECT = 1;

	private static final Logger LOG = Logger.getLogger(DeviceBWActivity.class.getName());

	public static final String TITLE[] = {"BWs", "带宽"};

	public DeviceBWActivity(SearchPlace place, ClientFactory clientFactory) {
		super(place, clientFactory);
	}

	private EucalyptusServiceAsync getBackendService() {
		return clientFactory.getBackendService();
	}

	private Session getSession() {
		return clientFactory.getLocalSession().getSession();
	}

	@Override
	protected void doSearch(String query, SearchRange range) {
		getBackendService().lookupDeviceBW(getSession(), query, range, new AsyncCallback<SearchResult>() {

			@Override
			public void onFailure(Throwable caught) {
				ActivityUtil.logoutForInvalidSession(clientFactory, caught);
				LOG.log(Level.WARNING, "search failed: " + caught);
				displayData(null);
			}

			@Override
			public void onSuccess(SearchResult result) {
				LOG.log(Level.INFO, "search success: " + result);
				displayData(result);
			}

		});
	}

	@Override
	protected String getTitle() {
		return TITLE[LAN_SELECT];
	}

	@Override
	protected void showView(SearchResult result) {
		DeviceBWView view = (DeviceBWView)this.view;
		if (view == null) {
			view = clientFactory.getDeviceBWView();
			view.setPresenter(this);
			container.setWidget(view);
			view.clear();
			this.view = view;
		}
		view.showSearchResult(result);
	}

	@Override
	public void onSelectionChange(Set<SearchResultRow> selections) {
		// TODO Auto-generated method stub
		//System.err.println(Debug.footprint());
	}

	@Override
	public void saveValue(ArrayList<String> keys, ArrayList<HasValueWidget> values) {
		// TODO Auto-generated method stub
		//System.err.println(Debug.footprint());
	}

}
