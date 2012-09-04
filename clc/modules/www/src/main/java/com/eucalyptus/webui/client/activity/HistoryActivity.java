package com.eucalyptus.webui.client.activity;

import java.util.ArrayList;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.eucalyptus.webui.client.ClientFactory;
import com.eucalyptus.webui.client.place.SearchPlace;
import com.eucalyptus.webui.client.service.SearchRange;
import com.eucalyptus.webui.client.service.SearchResult;
import com.eucalyptus.webui.client.service.SearchResultRow;
import com.eucalyptus.webui.client.view.ConfirmationView;
import com.eucalyptus.webui.client.view.FooterView;
import com.eucalyptus.webui.client.view.HasValueWidget;
import com.eucalyptus.webui.client.view.FooterView.StatusType;
import com.eucalyptus.webui.client.view.HistoryView;
import com.eucalyptus.webui.client.view.LogView.LogType;
import com.eucalyptus.webui.client.view.InputView;
import com.google.common.collect.Lists;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class HistoryActivity extends AbstractSearchActivity implements HistoryView.Presenter {

	public static final String TITLE[] = { "GROUPS", "用户组" };

	public static final String DELETE_GROUPS_CAPTION[] = {
			"Delete selected groups", "删除所选的用户组" };
	public static final String DELETE_GROUPS_SUBJECT[] = {
			"Are you sure you want to delete following selected groups?",
			"确定要删除所选择的用户组" };

	private final static String[] RESUME_GROUPS_CAPTION = { "Resume the group",
			"激活组" };
	private final static String[] RESUME_GROUPS_SUBJECT = {
			"Are you sure you resume the group?", "确定要激活该组？" };

	private final static String[] PAUSE_GROUPS_CAPTION = { "Pause the group",
			"暂停组" };
	private final static String[] PAUSE_GROUPS_SUBJECT = {
			"Are you sure you pause the group?", "确定要暂停该组？" };

	private final static String[] BAN_GROUPS_CAPTION = { "Ban the group", "禁止组" };
	private final static String[] BAN_GROUPS_SUBJECT = {
			"Are you sure you ban the group?", "确定要禁止该组？" };

	public static final String ADD_USERS_CAPTION[] = {
			"Add users to selected groups", "将用户加入所选择的组" };
	public static final String ADD_USERS_SUBJECT[] = {
			"Enter users to add to selected groups (using space to separate names):",
			"输入需要加入组的用户姓名（用空格隔开）" };
	public static final String USER_NAMES_INPUT_TITLE[] = { "User names",
			"用户姓名" };

	public static final String REMOVE_USERS_CAPTION[] = {
			"Remove users from selected groups", "从所选的组中删除用户" };
	public static final String REMOVE_USERS_SUBJECT[] = {
			"Enter users to remove from selected groups (using space to separate names):",
			"输入需要从组中删除的用户姓名（用空格隔开）" };

	public static final String ADD_POLICY_CAPTION[] = { "Add new policy",
			"增加新的策略" };
	public static final String ADD_POLICY_SUBJECT[] = {
			"Enter new policy to assign to the selected group:", "给选定的组增加新的策略" };
	public static final String POLICY_NAME_INPUT_TITLE[] = { "Policy name",
			"策略名称" };
	public static final String POLICY_CONTENT_INPUT_TITLE[] = {
			"Policy content", "策略内容" };

	private static final Logger LOG = Logger.getLogger(HistoryActivity.class
			.getName());

	private Set<SearchResultRow> currentSelected;

	private GroupDetailActivity groupDetailActivity;

	public HistoryActivity(SearchPlace place, ClientFactory clientFactory) {
		super(place, clientFactory);

		groupDetailActivity = new GroupDetailActivity(place, clientFactory);
	}

	@Override
	protected void doSearch(String query, SearchRange range) {
		this.clientFactory.getBackendService().lookupGroup(
				this.clientFactory.getLocalSession().getSession(), search,
				range, new AsyncCallback<SearchResult>() {

					@Override
					public void onFailure(Throwable caught) {
						ActivityUtil.logoutForInvalidSession(clientFactory,
								caught);
						LOG.log(Level.WARNING, "Search failed: " + caught);
						displayData(null);
					}

					@Override
					public void onSuccess(SearchResult result) {
						LOG.log(Level.INFO, "Search success:" + result);
						displayData(result);
					}

				});
	}

	@Override
	public void onSelectionChange(Set<SearchResultRow> selection) {
		this.currentSelected = selection;
		if (selection == null || selection.size() != 1) {
			LOG.log(Level.INFO, "Not a single selection");
			this.clientFactory.getShellView().hideDetail();
		} else {
			LOG.log(Level.INFO, "Selection changed to " + selection);
		}
	}

	static String[] GROUP_VIEW_DETAIL = { "Group View", "组视图" };

	protected void showSingleSelectedDetails(SearchResultRow selected) {
		String groupId = selected.getField(0);
		String accountId = selected.getField(1);

		this.groupDetailActivity.setGroupId(Integer.valueOf(groupId));

		// clientFactory.getGroupDetailView().setTitle(GROUP_VIEW_DETAIL[1]);
		clientFactory.getGroupDetailView().setPresenter(
				this.groupDetailActivity);
		clientFactory.getGroupDetailView().setAccountId(
				Integer.valueOf(accountId));
		clientFactory.getGroupDetailView().setGroupId(Integer.valueOf(groupId));

		this.clientFactory.getBackendService().lookupUserByGroupId(
				this.clientFactory.getLocalSession().getSession(),
				Integer.valueOf(groupId), range,
				new AsyncCallback<SearchResult>() {

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
	public void saveValue(ArrayList<String> keys, ArrayList<HasValueWidget> values) {
		final ArrayList<String> newVals = Lists.newArrayList();
		for (HasValueWidget w : values) {
			newVals.add(w.getValue());
		}

		final String groupId = emptyForNull(getField(newVals, 0));
		this.clientFactory
				.getShellView()
				.getFooterView()
				.showStatus(StatusType.LOADING,
						"Modifying group " + groupId + " ...", 0);

		clientFactory.getBackendService().modifyGroup(
				clientFactory.getLocalSession().getSession(), newVals,
				new AsyncCallback<Void>() {

					@Override
					public void onFailure(Throwable caught) {
						ActivityUtil.logoutForInvalidSession(clientFactory,
								caught);
						clientFactory
								.getShellView()
								.getFooterView()
								.showStatus(StatusType.ERROR,
										"Failed to modify group",
										FooterView.DEFAULT_STATUS_CLEAR_DELAY);
						clientFactory
								.getShellView()
								.getLogView()
								.log(LogType.ERROR,
										"Failed to modify group " + groupId
												+ ": " + caught.getMessage());
					}

					@Override
					public void onSuccess(Void arg0) {
						clientFactory
								.getShellView()
								.getFooterView()
								.showStatus(StatusType.NONE,
										"Successfully modified group",
										FooterView.DEFAULT_STATUS_CLEAR_DELAY);
						clientFactory.getShellView().getLogView()
								.log(LogType.INFO, "Modified group " + groupId);
						// clientFactory.getShellView( ).getDetailView(
						// ).disableSave( );
						reloadCurrentRange();
					}

				});
	}

	@Override
	protected String getTitle() {
		return TITLE[1];
	}

	@Override
	protected void showView(SearchResult result) {
		if (this.view == null) {
			this.view = this.clientFactory.getHistoryView();
			((HistoryView) this.view).setPresenter(this);
			container.setWidget(this.view);
			((HistoryView) this.view).clear();
		}
		((HistoryView) this.view).showSearchResult(result);
	}

	private final static String[] GROUP_ACTIVITY_No_SELECTION = {
			"Please select at least on group", "请至少选择一个组" };
	private final static String[] FOOTERVIEW_GROUP_ACTIVITY_UPDATE_USERSTATE_SUCCEED = {
			"Update user state succeeds", "更新用户状态成功" };
	private final static String[] FOOTERVIEW_GROUP_ACTIVITY_UPDATE_USERSTATE_FAIL = {
			"Failed to update user state", "更新用户状态成功" };


	private boolean selectionIsValid() {
		if (currentSelected == null || currentSelected.size() < 1) {
			clientFactory
					.getShellView()
					.getFooterView()
					.showStatus(StatusType.ERROR,
							GROUP_ACTIVITY_No_SELECTION[1],
							FooterView.DEFAULT_STATUS_CLEAR_DELAY);
			return false;
		}

		return true;
	}

	final static String[] FOOTERVIEW_GROUP_DELETING = { "Deleting groups ...",
			"正在删除用户组 ..." };
	final static String[] FOOTERVIEW_GROUP_DELET_SUCCEED = { "Groups deleted",
			"删除用户组成功" };
	final static String[] FOOTERVIEW_GROUP_DELET_FAIL = {
			"Failed to delete groups", "删除用户组失败" };


	@Override
	public void onDoubleClick(DoubleClickEvent event) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onRefresh() {
		// TODO Auto-generated method stub

	}
}
