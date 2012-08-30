package com.eucalyptus.webui.client.activity;

import java.util.ArrayList;

import com.eucalyptus.webui.client.ClientFactory;
import com.eucalyptus.webui.client.place.SearchPlace;
import com.eucalyptus.webui.client.service.SearchRange;
import com.eucalyptus.webui.client.service.SearchResult;
import com.eucalyptus.webui.client.view.DiskStatView;
import com.eucalyptus.webui.client.view.HasValueWidget;

public class DiskStatActivity extends AbstractSearchActivity implements
		DiskStatView.Presenter {

	public static final String TITLE = "DISK_STAT";

	public DiskStatActivity(SearchPlace place, ClientFactory clientFactory) {
		super(place, clientFactory);
	}

	@Override
	public void saveValue(ArrayList<String> keys, ArrayList<HasValueWidget> values) {
	}

	@Override
	protected void doSearch(String query, SearchRange range) {
//		displayData(null);
		showView(null);// to be deleted...
	}

	@Override
	protected String getTitle() {
		return TITLE;
	}

	@Override
	protected void showView(SearchResult result) {
		if (this.view == null) {
			this.view = this.clientFactory.getDiskStatView();
			((DiskStatView) this.view).setPresenter(this);
			container.setWidget(this.view);
			
		}
		((DiskStatView)this.view).showSearchResult(result);
	}

}
