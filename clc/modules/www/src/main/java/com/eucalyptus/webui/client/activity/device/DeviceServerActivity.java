package com.eucalyptus.webui.client.activity.device;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.eucalyptus.webui.client.ClientFactory;
import com.eucalyptus.webui.client.activity.AbstractSearchActivity;
import com.eucalyptus.webui.client.place.SearchPlace;
import com.eucalyptus.webui.client.service.EucalyptusServiceAsync;
import com.eucalyptus.webui.client.service.EucalyptusServiceException;
import com.eucalyptus.webui.client.service.SearchRange;
import com.eucalyptus.webui.client.service.SearchResult;
import com.eucalyptus.webui.client.service.SearchResultRow;
import com.eucalyptus.webui.client.session.Session;
import com.eucalyptus.webui.client.view.DeviceServerAddView;
import com.eucalyptus.webui.client.view.DeviceServerAddViewImpl;
import com.eucalyptus.webui.client.view.DeviceServerModifyView;
import com.eucalyptus.webui.client.view.DeviceServerModifyViewImpl;
import com.eucalyptus.webui.client.view.DeviceServerOperateView;
import com.eucalyptus.webui.client.view.DeviceServerOperateViewImpl;
import com.eucalyptus.webui.client.view.DeviceServerView;
import com.eucalyptus.webui.client.view.FooterView;
import com.eucalyptus.webui.client.view.HasValueWidget;
import com.eucalyptus.webui.client.view.LogView;
import com.eucalyptus.webui.client.view.FooterView.StatusType;
import com.eucalyptus.webui.client.view.LogView.LogType;
import com.eucalyptus.webui.shared.resource.device.status.ServerState;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class DeviceServerActivity extends AbstractSearchActivity implements DeviceServerView.Presenter {

	private static final ClientMessage title = new ClientMessage("Server", "服务器");
	
	private Date dateBegin;
	private Date dateEnd;
	private ServerState queryState = null;
	private Map<Integer, Integer> serverCounts = new HashMap<Integer, Integer>();

	private DeviceServerAddView serverAddView;
	private DeviceServerModifyView serverModifyView;
	private DeviceServerOperateView serverOperateView;
	
	public DeviceServerActivity(SearchPlace place, ClientFactory clientFactory) {
		super(place, clientFactory);
	}
	
	private DeviceServerView getView() {
		DeviceServerView view = (DeviceServerView)this.view;
		if (view == null) {
			view = clientFactory.getDeviceServerView();
			view.setPresenter(this);
			container.setWidget(view);
			view.clear();
			this.view = view;
		}
		return view;
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
	
	private final static int SERVER_ID = 0;
	private final static int SERVER_NAME = 3;
	private final static int SERVER_DESC = 4;
	private final static int SERVER_IP = 6;
	private final static int SERVER_BW = 7;
	private final static int SERVER_STATE = 8;
	
	private boolean isEmpty(String s) {
		return s == null || s.length() == 0;
	}
	
    private void showStatus(ClientMessage msg) {
        getFooterView().showStatus(StatusType.NONE, msg.toString(), FooterView.CLEAR_DELAY_SECOND * 3);
        getLogView().log(LogType.INFO, msg.toString());
    }
    
    private void onFrontendServiceFailure(Throwable caught) {
        Window.alert(new ClientMessage("", "前端服务运行错误").toString());
        getLogView().log(LogType.ERROR, caught.toString());
    }
    
    private void onBackendServiceFailure(Throwable caught) {
        if (caught instanceof EucalyptusServiceException) {
            EucalyptusServiceException exception = (EucalyptusServiceException)caught;
            ClientMessage msg = exception.getFrontendMessage();
            if (msg == null) {
                msg = new ClientMessage("Backend Service Failure", "后代服务运行错误");
            }
            Window.alert(msg.toString());
            getLogView().log(LogType.ERROR, msg.toString() + " : " + caught.toString());
        }
        else {
            getLogView().log(LogType.ERROR, caught.toString());
        }
    }
    
	@Override
	public void saveValue(ArrayList<String> keys, ArrayList<HasValueWidget> values) {
		/* do nothing */
	}
	
	@Override
	protected void doSearch(String query, SearchRange range) {
		getBackendService().lookupDeviceServerByDate(getSession(), range, queryState, dateBegin, dateEnd, new AsyncCallback<SearchResult>() {

			@Override
			public void onFailure(Throwable caught) {
				if (caught instanceof EucalyptusServiceException) {
					onBackendServiceFailure((EucalyptusServiceException)caught);
				}
				displayData(null);
			}

			@Override
			public void onSuccess(SearchResult result) {
				showStatus(new ClientMessage("", "查询服务器成功"));
				displayData(result);
			}
			
		});
		getBackendService().lookupDeviceServerCounts(getSession(), new AsyncCallback<Map<Integer, Integer>>() {

			@Override
			public void onFailure(Throwable caught) {
				if (caught instanceof EucalyptusServiceException) {
					onBackendServiceFailure((EucalyptusServiceException)caught);
				}
			}

			@Override
			public void onSuccess(Map<Integer, Integer> result) {
				showStatus(new ClientMessage("", "查询服务器数量成功"));
				serverCounts = result;
				getView().updateLabels();
			}
			
		});
	}
	
	@Override
	protected String getTitle() {
		return title.toString();
	}

	@Override
	protected void showView(SearchResult result) {
		getView().showSearchResult(result);
	}
	
	@Override
	public void onSelectionChange(Set<SearchResultRow> selection) {
		/* do nothing */
	}

	@Override
	public void onClick(SearchResultRow row, int row_index, int column_index) {
		/* do nothing */
	}

	@Override
	public void onHover(SearchResultRow row, int row_index, int columin_index) {
		/* do nothing */
	}

	@Override
	public void onDoubleClick(SearchResultRow row, int row_index, int column_index) {
		getView().setSelectedRow(row);
	}
	
	@Override
	public void onAddServer() {
		try {
			if (Window.confirm(new ClientMessage("", "确认创建新服务器").toString())) {
				if (serverAddView == null) {
					serverAddView = new DeviceServerAddViewImpl();
					serverAddView.setPresenter(new DeviceServerAddView.Presenter() {
						
						@Override
						public boolean onOK(String server_name, String server_desc, String server_ip, String bandwidth, ServerState server_state, String cabinet_name) {
							if (isEmpty(server_name)) {
								StringBuilder sb = new StringBuilder();
								sb.append(new ClientMessage("", "服务器名称非法")).append(" = '").append(server_name).append("' ");
								sb.append(new ClientMessage("", "请重新选择服务器"));
								Window.alert(sb.toString());
								return false;
							}
							if (isEmpty(cabinet_name)) {
								StringBuilder sb = new StringBuilder();
								sb.append(new ClientMessage("", "机柜名称非法")).append(" = '").append(cabinet_name).append("' ");
								sb.append(new ClientMessage("", "请重新选择机柜"));
								Window.alert(sb.toString());
								return false;
							}
							int server_bw = 0;
							try {
								if (!isEmpty(bandwidth)) {
									server_bw = Integer.parseInt(bandwidth);
								}
							}
							catch (Exception e) {
								StringBuilder sb = new StringBuilder();
								sb.append(new ClientMessage("", "带宽数值非法")).append(" = '").append(bandwidth).append("' ");
								sb.append(new ClientMessage("", "请重新选择带宽"));
								Window.alert(sb.toString());
								return false;
							}
							getBackendService().addDeviceServer(getSession(), server_name, server_desc, server_ip, server_bw, server_state, cabinet_name, new AsyncCallback<Void>() {

								@Override
								public void onFailure(Throwable caught) {
									if (caught instanceof EucalyptusServiceException) {
										onBackendServiceFailure((EucalyptusServiceException)caught);
									}
									getView().clearSelection();
								}

								@Override
								public void onSuccess(Void result) {
									showStatus(new ClientMessage("", "添加服务器成功"));
									reloadCurrentRange();
									getView().clearSelection();
								}
								
							});
							return true;
						}
						
						@Override
						public void lookupAreaNames() {
							getBackendService().lookupDeviceAreaNames(getSession(), new AsyncCallback<List<String>>() {

								@Override
								public void onFailure(Throwable caught) {
									if (caught instanceof EucalyptusServiceException) {
										onBackendServiceFailure((EucalyptusServiceException)caught);
									}
								}

								@Override
								public void onSuccess(List<String> area_name_list) {
									showStatus(new ClientMessage("", "获取区域列表成功"));
									serverAddView.setAreaNameList(area_name_list);
								}
								
							});
						}
						
						@Override
						public void lookupRoomNamesByAreaName(final String area_name) {
							if (isEmpty(area_name)) {
								StringBuilder sb = new StringBuilder();
								sb.append(new ClientMessage("", "区域名称非法")).append(" = '").append(area_name).append("'");
								Window.alert(sb.toString());
							}
							else {
								getBackendService().lookupDeviceRoomNamesByAreaName(getSession(), area_name, new AsyncCallback<List<String>>() {

									@Override
									public void onFailure(Throwable caught) {
										if (caught instanceof EucalyptusServiceException) {
											onBackendServiceFailure((EucalyptusServiceException)caught);
										}
									}

									@Override
									public void onSuccess(List<String> room_name_list) {
										showStatus(new ClientMessage("", "获取机房列表成功"));
										serverAddView.setRoomNameList(area_name, room_name_list);
									}
									
								});
							}
						}
						
						@Override
						public void lookupCabinetNamesByRoomName(final String room_name) {
							if (isEmpty(room_name)) {
								StringBuilder sb = new StringBuilder();
								sb.append(new ClientMessage("", "机房名称非法")).append(" = '").append(room_name).append("'");
								Window.alert(sb.toString());
							}
							else {
								getBackendService().lookupCabinetNamesByRoomName(getSession(), room_name, new AsyncCallback<List<String>>() {

									@Override
									public void onFailure(Throwable caught) {
										if (caught instanceof EucalyptusServiceException) {
											onBackendServiceFailure((EucalyptusServiceException)caught);
										}
									}

									@Override
									public void onSuccess(List<String> cabinet_name_list) {
										showStatus(new ClientMessage("", "获取机柜列表成功"));
										serverAddView.setCabinetNameList(room_name, cabinet_name_list);
									}
									
								});
							}
						}
						
					});
				}
				serverAddView.popup();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			onFrontendServiceFailure(e);
		}
	}
	
	@Override
	public void onModifyServer() {
		Set<SearchResultRow> set = getView().getSelectedSet();
		try {
			if (set != null && !set.isEmpty()) {
				SearchResultRow row = new LinkedList<SearchResultRow>(set).getFirst();
				if (Window.confirm(new ClientMessage("", "确认修改所选择的服务器").toString())) {
					int server_id = Integer.parseInt(row.getField(SERVER_ID));
					String server_name = row.getField(SERVER_NAME);
					String server_desc = row.getField(SERVER_DESC);
					String server_ip = row.getField(SERVER_IP);
					String server_bw = row.getField(SERVER_BW);
					ServerState server_state = ServerState.parse(row.getField(SERVER_STATE));
					if (serverModifyView == null) {
						serverModifyView = new DeviceServerModifyViewImpl();
						serverModifyView.setPresenter(new DeviceServerModifyView.Presenter() {
							
							@Override
							public boolean onOK(int server_id, String server_desc, String server_ip, String bandwidth, ServerState server_state) {
								int server_bw = 0;
								try {
									if (!isEmpty(bandwidth)) {
										server_bw = Integer.parseInt(bandwidth);
									}
								}
								catch (Exception e) {
									StringBuilder sb = new StringBuilder();
									sb.append(new ClientMessage("", "带宽数值非法")).append(" = '").append(server_bw).append("' ");
									sb.append(new ClientMessage("", "请重新选择带宽"));
									Window.alert(sb.toString());
									return false;
								}
								getBackendService().modifyDeviceServer(getSession(), server_id, server_desc, server_ip, server_bw, server_state, new AsyncCallback<Void>() {

									@Override
									public void onFailure(Throwable caught) {
										if (caught instanceof EucalyptusServiceException) {
											onBackendServiceFailure((EucalyptusServiceException)caught);
										}
										getView().clearSelection();
									}

									@Override
									public void onSuccess(Void result) {
										showStatus(new ClientMessage("", "修改服务器成功"));
										reloadCurrentRange();
										getView().clearSelection();
									}
									
								});
								return true;
							}
							
						});
					}
					serverModifyView.popup(server_id, server_name, server_desc, server_ip, server_bw, server_state);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			onFrontendServiceFailure(e);
		}
	}

	@Override
	public void onOperateServer() {
		Set<SearchResultRow> set = getView().getSelectedSet();
		try {
			if (set != null && !set.isEmpty()) {
				SearchResultRow row = new LinkedList<SearchResultRow>(set).getFirst();
				if (Window.confirm(new ClientMessage("", "确认操作所选择的服务器").toString())) {
					int server_id = Integer.parseInt(row.getField(SERVER_ID));
					String server_name = row.getField(SERVER_NAME);
					ServerState server_state = ServerState.parse(row.getField(SERVER_STATE));
					if (serverOperateView == null) {
						serverOperateView = new DeviceServerOperateViewImpl();
						serverOperateView.setPresenter(new DeviceServerOperateView.Presenter() {
							
							@Override
							public void onOK(int server_id, ServerState server_state) {
								getBackendService().modifyDeviceServerState(getSession(), server_id, server_state, new AsyncCallback<Void>() {

									@Override
									public void onFailure(Throwable caught) {
										if (caught instanceof EucalyptusServiceException) {
											onBackendServiceFailure((EucalyptusServiceException)caught);
										}
										getView().clearSelection();
									}

									@Override
									public void onSuccess(Void result) {
										showStatus(new ClientMessage("", "修改服务器状态成功"));
										reloadCurrentRange();
										getView().clearSelection();
									}
									
								});
							}
							
						});
					}
					serverOperateView.popup(server_id, server_name, server_state);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			onFrontendServiceFailure(e);
		}

	}

	@Override
	public void onDeleteServer() {
		Set<SearchResultRow> set = getView().getSelectedSet();
		try {
			if (set != null && !set.isEmpty()) {
				List<Integer> server_id_list = new ArrayList<Integer>();
				for (SearchResultRow row : set) {
					int server_id = Integer.parseInt(row.getField(SERVER_ID));
					server_id_list.add(server_id);
				}
				if (!server_id_list.isEmpty()) {
					if (Window.confirm(new ClientMessage("", "确认删除所选择的服务器").toString())) {
						getBackendService().deleteDeviceServer(getSession(), server_id_list, new AsyncCallback<Void>() {
		
							@Override
							public void onFailure(Throwable caught) {
								if (caught instanceof EucalyptusServiceException) {
									onBackendServiceFailure((EucalyptusServiceException)caught);
								}
								getView().clearSelection();
							}
		
							@Override
							public void onSuccess(Void result) {
								showStatus(new ClientMessage("", "删除服务器成功"));
								reloadCurrentRange();
								getView().clearSelection();
							}
							
						});
					}
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			onFrontendServiceFailure(e);
		}
	}

	@Override
	public ServerState getQueryState() {
		return queryState;
	}
	
	@Override
	public void setQueryState(ServerState queryState) {
		if (this.queryState != queryState) {
	    	getView().clearSelection();
			this.queryState = queryState;
	    	range = new SearchRange(0, getView().getPageSize(), -1, true);
	    	reloadCurrentRange();
		}
	}
	
	@Override
	public int getCounts(ServerState state) {
		Integer count = serverCounts.get(state == null ? -1 : state.getValue());
		if (count == null) {
			return 0;
		}
		return count;
	}
	
	@Override
	public void updateSearchResult(Date dateBegin, Date dateEnd) {
    	getView().clearSelection();
    	this.dateBegin = dateBegin;
    	this.dateEnd = dateEnd;
    	range = new SearchRange(0, getView().getPageSize(), -1, true);
    	reloadCurrentRange();
	}
	
}
