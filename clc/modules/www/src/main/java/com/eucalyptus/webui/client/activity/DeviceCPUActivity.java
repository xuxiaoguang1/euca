package com.eucalyptus.webui.client.activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.eucalyptus.webui.client.ClientFactory;
import com.eucalyptus.webui.client.place.SearchPlace;
import com.eucalyptus.webui.client.service.EucalyptusServiceAsync;
import com.eucalyptus.webui.client.service.SearchRange;
import com.eucalyptus.webui.client.service.SearchResult;
import com.eucalyptus.webui.client.service.SearchResultRow;
import com.eucalyptus.webui.client.session.Session;
import com.eucalyptus.webui.client.view.DeviceCPUView;
import com.eucalyptus.webui.client.view.DeviceServiceExtendView;
import com.eucalyptus.webui.client.view.DeviceCPUServiceModifyViewImpl;
import com.eucalyptus.webui.client.view.FooterView;
import com.eucalyptus.webui.client.view.HasValueWidget;
import com.eucalyptus.webui.client.view.LogView;
import com.eucalyptus.webui.client.view.DeviceCPUView.MirrorModeType;
import com.eucalyptus.webui.client.view.FooterView.StatusType;
import com.eucalyptus.webui.client.view.LogView.LogType;
import com.eucalyptus.webui.server.DeviceCPUServiceProcImpl;
import com.eucalyptus.webui.shared.checker.InvalidValueException;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class DeviceCPUActivity extends AbstractSearchActivity implements DeviceCPUView.Presenter {

	private static final int LAN_SELECT = 1;

	public static final String TITLE[] = {"CPUs", "CPU"};
	
	private DeviceCPUServiceModifyViewImpl serviceModifyView;
	
	private final boolean isSystemAdmin;
	
	public DeviceCPUActivity(SearchPlace place, ClientFactory clientFactory) {
		super(place, clientFactory);
		isSystemAdmin = clientFactory.getSessionData().getLoginUser().isSystemAdmin();
		serviceModifyView = new DeviceCPUServiceModifyViewImpl();
		serviceModifyView.setPresenter(new DeviceServiceExtendView.Presenter() {
			
			@Override
            public void onOK(String endtime, String state) {
				handleModifyExtend(endtime, state);
				getView().getMirrorTable().clearSelection();
            }
			
			@Override
			public void onCancel() {
				getView().getMirrorTable().clearSelection();
			}
			
		});
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

	private CPUState queryState = null;
	
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
		getBackendService().queryDeviceCPUCounts(getSession(), new AsyncCallback<Map<Integer, Integer>>(){

			@Override
            public void onFailure(Throwable caught) {
				log(QUERY_COUNT_FAILURE[LAN_SELECT], caught);
            }

			@Override
            public void onSuccess(Map<Integer, Integer> result) {
				for (Map.Entry<Integer, Integer> entry : result.entrySet()) {
					CPUState.setCount(CPUState.getCPUState(entry.getKey()), entry.getValue());
				}
				updateLabels();
            }
			
		});
		getBackendService().lookupDeviceCPU(getSession(), query, range, CPUState.getValue(queryState), new AsyncCallback<SearchResult>() {

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
	
	private DeviceCPUView getView() {
		DeviceCPUView view = (DeviceCPUView)this.view;
		if (view == null) {
			view = clientFactory.getDeviceCPUView();
			view.setPresenter(this);
			container.setWidget(view);
			view.clear();
			this.view = view;
		}
		return view;
	}
	
	private void updateLabels() {
		getView().updateLabels();
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
		// TODO Auto-generated method stub
		//System.err.println(Debug.footprint());
	}

	@Override
	protected String getTitle() {
		return TITLE[LAN_SELECT];
	}

	public static class CPUState {

		final private int value;
		final private String[] STATE_VALUES;
		private int count;

		public static final CPUState INUSE = new CPUState(0);
		public static final CPUState STOP = new CPUState(1);
		public static final CPUState RESERVED = new CPUState(2);

		private CPUState(int value) {
			assert(value >= 0);
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
		
		public static int getValue(CPUState state) {
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
				return getValue(INUSE);
			}
			else if (state.equals(STOP.toString())) {
				return getValue(STOP);
			}
			else if (state.equals(RESERVED.toString())) {
				return getValue(RESERVED);
			}
			throw new InvalidValueException(state);
		}

		public static CPUState getCPUState(int value) {
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
		
		public static int getCount(CPUState state) {
			if (state != null) {
				return state.count;
			}
			else {
				return countTotal;
			}
		}
		
		public static void setCount(CPUState state, int count) {
			if (state != null) {
				state.count = count;
			}
			else {
				countTotal = count;
			}
		}
		
		private static int countTotal = 0;
		
		@Override
		public String toString() {
			return STATE_VALUES[LAN_SELECT];
		}

	}

	@Override
    public void onAddService() {
	    // TODO Auto-generated method stub
	    
    }
	
	@Override
    public void onAddCPU() {
	    // TODO Auto-generated method stub
	    
    }

	@Override
    public void onDelCPU() {
	    // TODO Auto-generated method stub
	    
    }

	@Override
    public void onModifyCPU() {
	    // TODO Auto-generated method stub
	    
    }
	

	@Override
    public void onDelService() {
		Set<SearchResultRow> selected = getView().getSelectedSet();
		if (selected == null || selected.isEmpty()) {
			showStatus(ACTION_SELECTED_FAILURE[LAN_SELECT]);
		}
		int col;
		if (isSystemAdmin) {
			col = DeviceCPUServiceProcImpl.TABLE_COL_INDEX_ROOT_STATE;
		}
		else {
			col = DeviceCPUServiceProcImpl.TABLE_COL_INDEX_USER_STATE;
		}
		List<Integer> list = new ArrayList<Integer>();
		for (SearchResultRow row : selected) {
			if (!row.getField(col).equals(CPUState.getValue(CPUState.RESERVED))) {
				list.add(Integer.parseInt(row.getField(DeviceCPUServiceProcImpl.TABLE_COL_INDEX_CS_ID)));
			}
		}
		if (list.isEmpty()) {
			showStatus(ACTION_SELECTED_FAILURE[LAN_SELECT]);
			return ;
		}
		getBackendService().deleteDeviceCPUService(getSession(), list, new AsyncCallback<Boolean>() {

			@Override
            public void onFailure(Throwable caught) {
				ActivityUtil.logoutForInvalidSession(clientFactory, caught);
				log(DELETE_SERVICE_FAILURE[LAN_SELECT], caught);
            }

			@Override
            public void onSuccess(Boolean result) {
				if (result) {
					showStatus(DELETE_SERVICE_SUCCESS[LAN_SELECT]);
				}
				else {
					showStatus(DELETE_SERVICE_FAILURE[LAN_SELECT]);
				}
				reloadCurrentRange();
            }
			
		});
		getView().clearSelection();
    }
	
	@Override
	public void setQueryState(CPUState state) {
		if (this.queryState == state) {
			return;
		}
		this.queryState = state;
		doSearch(search, new SearchRange(0, DeviceCPUView.DEFAULT_PAGESIZE, -1, true));
    }
	
	@Override
    public CPUState getQueryState() {
		return queryState;
    }
	
	@Override
    public int getCounts(CPUState state) {
		return CPUState.getCount(state);
    }
	
	private static final String[] QUERY_COUNT_FAILURE = {"", "获取资源失败"};
	private static final String[] QUERY_TABLE_FAILURE = {"", "获取列表失败"};
	private static final String[] UPDATE_SERVICE_FAILURE = {"", "更新服务失败"};
	private static final String[] UPDATE_SERVICE_SUCCESS = {"", "更新服务成功"};
	private static final String[] DELETE_SERVICE_FAILURE = {"", "删除服务失败"};
	private static final String[] DELETE_SERVICE_SUCCESS = {"", "删除成功"};
	private static final String[] ACTION_SELECTED_FAILURE = {"", "请选择操作对象"};

	@Override
    public void onMirrorSelectRow(SearchResultRow row) {
		DeviceCPUView view = getView();
		if (row == null || !view.isMirrorMode()) {
			return ;
		}
		mirrorSelectedRow = row;
		if (view.getMirrorModeType() == MirrorModeType.MODIFY_SERVICE) {
			String starttime;
			String state;
			String life;
			if (isSystemAdmin) {
				starttime = row.getField(DeviceCPUServiceProcImpl.TABLE_COL_INDEX_ROOT_STARTTIME);
				state = row.getField(DeviceCPUServiceProcImpl.TABLE_COL_INDEX_ROOT_STATE);
				life = row.getField(DeviceCPUServiceProcImpl.TABLE_COL_INDEX_ROOT_LIFE);
			}
			else {
				starttime = row.getField(DeviceCPUServiceProcImpl.TABLE_COL_INDEX_USER_STARTTIME);
				state = row.getField(DeviceCPUServiceProcImpl.TABLE_COL_INDEX_USER_STATE);
				life = row.getField(DeviceCPUServiceProcImpl.TABLE_COL_INDEX_USER_LIFE);
			}
			assert(starttime != null && state != null && life != null);
			serviceModifyView.setValue(DeviceCPUServiceModifyViewImpl.getDate(starttime, Integer.parseInt(life)), new String[]{CPUState.INUSE.toString(), CPUState.STOP.toString()}, state);
		}
    }
	
	private SearchResultRow mirrorSelectedRow;
	
	private void handleModifyExtend(String endtime, String state) {
		DeviceCPUView view = getView();
		SearchResultRow from = mirrorSelectedRow;
		if (from == null || !view.isMirrorMode() || view.getMirrorModeType() != MirrorModeType.MODIFY_SERVICE) {
			return;
		}
		getBackendService().modifyDeviceCPUService(getSession(), from, endtime, CPUState.getValue(state), new AsyncCallback<SearchResultRow>() {
	
				@Override
	            public void onFailure(Throwable caught) {
					ActivityUtil.logoutForInvalidSession(clientFactory, caught);
					log(UPDATE_SERVICE_FAILURE[LAN_SELECT], caught);
	            }
	
				@Override
	            public void onSuccess(SearchResultRow result) {
					if (result != null) {
						showStatus(UPDATE_SERVICE_SUCCESS[LAN_SELECT]);
						getView().getMirrorTable().updateRow(result);
					}
					else {
						showStatus(UPDATE_SERVICE_FAILURE[LAN_SELECT]);
					}
	            }
				
			});
	}
	
	private SearchResultRow copyRow(SearchResultRow row) {
		SearchResultRow tmp = row.copy();
		tmp.setField(DeviceCPUServiceProcImpl.TABLE_COL_INDEX_CHECKBOX, "");
		return tmp;
	}
	
	@Override
    public void onExtendService() {
		Set<SearchResultRow> selected = getView().getSelectedSet();
		if (selected == null || selected.isEmpty()) {
			showStatus(ACTION_SELECTED_FAILURE[LAN_SELECT]);
			return ;
		}
		int col;
		if (isSystemAdmin) {
			col = DeviceCPUServiceProcImpl.TABLE_COL_INDEX_ROOT_STARTTIME;
		}
		else {
			col = DeviceCPUServiceProcImpl.TABLE_COL_INDEX_USER_STARTTIME;
		}
		List<SearchResultRow> list = new ArrayList<SearchResultRow>();
		for (SearchResultRow row : selected) {
			if (row.getField(col) != null) {
				list.add(copyRow(row));
			}
		}
		getView().openMirrorMode(MirrorModeType.MODIFY_SERVICE, sortSearchResultRow(list));
    }
	
	@Override
	public void onClearSelection() {
		getView().clearSelection();
	}

	@Override
    public void onMirrorCancel() {
		getView().closeMirrorMode();
		getView().clearSelection();
		reloadCurrentRange();
    }
	
	private List<SearchResultRow> sortSearchResultRow(List<SearchResultRow> list) {
		Collections.sort(list, new Comparator<SearchResultRow>() {

			@Override
            public int compare(SearchResultRow arg0, SearchResultRow arg1) {
				String v0 = arg0.getField(DeviceCPUServiceProcImpl.TABLE_COL_INDEX_NO);
				String v1 = arg1.getField(DeviceCPUServiceProcImpl.TABLE_COL_INDEX_NO);
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

	@Override
	public boolean updateRow(SearchResultRow row, SearchResultRow result) {
		int col = DeviceCPUServiceProcImpl.TABLE_COL_INDEX_CS_ID;
		if (row.getField(col).equals(result.getField(col))) {
			row.getRow().clear();
			row.getRow().addAll(result.getRow());
			row.setField(2, "+");
			return true;
		}
		return false;
	}
	
}
