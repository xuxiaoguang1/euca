package com.eucalyptus.webui.client.activity.device;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
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
import com.eucalyptus.webui.client.view.DeviceMemoryAddView;
import com.eucalyptus.webui.client.view.DeviceMemoryAddViewImpl;
import com.eucalyptus.webui.client.view.DeviceMemoryModifyView;
import com.eucalyptus.webui.client.view.DeviceMemoryModifyViewImpl;
import com.eucalyptus.webui.client.view.DeviceMemoryServiceAddView;
import com.eucalyptus.webui.client.view.DeviceMemoryServiceAddViewImpl;
import com.eucalyptus.webui.client.view.DeviceMemoryServiceModifyView;
import com.eucalyptus.webui.client.view.DeviceMemoryServiceModifyViewImpl;
import com.eucalyptus.webui.client.view.DeviceMemoryView;
import com.eucalyptus.webui.client.view.FooterView;
import com.eucalyptus.webui.client.view.HasValueWidget;
import com.eucalyptus.webui.client.view.LogView;
import com.eucalyptus.webui.client.view.FooterView.StatusType;
import com.eucalyptus.webui.client.view.LogView.LogType;
import com.eucalyptus.webui.shared.resource.device.CellTableColumns;
import com.eucalyptus.webui.shared.resource.device.status.MemoryState;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class DeviceMemoryActivity extends AbstractSearchActivity implements DeviceMemoryView.Presenter {

	private static final ClientMessage title = new ClientMessage("Memory", "内存");

	private Date dateBegin;
	private Date dateEnd;
	private MemoryState queryState = null;
	private Map<Integer, Long> memoryCounts = new HashMap<Integer, Long>();

	private DeviceMemoryAddView memoryAddView;
	private DeviceMemoryModifyView memoryModifyView;
	private DeviceMemoryServiceAddView memoryServiceAddView;
	private DeviceMemoryServiceModifyView memoryServiceModifyView;

	public DeviceMemoryActivity(SearchPlace place, ClientFactory clientFactory) {
		super(place, clientFactory);
		super.pageSize = DevicePageSize.getPageSize();
	}

	private DeviceMemoryView getView() {
		DeviceMemoryView view = (DeviceMemoryView)this.view;
		if (view == null) {
			view = clientFactory.getDeviceMemoryView();
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
			EucalyptusServiceException exception = (EucalyptusServiceException) caught;
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
		getBackendService().lookupDeviceMemoryByDate(getSession(), range, queryState, dateBegin, dateEnd, new AsyncCallback<SearchResult>() {

			@Override
			public void onFailure(Throwable caught) {
				if (caught instanceof EucalyptusServiceException) {
					onBackendServiceFailure((EucalyptusServiceException) caught);
				}
				displayData(null);
			}

			@Override
			public void onSuccess(SearchResult result) {
				showStatus(new ClientMessage("", "查询内存成功"));
				displayData(result);
			}

		});
		getBackendService().lookupDeviceMemoryCounts(getSession(), new AsyncCallback<Map<Integer, Long>>() {

			@Override
			public void onFailure(Throwable caught) {
				if (caught instanceof EucalyptusServiceException) {
					onBackendServiceFailure((EucalyptusServiceException) caught);
				}
			}

			@Override
			public void onSuccess(Map<Integer, Long> result) {
				showStatus(new ClientMessage("", "查询内存数量成功"));
				memoryCounts = result;
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
	public void onAddMemory() {
		try {
			if (Window.confirm(new ClientMessage("", "确认创建新内存设备").toString())) {
				if (memoryAddView == null) {
					memoryAddView = new DeviceMemoryAddViewImpl();
					memoryAddView.setPresenter(new DeviceMemoryAddView.Presenter() {

						@Override
						public boolean onOK(String memory_name, String memory_desc, long memory_size, String server_name) {
							if (isEmpty(memory_name)) {
								StringBuilder sb = new StringBuilder();
								sb.append(new ClientMessage("", "内存名称非法")).append(" = '").append(memory_name).append("' ");
								sb.append(new ClientMessage("", "请重新选择内存名称"));
								Window.alert(sb.toString());
								return false;
							}
							if (isEmpty(server_name)) {
								StringBuilder sb = new StringBuilder();
								sb.append(new ClientMessage("", "服务器名称非法")).append(" = '").append(server_name).append("' ");
								sb.append(new ClientMessage("", "请重新选择服务器"));
								Window.alert(sb.toString());
								return false;
							}
							if (memory_size <= 0) {
								StringBuilder sb = new StringBuilder();
								sb.append(new ClientMessage("", "内存大小非法")).append(" = '").append(memory_size).append("' ");
								sb.append(new ClientMessage("", "请重新选择内存大小"));
								Window.alert(sb.toString());
								return false;
							}
							getBackendService().addDeviceMemory(getSession(), memory_name, memory_desc, memory_size, server_name, new AsyncCallback<Void>() {
								
								@Override
								public void onFailure(Throwable caught) {
									if (caught instanceof EucalyptusServiceException) {
										onBackendServiceFailure((EucalyptusServiceException) caught);
									}
									getView().clearSelection();
								}

								@Override
								public void onSuccess(Void result) {
									showStatus(new ClientMessage("", "添加内存成功"));
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
										onBackendServiceFailure((EucalyptusServiceException) caught);
									}
								}

								@Override
								public void onSuccess(List<String> area_name_list) {
									showStatus(new ClientMessage("", "获取区域列表成功"));
									memoryAddView.setAreaNameList(area_name_list);
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
											onBackendServiceFailure((EucalyptusServiceException) caught);
										}
									}

									@Override
									public void onSuccess(List<String> room_name_list) {
										showStatus(new ClientMessage("", "获取机房列表成功"));
										memoryAddView.setRoomNameList(area_name, room_name_list);
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
											onBackendServiceFailure((EucalyptusServiceException) caught);
										}
									}

									@Override
									public void onSuccess(List<String> cabinet_name_list) {
										showStatus(new ClientMessage("", "获取机柜列表成功"));
										memoryAddView.setCabinetNameList(room_name, cabinet_name_list);
									}
									
								});
							}
						}

						@Override
						public void lookupServerNameByCabinetName(final String cabinet_name) {
							if (isEmpty(cabinet_name)) {
								StringBuilder sb = new StringBuilder();
								sb.append(new ClientMessage("", "机房机柜非法")).append(" = '").append(cabinet_name).append("'");
								Window.alert(sb.toString());
							}
							else {
								getBackendService().lookupDeviceServerNamesByCabinetName(getSession(), cabinet_name, new AsyncCallback<List<String>>() {

									@Override
									public void onFailure(Throwable caught) {
										if (caught instanceof EucalyptusServiceException) {
											onBackendServiceFailure((EucalyptusServiceException) caught);
										}
									}

									@Override
									public void onSuccess(List<String> cabinet_name_list) {
										showStatus(new ClientMessage("", "获取服务器列表成功"));
										memoryAddView.setServerNameList(cabinet_name, cabinet_name_list);
									}

								});
							}
						}

					});
				}
				memoryAddView.popup();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			onFrontendServiceFailure(e);
		}
	}

	@Override
	public void onModifyMemory() {
		try {
			if (Window.confirm(new ClientMessage("", "确认修改所选择的内存").toString())) {
				SearchResultRow row = getView().getSelectedSet().iterator().next();
				if (memoryModifyView == null) {
					memoryModifyView = new DeviceMemoryModifyViewImpl();
					memoryModifyView.setPresenter(new DeviceMemoryModifyView.Presenter() {

						@Override
						public boolean onOK(int memory_id,String memory_desc, long memory_size) {
							if (memory_size <= 0) {
								StringBuilder sb = new StringBuilder();
								sb.append(new ClientMessage("", "内存大小非法")).append(" = '").append(memory_size).append("' ");
								sb.append(new ClientMessage("", "请重新选择内存大小"));
								Window.alert(sb.toString());
								return false;
							}
							getBackendService().modifyDeviceMemory(getSession(), memory_id, memory_desc, memory_size, new AsyncCallback<Void>() {

								@Override
								public void onFailure(Throwable caught) {
									if (caught instanceof EucalyptusServiceException) {
										onBackendServiceFailure((EucalyptusServiceException) caught);
									}
									getView().clearSelection();
								}

								@Override
								public void onSuccess(Void result) {
									showStatus(new ClientMessage("", "修改内存成功"));
									reloadCurrentRange();
									getView().clearSelection();
								}

							});
							return true;
						}

					});
				}
				int memory_id = Integer.parseInt(row.getField(CellTableColumns.MEMORY.MEMORY_ID));
			    String memory_name = row.getField(CellTableColumns.MEMORY.MEMORY_NAME);
			    String memory_desc = row.getField(CellTableColumns.MEMORY.MEMORY_DESC);
			    long memory_size = Long.parseLong(row.getField(CellTableColumns.MEMORY.MEMORY_TOTAL));
			    String server_name = row.getField(CellTableColumns.MEMORY.SERVER_NAME);
			    memoryModifyView.popup(memory_id, memory_name, memory_desc, memory_size, server_name);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			onFrontendServiceFailure(e);
		}
	}

	@Override
	public void onDeleteMemory() {
		try {
			if (canDeleteMemory()) {
				List<Integer> memory_id_list = new ArrayList<Integer>();
				for (SearchResultRow row : getView().getSelectedSet()) {
					int memory_id = Integer.parseInt(row.getField(CellTableColumns.MEMORY.MEMORY_ID));
					memory_id_list.add(memory_id);
				}
				if (!memory_id_list.isEmpty()) {
					if (Window.confirm(new ClientMessage("", "确认删除所选择的内存").toString())) {
						getBackendService().deleteDeviceMemory(getSession(), memory_id_list, new AsyncCallback<Void>() {

							@Override
							public void onFailure(Throwable caught) {
								if (caught instanceof EucalyptusServiceException) {
									onBackendServiceFailure((EucalyptusServiceException) caught);
								}
								getView().clearSelection();
							}

							@Override
							public void onSuccess(Void result) {
								showStatus(new ClientMessage("", "删除内存成功"));
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
	public void onAddMemoryService() {
		try {
			if (Window.confirm(new ClientMessage("", "确认添加的内存服务").toString())) {
				SearchResultRow row = getView().getSelectedSet().iterator().next();
				if (memoryServiceAddView == null) {
					memoryServiceAddView = new DeviceMemoryServiceAddViewImpl();
					memoryServiceAddView.setPresenter(new DeviceMemoryServiceAddView.Presenter() {

						@Override
						public boolean onOK(int memory_id, String ms_desc, long ms_used, Date ms_starttime, Date ms_endtime, String account_name, String user_name) {
							if (ms_used <= 0) {
								StringBuilder sb = new StringBuilder();
								sb.append(new ClientMessage("", "内存数量非法")).append(" = '").append(ms_used).append("' ");
								sb.append(new ClientMessage("", "请重新选择数量"));
								Window.alert(sb.toString());
								return false;
							}
							if (ms_starttime == null || ms_endtime == null || DeviceDate.calcLife(ms_endtime, ms_starttime) <= 0) {
								StringBuilder sb = new StringBuilder();
                                sb.append(new ClientMessage("", "非法的服务时间"));
                                if (ms_starttime != null && ms_endtime != null) {
                                	sb.append(" = '").append(DeviceDate.format(ms_starttime)).append("' >= '").append(DeviceDate.format(ms_endtime)).append("'");
                                }
                                sb.append(new ClientMessage("", "请重新选择时间"));
                                Window.alert(sb.toString());
                                return false;
							}
							if (isEmpty(account_name)) {
								StringBuilder sb = new StringBuilder();
								sb.append(new ClientMessage("", "非法的账户名称")).append(" = '").append(account_name).append("' ");
								sb.append(new ClientMessage("", "请重新选择账户"));
								Window.alert(sb.toString());
								return false;
							}
							if (isEmpty(user_name)) {
								StringBuilder sb = new StringBuilder();
								sb.append(new ClientMessage("", "非法的用户名称")).append(" = '").append(user_name).append("' ");
								sb.append(new ClientMessage("", "请重新选择账户"));
								Window.alert(sb.toString());
								return false;
							}
							getBackendService().addDeviceMemoryService(getSession(), ms_desc, ms_used, ms_starttime, ms_endtime, memory_id, account_name, user_name, new AsyncCallback<Void>() {

								@Override
								public void onFailure(Throwable caught) {
									if (caught instanceof EucalyptusServiceException) {
										onBackendServiceFailure((EucalyptusServiceException) caught);
									}
									getView().clearSelection();
								}

								@Override
								public void onSuccess(Void result) {
									showStatus(new ClientMessage("", "添加内存服务成功"));
									reloadCurrentRange();
									getView().clearSelection();
								}

							});
							return true;
						}

						@Override
						public void lookupAccountNames() {
							getBackendService().lookupDeviceAccountNames(getSession(), new AsyncCallback<List<String>>() {

								@Override
								public void onFailure(Throwable caught) {
									if (caught instanceof EucalyptusServiceException) {
										onBackendServiceFailure((EucalyptusServiceException) caught);
									}
								}

								@Override
								public void onSuccess(List<String> account_name_list) {
									showStatus(new ClientMessage("", "获取账户列表成功"));
									memoryServiceAddView.setAccountNameList(account_name_list);
								}

							});
						}

						@Override
						public void lookupUserNamesByAccountName(final String account_name) {
							if (isEmpty(account_name)) {
								StringBuilder sb = new StringBuilder();
								sb.append(new ClientMessage("", "账户名称非法")).append(" = '").append(account_name).append("'");
								Window.alert(sb.toString());
							}
							else {
								getBackendService().lookupDeviceUserNamesByAccountName(getSession(), account_name, new AsyncCallback<List<String>>() {

									@Override
									public void onFailure(Throwable caught) {
										if (caught instanceof EucalyptusServiceException) {
											onBackendServiceFailure((EucalyptusServiceException) caught);
										}
									}

									@Override
									public void onSuccess(List<String> user_name_list) {
										showStatus(new ClientMessage("", "获取用户列表成功"));
										memoryServiceAddView.setUserNameList(account_name, user_name_list);
									}

								});
							}
						}

					});
				}
				int memory_id = Integer.parseInt(row.getField(CellTableColumns.MEMORY.MEMORY_ID));
			    String memory_name = row.getField(CellTableColumns.MEMORY.MEMORY_NAME);
			    String server_name = row.getField(CellTableColumns.MEMORY.SERVER_NAME);
			    long ms_reserved = Long.parseLong(row.getField(CellTableColumns.MEMORY.MEMORY_SERVICE_USED));
				memoryServiceAddView.popup(memory_id, memory_name, server_name, ms_reserved);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			onFrontendServiceFailure(e);
		}
	}

	@Override
	public void onModifyMemoryService() {
		try {
			if (Window.confirm(new ClientMessage("", "确认修改所选择的内存服务").toString())) {
				SearchResultRow row = getView().getSelectedSet().iterator().next();
				if (memoryServiceModifyView == null) {
					memoryServiceModifyView = new DeviceMemoryServiceModifyViewImpl();
					memoryServiceModifyView.setPresenter(new DeviceMemoryServiceModifyView.Presenter() {

						@Override
						public boolean onOK(int ms_id, String ms_desc, Date ms_starttime, Date ms_endtime) {
							if (ms_starttime == null || ms_endtime == null || DeviceDate.calcLife(ms_endtime, ms_starttime) <= 0) {
								StringBuilder sb = new StringBuilder();
                                sb.append(new ClientMessage("", "非法的服务时间"));
                                if (ms_starttime != null && ms_endtime != null) {
                                	sb.append(" = '").append(DeviceDate.format(ms_starttime)).append("' >= '").append(DeviceDate.format(ms_endtime)).append("'");
                                }
                                sb.append(new ClientMessage("", "请重新选择时间"));
                                Window.alert(sb.toString());
                                return false;
							}
							getBackendService().modifyDeviceMemoryService(getSession(), ms_id, ms_desc, ms_starttime, ms_endtime, new AsyncCallback<Void>() {

								@Override
								public void onFailure(Throwable caught) {
									if (caught instanceof EucalyptusServiceException) {
										onBackendServiceFailure((EucalyptusServiceException) caught);
									}
									getView().clearSelection();
								}

								@Override
								public void onSuccess(Void result) {
									showStatus(new ClientMessage("", "变更内存服务成功"));
									reloadCurrentRange();
									getView().clearSelection();
								}

							});
							return true;
						}

					});
				}
				int ms_id = Integer.parseInt(row.getField(CellTableColumns.MEMORY.MEMORY_SERVICE_ID));
				String ms_desc = row.getField(CellTableColumns.MEMORY.MEMORY_SERVICE_DESC);
			    String memory_name = row.getField(CellTableColumns.MEMORY.MEMORY_NAME);
			    int ms_used = Integer.parseInt(row.getField(CellTableColumns.MEMORY.MEMORY_SERVICE_USED));
			    Date ms_starttime = DeviceDate.parse(row.getField(CellTableColumns.MEMORY.MEMORY_SERVICE_STARTTIME));
			    Date ms_endtime = DeviceDate.parse(row.getField(CellTableColumns.MEMORY.MEMORY_SERVICE_ENDTIME));
			    String server_name = row.getField(CellTableColumns.MEMORY.SERVER_NAME);
			    String account_name = row.getField(CellTableColumns.MEMORY.ACCOUNT_NAME);
			    String user_name = row.getField(CellTableColumns.MEMORY.USER_NAME);
				memoryServiceModifyView.popup(ms_id, memory_name, ms_desc, ms_used, ms_starttime, ms_endtime, server_name, account_name, user_name);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			onFrontendServiceFailure(e);
		}
	}

	@Override
	public void onDeleteMemoryService() {
		try {
			if (canDeleteMemoryService()) {
				List<Integer> ms_id_list = new ArrayList<Integer>();
				for (SearchResultRow row : getView().getSelectedSet()) {
					int ms_id = Integer.parseInt(row.getField(CellTableColumns.MEMORY.MEMORY_SERVICE_ID));
					ms_id_list.add(ms_id);
				}
				if (!ms_id_list.isEmpty()) {
					if (Window.confirm(new ClientMessage("", "确认删除所选择的内存服务").toString())) {
						getBackendService().deleteDeviceMemoryService(getSession(), ms_id_list, new AsyncCallback<Void>() {

							@Override
							public void onFailure(Throwable caught) {
								if (caught instanceof EucalyptusServiceException) {
									onBackendServiceFailure((EucalyptusServiceException) caught);
								}
								getView().clearSelection();
							}

							@Override
							public void onSuccess(Void result) {
								showStatus(new ClientMessage("", "删除内存服务成功"));
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
	public MemoryState getQueryState() {
		return queryState;
	}

	@Override
	public void setQueryState(MemoryState queryState) {
		if (this.queryState != queryState) {
			getView().clearSelection();
			this.queryState = queryState;
			range = new SearchRange(0, DevicePageSize.getPageSize(), -1, true);
			reloadCurrentRange();
		}
	}

	@Override
	public long getCounts(MemoryState state) {
		Long count = memoryCounts.get(state == null ? -1 : state.getValue());
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
		range = new SearchRange(0, DevicePageSize.getPageSize(), -1, true);
		reloadCurrentRange();
	}

	@Override
	public boolean canDeleteMemory() {
		Set<SearchResultRow> set = getView().getSelectedSet();
		if (!set.isEmpty()) {
			for (SearchResultRow row : set) {
				try {
					MemoryState memory_state = MemoryState.parse(row.getField(CellTableColumns.MEMORY.MEMORY_SERVICE_STATE));
					if (memory_state != MemoryState.RESERVED) {
						return false;
					}
					long memory_total = Long.parseLong(row.getField(CellTableColumns.MEMORY.MEMORY_TOTAL));
					long ms_used = Long.parseLong(row.getField(CellTableColumns.MEMORY.MEMORY_SERVICE_USED));
					if (memory_total != ms_used) {
						return false;
					}
				}
				catch (Exception e) {
					e.printStackTrace();
					return false;
				}
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean canModifyMemory() {
		Set<SearchResultRow> set = getView().getSelectedSet();
		if (set.size() == 1) {
			return true;
		}
		return false;
	}

	@Override
	public boolean canAddMemoryService() {
		Set<SearchResultRow> set = getView().getSelectedSet();
		if (set.size() == 1) {
			try {
				SearchResultRow row = set.iterator().next();
				MemoryState memory_state = MemoryState.parse(row.getField(CellTableColumns.MEMORY.MEMORY_SERVICE_STATE));
				if (memory_state != MemoryState.RESERVED) {
					return false;
				}
				long ms_used = Integer.parseInt(row.getField(CellTableColumns.MEMORY.MEMORY_SERVICE_USED));
				if (ms_used == 0) {
					return false;
				}
			}
			catch (Exception e) {
				e.printStackTrace();
				return false;
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean canDeleteMemoryService() {
		Set<SearchResultRow> set = getView().getSelectedSet();
		if (!set.isEmpty()) {
			for (SearchResultRow row : set) {
				try {
					MemoryState memory_state = MemoryState.parse(row.getField(CellTableColumns.MEMORY.MEMORY_SERVICE_STATE));
					if (memory_state != MemoryState.INUSE && memory_state != MemoryState.STOP) {
						return false;
					}
				}
				catch (Exception e) {
					e.printStackTrace();
					return false;
				}
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean canModifyMemoryService() {
		Set<SearchResultRow> set = getView().getSelectedSet();
		if (set.size() == 1) {
			try {
				SearchResultRow row = set.iterator().next();
				MemoryState memory_state = MemoryState.parse(row.getField(CellTableColumns.MEMORY.MEMORY_SERVICE_STATE));
				if (memory_state != MemoryState.INUSE && memory_state != MemoryState.STOP) {
					return false;
				}
			}
			catch (Exception e) {
				e.printStackTrace();
				return false;
			}
			return true;
		}
		return false;
	}

}
