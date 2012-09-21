package com.eucalyptus.webui.client.activity;

import java.util.ArrayList;
import java.util.Set;
import java.util.logging.Logger;

import com.eucalyptus.webui.client.ClientFactory;
import com.eucalyptus.webui.client.place.SearchPlace;
import com.eucalyptus.webui.client.service.SearchRange;
import com.eucalyptus.webui.client.service.SearchResult;
import com.eucalyptus.webui.client.service.SearchResultRow;
import com.eucalyptus.webui.client.service.ViewSearchTableClientConfig;
import com.eucalyptus.webui.client.view.ConfirmationView;
import com.eucalyptus.webui.client.view.FooterView;
import com.eucalyptus.webui.client.view.GroupAddingUserListView;
import com.eucalyptus.webui.client.view.GroupDetailView;
import com.eucalyptus.webui.client.view.HasValueWidget;
import com.eucalyptus.webui.client.view.FooterView.StatusType;
import com.eucalyptus.webui.client.view.LogView.LogType;
import com.eucalyptus.webui.shared.config.EnumService;
import com.eucalyptus.webui.shared.config.LanguageSelection;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class GroupDetailActivity extends AbstractSearchActivity implements GroupDetailView.Presenter, GroupAddingUserListView.Presenter, ConfirmationView.Presenter {

	private GroupAddingUserActivity groupAddingUserActivity;
	
	public GroupDetailActivity(SearchPlace place, ClientFactory clientFactory) {
		super(place, clientFactory);
		this.groupAddingUserActivity = new GroupAddingUserActivity(place, clientFactory);
		
		// TODO Auto-generated constructor stub
		this.range = new SearchRange( 0, pageSize, -1/*sortField*/, true );
	}
	
	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}
	public int getGroupId() {
		return this.groupId;
	}
	
	public SearchRange getRange() {
		return this.range;
	}

	@Override
	public void onSelectionChange(Set<SearchResultRow> selections) {
		// TODO Auto-generated method stub
		this.currentSelected = selections;
	}

	/**
	 * GroupDetailView Presenter
	 * 
	 */
	@Override
	public void onAddUsers(int accountId, int groupId) {
		// TODO Auto-generated method stub
		this.groupAddingUserActivity.setAccountAndGroupId(accountId, groupId);
		this.clientFactory.getGroupAddingUserListView().setPresenter(this);

		this.clientFactory.getBackendService().lookupUserExcludeGroupId(
		        this.clientFactory.getLocalSession().getSession(), accountId, groupId, this.groupAddingUserActivity.getRange(),
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
	  
		int lan = LanguageSelection.instance().getCurLanguage().ordinal();
		confirmView.display(GROUP_ACTIVITY_REMOVE_USERS_CAPTION[lan], GROUP_ACTIVITY_REMOVE_USERS_SUBJECT[lan]);
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
	public int getPageSize() {
		return ViewSearchTableClientConfig.instance().getPageSize(EnumService.GROUP_DETAIL_SRV);
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
		int lan = LanguageSelection.instance().getCurLanguage().ordinal();
		if ( GROUP_ACTIVITY_REMOVE_USERS_SUBJECT[lan].equals( subject ) ) {
			doRemoveUsers( );
		  }
	}
	
	public void doRemoveUsers() {
		final ArrayList<String> ids = new ArrayList<String>();

		for (SearchResultRow row : currentSelected) {
			ids.add(row.getField(0));
		}
		
		final int lan = LanguageSelection.instance().getCurLanguage().ordinal();

		clientFactory.getShellView().getFooterView()
		        .showStatus(StatusType.LOADING, GROUP_ACTIVITY_REMOVE_USERS_STATUS[lan], 0);

		clientFactory.getBackendService().removeUsersFromGroup(clientFactory.getLocalSession().getSession(), ids,
		        new AsyncCallback<Void>() {

			        @Override
			        public void onFailure(Throwable caught) {
				        ActivityUtil.logoutForInvalidSession(clientFactory, caught);
				        clientFactory
				                .getShellView()
				                .getFooterView()
				                .showStatus(StatusType.ERROR, GROUP_ACTIVITY_REMOVE_USERS_FAIL[lan],
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
			        	int lan = LanguageSelection.instance().getCurLanguage().ordinal();
				        clientFactory
				                .getShellView()
				                .getFooterView()
				                .showStatus(StatusType.NONE, GROUP_ACTIVITY_REMOVE_USERS_SUCCEED[lan],
				                        FooterView.DEFAULT_STATUS_CLEAR_DELAY);
				        clientFactory.getShellView().getLogView()
				                .log(LogType.INFO, "Users " + ids + " are removed from groups ");

				        reloadCurrentRange();
			        }

		        });
	}
	
	@Override
	public void process(ArrayList<String> userIds) {
		// TODO Auto-generated method stub
		this.clientFactory.getBackendService().addUsersToGroupsById(this.clientFactory.getLocalSession().getSession(),
		        userIds, this.groupId, new AsyncCallback<Void>() {
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
	

}
