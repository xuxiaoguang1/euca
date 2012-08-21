package com.eucalyptus.webui.client.activity;

import java.util.ArrayList;
import java.util.Set;
import java.util.logging.Logger;

import com.eucalyptus.webui.client.ClientFactory;
import com.eucalyptus.webui.client.place.SearchPlace;
import com.eucalyptus.webui.client.service.SearchRange;
import com.eucalyptus.webui.client.service.SearchResult;
import com.eucalyptus.webui.client.service.SearchResultRow;
import com.eucalyptus.webui.client.view.ConfirmationView;
import com.eucalyptus.webui.client.view.FooterView;
import com.eucalyptus.webui.client.view.GroupDetailView;
import com.eucalyptus.webui.client.view.HasValueWidget;
import com.eucalyptus.webui.client.view.UserListView;
import com.eucalyptus.webui.client.view.FooterView.StatusType;
import com.eucalyptus.webui.client.view.LogView.LogType;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class GroupDetailActivity extends AbstractSearchActivity implements GroupDetailView.Presenter, UserListView.Presenter, ConfirmationView.Presenter {

	public GroupDetailActivity(SearchPlace place, ClientFactory clientFactory) {
		super(place, clientFactory);
		// TODO Auto-generated constructor stub
		this.range = new SearchRange( 0, pageSize, -1/*sortField*/, true );
	}
	
	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}
	public int getGroupId() {
		return this.groupId;
	}

	@Override
	public void onSelectionChange(Set<SearchResultRow> selections) {
		// TODO Auto-generated method stub
		this.currentSelected = selections;
	}

	@Override
	public void process(ArrayList<String> ids) {
		// TODO Auto-generated method stub
		System.out.println(ids);

		this.clientFactory.getBackendService().addUsersToGroupsById(this.clientFactory.getLocalSession().getSession(),
		        ids, this.getGroupId(), new AsyncCallback<Void>() {

			        @Override
			        public void onFailure(Throwable caught) {
				        // TODO Auto-generated method stub

			        }

			        @Override
			        public void onSuccess(Void result) {
				        // TODO Auto-generated method stub
				        reloadCurrentRange();
			        }
		        });
	}

	/**
	 * GroupDetailView Presenter
	 * 
	 */
	@Override
	public void onAddUsers(int accountId, int groupId) {
		// TODO Auto-generated method stub
		this.clientFactory.getUserListView().setPresenter(this);

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
				        // remove the row with the same group id

				        clientFactory.getUserListView().showSearchResult(result);
				        clientFactory.getUserListView().display();
			        }
		        });
	}

	final static String[] GROUP_ACTIVITY_REMOVE_USERS_CAPTION = {
        "Remove the users", "从组删除用户"};
	final static String[] GROUP_ACTIVITY_REMOVE_USERS_SUBJECT = {
	        "Are you sure you remove the selected users from this group?", "确定从该组删除所选用户?"};
	final static String[] GROUP_ACTIVITY_REMOVE_USERS_STATUS = {"Removing users from selected groups ...",
	        "从所选的组中删除用户 ..."};
	final static String[] GROUP_ACTIVITY_REMOVE_USERS_FAIL = {"Failed to remove users", "从所选的组中删除用户失败"};
	final static String[] GROUP_ACTIVITY_REMOVE_USERS_SUCCEED = {"Users removed from selected group", "从所选的组中删除用户成功"};

	@Override
	public void onRemoveUsers() {
		// TODO Auto-generated method stub
		if (this.currentSelected == null || this.currentSelected.size() < 1)
			return;
		
		ConfirmationView confirmView = this.clientFactory.getConfirmationView();
		confirmView.setPresenter(this);
	  
		confirmView.display(GROUP_ACTIVITY_REMOVE_USERS_CAPTION[1], GROUP_ACTIVITY_REMOVE_USERS_SUBJECT[1]);
	}
	
	@Override
	protected void doSearch(String query, SearchRange range) {
		// TODO Auto-generated method stub

		this.clientFactory.getBackendService().lookupUserByGroupId(this.clientFactory.getLocalSession().getSession(),
		        getGroupId(), range, new AsyncCallback<SearchResult>() {

			        @Override
			        public void onFailure(Throwable caught) {
				        // TODO Auto-generated method stub

			        }

			        @Override
			        public void onSuccess(SearchResult result) {
				        // TODO Auto-generated method stub
				        clientFactory.getGroupDetailView().showSearchResult(result);
			        }
		        });
	}

	@Override
	protected String getTitle() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void showView(SearchResult result) {
		// TODO Auto-generated method stub
		
	}
	
	private static final Logger LOG = Logger.getLogger( GroupActivity.class.getName( ) );
	  
	private Set<SearchResultRow> currentSelected;
	
	private int groupId;

	@Override
	public void saveValue(ArrayList<String> keys,
			ArrayList<HasValueWidget> values) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void confirm(String subject) {
		// TODO Auto-generated method stub
		if ( GROUP_ACTIVITY_REMOVE_USERS_SUBJECT[1].equals( subject ) ) {
			doRemoveUsers( );
		  }
	}
	
	public void doRemoveUsers() {
		final ArrayList<String> ids = new ArrayList<String>();

		for (SearchResultRow row : currentSelected) {
			ids.add(row.getField(0));
		}

		clientFactory.getShellView().getFooterView()
		        .showStatus(StatusType.LOADING, GROUP_ACTIVITY_REMOVE_USERS_STATUS[1], 0);

		clientFactory.getBackendService().removeUsersFromGroup(clientFactory.getLocalSession().getSession(), ids,
		        new AsyncCallback<Void>() {

			        @Override
			        public void onFailure(Throwable caught) {
				        ActivityUtil.logoutForInvalidSession(clientFactory, caught);
				        clientFactory
				                .getShellView()
				                .getFooterView()
				                .showStatus(StatusType.ERROR, GROUP_ACTIVITY_REMOVE_USERS_FAIL[1],
				                        FooterView.DEFAULT_STATUS_CLEAR_DELAY);
				        clientFactory
				                .getShellView()
				                .getLogView()
				                .log(LogType.ERROR,
				                        "Failed to remove users " + ids + " from group " + ": "
				                                + caught.getMessage());
			        }

			        @Override
			        public void onSuccess(Void arg0) {
				        clientFactory
				                .getShellView()
				                .getFooterView()
				                .showStatus(StatusType.NONE, GROUP_ACTIVITY_REMOVE_USERS_SUCCEED[1],
				                        FooterView.DEFAULT_STATUS_CLEAR_DELAY);
				        clientFactory.getShellView().getLogView()
				                .log(LogType.INFO, "Users " + ids + " are removed from groups ");

				        reloadCurrentRange();
			        }

		        });
	}

}
