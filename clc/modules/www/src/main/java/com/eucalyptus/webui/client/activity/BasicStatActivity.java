package com.eucalyptus.webui.client.activity;

import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Logger;

import com.eucalyptus.webui.client.ClientFactory;
import com.eucalyptus.webui.client.activity.device.DeviceDiskActivity.DiskState;
import com.eucalyptus.webui.client.place.SearchPlace;
import com.eucalyptus.webui.client.service.SearchRange;
import com.eucalyptus.webui.client.service.SearchResult;
import com.eucalyptus.webui.client.view.FooterView;
import com.eucalyptus.webui.client.view.HasValueWidget;
import com.eucalyptus.webui.client.view.BasicStatView;
import com.eucalyptus.webui.client.view.FooterView.StatusType;
import com.eucalyptus.webui.client.view.LogView.LogType;
import com.eucalyptus.webui.shared.checker.InvalidValueException;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class BasicStatActivity extends AbstractSearchActivity implements BasicStatView.Presenter {
	
	private static final int LAN_SELECT = 1;

	public static final String TITLE[] = { "Basic", "基本统计信息" };
	
	private static final String[] QUERY_COUNT_FAILURE = {"", "获取资源失败"};

	private static final Logger LOG = Logger.getLogger(BasicStatActivity.class.getName());

	public BasicStatActivity(SearchPlace place, ClientFactory clientFactory) {
		super(place, clientFactory);
	}

	@Override
	protected void doSearch(String query, SearchRange range) {
		reloadServerLabels();
		
		reloadDiskLabels();
		reloadUserLabels();
	}
	
	private void reloadServerLabels() {
		clientFactory.getBackendService().getDeviceServerCounts(clientFactory.getLocalSession().getSession(), new AsyncCallback<Map<Integer, Integer>>() {

			@Override
			public void onFailure(Throwable caught) {
				log(QUERY_COUNT_FAILURE[LAN_SELECT], caught);
			}

			@Override
			public void onSuccess(Map<Integer, Integer> result) {
				ServerState.reset();
				for (Map.Entry<Integer, Integer> entry : result.entrySet()) {
					ServerState.setCount(ServerState.getServerState(entry.getKey()), entry.getValue());
				}
				getView().updateServerLabels();
			}

		});
	}
	
	private void reloadVMLabels() {
		
	}
	
	private void reloadDiskLabels() {
		clientFactory.getBackendService().getDeviceDiskCounts(clientFactory.getLocalSession().getSession(), new AsyncCallback<Map<Integer, Long>>() {

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
				getView().updateDiskLabels();
			}

		});
	}
	
	private void reloadUserLabels() {
		clientFactory.getBackendService().getUserCounts(clientFactory.getLocalSession().getSession(), new AsyncCallback<ArrayList<Integer>>() {

			@Override
			public void onFailure(Throwable caught) {
				log(QUERY_COUNT_FAILURE[LAN_SELECT], caught);
			}

			@Override
			public void onSuccess(ArrayList<Integer> result) {
				getView().updateUserLabels(result.get(0), result.get(1));
			}

		});
	}
	


	@Override
	protected String getTitle() {
		return TITLE[1];
	}
	
	private BasicStatView getView() {
		BasicStatView view = (BasicStatView)this.view;
		if (view == null) {
			view = clientFactory.getBasicStatView();
			view.setPresenter(this);
			container.setWidget(view);
			this.view = view;
		}
		return view;
	}

	@Override
	protected void showView(SearchResult result) {
		getView().showSearchResult(result);
	}

	@Override
	public void onRefresh() {
		reloadServerLabels();
		
		reloadDiskLabels();
		reloadUserLabels();
	}
	
	private void log(String msg, Throwable caught) {
		clientFactory.getShellView().getFooterView().showStatus(StatusType.ERROR, msg, FooterView.CLEAR_DELAY_SECOND * 5);
		clientFactory.getShellView().getLogView().log(LogType.ERROR, msg + ": " + caught.getMessage());
	}
	
	public static class ServerState {

		final private int value;
		final private String[] STATE_VALUES;
		private int count;

		public static final ServerState INUSE = new ServerState(0);
		public static final ServerState ERROR = new ServerState(1);
		public static final ServerState STOP = new ServerState(2);

		private ServerState(int value) {
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
				STATE_VALUES = new String[]{"ERROR", "故障"};
				return;
			case 2:
				STATE_VALUES = new String[]{"STOP", "停止"};
				return;
			}
		}

		public int getValue() {
			return value;
		}

		public static int getValue(ServerState state) {
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
			else if (state.equals(ERROR.toString())) {
				return ERROR.getValue();
			}
			else if (state.equals(STOP.toString())) {
				return STOP.getValue();
			}
			throw new InvalidValueException(state);
		}

		public static ServerState getServerState(int value) {
			if (value == -1) {
				return null;
			}
			else if (value == INUSE.value) {
				return INUSE;
			}
			else if (value == ERROR.value) {
				return ERROR;
			}
			else if (value == STOP.value) {
				return STOP;
			}
			throw new InvalidValueException(Integer.toString(value));
		}

		public static int getCount(ServerState state) {
			if (state != null) {
				return state.count;
			}
			else {
				return countTotal;
			}
		}

		public static void setCount(ServerState state, int count) {
			if (state != null) {
				state.count = count;
			}
			else {
				countTotal = count;
			}
		}

		public static void reset() {
			INUSE.count = 0;
			ERROR.count = 0;
			STOP.count = 0;
			countTotal = 0;
		}

		private static int countTotal = 0;

		@Override
		public String toString() {
			return STATE_VALUES[LAN_SELECT];
		}

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
	public void saveValue(ArrayList<String> keys, ArrayList<HasValueWidget> values) {
		//TODO: nothing...
	}

	@Override
	public int getServerCounts(ServerState state) {
		return ServerState.getCount(state);
	}

	@Override
	public long getDiskCounts(DiskState state) {
		return DiskState.getCount(state);
	}
	
	
}
