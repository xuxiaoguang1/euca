package com.eucalyptus.webui.client.activity;

import java.util.ArrayList;
import java.util.logging.Logger;

import com.eucalyptus.webui.client.ClientFactory;
import com.eucalyptus.webui.client.place.SearchPlace;
import com.eucalyptus.webui.client.service.SearchRange;
import com.eucalyptus.webui.client.service.SearchResult;
import com.eucalyptus.webui.client.service.ViewSearchTableSizeConf;
import com.eucalyptus.webui.client.view.HasValueWidget;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class GroupAddingUserActivity extends AbstractSearchActivity {

	public GroupAddingUserActivity(SearchPlace place, ClientFactory clientFactory) {
		super(place, clientFactory);
		// TODO Auto-generated constructor stub
		this.range = new SearchRange( 0, pageSize, -1/*sortField*/, true );
	}
	
	public SearchRange getRange() {
		return this.range;
	}

	@Override
	protected void doSearch(String query, SearchRange range) {
		// TODO Auto-generated method stub
		this.clientFactory.getBackendService().lookupUserExcludeGroupId(
		        this.clientFactory.getLocalSession().getSession(), accountId, groupId, range,
		        new AsyncCallback<SearchResult>() {

			        @Override
			        public void onFailure(Throwable caught) {
				        // TODO Auto-generated method stub
			        }

			        @Override
			        public void onSuccess(SearchResult result) {
				        // TODO Auto-generated method stub
				        clientFactory.getGroupAddingUserListView().showSearchResult(result);
			        }
		        });
	}

	@Override
	public void saveValue(ArrayList<String> keys,
			ArrayList<HasValueWidget> values) {
		// TODO Auto-generated method stub
		
	}

	public void setAccountAndGroupId(int accountId, int groupId) {
		// TODO Auto-generated method stub
		this.accountId = accountId;
		this.groupId = groupId;
	}
	
	@Override
	protected String getTitle() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public int getPageSize() {
		return ViewSearchTableSizeConf.instance().getPageSize(GroupAddingUserActivity.class.getName());
	}

	@Override
	protected void showView(SearchResult result) {
		// TODO Auto-generated method stub
		
	}
	
	private static final Logger LOG = Logger.getLogger( GroupAddingUserActivity.class.getName( ) );
	private int accountId;
	private int groupId;
}
