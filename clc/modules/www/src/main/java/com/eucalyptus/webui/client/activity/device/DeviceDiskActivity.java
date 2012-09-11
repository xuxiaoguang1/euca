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
import com.eucalyptus.webui.client.view.FooterView;
import com.eucalyptus.webui.client.view.HasValueWidget;
import com.eucalyptus.webui.client.view.LogView;
import com.eucalyptus.webui.client.view.FooterView.StatusType;
import com.eucalyptus.webui.client.view.LogView.LogType;
import com.eucalyptus.webui.client.view.device.DeviceDiskDeviceAddView;
import com.eucalyptus.webui.client.view.device.DeviceDiskDeviceAddViewImpl;
import com.eucalyptus.webui.client.view.device.DeviceDiskServiceAddView;
import com.eucalyptus.webui.client.view.device.DeviceDiskServiceAddViewImpl;
import com.eucalyptus.webui.client.view.device.DeviceDiskView;
import com.eucalyptus.webui.client.view.device.DeviceServiceDatePicker;
import com.eucalyptus.webui.client.view.device.DeviceServiceModifyView;
import com.eucalyptus.webui.client.view.device.DeviceServiceModifyViewImpl;
import com.eucalyptus.webui.client.view.device.DeviceDiskDeviceAddView.DataCache;
import com.eucalyptus.webui.client.view.device.DeviceDiskView.MirrorModeType;
import com.eucalyptus.webui.client.view.device.DeviceMirrorSearchResultTable.SearchResultRowMatcher;
import com.eucalyptus.webui.shared.checker.InvalidValueException;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class DeviceDiskActivity extends AbstractSearchActivity implements DeviceDiskView.Presenter {

	private static final int LAN_SELECT = 1;

	public static final String TITLE[] = {"Disk", "磁盘"};

	private DeviceServiceModifyView serviceModifyView;
	private DeviceDiskServiceAddView serviceAddView;
	private DeviceDiskDeviceAddView deviceAddView;
	
	public static final long DIV_UNIT = 1000;

	public static final int TABLE_COL_INDEX_DS_ID = 0;
	public static final int TABLE_COL_INDEX_DISK_ID = 1;
	public static final int TABLE_COL_INDEX_CHECKBOX = 2;
	public static final int TABLE_COL_INDEX_NO = 3;
	public static final int TABLE_COL_INDEX_TOTAL = 6;
	public static final int TABLE_COL_INDEX_USED = 7;
	public static final int TABLE_COL_INDEX_ACCOUNT = 8;
	public static final int TABLE_COL_INDEX_USER = 9;
	public static final int TABLE_COL_INDEX_STARTTIME = 10;
	public static final int TABLE_COL_INDEX_LIFE = 11;
	public static final int TABLE_COL_INDEX_REMAINS = 12;
	public static final int TABLE_COL_INDEX_STATE = 13;

	public DeviceDiskActivity(SearchPlace place, ClientFactory clientFactory) {
		super(place, clientFactory);
		final String[] stateValueList = new String[]{DiskState.INUSE.toString(), DiskState.STOP.toString()};
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

		serviceAddView = new DeviceDiskServiceAddViewImpl(stateValueList);
		serviceAddView.setPresenter(new DeviceDiskServiceAddView.Presenter() {

			@Override
			public boolean onOK(SearchResultRow row, String account, String user, Date starttime, Date endtime,
			        String state, long used, long max) {
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
				if (isEmpty(account) || isEmpty(user) || isEmpty(state) || !(used > 0 && used <= max)) {
					StringBuilder sb = new StringBuilder();
					sb.append(ADD_SERVICE_FAILURE_INVALID_ARGS[LAN_SELECT]).append("\n");
					sb.append("<account='").append(account).append("'").append(", ");
					sb.append("user='").append(user).append("'").append(", ");
					sb.append("state='").append(state).append("'").append(", ");
					sb.append("used='").append(used).append("'>");
					Window.alert(sb.toString());
					return false;
				}
				handleAddService(row, account, user, DeviceServiceDatePicker.format(starttime), (int)(endtime.getTime()
				        / div - starttime.getTime() / div), state, used);
				getView().getMirrorTable().clearSelection();
				return true;
			}

			@Override
			public void lookupAccounts() {
				getBackendService().listDeviceDiskAccounts(getSession(), new AsyncCallback<List<String>>() {

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
				getBackendService().listDeviceDiskUsersByAccount(getSession(), account, new AsyncCallback<List<String>>() {

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
			public void onCancel() {
				getView().getMirrorTable().clearSelection();
			}

		});

		deviceAddView = new DeviceDiskDeviceAddViewImpl();
		deviceAddView.setPresenter(new DeviceDiskDeviceAddView.Presenter() {

			@Override
			public void lookupDevicesInfo() {
				getBackendService().lookupDeviceDiskInfo(getSession(), new AsyncCallback<DataCache>() {

					@Override
					public void onFailure(Throwable caught) {
						log(QUERY_DEVICES_INFO_FAILURE[LAN_SELECT], caught);
					}

					@Override
					public void onSuccess(DataCache result) {
						if (result != null) {
							deviceAddView.setDevicesInfo(result);
						}
						else {
							showStatus(QUERY_DEVICES_INFO_FAILURE[LAN_SELECT]);
						}
					}

				});
			}

			@Override
			public boolean onOK(String serverMark, String name, long total, int num) {
				if (serverMark == null || name == null || total <= 0) {
					StringBuilder sb = new StringBuilder();
					sb.append(ADD_DEVICE_FAILURE_INVALID_ARGS[LAN_SELECT]).append("\n");
					sb.append("<server='").append(serverMark).append("'").append(", ");
					sb.append("DiskName='").append(name).append("'").append(", ");
					sb.append("DiskTotal='").append(total).append("'>");
					Window.alert(sb.toString());
					return false;
				}
				handleAddDevice(serverMark, name, total, num);
				return true;
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

	private DiskState queryState = null;

	private void log(String msg, Throwable caught) {
		getFooterView().showStatus(StatusType.ERROR, msg, FooterView.CLEAR_DELAY_SECOND * 5);
		getLogView().log(LogType.ERROR, msg + ": " + caught.getMessage());
	}

	private void showStatus(String msg) {
		getFooterView().showStatus(StatusType.ERROR, msg, FooterView.CLEAR_DELAY_SECOND * 5);
		getLogView().log(LogType.ERROR, msg);
	}
	
	private void reloadLabels() {
		getBackendService().getDeviceDiskCounts(getSession(), new AsyncCallback<Map<Integer, Long>>() {

			@Override
			public void onFailure(Throwable caught) {
				log(QUERY_COUNT_FAILURE[LAN_SELECT], caught);
			}

			@Override
			public void onSuccess(Map<Integer, Long> result) {
				DiskState.reset();
				for (Map.Entry<Integer, Long> entry : result.entrySet()) {
					DiskState.setCount(DiskState.getDiskState(entry.getKey()), entry.getValue());
				}
				getView().updateLabels();
			}

		});
	}

	@Override
	protected void doSearch(String query, SearchRange range) {
		getBackendService().lookupDeviceDisk(getSession(), query, range, DiskState.getValue(queryState),
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
		reloadLabels();
	}

	private DeviceDiskView getView() {
		DeviceDiskView view = (DeviceDiskView)this.view;
		if (view == null) {
			view = clientFactory.getDeviceDiskView();
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

	public static class DiskState {

		final private int value;
		final private String[] STATE_VALUES;
		private long count;

		public static final DiskState INUSE = new DiskState(0);
		public static final DiskState STOP = new DiskState(1);
		public static final DiskState RESERVED = new DiskState(2);

		private DiskState(int value) {
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

		public static int getValue(DiskState state) {
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

		public static DiskState getDiskState(int value) {
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

		public static long getCount(DiskState state) {
			if (state != null) {
				return state.count;
			}
			else {
				return countTotal;
			}
		}

		public static void setCount(DiskState state, long count) {
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

		private static long countTotal = 0;

		@Override
		public String toString() {
			return STATE_VALUES[LAN_SELECT];
		}

	}

	@Override
	public void setQueryState(DiskState state) {
		getView().clearSelection();
		this.queryState = state;
		this.range = new SearchRange(0, DeviceDiskView.DEFAULT_PAGESIZE, -1, true);
		reloadCurrentRange();
	}

	@Override
	public DiskState getQueryState() {
		return queryState;
	}

	@Override
	public long getCounts(DiskState state) {
		return DiskState.getCount(state);
	}

	private static final String[] QUERY_COUNT_FAILURE = {"", "获取资源失败"};
	private static final String[] QUERY_TABLE_FAILURE = {"", "获取列表失败"};
	private static final String[] QUERY_ACCOUNTS_FAILURE = {"", "获取账户列表失败"};
	private static final String[] QUERY_DEVICES_INFO_FAILURE = {"", "获取资源列表失败"};
	private static final String[] QUERY_USERS_BY_ACCOUNT_FAILURE = {"", "获取用户列表失败"};
	private static final String[] UPDATE_SERVICE_FAILURE = {"", "更新服务失败"};
	private static final String[] UPDATE_SERVICE_FAILURE_INVALID_DATE = {"", "更新服务失败：选择时间无效"};
	private static final String[] UPDATE_SERVICE_SUCCESS = {"", "更新服务成功"};
	private static final String[] ADD_DEVICE_SUCCESS = {"", "添加设备成功"};
	private static final String[] ADD_DEVICE_FAILURE = {"", "添加设备失败"};
	private static final String[] ADD_SERVICE_SUCCESS = {"", "添加服务成功"};
	private static final String[] ADD_SERVICE_FAILURE = {"", "添加服务失败"};
	private static final String[] ADD_SERVICE_FAILURE_INVALID_DATE = {"", "添加服务失败：选择时间无效"};
	private static final String[] ADD_SERVICE_FAILURE_INVALID_ARGS = {"", "添加服务失败：选择参数无效"};
	private static final String[] ADD_DEVICE_FAILURE_INVALID_ARGS = {"", "添加设备失败：无效的参数"};
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
		String used = row.getField(TABLE_COL_INDEX_USED);
		Date date0 = null, date1 = null;
		if (!isEmpty(starttime)) {
			date0 = DeviceServiceDatePicker.parse(starttime);
			date1 = DeviceServiceDatePicker.parse(starttime, life);
		}
		serviceAddView.setValue(row, date0, date1, state, used);
	}

	private void prepareModifyService(SearchResultRow row) {
		String starttime = row.getField(TABLE_COL_INDEX_STARTTIME);
		String state = row.getField(TABLE_COL_INDEX_STATE);
		String life = row.getField(TABLE_COL_INDEX_LIFE);
		assert (!isEmpty(starttime) && !isEmpty(state) && !isEmpty(life));
		final String[] stateValueList = new String[]{DiskState.INUSE.toString(), DiskState.STOP.toString()};
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

	private void handleAddDevice(String serverMark, String name, long total, int num) {
		assert (!isEmpty(serverMark) && !isEmpty(name));
		getBackendService().addDeviceDiskDevice(getSession(), serverMark, name, total, num,
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

	private void handleAddService(SearchResultRow row, String account, String user, String starttime, int life,
	        String state, long used) {
		assert (row != null && getView().getMirrorModeType() == MirrorModeType.ADD_SERVICE);
		getBackendService().addDeviceDiskService(getSession(), row, account, user, used, starttime, life,
		        DiskState.getValue(state), new AsyncCallback<SearchResultRow>() {

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
						        final int col = TABLE_COL_INDEX_DS_ID;
						        result.setField(TABLE_COL_INDEX_CHECKBOX, "+");
						        final SearchResultRowMatcher matcher = new SearchResultRowMatcher() {

							        @Override
							        public boolean match(SearchResultRow row0, SearchResultRow row1) {
								        return row0.getField(col).equals(row1.getField(col));
							        }

						        };
						        long used = 0;
						        try {
						        	used = Long.parseLong(result.getField(TABLE_COL_INDEX_USED));
						        }
						        catch (Exception e) {
						        }
						        if (used != 0) {
						        	getView().getMirrorTable().updateRow(result, matcher);
						        }
						        else {
						        	getView().getMirrorTable().deleteRow(result, matcher);
						        }
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
		getBackendService().modifyDeviceDiskService(getSession(), row, endtime, DiskState.getValue(state),
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
						        final int col = TABLE_COL_INDEX_DS_ID;
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
		DeviceDiskView view = getView();
		assert (list.size() != 0 && view.getMirrorModeType() == MirrorModeType.DELETE_SERVICE);
		getBackendService().deleteDeviceDiskService(getSession(), list, new AsyncCallback<List<SearchResultRow>>() {

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
						final int col = TABLE_COL_INDEX_DS_ID;
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
		DeviceDiskView view = getView();
		assert (list.size() != 0 && view.getMirrorModeType() == MirrorModeType.DELETE_DEVICE);
		getBackendService().deleteDeviceDiskDevice(getSession(), list, new AsyncCallback<List<SearchResultRow>>() {

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
						final int col = TABLE_COL_INDEX_DISK_ID;
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
				String used = row.getField(TABLE_COL_INDEX_USED);
				String total = row.getField(TABLE_COL_INDEX_TOTAL);
				try {
					if (!isEmpty(total) && !isEmpty(used)) {
						if (Long.parseLong(total) != Long.parseLong(used)) {
							continue ;
						}
					}
				}
				catch (Exception e) {
				}
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
