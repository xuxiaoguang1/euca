package com.eucalyptus.webui.client.activity.device;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
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
import com.eucalyptus.webui.client.view.DeviceIPDeviceAddView;
import com.eucalyptus.webui.client.view.DeviceIPDeviceAddViewImpl;
import com.eucalyptus.webui.client.view.DeviceIPServiceAddView;
import com.eucalyptus.webui.client.view.DeviceIPServiceAddViewImpl;
import com.eucalyptus.webui.client.view.DeviceIPView;
import com.eucalyptus.webui.client.view.DeviceServiceDatePicker;
import com.eucalyptus.webui.client.view.DeviceServiceModifyView;
import com.eucalyptus.webui.client.view.DeviceServiceModifyViewImpl;
import com.eucalyptus.webui.client.view.FooterView;
import com.eucalyptus.webui.client.view.HasValueWidget;
import com.eucalyptus.webui.client.view.LogView;
import com.eucalyptus.webui.client.view.DeviceIPView.MirrorModeType;
import com.eucalyptus.webui.client.view.DeviceMirrorSearchResultTable.SearchResultRowMatcher;
import com.eucalyptus.webui.client.view.FooterView.StatusType;
import com.eucalyptus.webui.client.view.LogView.LogType;
import com.eucalyptus.webui.shared.checker.InvalidValueException;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class DeviceIPActivity extends AbstractSearchActivity implements DeviceIPView.Presenter {

	private static final int LAN_SELECT = 1;

	public static final String TITLE[] = {"IP", "IP"};

	private DeviceServiceModifyView serviceModifyView;
	private DeviceIPServiceAddView serviceAddView;
	private DeviceIPDeviceAddView deviceAddView;
	
	public static final int TABLE_COL_INDEX_IS_ID = 0;
	public static final int TABLE_COL_INDEX_IP_ID = 1;
	public static final int TABLE_COL_INDEX_CHECKBOX = 2;
	public static final int TABLE_COL_INDEX_NO = 3;
	public static final int TABLE_COL_INDEX_VM = 6;
	public static final int TABLE_COL_INDEX_ACCOUNT = 7;
	public static final int TABLE_COL_INDEX_USER = 8;
	public static final int TABLE_COL_INDEX_STARTTIME = 9;
	public static final int TABLE_COL_INDEX_LIFE = 10;
	public static final int TABLE_COL_INDEX_REMAINS = 11;
	public static final int TABLE_COL_INDEX_STATE = 12;

	public DeviceIPActivity(SearchPlace place, ClientFactory clientFactory) {
		super(place, clientFactory);
		final String[] stateValueList = new String[]{IPState.INUSE.toString(), IPState.STOP.toString()};
		serviceModifyView = new DeviceServiceModifyViewImpl();
		serviceModifyView.setPresenter(new DeviceServiceModifyView.Presenter() {

			@Override
			public boolean onOK(SearchResultRow row, Date starttime, Date endtime, String state) {
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
				handleModifyService(row, DeviceServiceDatePicker.format(endtime), state);
				getView().getMirrorTable().clearSelection();
				return true;
			}

			@Override
			public void onCancel() {
				getView().getMirrorTable().clearSelection();
			}

		});

		serviceAddView = new DeviceIPServiceAddViewImpl(stateValueList);
		serviceAddView.setPresenter(new DeviceIPServiceAddView.Presenter() {

			@Override
			public boolean onOK(SearchResultRow row, String account, String user, String vmMark,
					Date starttime, Date endtime, String state) {
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
				if (isEmpty(account) || isEmpty(user) || isEmpty(state) || isEmpty(vmMark)) {
					StringBuilder sb = new StringBuilder();
					sb.append(ADD_SERVICE_FAILURE_INVALID_ARGS[LAN_SELECT]).append("\n");
					sb.append("<account='").append(account).append("'").append(", ");
					sb.append("user='").append(user).append("'").append(", ");
					sb.append("vm='").append(vmMark).append("'").append(", ");
					sb.append("state='").append(state).append("'>");
					Window.alert(sb.toString());
					return false;
				}
				handleAddService(row, account, user, vmMark, DeviceServiceDatePicker.format(starttime),
						(int)(endtime.getTime() / div - starttime.getTime() / div), state);
				getView().getMirrorTable().clearSelection();
				return true;
			}

			@Override
			public void lookupAccounts() {
				getBackendService().listDeviceIPAccounts(getSession(), new AsyncCallback<List<String>>() {

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
				getBackendService().listDeviceIPUsersByAccount(getSession(), account, new AsyncCallback<List<String>>() {

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
            public void lookupVMsByUser(final String account, final String user) {
				getBackendService().listDeviceVMsByUser(getSession(), account, user, new AsyncCallback<List<String>>() {

					@Override
                    public void onFailure(Throwable caught) {
						log(QUERY_VMS_BY_USER_FAILURE[LAN_SELECT], caught);
                    }

					@Override
                    public void onSuccess(List<String> result) {
						if (result != null) {
							serviceAddView.setVMList(account, user, result);
						}
						else {
							showStatus(QUERY_VMS_BY_USER_FAILURE[LAN_SELECT]);
						}
                    }
				});
            }

			@Override
			public void onCancel() {
				getView().getMirrorTable().clearSelection();
			}

		});

		deviceAddView = new DeviceIPDeviceAddViewImpl();
		deviceAddView.setPresenter(new DeviceIPDeviceAddView.Presenter() {

			@Override
			public void onOK(List<String> publicList, List<String> privateList) {
				if (publicList != null || privateList != null) {
					handleAddDevice(publicList, privateList);
				}
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

	private IPState queryState = null;
	private IPType queryType = null;

	private void log(String msg, Throwable caught) {
		getFooterView().showStatus(StatusType.ERROR, msg, FooterView.CLEAR_DELAY_SECOND * 5);
		getLogView().log(LogType.ERROR, msg + ": " + caught.getMessage());
	}

	private void showStatus(String msg) {
		getFooterView().showStatus(StatusType.ERROR, msg, FooterView.CLEAR_DELAY_SECOND * 5);
		getLogView().log(LogType.ERROR, msg);
	}
	
	private void reloadLabels() {
		getBackendService().getDeviceIPCounts(getSession(), IPType.getValue(queryType), 
				new AsyncCallback<Map<Integer, Integer>>() {

			@Override
			public void onFailure(Throwable caught) {
				log(QUERY_COUNT_FAILURE[LAN_SELECT], caught);
			}

			@Override
			public void onSuccess(Map<Integer, Integer> result) {
				IPState.reset();
				for (Map.Entry<Integer, Integer> entry : result.entrySet()) {
					IPState.setCount(IPState.getIPState(entry.getKey()), entry.getValue());
				}
				getView().updateLabels();
			}

		});
	}

	@Override
	protected void doSearch(String query, SearchRange range) {
		getBackendService().lookupDeviceIP(getSession(), query, range, IPState.getValue(queryState),
				IPType.getValue(queryType), new AsyncCallback<SearchResult>() {

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
		reloadLabels();
	}

	private DeviceIPView getView() {
		DeviceIPView view = (DeviceIPView)this.view;
		if (view == null) {
			view = clientFactory.getDeviceIPView();
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
	
	public static class IPType {
		
		final private int value;
		final private String[] TYPE_VALUES;
		
		public static final IPType PUBLIC = new IPType(0);
		public static final IPType PRIVATE = new IPType(1);
		
		private IPType(int value) {
			assert(value >= 0);
			this.value = value;
			switch (value) {
			default:
				throw new InvalidValueException(Integer.toString(value));
			case 0:
				TYPE_VALUES = new String[]{"PUBLIC", "公有"};
				return;
			case 1:
				TYPE_VALUES = new String[]{"PRIVATE", "私有"};
				return;
			}
		}
		
		public int getValue() {
			return value;
		}
		
		public static int getValue(IPType type) {
			if (type == null) {
				return -1;
			}
			return type.value;
		}

		public static int getValue(String type) {
			if (type == null) {
				return -1;
			}
			else if (type.equals(PUBLIC.toString())) {
				return PUBLIC.getValue();
			}
			else if (type.equals(PRIVATE.toString())) {
				return PRIVATE.getValue();
			}
			throw new InvalidValueException(type);
		}

		public static IPType getIPType(int value) {
			if (value == -1) {
				return null;
			}
			else if (value == PUBLIC.value) {
				return PUBLIC;
			}
			else if (value == PRIVATE.value) {
				return PRIVATE;
			}
			throw new InvalidValueException(Integer.toString(value));
		}

		@Override
		public String toString() {
			return TYPE_VALUES[LAN_SELECT];
		}
		
	}

	public static class IPState {

		final private int value;
		final private String[] STATE_VALUES;
		private int count;

		public static final IPState INUSE = new IPState(0);
		public static final IPState STOP = new IPState(1);
		public static final IPState RESERVED = new IPState(2);

		private IPState(int value) {
			assert (value >= 0);
			this.value = value;
			this.count = 0;
			switch (value) {
			default:
				throw new InvalidValueException(Integer.toString(value));
			case 0:
				STATE_VALUES = new String[]{"NORMAL", "使用"};
				return;
			case 1:
				STATE_VALUES = new String[]{"STOP", "未使用"};
				return;
			case 2:
				STATE_VALUES = new String[]{"RESERVED", "预留"};
				return;
			}
		}

		public int getValue() {
			return value;
		}

		public static int getValue(IPState state) {
			if (state == null) {
				return -1;
			}
			return state.value;
		}

		public static int getValue(String state) {
			if (state == null) {
				return -1;
			}
			else if (state.equals(INUSE.toString())) {
				return INUSE.getValue();
			}
			else if (state.equals(STOP.toString())) {
				return STOP.getValue();
			}
			else if (state.equals(RESERVED.toString())) {
				return RESERVED.getValue();
			}
			throw new InvalidValueException(state);
		}

		public static IPState getIPState(int value) {
			if (value == -1) {
				return null;
			}
			else if (value == INUSE.value) {
				return INUSE;
			}
			else if (value == STOP.value) {
				return STOP;
			}
			else if (value == RESERVED.value) {
				return RESERVED;
			}
			throw new InvalidValueException(Integer.toString(value));
		}

		public static int getCount(IPState state) {
			if (state != null) {
				return state.count;
			}
			else {
				return countTotal;
			}
		}

		public static void setCount(IPState state, int count) {
			if (state != null) {
				state.count = count;
			}
			else {
				countTotal = count;
			}
		}

		public static void reset() {
			INUSE.count = 0;
			STOP.count = 0;
			RESERVED.count = 0;
			countTotal = 0;
		}

		private static int countTotal = 0;

		@Override
		public String toString() {
			return STATE_VALUES[LAN_SELECT];
		}

	}

	@Override
	public void setQueryState(IPState state) {
		getView().clearSelection();
		this.queryState = state;
		this.range = new SearchRange(0, DeviceIPView.DEFAULT_PAGESIZE, -1, true);
		reloadCurrentRange();
	}
	
	@Override
	public void setQueryType(IPType type) {
		getView().clearSelection();
		this.queryType = type;
		this.range = new SearchRange(0, DeviceIPView.DEFAULT_PAGESIZE, -1, true);
		reloadCurrentRange();
	}

	@Override
	public IPState getQueryState() {
		return queryState;
	}

	@Override
	public int getCounts(IPState state) {
		return IPState.getCount(state);
	}

	private static final String[] QUERY_COUNT_FAILURE = {"", "获取资源失败"};
	private static final String[] QUERY_TABLE_FAILURE = {"", "获取列表失败"};
	private static final String[] QUERY_ACCOUNTS_FAILURE = {"", "获取账户列表失败"};
	private static final String[] QUERY_USERS_BY_ACCOUNT_FAILURE = {"", "获取用户列表失败"};
	private static final String[] QUERY_VMS_BY_USER_FAILURE = {"", "获取虚拟机列表失败"};
	private static final String[] UPDATE_SERVICE_FAILURE = {"", "更新服务失败"};
	private static final String[] UPDATE_SERVICE_FAILURE_INVALID_DATE = {"", "更新服务失败：选择时间无效"};
	private static final String[] UPDATE_SERVICE_SUCCESS = {"", "更新服务成功"};
	private static final String[] ADD_DEVICE_SUCCESS = {"", "添加设备成功"};
	private static final String[] ADD_DEVICE_FAILURE = {"", "添加设备失败"};
	private static final String[] ADD_SERVICE_SUCCESS = {"", "添加服务成功"};
	private static final String[] ADD_SERVICE_FAILURE = {"", "添加服务失败"};
	private static final String[] ADD_SERVICE_FAILURE_INVALID_DATE = {"", "添加服务失败：选择时间无效"};
	private static final String[] ADD_SERVICE_FAILURE_INVALID_ARGS = {"", "添加服务失败：选择参数无效"};
	private static final String[] DELETE_SERVICE_FAILURE = {"", "删除服务失败"};
	private static final String[] DELETE_SERVICE_SUCCESS = {"", "删除服务成功"};
	private static final String[] DELETE_SERVICE_CONFIRM = {"", "确认删除所选择的 服务？"};
	private static final String[] DELETE_ALL_SERVICE_CONFIRM = {"", "确认删除所选择的 全部服务？"};
	private static final String[] ACTION_SELECTED_FAILURE = {"", "请选择操作对象"};
	private static final String[] DELETE_DEVICE_FAILURE = {"", "删除设备失败"};
	private static final String[] DELETE_DEVICE_SUCCESS = {"", "删除设备成功"};
	private static final String[] DELETE_DELETE_CONFIRM = {"", "确认删除所选择的 设备？"};
	private static final String[] DELETE_ALL_DEVICE_CONFIRM = {"", "确认删除所选择的 全部设备？"};

	private void prepareAddService(SearchResultRow row) {
		String starttime = row.getField(TABLE_COL_INDEX_STARTTIME);
		String state = row.getField(TABLE_COL_INDEX_STATE);
		String life = row.getField(TABLE_COL_INDEX_LIFE);
		Date date0 = null, date1 = null;
		if (!isEmpty(starttime)) {
			date0 = DeviceServiceDatePicker.parse(starttime);
			date1 = DeviceServiceDatePicker.parse(starttime, life);
		}
		serviceAddView.setValue(row, date0, date1, state);
	}

	private void prepareModifyService(SearchResultRow row) {
		String starttime = row.getField(TABLE_COL_INDEX_STARTTIME);
		String state = row.getField(TABLE_COL_INDEX_STATE);
		String life = row.getField(TABLE_COL_INDEX_LIFE);
		assert (!isEmpty(starttime) && !isEmpty(state) && !isEmpty(life));
		final String[] stateValueList = new String[]{IPState.INUSE.toString(), IPState.STOP.toString()};
		serviceModifyView.setValue(row, DeviceServiceDatePicker.parse(starttime),
		        DeviceServiceDatePicker.parse(starttime, life), stateValueList, state);
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

	private void prepareDeleteDevice(SearchResultRow row) {
		if (!Window.confirm(DELETE_DELETE_CONFIRM[LAN_SELECT])) {
			getView().getMirrorTable().clearSelection();
			return;
		}
		List<SearchResultRow> list = new ArrayList<SearchResultRow>();
		list.add(row);
		handleDeleteDevice(list);
	}

	@Override
	public void onMirrorSelectRow(SearchResultRow row) {
		if (row == null) {
			return;
		}
		switch (getView().getMirrorModeType()) {
		case ADD_SERVICE:
			prepareAddService(row);
			return;
		case MODIFY_SERVICE:
			prepareModifyService(row);
			return;
		case DELETE_SERVICE:
			prepareDeleteService(row);
			return;
		case DELETE_DEVICE:
			prepareDeleteDevice(row);
			return;
		default:
			return;
		}
	}

	private void handleAddDevice(List<String> publicList, List<String> privateList) {
		getBackendService().addDeviceIPDevice(getSession(), publicList, privateList, 
		        new AsyncCallback<Boolean>() {

			        @Override
			        public void onFailure(Throwable caught) {
				        ActivityUtil.logoutForInvalidSession(clientFactory, caught);
				        log(ADD_DEVICE_FAILURE[LAN_SELECT], caught);
			        }

			        @Override
			        public void onSuccess(Boolean result) {
				        if (!result) {
					        showStatus(ADD_DEVICE_FAILURE[LAN_SELECT]);
				        }
				        else {
					        showStatus(ADD_DEVICE_SUCCESS[LAN_SELECT]);
				        }
				        reloadCurrentRange();
			        }

		        });
	}

	private void handleAddService(SearchResultRow row, String account, String user, String vmMark,
			String starttime, int life, String state) {
		assert (row != null && getView().getMirrorModeType() == MirrorModeType.ADD_SERVICE);
		getBackendService().addDeviceIPService(getSession(), row, account, user, vmMark, starttime, life,
		        IPState.getValue(state), new AsyncCallback<SearchResultRow>() {

			        @Override
			        public void onFailure(Throwable caught) {
				        ActivityUtil.logoutForInvalidSession(clientFactory, caught);
				        log(ADD_SERVICE_FAILURE[LAN_SELECT], caught);
			        }

			        @Override
			        public void onSuccess(SearchResultRow result) {
				        if (result != null) {
					        showStatus(ADD_SERVICE_SUCCESS[LAN_SELECT]);
					        if (getView().isMirrorMode()) {
						        final int col = TABLE_COL_INDEX_IS_ID;
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
					        showStatus(ADD_SERVICE_FAILURE[LAN_SELECT]);
				        }
			        	reloadCurrentRange();
			        }

		        });
	}

	private void handleModifyService(SearchResultRow row, String endtime, String state) {
		assert (row != null && getView().getMirrorModeType() == MirrorModeType.MODIFY_SERVICE);
		getBackendService().modifyDeviceIPService(getSession(), row, endtime, IPState.getValue(state),
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
						        final int col = TABLE_COL_INDEX_IS_ID;
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
		DeviceIPView view = getView();
		assert (list.size() != 0 && view.getMirrorModeType() == MirrorModeType.DELETE_SERVICE);
		getBackendService().deleteDeviceIPService(getSession(), list, new AsyncCallback<List<SearchResultRow>>() {

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
						final int col = TABLE_COL_INDEX_IS_ID;
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

	private void handleDeleteDevice(List<SearchResultRow> list) {
		DeviceIPView view = getView();
		assert (list.size() != 0 && view.getMirrorModeType() == MirrorModeType.DELETE_DEVICE);
		getBackendService().deleteDeviceIPDevice(getSession(), list, new AsyncCallback<List<SearchResultRow>>() {

			@Override
			public void onFailure(Throwable caught) {
				ActivityUtil.logoutForInvalidSession(clientFactory, caught);
				log(DELETE_DEVICE_FAILURE[LAN_SELECT], caught);
			}

			@Override
			public void onSuccess(List<SearchResultRow> result) {
				if (result != null) {
					showStatus(DELETE_DEVICE_SUCCESS[LAN_SELECT]);
					if (getView().isMirrorMode()) {
						final int col = TABLE_COL_INDEX_IP_ID;
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
					showStatus(DELETE_DEVICE_FAILURE[LAN_SELECT]);
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
		Set<SearchResultRow> selected = getView().getSelectedSet();
		if (selected == null || selected.isEmpty()) {
			showStatus(ACTION_SELECTED_FAILURE[LAN_SELECT]);
			return;
		}
		List<SearchResultRow> list = new ArrayList<SearchResultRow>();
		for (SearchResultRow row : selected) {
			if (!hasService(row)) {
				list.add(copyRow(row));
			}
		}
		getView().openMirrorMode(MirrorModeType.ADD_SERVICE, sortSearchResultRow(list));
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
	public void onDeleteDevice() {
		Set<SearchResultRow> selected = getView().getSelectedSet();
		if (selected == null || selected.isEmpty()) {
			showStatus(ACTION_SELECTED_FAILURE[LAN_SELECT]);
			return;
		}
		List<SearchResultRow> list = new ArrayList<SearchResultRow>();
		for (SearchResultRow row : selected) {
			if (!hasService(row)) {
				list.add(copyRow(row));
			}
		}
		getView().openMirrorMode(MirrorModeType.DELETE_DEVICE, sortSearchResultRow(list));
	}

	@Override
	public void onAddDevice() {
		deviceAddView.popup();
	}

	@Override
	public void onClearSelection() {
		getView().clearSelection();
	}

	@Override
	public void onMirrorBack() {
		if (getView().getMirrorModeType() == MirrorModeType.ADD_SERVICE) {
			serviceAddView.clearCache();
		}
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
		case DELETE_DEVICE:
			data = getView().getMirrorTable().getData();
			if (data != null && data.size() != 0) {
				if (Window.confirm(DELETE_ALL_DEVICE_CONFIRM[LAN_SELECT])) {
					handleDeleteDevice(data);
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
