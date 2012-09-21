package com.eucalyptus.webui.client.activity;

import java.util.ArrayList;

import com.eucalyptus.webui.client.ClientFactory;
import com.eucalyptus.webui.client.place.SearchPlace;
import com.eucalyptus.webui.client.service.SearchRange;
import com.eucalyptus.webui.client.service.SearchResult;
import com.eucalyptus.webui.client.service.ViewSearchTableClientConfig;
import com.eucalyptus.webui.client.view.CPUStatView;
import com.eucalyptus.webui.client.view.HasValueWidget;
import com.eucalyptus.webui.shared.config.EnumService;

public class CPUStatActivity extends AbstractSearchActivity implements
		CPUStatView.Presenter {

	public static final String TITLE = "CPU_STAT";

	public CPUStatActivity(SearchPlace place, ClientFactory clientFactory) {
		super(place, clientFactory);
	}

	@Override
	public void saveValue(ArrayList<String> keys, ArrayList<HasValueWidget> values) {
	}

	@Override
	protected void doSearch(String query, SearchRange range) {
//		displayData(null);
		showView(null);
	}

	@Override
	protected String getTitle() {
		return TITLE;
	}
	
	@Override
	public int getPageSize() {
		return ViewSearchTableClientConfig.instance().getPageSize(EnumService.CPU_STAT_SRV);
	}

	@Override
	protected void showView(SearchResult result) {
		if (this.view == null) {
			this.view = this.clientFactory.getCPUStatView();
			((CPUStatView) this.view).setPresenter(this);
			container.setWidget(this.view);
			
		}
		((CPUStatView)this.view).showSearchResult(result);
	}

}
