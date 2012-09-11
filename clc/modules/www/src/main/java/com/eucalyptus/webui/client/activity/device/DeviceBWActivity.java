package com.eucalyptus.webui.client.activity.device;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;

import com.eucalyptus.webui.client.ClientFactory;
import com.eucalyptus.webui.client.activity.AbstractSearchActivity;
import com.eucalyptus.webui.client.activity.ActivityUtil;
import com.eucalyptus.webui.client.place.SearchPlace;
import com.eucalyptus.webui.client.service.EucalyptusServiceAsync;
import com.eucalyptus.webui.client.service.SearchRange;
import com.eucalyptus.webui.client.service.SearchResult;
import com.eucalyptus.webui.client.service.SearchResultRow;
import com.eucalyptus.webui.client.session.Session;
import com.eucalyptus.webui.client.view.DeviceBWServiceAddView;
import com.eucalyptus.webui.client.view.DeviceBWServiceAddViewImpl;
import com.eucalyptus.webui.client.view.DeviceBWView;
import com.eucalyptus.webui.client.view.DeviceServiceDatePicker;
import com.eucalyptus.webui.client.view.DeviceServiceSimpleModifyView;
import com.eucalyptus.webui.client.view.DeviceServiceSimpleModifyViewImpl;
import com.eucalyptus.webui.client.view.FooterView;
import com.eucalyptus.webui.client.view.HasValueWidget;
import com.eucalyptus.webui.client.view.LogView;
import com.eucalyptus.webui.client.view.DeviceBWView.MirrorModeType;
import com.eucalyptus.webui.client.view.DeviceMirrorSearchResultTable.SearchResultRowMatcher;
import com.eucalyptus.webui.client.view.FooterView.StatusType;
import com.eucalyptus.webui.client.view.LogView.LogType;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class DeviceBWActivity extends AbstractSearchActivity implements DeviceBWView.Presenter {

	private static final int LAN_SELECT = 1;

	public static final String TITLE[] = {"BW", "带宽"};
	
	public static final int TABLE_COL_INDEX_BS_ID = 0;
	public static final int TABLE_COL_INDEX_CHECKBOX = 1;
	public static final int TABLE_COL_INDEX_NO = 2;
	public static final int TABLE_COL_INDEX_ROOT_ACCOUNT = 3;
	public static final int TABLE_COL_INDEX_USER = 4;
	public static final int TABLE_COL_INDEX_STARTTIME = 8;
	public static final int TABLE_COL_INDEX_LIFE = 9;
	public static final int TABLE_COL_INDEX_REMAINS = 10;

	private DeviceServiceSimpleModifyView serviceModifyView;
	private DeviceBWServiceAddView serviceAddView;

	public DeviceBWActivity(SearchPlace place, ClientFactory clientFactory) {
		super(place, clientFactory);
		serviceModifyView = new DeviceServiceSimpleModifyViewImpl();
		serviceModifyView.setPresenter(new DeviceServiceSimpleModifyView.Presenter() {

			@Override
			public boolean onOK(SearchResultRow row, Date starttime, Date endtime) {
				final long div = DeviceServiceDatePicker.DAY_MILLIS;
				if (starttime.getTime() / div > endtime.getTime() / div) {
					StringBuilder sb = new StringBuilder();
					sb.append(UPDATE_SERVICE_FAILURE_INVALID_DATE[LAN_SELECT]).append("\n");
					sb.append("<");
					sb.append(DeviceServiceDatePicker.format(starttime));
					sb.append(", ");
					sb.append(DeviceServiceDatePicker.format(endtime));
					sb.append(">");
					Window.alert(sb.toString());
					return false;
				}
				handleModifyService(row, DeviceServiceDatePicker.format(endtime));
				getView().getMirrorTable().clearSelection();
				return true;
			}

			@Override
			public void onCancel() {
				getView().getMirrorTable().clearSelection();
			}

		});

		serviceAddView = new DeviceBWServiceAddViewImpl();
		serviceAddView.setPresenter(new DeviceBWServiceAddView.Presenter() {

			@Override
			public boolean onOK(String account, String user, Date starttime, Date endtime, String ip, long bandwidth) {
				final long div = DeviceServiceDatePicker.DAY_MILLIS;
				if (starttime.getTime() / div > endtime.getTime() / div) {
					StringBuilder sb = new StringBuilder();
					sb.append(ADD_SERVICE_FAILURE_INVALID_DATE[LAN_SELECT]).append("\n");
					sb.append("<");
					sb.append(DeviceServiceDatePicker.format(starttime)).append(", ");
					sb.append(DeviceServiceDatePicker.format(endtime));
					sb.append(">");
					Window.alert(sb.toString());
					return false;
				}
				if (isEmpty(account) || isEmpty(user) || isEmpty(ip)) {
					StringBuilder sb = new StringBuilder();
					sb.append(ADD_SERVICE_FAILURE_INVALID_ARGS[LAN_SELECT]).append("\n");
					sb.append("<account='").append(account).append("'").append(", ");
					sb.append("user='").append(user).append("'").append("', ");
					sb.append("ip='").append(user).append("'").append("'>");
					Window.alert(sb.toString());
					return false;
				}
				handleAddService(account, user, DeviceServiceDatePicker.format(starttime), (int)(endtime.getTime()
				        / div - starttime.getTime() / div), ip, bandwidth);
				return true;
			}

			@Override
			public void lookupAccounts() {
				getBackendService().listDeviceBWAccounts(getSession(), new AsyncCallback<List<String>>() {

					@Override
					public void onFailure(Throwable caught) {
						log(QUERY_ACCOUNTS_FAILURE[LAN_SELECT], caught);
					}

					@Override
					public void onSuccess(List<String> result) {
						if (result != null) {
							serviceAddView.setAccountList(result);
						}
						else {
							showStatus(QUERY_ACCOUNTS_FAILURE[LAN_SELECT]);
						}
					}

				});
			}

			@Override
			public void lookupUserByAccount(final String account) {
				getBackendService().listDeviceBWUsersByAccount(getSession(), account, new AsyncCallback<List<String>>() {

					@Override
					public void onFailure(Throwable caught) {
						log(QUERY_USERS_BY_ACCOUNT_FAILURE[LAN_SELECT], caught);
					}

					@Override
					public void onSuccess(List<String> result) {
						if (result != null) {
							serviceAddView.setUserList(account, result);
						}
						else {
							showStatus(QUERY_USERS_BY_ACCOUNT_FAILURE[LAN_SELECT]);
						}
					}

				});
			}
			
			@Override
            public void lookupIPsByUser(final String account, final String user) {
				getBackendService().listDeviceIPsByUser(getSession(), account, user, new AsyncCallback<List<String>>() {

					@Override
                    public void onFailure(Throwable caught) {
						log(QUERY_IPS_BY_USER_FAILURE[LAN_SELECT], caught);
                    }

					@Override
                    public void onSuccess(List<String> result) {
						if (result != null) {
							serviceAddView.setIPList(account, user, result);
						}
						else {
							showStatus(QUERY_IPS_BY_USER_FAILURE[LAN_SELECT]);
						}
                    }
				});
            }

			@Override
			public void onCancel() {
				getView().getMirrorTable().clearSelection();
			}

		});
	}
	
	private boolean isEmpty(String s) {
		return s == null || s.length() == 0;
	}

	private EucalyptusServiceAsync getBackendService() {
		return clientFactory.getBackendService();
	}

	private FooterView getFooterView() {
		return clientFactory.getShellView().getFooterView();
	}

	private LogView getLogView() {
		return clientFactory.getShellView().getLogView();
	}

	private Session getSession() {
		return clientFactory.getLocalSession().getSession();
	}

	private void log(String msg, Throwable caught) {
		getFooterView().showStatus(StatusType.ERROR, msg, FooterView.CLEAR_DELAY_SECOND * 5);
		getLogView().log(LogType.ERROR, msg + ": " + caught.getMessage());
	}

	private void showStatus(String msg) {
		getFooterView().showStatus(StatusType.ERROR, msg, FooterView.CLEAR_DELAY_SECOND * 5);
		getLogView().log(LogType.ERROR, msg);
	}
	
	@Override
	protected void doSearch(String query, SearchRange range) {
		getBackendService().lookupDeviceBW(getSession(), query, range,
		        new AsyncCallback<SearchResult>() {

			        @Override
			        public void onFailure(Throwable caught) {
				        ActivityUtil.logoutForInvalidSession(clientFactory, caught);
				        log(QUERY_TABLE_FAILURE[LAN_SELECT], caught);
				        displayData(null);
			        }

			        @Override
			        public void onSuccess(SearchResult result) {
				        displayData(result);
			        }

		        });
	}

	private DeviceBWView getView() {
		DeviceBWView view = (DeviceBWView)this.view;
		if (view == null) {
			view = clientFactory.getDeviceBWView();
			view.setPresenter(this);
			container.setWidget(view);
			view.clear();
			this.view = view;
		}
		return view;
	}

	@Override
	protected void showView(SearchResult result) {
		getView().showSearchResult(result);
	}

	@Override
	public void onSelectionChange(Set<SearchResultRow> selections) {
		/* do nothing */
	}

	@Override
	public void saveValue(ArrayList<String> keys, ArrayList<HasValueWidget> values) {
		/* do nothing */
	}

	@Override
	protected String getTitle() {
		return TITLE[LAN_SELECT];
	}

	private static final String[] QUERY_TABLE_FAILURE = {"", "获取列表失败"};
	private static final String[] QUERY_IPS_BY_USER_FAILURE = {"", "获取IP列表失败"};
	private static final String[] QUERY_ACCOUNTS_FAILURE = {"", "获取账户列表失败"};
	private static final String[] QUERY_USERS_BY_ACCOUNT_FAILURE = {"", "获取用户列表失败"};
	private static final String[] UPDATE_SERVICE_FAILURE = {"", "更新服务失败"};
	private static final String[] UPDATE_SERVICE_FAILURE_INVALID_DATE = {"", "更新服务失败：选择时间无效"};
	private static final String[] UPDATE_SERVICE_SUCCESS = {"", "更新服务成功"};
	private static final String[] ADD_SERVICE_SUCCESS = {"", "添加服务成功"};
	private static final String[] ADD_SERVICE_FAILURE = {"", "添加服务失败"};
	private static final String[] ADD_SERVICE_FAILURE_INVALID_DATE = {"", "添加服务失败：选择时间无效"};
	private static final String[] ADD_SERVICE_FAILURE_INVALID_ARGS = {"", "添加服务失败：选择参数无效"};
	private static final String[] DELETE_SERVICE_FAILURE = {"", "删除服务失败"};
	private static final String[] DELETE_SERVICE_SUCCESS = {"", "删除服务成功"};
	private static final String[] DELETE_SERVICE_CONFIRM = {"", "确认删除所选择的 服务？"};
	private static final String[] DELETE_ALL_SERVICE_CONFIRM = {"", "确认删除所选择的 全部服务？"};
	private static final String[] ACTION_SELECTED_FAILURE = {"", "请选择操作对象"};

	private void prepareModifyService(SearchResultRow row) {
		String starttime = row.getField(TABLE_COL_INDEX_STARTTIME);
		String life = row.getField(TABLE_COL_INDEX_LIFE);
		assert (!isEmpty(starttime) && !isEmpty(life));
		serviceModifyView.setValue(row, DeviceServiceDatePicker.parse(starttime),
		        DeviceServiceDatePicker.parse(starttime, life));
	}

	private void prepareDeleteService(SearchResultRow row) {
		if (!Window.confirm(DELETE_SERVICE_CONFIRM[LAN_SELECT])) {
			getView().getMirrorTable().clearSelection();
			return;
		}
		List<SearchResultRow> list = new ArrayList<SearchResultRow>();
		list.add(row);
		handleDeleteService(list);
	}

	@Override
	public void onMirrorSelectRow(SearchResultRow row) {
		if (row == null) {
			return;
		}
		switch (getView().getMirrorModeType()) {
		case MODIFY_SERVICE:
			prepareModifyService(row);
			return;
		case DELETE_SERVICE:
			prepareDeleteService(row);
			return;
		default:
			return;
		}
	}

	private void handleAddService(String account, String user, String starttime, int life, String ip, long bandwidth) {
		getBackendService().addDeviceBWService(getSession(), account, user, starttime, life, ip, bandwidth,
				new AsyncCallback<Boolean>() {

			        @Override
			        public void onFailure(Throwable caught) {
				        ActivityUtil.logoutForInvalidSession(clientFactory, caught);
				        log(ADD_SERVICE_FAILURE[LAN_SELECT], caught);
			        }

			        @Override
			        public void onSuccess(Boolean result) {
				        if (result) {
					        showStatus(ADD_SERVICE_SUCCESS[LAN_SELECT]);
				        }
				        else {
					        showStatus(ADD_SERVICE_FAILURE[LAN_SELECT]);
				        }
			        	reloadCurrentRange();
			        }

		        });
	}

	private void handleModifyService(SearchResultRow row, String endtime) {
		assert (row != null && getView().getMirrorModeType() == MirrorModeType.MODIFY_SERVICE);
		getBackendService().modifyDeviceBWService(getSession(), row, endtime,
		        new AsyncCallback<SearchResultRow>() {

			        @Override
			        public void onFailure(Throwable caught) {
				        ActivityUtil.logoutForInvalidSession(clientFactory, caught);
				        log(UPDATE_SERVICE_FAILURE[LAN_SELECT], caught);
			        }

			        @Override
			        public void onSuccess(SearchResultRow result) {
				        if (result != null) {
					        showStatus(UPDATE_SERVICE_SUCCESS[LAN_SELECT]);
					        if (getView().isMirrorMode()) {
						        final int col = TABLE_COL_INDEX_BS_ID;
						        result.setField(TABLE_COL_INDEX_CHECKBOX, "+");
						        final SearchResultRowMatcher matcher = new SearchResultRowMatcher() {

							        @Override
							        public boolean match(SearchResultRow row0, SearchResultRow row1) {
								        return row0.getField(col).equals(row1.getField(col));
							        }

						        };
						        getView().getMirrorTable().updateRow(result, matcher);
					        }
				        }
				        else {
					        showStatus(UPDATE_SERVICE_FAILURE[LAN_SELECT]);
				        }
			        	reloadCurrentRange();
			        }

		        });
	}

	private void handleDeleteService(List<SearchResultRow> list) {
		DeviceBWView view = getView();
		assert (list.size() != 0 && view.getMirrorModeType() == MirrorModeType.DELETE_SERVICE);
		getBackendService().deleteDeviceBWService(getSession(), list, new AsyncCallback<List<SearchResultRow>>() {

			@Override
			public void onFailure(Throwable caught) {
				ActivityUtil.logoutForInvalidSession(clientFactory, caught);
				log(DELETE_SERVICE_FAILURE[LAN_SELECT], caught);
			}

			@Override
			public void onSuccess(List<SearchResultRow> result) {
				if (result != null) {
					showStatus(DELETE_SERVICE_SUCCESS[LAN_SELECT]);
					if (getView().isMirrorMode()) {
						final int col = TABLE_COL_INDEX_BS_ID;
						final SearchResultRowMatcher matcher = new SearchResultRowMatcher() {

							@Override
							public boolean match(SearchResultRow row0, SearchResultRow row1) {
								return row0.getField(col).equals(row1.getField(col));
							}

						};
						for (SearchResultRow row : result) {
							getView().getMirrorTable().deleteRow(row, matcher);
						}
			        }
				}
				else {
					showStatus(DELETE_SERVICE_FAILURE[LAN_SELECT]);
				}
	        	reloadCurrentRange();
			}

		});
	}

	private SearchResultRow copyRow(SearchResultRow row) {
		SearchResultRow tmp = row.copy();
		tmp.setField(TABLE_COL_INDEX_CHECKBOX, "");
		return tmp;
	}

	private boolean hasService(SearchResultRow row) {
		return !isEmpty(row.getField(TABLE_COL_INDEX_STARTTIME));
	}

	@Override
	public void onAddService() {
		serviceAddView.popup();
	}

	@Override
	public void onModifyService() {
		Set<SearchResultRow> selected = getView().getSelectedSet();
		if (selected == null || selected.isEmpty()) {
			showStatus(ACTION_SELECTED_FAILURE[LAN_SELECT]);
			return;
		}
		List<SearchResultRow> list = new ArrayList<SearchResultRow>();
		for (SearchResultRow row : selected) {
			if (hasService(row)) {
				list.add(copyRow(row));
			}
		}
		getView().openMirrorMode(MirrorModeType.MODIFY_SERVICE, sortSearchResultRow(list));
	}

	@Override
	public void onDeleteService() {
		Set<SearchResultRow> selected = getView().getSelectedSet();
		if (selected == null || selected.isEmpty()) {
			showStatus(ACTION_SELECTED_FAILURE[LAN_SELECT]);
			return;
		}
		List<SearchResultRow> list = new ArrayList<SearchResultRow>();
		for (SearchResultRow row : selected) {
			if (hasService(row)) {
				list.add(copyRow(row));
			}
		}
		getView().openMirrorMode(MirrorModeType.DELETE_SERVICE, sortSearchResultRow(list));
	}

	@Override
	public void onClearSelection() {
		getView().clearSelection();
	}

	@Override
	public void onMirrorBack() {
		getView().closeMirrorMode();
	}

	@Override
	public void onMirrorDeleteAll() {
		List<SearchResultRow> data;
		switch (getView().getMirrorModeType()) {
		case DELETE_SERVICE:
			data = getView().getMirrorTable().getData();
			if (data != null && data.size() != 0) {
				if (Window.confirm(DELETE_ALL_SERVICE_CONFIRM[LAN_SELECT])) {
					handleDeleteService(data);
				}
			}
			break;
		default:
			break;
		}
	}

	private List<SearchResultRow> sortSearchResultRow(List<SearchResultRow> list) {
		Collections.sort(list, new Comparator<SearchResultRow>() {

			@Override
			public int compare(SearchResultRow arg0, SearchResultRow arg1) {
				String v0 = arg0.getField(TABLE_COL_INDEX_NO);
				String v1 = arg1.getField(TABLE_COL_INDEX_NO);
				if (v0 == null) {
					return v1 == null ? 0 : 1;
				}
				if (v1 == null) {
					return -1;
				}
				try {
					return Integer.parseInt(v0) - Integer.parseInt(v1);
				}
				catch (Exception e) {
					e.printStackTrace();
				}
				return 0;
			}

		});
		return list;
	}

}
