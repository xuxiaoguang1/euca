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
import com.eucalyptus.webui.client.view.DeviceCPUAddView;
import com.eucalyptus.webui.client.view.DeviceCPUAddViewImpl;
import com.eucalyptus.webui.client.view.DeviceCPUModifyView;
import com.eucalyptus.webui.client.view.DeviceCPUModifyViewImpl;
import com.eucalyptus.webui.client.view.DeviceCPUServiceAddView;
import com.eucalyptus.webui.client.view.DeviceCPUServiceAddViewImpl;
import com.eucalyptus.webui.client.view.DeviceCPUServiceModifyView;
import com.eucalyptus.webui.client.view.DeviceCPUServiceModifyViewImpl;
import com.eucalyptus.webui.client.view.DeviceCPUView;
import com.eucalyptus.webui.client.view.FooterView;
import com.eucalyptus.webui.client.view.HasValueWidget;
import com.eucalyptus.webui.client.view.LogView;
import com.eucalyptus.webui.client.view.FooterView.StatusType;
import com.eucalyptus.webui.client.view.LogView.LogType;
import com.eucalyptus.webui.shared.message.ClientMessage;
import com.eucalyptus.webui.shared.resource.device.CellTableColumns;
import com.eucalyptus.webui.shared.resource.device.status.CPUState;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class DeviceCPUActivity extends AbstractSearchActivity implements DeviceCPUView.Presenter {
	
	private static final ClientMessage title = new ClientMessage("CPU", "CPU");
	
	private Date dateBegin;
	private Date dateEnd;
	private CPUState queryState = null;
	private Map<Integer, Integer> cpuCounts = new HashMap<Integer, Integer>();
	
	private DeviceCPUAddView cpuAddView;
	private DeviceCPUModifyView cpuModifyView;
	private DeviceCPUServiceAddView cpuServiceAddView;
	private DeviceCPUServiceModifyView cpuServiceModifyView;
	
	public DeviceCPUActivity(SearchPlace place, ClientFactory clientFactory) {
		super(place, clientFactory);
		super.pageSize = DevicePageSize.getPageSize();
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
		getBackendService().lookupDeviceCPUByDate(getSession(), range, queryState, dateBegin, dateEnd, new AsyncCallback<SearchResult>() {

			@Override
			public void onFailure(Throwable caught) {
				if (caught instanceof EucalyptusServiceException) {
					onBackendServiceFailure((EucalyptusServiceException)caught);
				}
				displayData(null);
			}

			@Override
			public void onSuccess(SearchResult result) {
				showStatus(new ClientMessage("", "查询CPU成功"));
				displayData(result);
			}
			
		});
		getBackendService().lookupDeviceCPUCounts(getSession(), new AsyncCallback<Map<Integer, Integer>>() {

			@Override
			public void onFailure(Throwable caught) {
				if (caught instanceof EucalyptusServiceException) {
					onBackendServiceFailure((EucalyptusServiceException)caught);
				}
			}

			@Override
			public void onSuccess(Map<Integer, Integer> result) {
				showStatus(new ClientMessage("", "查询CPU数量成功"));
				cpuCounts = result;
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
	public void onAddCPU() {
		try {
			if (Window.confirm(new ClientMessage("", "确认创建新CPU设备").toString())) {
				if (cpuAddView == null) {
					cpuAddView = new DeviceCPUAddViewImpl();
					cpuAddView.setPresenter(new DeviceCPUAddView.Presenter() {
						
						@Override
						public boolean onOK(String cpu_name, String cpu_desc, int cpu_total, String cpu_vendor, String cpu_model, double cpu_ghz, double cpu_cache, String server_name) {
							if (isEmpty(cpu_name)) {
								StringBuilder sb = new StringBuilder();
								sb.append(new ClientMessage("", "CPU名称非法")).append(" = '").append(cpu_name).append("' ");
								sb.append(new ClientMessage("", "请重新选择CPU"));
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
							getBackendService().addDeviceCPU(getSession(), cpu_name, cpu_desc, cpu_total, cpu_vendor, cpu_model, cpu_ghz, cpu_cache, server_name, new AsyncCallback<Void>() {

								@Override
								public void onFailure(Throwable caught) {
									if (caught instanceof EucalyptusServiceException) {
										onBackendServiceFailure((EucalyptusServiceException)caught);
									}
									getView().clearSelection();
								}

								@Override
								public void onSuccess(Void result) {
									showStatus(new ClientMessage("", "添加CPU成功"));
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
									cpuAddView.setAreaNameList(area_name_list);
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
										cpuAddView.setRoomNameList(area_name, room_name_list);
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
								getBackendService().lookupCabinetNamesByRoomID(getSession(), room_name, new AsyncCallback<List<String>>() {

									@Override
									public void onFailure(Throwable caught) {
										if (caught instanceof EucalyptusServiceException) {
											onBackendServiceFailure((EucalyptusServiceException)caught);
										}
									}

									@Override
									public void onSuccess(List<String> cabinet_name_list) {
										showStatus(new ClientMessage("", "获取机柜列表成功"));
										cpuAddView.setCabinetNameList(room_name, cabinet_name_list);
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
											onBackendServiceFailure((EucalyptusServiceException)caught);
										}
									}

									@Override
									public void onSuccess(List<String> cabinet_name_list) {
										showStatus(new ClientMessage("", "获取服务器列表成功"));
										cpuAddView.setServerNameList(cabinet_name, cabinet_name_list);
									}
									
								});
							}
						}
						
					});
				}
				cpuAddView.popup();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			onFrontendServiceFailure(e);
		}
	}

	@Override
	public void onModifyCPU() {
		try {
			if (canModifyCPU()) {
				if (Window.confirm(new ClientMessage("", "确认修改所选择的CPU").toString())) {
					SearchResultRow row = getView().getSelectedSet().iterator().next();
				    if (cpuModifyView == null) {
				        cpuModifyView = new DeviceCPUModifyViewImpl();
				        cpuModifyView.setPresenter(new DeviceCPUModifyView.Presenter() {
                            
                            @Override
                            public boolean onOK(int cpu_id, String cpu_desc, int cpu_total, String cpu_vendor, String cpu_model, double cpu_ghz, double cpu_cache) {
                                if (cpu_total <= 0) {
                                    StringBuilder sb = new StringBuilder();
                                    sb.append(new ClientMessage("", "主频CPU数量非法")).append(" = '").append(cpu_total).append("' ");
                                    sb.append(new ClientMessage("", "请重新选择数量"));
                                    Window.alert(sb.toString());
                                    return false;
                                }
                                getBackendService().modifyDeviceCPU(getSession(), cpu_id, cpu_desc, cpu_total, cpu_vendor, cpu_model, cpu_ghz, cpu_cache, new AsyncCallback<Void>() {

                                    @Override
                                    public void onFailure(Throwable caught) {
                                        if (caught instanceof EucalyptusServiceException) {
                                            onBackendServiceFailure((EucalyptusServiceException)caught);
                                        }
                                        getView().clearSelection();
                                    }

                                    @Override
                                    public void onSuccess(Void result) {
                                        showStatus(new ClientMessage("", "修改CPU成功"));
                                        reloadCurrentRange();
                                        getView().clearSelection();
                                    }
                                    
                                });
                                return true;
                            }
                            
                        });
				    }
				    int cpu_id = Integer.parseInt(row.getField(CellTableColumns.CPU.CPU_ID));
				    String cpu_name = row.getField(CellTableColumns.CPU.CPU_NAME);
				    String cpu_desc = row.getField(CellTableColumns.CPU.CPU_DESC);
				    int cpu_total = Integer.parseInt(row.getField(CellTableColumns.CPU.CPU_TOTAL));
				    String cpu_vendor = row.getField(CellTableColumns.CPU.CPU_VENDOR);
				    String cpu_model = row.getField(CellTableColumns.CPU.CPU_MODEL);
				    double cpu_ghz = Double.parseDouble(row.getField(CellTableColumns.CPU.CPU_GHZ)); 
				    double cpu_cache = Double.parseDouble(row.getField(CellTableColumns.CPU.CPU_CACHE));
				    String server_name = row.getField(CellTableColumns.CPU.SERVER_NAME);
				    cpuModifyView.popup(cpu_id, cpu_name, cpu_desc, cpu_total, cpu_vendor, cpu_model, cpu_ghz, cpu_cache, server_name); 
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			onFrontendServiceFailure(e);
		}
	}

	@Override
	public void onDeleteCPU() {
		try {
			if (canDeleteCPU()) {
				List<Integer> cpu_id_list = new ArrayList<Integer>();
				for (SearchResultRow row : getView().getSelectedSet()) {
					int cpu_id = Integer.parseInt(row.getField(CellTableColumns.CPU.CPU_ID));
					cpu_id_list.add(cpu_id);
				}
				if (!cpu_id_list.isEmpty()) {
					if (Window.confirm(new ClientMessage("", "确认删除所选择的CPU").toString())) {
						getBackendService().deleteDeviceCPU(getSession(), cpu_id_list, new AsyncCallback<Void>() {
		
							@Override
							public void onFailure(Throwable caught) {
								if (caught instanceof EucalyptusServiceException) {
									onBackendServiceFailure((EucalyptusServiceException)caught);
								}
								getView().clearSelection();
							}
		
							@Override
							public void onSuccess(Void result) {
								showStatus(new ClientMessage("", "删除CPU成功"));
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
	public void onAddCPUService() {
		try {
			if (canAddCPUService()) {
				if (Window.confirm(new ClientMessage("", "确认添加的CPU服务").toString())) {
					SearchResultRow row = getView().getSelectedSet().iterator().next();
					if (cpuServiceAddView == null) {
						cpuServiceAddView = new DeviceCPUServiceAddViewImpl();
						cpuServiceAddView.setPresenter(new DeviceCPUServiceAddView.Presenter() {
							
							@Override
							public boolean onOK(int cpu_id, String cs_desc, int cs_used, Date cs_starttime, Date cs_endtime, String account_name, String user_name) {
								if (cs_used <= 0) {
                                    StringBuilder sb = new StringBuilder();
                                    sb.append(new ClientMessage("", "CPU数量非法")).append(" = '").append(cs_used).append("' ");
                                    sb.append(new ClientMessage("", "请重新选择数量"));
                                    Window.alert(sb.toString());
                                    return false;
                                }
								if (cs_starttime == null || cs_endtime == null || DeviceDate.calcLife(cs_endtime, cs_starttime) <= 0) {
									StringBuilder sb = new StringBuilder();
                                    sb.append(new ClientMessage("", "非法的服务时间"));
                                    if (cs_starttime != null && cs_endtime != null) {
                                    	sb.append(" = '").append(DeviceDate.format(cs_starttime)).append("' >= '").append(DeviceDate.format(cs_endtime)).append("'");
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
								getBackendService().addDeviceCPUService(getSession(), cs_desc, cs_used, cs_starttime, cs_endtime, cpu_id, account_name, user_name, new AsyncCallback<Void>() {
									
									@Override
									public void onFailure(Throwable caught) {
										if (caught instanceof EucalyptusServiceException) {
											onBackendServiceFailure((EucalyptusServiceException)caught);
										}
										getView().clearSelection();
									}

									@Override
									public void onSuccess(Void result) {
										showStatus(new ClientMessage("", "添加CPU服务成功"));
										reloadCurrentRange();
										getView().clearSelection();
									}
											
								});
								return true;
							}

							@Override
							public void lookupAccountNames() {
//								getBackendService().lookupDeviceAccountNames(getSession(), new AsyncCallback<List<String>>() {
//
//									@Override
//									public void onFailure(Throwable caught) {
//										if (caught instanceof EucalyptusServiceException) {
//											onBackendServiceFailure((EucalyptusServiceException)caught);
//										}
//									}
//
//									@Override
//									public void onSuccess(List<String> account_name_list) {
//										showStatus(new SharedMessage("", "获取账户列表成功"));
//										cpuServiceAddView.setAccountNameList(account_name_list);
//									}
//									
//								});
							}

							@Override
							public void lookupUserNamesByAccountName(final String account_name) {
								if (isEmpty(account_name)) {
									StringBuilder sb = new StringBuilder();
									sb.append(new ClientMessage("", "账户名称非法")).append(" = '").append(account_name).append("'");
									Window.alert(sb.toString());
								}
								else {
//									getBackendService().lookupDeviceUserNamesByAccountName(getSession(), account_name, new AsyncCallback<List<String>>() {
//
//										@Override
//										public void onFailure(Throwable caught) {
//											if (caught instanceof EucalyptusServiceException) {
//												onBackendServiceFailure((EucalyptusServiceException)caught);
//											}
//										}
//
//										@Override
//										public void onSuccess(List<String> user_name_list) {
//											showStatus(new SharedMessage("", "获取用户列表成功"));
//											cpuServiceAddView.setUserNameList(account_name, user_name_list);
//										}
//										
//									});
								}
							}
							
						});
					}
				    int cpu_id = Integer.parseInt(row.getField(CellTableColumns.CPU.CPU_ID));
				    String cpu_name = row.getField(CellTableColumns.CPU.CPU_NAME);
				    String server_name = row.getField(CellTableColumns.CPU.SERVER_NAME);
				    int cs_used = Integer.parseInt(row.getField(CellTableColumns.CPU.CPU_SERVICE_USED));
					cpuServiceAddView.popup(cpu_id, cpu_name, server_name, cs_used);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			onFrontendServiceFailure(e);
		}
	}

	@Override
	public void onModifyCPUService() {
		try {
			if (canModifyCPUService()) {
				if (Window.confirm(new ClientMessage("", "确认修改所选择的CPU服务").toString())) {
					SearchResultRow row = getView().getSelectedSet().iterator().next();
					if (cpuServiceModifyView == null) {
						cpuServiceModifyView = new DeviceCPUServiceModifyViewImpl();
						cpuServiceModifyView.setPresenter(new DeviceCPUServiceModifyView.Presenter() {
							
							@Override
							public boolean onOK(int cs_id, String cs_desc, Date cs_starttime, Date cs_endtime) {
								if (cs_starttime == null || cs_endtime == null || DeviceDate.calcLife(cs_endtime, cs_starttime) <= 0) {
									StringBuilder sb = new StringBuilder();
									sb.append(new ClientMessage("", "非法的服务时间"));
									if (cs_starttime != null && cs_endtime != null) {
                                    	sb.append(" = '").append(DeviceDate.format(cs_starttime)).append("' >= '").append(DeviceDate.format(cs_endtime)).append("'");
                                    }
                                    sb.append(new ClientMessage("", "请重新选择时间"));
                                    Window.alert(sb.toString());
                                    return false;
								}
								getBackendService().modifyDeviceCPUService(getSession(), cs_id, cs_desc, cs_starttime, cs_endtime, new AsyncCallback<Void>() {

									@Override
									public void onFailure(Throwable caught) {
										if (caught instanceof EucalyptusServiceException) {
											onBackendServiceFailure((EucalyptusServiceException)caught);
										}
										getView().clearSelection();
									}

									@Override
									public void onSuccess(Void result) {
										showStatus(new ClientMessage("", "变更CPU服务成功"));
										reloadCurrentRange();
										getView().clearSelection();
									}

								});
								return true;
							}
						});
					}
					int cs_id = Integer.parseInt(row.getField(CellTableColumns.CPU.CPU_SERVICE_ID));
					String cs_desc = row.getField(CellTableColumns.CPU.CPU_SERVICE_DESC);
				    String cpu_name = row.getField(CellTableColumns.CPU.CPU_NAME);
				    int cs_used = Integer.parseInt(row.getField(CellTableColumns.CPU.CPU_SERVICE_USED));
				    Date cs_starttime = DeviceDate.parse(row.getField(CellTableColumns.CPU.CPU_SERVICE_STARTTIME));
				    Date cs_endtime = DeviceDate.parse(row.getField(CellTableColumns.CPU.CPU_SERVICE_ENDTIME));
				    String server_name = row.getField(CellTableColumns.CPU.SERVER_NAME);
				    String account_name = row.getField(CellTableColumns.CPU.ACCOUNT_NAME);
				    String user_name = row.getField(CellTableColumns.CPU.USER_NAME);
					cpuServiceModifyView.popup(cs_id, cpu_name, cs_desc, cs_used, cs_starttime, cs_endtime, server_name, account_name, user_name);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			onFrontendServiceFailure(e);
		}
	}

	@Override
	public void onDeleteCPUService() {
		try {
			if (canDeleteCPUService()) {
				List<Integer> cs_id_list = new ArrayList<Integer>();
				for (SearchResultRow row : getView().getSelectedSet()) {
					int cs_id = Integer.parseInt(row.getField(CellTableColumns.CPU.CPU_SERVICE_ID));
					cs_id_list.add(cs_id);
				}
				if (!cs_id_list.isEmpty()) {
					if (Window.confirm(new ClientMessage("", "确认删除所选择的CPU服务").toString())) {
						getBackendService().deleteDeviceCPUService(getSession(), cs_id_list, new AsyncCallback<Void>() {
		
							@Override
							public void onFailure(Throwable caught) {
								if (caught instanceof EucalyptusServiceException) {
									onBackendServiceFailure((EucalyptusServiceException)caught);
								}
								getView().clearSelection();
							}
		
							@Override
							public void onSuccess(Void result) {
								showStatus(new ClientMessage("", "删除CPU服务成功"));
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
	public CPUState getQueryState() {
		return queryState;
	}

	@Override
	public void setQueryState(CPUState queryState) {
		if (this.queryState != queryState) {
	    	getView().clearSelection();
			this.queryState = queryState;
	    	range = new SearchRange(0, DevicePageSize.getPageSize(), -1, true);
	    	reloadCurrentRange();
		}
	}
	
	@Override
	public int getCounts(CPUState state) {
		Integer count = cpuCounts.get(state == null ? -1 : state.getValue());
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
	public boolean canDeleteCPU() {
		Set<SearchResultRow> set = getView().getSelectedSet();
		if (!set.isEmpty()) {
			for (SearchResultRow row : set) {
				try {
					CPUState cpu_state = CPUState.parse(row.getField(CellTableColumns.CPU.CPU_SERVICE_STATE));
					if (cpu_state != CPUState.RESERVED) {
						return false;
					}
					int cpu_total = Integer.parseInt(row.getField(CellTableColumns.CPU.CPU_TOTAL));
					int cs_used = Integer.parseInt(row.getField(CellTableColumns.CPU.CPU_SERVICE_USED));
					if (cpu_total != cs_used) {
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
	public boolean canModifyCPU() {
		Set<SearchResultRow> set = getView().getSelectedSet();
		if (set.size() == 1) {
			return true;
		}
		return false;
	}

	@Override
	public boolean canAddCPUService() {
		Set<SearchResultRow> set = getView().getSelectedSet();
		if (set.size() == 1) {
			try {
				SearchResultRow row = set.iterator().next();
				CPUState cpu_state = CPUState.parse(row.getField(CellTableColumns.CPU.CPU_SERVICE_STATE));
				if (cpu_state != CPUState.RESERVED) {
					return false;
				}
				int cs_used = Integer.parseInt(row.getField(CellTableColumns.CPU.CPU_SERVICE_USED));
				if (cs_used == 0) {
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
	public boolean canDeleteCPUService() {
		Set<SearchResultRow> set = getView().getSelectedSet();
		if (!set.isEmpty()) {
			for (SearchResultRow row : set) {
				try {
					CPUState cpu_state = CPUState.parse(row.getField(CellTableColumns.CPU.CPU_SERVICE_STATE));
					if (cpu_state != CPUState.INUSE && cpu_state != CPUState.STOP) {
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
	public boolean canModifyCPUService() {
		Set<SearchResultRow> set = getView().getSelectedSet();
		if (set.size() == 1) {
			try {
				SearchResultRow row = set.iterator().next();
				CPUState cpu_state = CPUState.parse(row.getField(CellTableColumns.CPU.CPU_SERVICE_STATE));
				if (cpu_state != CPUState.INUSE && cpu_state != CPUState.STOP) {
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
