package com.eucalyptus.webui.client.activity;

import java.util.ArrayList;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.eucalyptus.webui.client.ClientFactory;
import com.eucalyptus.webui.client.place.SearchPlace;
import com.eucalyptus.webui.client.service.EucalyptusServiceAsync;
import com.eucalyptus.webui.client.service.SearchRange;
import com.eucalyptus.webui.client.service.SearchResult;
import com.eucalyptus.webui.client.service.SearchResultRow;
import com.eucalyptus.webui.client.session.Session;
import com.eucalyptus.webui.client.view.DeviceMemoryView;
import com.eucalyptus.webui.client.view.HasValueWidget;
import com.eucalyptus.webui.shared.checker.InvalidValueException;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class DeviceMemoryActivity extends AbstractSearchActivity implements DeviceMemoryView.Presenter {

	private static final int LAN_SELECT = 1;

	private static final Logger LOG = Logger.getLogger(DeviceMemoryActivity.class.getName());

	public static final String TITLE[] = {"Memory", "内存"};

	public DeviceMemoryActivity(SearchPlace place, ClientFactory clientFactory) {
		super(place, clientFactory);
	}

	private EucalyptusServiceAsync getBackendService() {
		return clientFactory.getBackendService();
	}

	private Session getSession() {
		return clientFactory.getLocalSession().getSession();
	}

	private int queryState = MemoryState.getInvalidValue();

	@Override
	protected void doSearch(String query, SearchRange range) {
		getBackendService().lookupDeviceMemory(getSession(), query, range, queryState, new AsyncCallback<SearchResult>() {

			@Override
			public void onFailure(Throwable caught) {
				ActivityUtil.logoutForInvalidSession(clientFactory, caught);
				LOG.log(Level.WARNING, "search failed: " + caught);
				displayData(null);
			}

			@Override
			public void onSuccess(SearchResult result) {
				LOG.log(Level.INFO, "search success: " + result);
				displayData(result);
			}

		});
	}

	@Override
	protected void showView(SearchResult result) {
		DeviceMemoryView view = (DeviceMemoryView)this.view;
		if (view == null) {
			view = clientFactory.getDeviceMemoryView();
			view.setPresenter(this);
			container.setWidget(view);
			view.clear();
			this.view = view;
		}
		view.showSearchResult(result);
	}

	@Override
	public void onSelectionChange(Set<SearchResultRow> selections) {
		// TODO Auto-generated method stub
		//System.err.println(Debug.footprint());

	}

	@Override
	public void saveValue(ArrayList<String> keys, ArrayList<HasValueWidget> values) {
		// TODO Auto-generated method stub
		//System.err.println(Debug.footprint());
	}

	@Override
	public void onAdd() {
		// TODO Auto-generated method stub
		//System.err.println(Debug.footprint());
	}

	@Override
	public void onExtend() {
		// TODO Auto-generated method stub
		//System.err.println(Debug.footprint());
	}

	@Override
	public void onModify() {
		// TODO Auto-generated method stub
		//System.err.println(Debug.footprint());
	}

	@Override
	public void onDelete() {
		// TODO Auto-generated method stub
		//System.err.println(Debug.footprint());
	}

	@Override
	protected String getTitle() {
		return TITLE[LAN_SELECT];
	}

	public static class MemoryState {

		final State state;
		final String[] STATE_VALUES;

		private MemoryState(State state) {
			this.state = state;
			switch (state) {
			default:
				throw new InvalidValueException("" + state);
			case START:
				STATE_VALUES = new String[]{"NORMAL", "使用"};
				return;
			case STOP:
				STATE_VALUES = new String[]{"STOP", "未使用"};
				return;
			case RESERVED:
				STATE_VALUES = new String[]{"RESERVED", "预留"};
				return;
			}
		}

		public static int getInvalidValue() {
			return - 1;
		}

		public int getValue() {
			switch (state) {
			default:
				throw new InvalidValueException("" + state);
			case START:
				return 0;
			case STOP:
				return 1;
			case RESERVED:
				return 2;
			}
		}

		enum State {
			START, STOP, RESERVED;
		};

		final private static MemoryState[] ALL_STATES = {new MemoryState(State.START), new MemoryState(State.STOP),
		        new MemoryState(State.RESERVED)};

		public static MemoryState getMemoryState(int state) {
			for (MemoryState s : ALL_STATES) {
				if (s.getValue() == state) {
					return s;
				}
			}
			throw new InvalidValueException("" + state);
		}

		@Override
		public String toString() {
			return STATE_VALUES[LAN_SELECT];
		}

	}

}
