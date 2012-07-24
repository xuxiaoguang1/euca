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
import com.eucalyptus.webui.client.view.DeviceServerView;
import com.eucalyptus.webui.client.view.HasValueWidget;
import com.eucalyptus.webui.shared.checker.InvalidValueException;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class DeviceServerActivity extends AbstractSearchActivity implements DeviceServerView.Presenter {

	private static final int LAN_SELECT = 1;

	private static final Logger LOG = Logger.getLogger(DeviceServerActivity.class.getName());

	public static final String TITLE[] = {"SERVERs", "服务器"};

	public DeviceServerActivity(SearchPlace place, ClientFactory clientFactory) {
		super(place, clientFactory);
	}

	private EucalyptusServiceAsync getBackendService() {
		return clientFactory.getBackendService();
	}

	private Session getSession() {
		return clientFactory.getLocalSession().getSession();
	}

	private int queryState = ServerState.getInvalidValue();

	@Override
	protected void doSearch(String query, SearchRange range) {
		getBackendService().lookupDeviceServer(getSession(), query, range, queryState, new AsyncCallback<SearchResult>() {

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
	protected String getTitle() {
		return TITLE[LAN_SELECT];
	}

	@Override
	protected void showView(SearchResult result) {
		DeviceServerView view = (DeviceServerView)this.view;
		if (view == null) {
			view = clientFactory.getDeviceServerView();
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
	public void onSecretKey() {
		// TODO Auto-generated method stub
		//System.err.println(Debug.footprint());
	}

	@Override
	public void onPower() {
		// TODO Auto-generated method stub
		//System.err.println(Debug.footprint());
	}

	@Override
	public void onConnect() {
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

	public static class ServerState {

		final State state;
		final String[] STATE_VALUES;

		private ServerState(State state) {
			this.state = state;
			switch (state) {
			default:
				throw new InvalidValueException("" + state);
			case START:
				STATE_VALUES = new String[]{"NORMAL", "正常"};
				return;
			case STOP:
				STATE_VALUES = new String[]{"STOP", "停止"};
				return;
			case ERROR:
				STATE_VALUES = new String[]{"ERROR", "故障"};
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
			case ERROR:
				return 2;
			}
		}

		enum State {
			START, STOP, ERROR;
		};

		final private static ServerState[] ALL_STATES = {new ServerState(State.START), new ServerState(State.STOP),
		        new ServerState(State.ERROR)};

		public static ServerState getServerState(int state) {
			for (ServerState s : ALL_STATES) {
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
