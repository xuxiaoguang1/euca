package com.eucalyptus.webui.client.activity.device;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.eucalyptus.webui.client.ClientFactory;
import com.eucalyptus.webui.client.activity.AbstractSearchActivity;
import com.eucalyptus.webui.client.activity.ActivityUtil;
import com.eucalyptus.webui.client.place.SearchPlace;
import com.eucalyptus.webui.client.service.EucalyptusServiceAsync;
import com.eucalyptus.webui.client.service.EucalyptusServiceException;
import com.eucalyptus.webui.client.service.SearchRange;
import com.eucalyptus.webui.client.service.SearchResult;
import com.eucalyptus.webui.client.service.SearchResultRow;
import com.eucalyptus.webui.client.session.Session;
import com.eucalyptus.webui.client.view.DeviceServerDeviceAddView;
import com.eucalyptus.webui.client.view.DeviceServerDeviceAddViewImpl;
import com.eucalyptus.webui.client.view.DeviceServerModifyView;
import com.eucalyptus.webui.client.view.DeviceServerModifyViewImpl;
import com.eucalyptus.webui.client.view.DeviceServerView;
import com.eucalyptus.webui.client.view.FooterView;
import com.eucalyptus.webui.client.view.HasValueWidget;
import com.eucalyptus.webui.client.view.LogView;
import com.eucalyptus.webui.client.view.FooterView.StatusType;
import com.eucalyptus.webui.client.view.LogView.LogType;
import com.eucalyptus.webui.shared.checker.InvalidValueException;
import com.eucalyptus.webui.shared.resource.device.status.ServerState;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class DeviceServerActivity extends AbstractSearchActivity implements DeviceServerView.Presenter {

	private static final ClientMessage title = new ClientMessage("Server", "服务器");
	
	private Date dateBegin;
	private Date dateEnd;
	private ServerState queryState = null;
	private Map<Integer, Integer> serverCounts = new HashMap<Integer, Integer>();
	
	private DeviceServerModifyView serverModifyView;
	private DeviceServerDeviceAddView deviceAddView;
	
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
//		final String[] stateValueList = new String[]{ServerState.INUSE.toString(), ServerState.STOP.toString(),
//		        ServerState.ERROR.toString()};
//		deviceAddView.popup(stateValueList);
	}

	@Override
	public void onModifyServer() {
		// TODO Auto-generated method stub
	}

	@Override
	public void onOperateServer() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDeleteServer() {
		// TODO Auto-generated method stub
		
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
	

    
    
    
    
    
    
    
    
    
    
//    public static final int TABLE_COL_INDEX_SERVER_ID = 0;
//    public static final int TABLE_COL_INDEX_CHECKBOX = 1;
//    public static final int TABLE_COL_INDEX_NO = 2;
//    public static final int TABLE_COL_INDEX_STATE = 10;
//
////	public DeviceServerActivity(SearchPlace place, ClientFactory clientFactory) {
////		super(place, clientFactory);
////		serverModifyView = new DeviceServerModifyViewImpl();
////		serverModifyView.setPresenter(new DeviceServerModifyView.Presenter() {
////
////			@Override
////			public void onOK(SearchResultRow row, int state) {
////				handleModifyServer(row, state);
////				getView().getMirrorTable().clearSelection();
////			}
////
////			@Override
////			public void onCancel() {
////				getView().getMirrorTable().clearSelection();
////			}
////
////		});
////
////		deviceAddView = new DeviceServerDeviceAddViewImpl();
////		deviceAddView.setPresenter(new DeviceServerDeviceAddView.Presenter() {
////
////			@Override
////			public boolean onOK(String mark, String name, String conf, String ip, int bw, String sstate, String cabinet_name) {
////				if (isEmpty(mark) || isEmpty(name)) {
////					StringBuilder sb = new StringBuilder();
////					sb.append(ADD_DEVICE_FAILURE_INVALID_ARGS[LAN_SELECT]).append("\n");
////					sb.append("<mark='").append(mark).append("'").append(", ");
////					sb.append("name='").append(name).append("', ");
////					sb.append("conf='").append(conf).append("', ");
////					sb.append("ip='").append(ip).append("', ");
////					sb.append("bw='").append(bw).append("', ");
////					sb.append("state='").append(sstate).append("', ");
////					sb.append("cabinet_name='").append(cabinet_name).append("'>");
////					Window.alert(sb.toString());
////					return false;
////				}
////				int state = ServerState.ERROR.getValue();
////				try {
////					state = ServerState.getValue(sstate);
////				}
////				catch (Exception e) {
////				}
////				handleAddDevice(mark, name, conf, ip, bw, state, cabinet_name);
////				return true;
////			}
////
////            @Override
////            public void lookupAreaNames() {
////                getBackendService().lookupDeviceAreaNames(getSession(), new AsyncCallback<List<String>>() {
////
////                    @Override
////                    public void onFailure(Throwable caught) {
////                        if (caught instanceof EucalyptusServiceException) {
////                            onBackendServiceFailure((EucalyptusServiceException)caught);
////                        }
////                    }
////
////                    @Override
////                    public void onSuccess(List<String> area_name_list) {
////                        showStatus(new ClientMessage("", "获取区域列表成功"));
////                        deviceAddView.setAreaNameList(area_name_list);
////                    }
////                    
////                });
////            }
////
////            @Override
////            public void lookupRoomNamesByAreaName(final String area_name) {
////                if (isEmpty(area_name)) {
////                    StringBuilder sb = new StringBuilder();
////                    sb.append(new ClientMessage("", "区域名称非法")).append(" = '").append(area_name).append("'");
////                    Window.alert(sb.toString());
////                }
////                else {
////                    getBackendService().lookupDeviceRoomNamesByAreaName(getSession(), area_name, new AsyncCallback<List<String>>() {
////
////                        @Override
////                        public void onFailure(Throwable caught) {
////                            if (caught instanceof EucalyptusServiceException) {
////                                onBackendServiceFailure((EucalyptusServiceException)caught);
////                            }
////                        }
////
////                        @Override
////                        public void onSuccess(List<String> room_name_list) {
////                            showStatus(new ClientMessage("", "获取机房列表成功"));
////                            deviceAddView.setRoomNameList(area_name, room_name_list);
////                        }
////                        
////                    });
////                }
////            }
////
////            @Override
////            public void lookupCabinetNamesByRoomName(final String room_name) {
////                if (isEmpty(room_name)) {
////                    StringBuilder sb = new StringBuilder();
////                    sb.append(new ClientMessage("", "机房名称非法")).append(" = '").append(room_name).append("'");
////                    Window.alert(sb.toString());
////                }
////                else {
////                    getBackendService().lookupCabinetNamesByRoomName(getSession(), room_name, new AsyncCallback<List<String>>() {
////
////                        @Override
////                        public void onFailure(Throwable caught) {
////                            if (caught instanceof EucalyptusServiceException) {
////                                onBackendServiceFailure((EucalyptusServiceException)caught);
////                            }
////                        }
////
////                        @Override
////                        public void onSuccess(List<String> cabinet_name_list) {
////                            showStatus(new ClientMessage("", "获取机柜列表成功"));
////                            deviceAddView.setCabinetNameList(room_name, cabinet_name_list);
////                        }
////                        
////                    });
////                }
////            }
////
////		});
////	}
//	
//	private void reloadLabels() {
////		getBackendService().getDeviceServerCounts(getSession(), new AsyncCallback<Map<Integer, Integer>>() {
////
////			@Override
////			public void onFailure(Throwable caught) {
////				log(QUERY_COUNT_FAILURE[LAN_SELECT], caught);
////			}
////
////			@Override
////			public void onSuccess(Map<Integer, Integer> result) {
////				ServerState.reset();
////				for (Map.Entry<Integer, Integer> entry : result.entrySet()) {
////					ServerState.setCount(ServerState.getServerState(entry.getKey()), entry.getValue());
////				}
////				getView().updateLabels();
////			}
////
////		});
//	}
//	
//	@Override
//	public void setQueryState(ServerState state) {
//		getView().clearSelection();
//		this.queryState = state;
//		this.range = new SearchRange(0, DeviceServerView.DEFAULT_PAGESIZE, -1, true);
//		reloadCurrentRange();
//	}
//
//	@Override
//	public ServerState getQueryState() {
//		return queryState;
//	}
//
//	@Override
//	public int getCounts(ServerState state) {
//		return ServerState.getCount(state);
//	}
//
//	private static final String[] QUERY_COUNT_FAILURE = {"", "获取资源失败"};
//	private static final String[] QUERY_TABLE_FAILURE = {"", "获取列表失败"};
//	private static final String[] UPDATE_SERVER_FAILURE = {"", "更新服务失败"};
//	private static final String[] UPDATE_SERVER_SUCCESS = {"", "更新服务成功"};
//	private static final String[] ADD_DEVICE_FAILURE_INVALID_ARGS = {"", "添加设备失败：无效的参数"};
//	private static final String[] MODIFY_STATE_CONFIRM = {"", "确认对所选择服务器进行操作？"};
//	private static final String[] ACTION_SELECTED_FAILURE = {"", "请选择操作对象"};
//	private static final String[] DELETE_DEVICE_FAILURE = {"", "删除设备失败"};
//	private static final String[] DELETE_DEVICE_SUCCESS = {"", "删除设备成功"};
//	private static final String[] DELETE_DELETE_CONFIRM = {"", "确认删除所选择的 设备？"};
//	private static final String[] DELETE_ALL_DEVICE_CONFIRM = {"", "确认删除所选择的 全部设备？"};
//
//	private void prepareModifyServerState(SearchResultRow row) {
//		if (!Window.confirm(MODIFY_STATE_CONFIRM[LAN_SELECT])) {
//			getView().getMirrorTable().clearSelection();
//			return;
//		}
//		int state = -1;
//		try {
//			state = ServerState.getValue(row.getField(TABLE_COL_INDEX_STATE));
//		}
//		catch (Exception e) {
//		}
//		serverModifyView.popup(row, state);
//	}
//
//	private void prepareDeleteDevice(SearchResultRow row) {
//		if (!Window.confirm(DELETE_DELETE_CONFIRM[LAN_SELECT])) {
//			getView().getMirrorTable().clearSelection();
//			return;
//		}
//		List<SearchResultRow> list = new ArrayList<SearchResultRow>();
//		list.add(row);
//		handleDeleteDevice(list);
//	}
//
//	@Override
//	public void onMirrorSelectRow(SearchResultRow row) {
//		if (row == null) {
//			return;
//		}
//		switch (getView().getMirrorModeType()) {
//		case MODIFY_STATE:
//			prepareModifyServerState(row);
//			return;
//		case DELETE_DEVICE:
//			prepareDeleteDevice(row);
//			return;
//		default:
//			return;
//		}
//	}
//
//	private void handleAddDevice(String mark, String name, String conf, String ip, int bw, int state, String cabinet_name) {
////		getBackendService().createDeviceServer(getSession(), mark, name, conf, ip, bw, state, cabinet_name,
////		        new AsyncCallback<Void>() {
////
////			        @Override
////			        public void onFailure(Throwable caught) {
////                        if (caught instanceof EucalyptusServiceException) {
////                            onBackendServiceFailure((EucalyptusServiceException)caught);
////                        }
////                        getView().clearSelection();
////			        }
////
////			        @Override
////			        public void onSuccess(Void result) {
////                        showStatus(new ClientMessage("", "添加服务器成功"));
////                        reloadCurrentRange();
////                        getView().clearSelection();
////			        }
////
////		        });
//	}
//
//	private void handleModifyServer(SearchResultRow row, int state) {
////		assert (row != null && getView().getMirrorModeType() == MirrorModeType.MODIFY_STATE);
////		getBackendService().modifyDeviceServerState(getSession(), row, state, new AsyncCallback<SearchResultRow>() {
////
////			@Override
////			public void onFailure(Throwable caught) {
////				ActivityUtil.logoutForInvalidSession(clientFactory, caught);
////				log(UPDATE_SERVER_FAILURE[LAN_SELECT], caught);
////			}
////
////			@Override
////			public void onSuccess(SearchResultRow result) {
////				if (result != null) {
////					showStatus(UPDATE_SERVER_SUCCESS[LAN_SELECT]);
////					if (getView().isMirrorMode()) {
////						final int col = TABLE_COL_INDEX_SERVER_ID;
////						result.setField(TABLE_COL_INDEX_CHECKBOX, "+");
////						final SearchResultRowMatcher matcher = new SearchResultRowMatcher() {
////
////							@Override
////							public boolean match(SearchResultRow row0, SearchResultRow row1) {
////								return row0.getField(col).equals(row1.getField(col));
////							}
////
////						};
////						getView().getMirrorTable().updateRow(result, matcher);
////					}
////				}
////				else {
////					showStatus(UPDATE_SERVER_FAILURE[LAN_SELECT]);
////				}
////				reloadCurrentRange();
////			}
////
////		});
//	}
//
//	private void handleDeleteDevice(List<SearchResultRow> list) {
////		DeviceServerView view = getView();
////		assert (list.size() != 0 && view.getMirrorModeType() == MirrorModeType.DELETE_DEVICE);
////		getBackendService().deleteDeviceServer(getSession(), list, new AsyncCallback<List<SearchResultRow>>() {
////
////			@Override
////			public void onFailure(Throwable caught) {
////				ActivityUtil.logoutForInvalidSession(clientFactory, caught);
////				log(DELETE_DEVICE_FAILURE[LAN_SELECT], caught);
////			}
////
////			@Override
////			public void onSuccess(List<SearchResultRow> result) {
////				if (result != null) {
////					showStatus(DELETE_DEVICE_SUCCESS[LAN_SELECT]);
////					if (getView().isMirrorMode()) {
////						final int col = TABLE_COL_INDEX_SERVER_ID;
////						final SearchResultRowMatcher matcher = new SearchResultRowMatcher() {
////
////							@Override
////							public boolean match(SearchResultRow row0, SearchResultRow row1) {
////								return row0.getField(col).equals(row1.getField(col));
////							}
////
////						};
////						for (SearchResultRow row : result) {
////							getView().getMirrorTable().deleteRow(row, matcher);
////						}
////					}
////				}
////				else {
////					showStatus(DELETE_DEVICE_FAILURE[LAN_SELECT]);
////				}
////				reloadCurrentRange();
////			}
////
////		});
//	}

//	private SearchResultRow copyRow(SearchResultRow row) {
//		SearchResultRow tmp = row.copy();
//		tmp.setField(TABLE_COL_INDEX_CHECKBOX, "");
//		return tmp;
//	}
//
//	@Override
//	public void onModifyState() {
//		Set<SearchResultRow> selected = getView().getSelectedSet();
//		if (selected == null || selected.isEmpty()) {
//			showStatus(ACTION_SELECTED_FAILURE[LAN_SELECT]);
//			return;
//		}
//		List<SearchResultRow> list = new ArrayList<SearchResultRow>();
//		for (SearchResultRow row : selected) {
//			list.add(copyRow(row));
//		}
//		getView().openMirrorMode(MirrorModeType.MODIFY_STATE, sortSearchResultRow(list));
//	}
//
//	@Override
//	public void onDeleteDevice() {
//		Set<SearchResultRow> selected = getView().getSelectedSet();
//		if (selected == null || selected.isEmpty()) {
//			showStatus(ACTION_SELECTED_FAILURE[LAN_SELECT]);
//			return;
//		}
//		List<SearchResultRow> list = new ArrayList<SearchResultRow>();
//		for (SearchResultRow row : selected) {
//			list.add(copyRow(row));
//		}
//		getView().openMirrorMode(MirrorModeType.DELETE_DEVICE, sortSearchResultRow(list));
//	}

}
