package com.eucalyptus.webui.client.activity.device;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.eucalyptus.webui.client.ClientFactory;
import com.eucalyptus.webui.client.place.SearchPlace;
import com.eucalyptus.webui.client.service.SearchRange;
import com.eucalyptus.webui.client.service.SearchResult;
import com.eucalyptus.webui.client.service.SearchResultRow;
import com.eucalyptus.webui.client.view.DeviceCPUAddView;
import com.eucalyptus.webui.client.view.DeviceCPUAddViewImpl;
import com.eucalyptus.webui.client.view.DeviceCPUModifyView;
import com.eucalyptus.webui.client.view.DeviceCPUModifyViewImpl;
import com.eucalyptus.webui.client.view.DeviceCPUServiceAddView;
import com.eucalyptus.webui.client.view.DeviceCPUServiceAddViewImpl;
import com.eucalyptus.webui.client.view.DeviceCPUServiceModifyView;
import com.eucalyptus.webui.client.view.DeviceCPUServiceModifyViewImpl;
import com.eucalyptus.webui.client.view.DeviceCPUView;
import com.eucalyptus.webui.shared.message.ClientMessage;
import com.eucalyptus.webui.shared.resource.device.CPUInfo;
import com.eucalyptus.webui.shared.resource.device.CPUServiceInfo;
import com.eucalyptus.webui.shared.resource.device.CellTableColumns;
import com.eucalyptus.webui.shared.resource.device.status.CPUState;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class DeviceCPUActivity extends DeviceActivity implements DeviceCPUView.Presenter {
	
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
	
	@Override
	protected void doSearch(String query, SearchRange range) {
		getBackendService().lookupDeviceCPUByDate(getSession(), range, queryState, dateBegin, dateEnd, new AsyncCallback<SearchResult>() {

			@Override
			public void onFailure(Throwable caught) {
				onBackendServiceFailure(caught);
                displayData(null);
			}

			@Override
			public void onSuccess(SearchResult result) {
				onBackendServiceFinished();
                displayData(result);
			}
			
		});
		getBackendService().lookupDeviceCPUCounts(getSession(), new AsyncCallback<Map<Integer, Integer>>() {

			@Override
			public void onFailure(Throwable caught) {
				onBackendServiceFailure(caught);
			}

			@Override
			public void onSuccess(Map<Integer, Integer> result) {
				onBackendServiceFinished();
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
			if (Window.confirm(new ClientMessage("Create a new CPU.", "确认创建新CPU.").toString())) {
				if (cpuAddView == null) {
					cpuAddView = new DeviceCPUAddViewImpl();
					cpuAddView.setPresenter(new DeviceCPUAddView.Presenter() {
						
						@Override
						public boolean onOK(String cpu_name, String cpu_desc, int cpu_total, String cpu_vendor, String cpu_model, double cpu_ghz, double cpu_cache, int server_id) {
						    if (cpu_name == null || cpu_name.isEmpty()) {
                                StringBuilder sb = new StringBuilder();
                                sb.append(new ClientMessage("Invalid CPU Name: ", "CPU名称非法")).append(" = (null).").append("\n");
                                sb.append(new ClientMessage("Please try again.", "请重试."));
                                Window.alert(sb.toString());
                                return false;
                            }
						    if (server_id == -1) {
                                StringBuilder sb = new StringBuilder();
                                sb.append(new ClientMessage("Invalid Server Name.", "服务器名称非法.")).append("\n");
                                sb.append(new ClientMessage("Please try again.", "请重试."));
                                Window.alert(sb.toString());
                                return false;
                            }
							getBackendService().createDeviceCPU(getSession(), cpu_name, cpu_desc, cpu_total, cpu_vendor, cpu_model, cpu_ghz, cpu_cache, server_id, new AsyncCallback<Void>() {

								@Override
								public void onFailure(Throwable caught) {
								    onBackendServiceFailure(caught);
                                    getView().clearSelection();
								}

								@Override
								public void onSuccess(Void result) {
								    onBackendServiceFinished(new ClientMessage("Successfully create CPU.", "CPU添加成功."));
                                    getView().clearSelection();
                                    reloadCurrentRange();
								}
								
							});
							return true;
						}
						
						@Override
                        public void lookupAreaNames() {
                            getBackendService().lookupDeviceAreaNames(getSession(), new AsyncCallback<Map<String, Integer>>() {

                                @Override
                                public void onFailure(Throwable caught) {
                                    onBackendServiceFailure(caught);
                                }

                                @Override
                                public void onSuccess(Map<String, Integer> area_map) {
                                    onBackendServiceFinished();
                                    cpuAddView.setAreaNames(area_map);
                                }
                                
                            });
                        }
						
						@Override
                        public void lookupRoomNamesByAreaID(final int area_id) {
                            if (area_id == -1) {
                                StringBuilder sb = new StringBuilder();
                                sb.append(new ClientMessage("Invalid Area Name.", "区域名称非法.")).append("\n");
                                sb.append(new ClientMessage("Please try again.", "请重试."));
                                Window.alert(sb.toString());
                            }
                            else {
                                getBackendService().lookupDeviceRoomNamesByAreaID(getSession(), area_id, new AsyncCallback<Map<String, Integer>>() {
    
                                    @Override
                                    public void onFailure(Throwable caught) {
                                        onBackendServiceFailure(caught);
                                    }
    
                                    @Override
                                    public void onSuccess(Map<String, Integer> room_map) {
                                        onBackendServiceFinished();
                                        cpuAddView.setRoomNames(area_id, room_map);
                                    }
                                    
                                });
                            }
                        }					
						
                        @Override
                        public void lookupCabinetNamesByRoomID(final int room_id) {
                            if (room_id == -1) {
                                StringBuilder sb = new StringBuilder();
                                sb.append(new ClientMessage("Invalid Room Name.", "机房名称非法.")).append("\n");
                                sb.append(new ClientMessage("Please try again.", "请重试."));
                                Window.alert(sb.toString());
                            }
                            else {
                                getBackendService().lookupDeviceCabinetNamesByRoomID(getSession(), room_id, new AsyncCallback<Map<String, Integer>>() {

                                    @Override
                                    public void onFailure(Throwable caught) {
                                        onBackendServiceFailure(caught);
                                    }

                                    @Override
                                    public void onSuccess(Map<String, Integer> cabinet_map) {
                                        onBackendServiceFinished();
                                        cpuAddView.setCabinetNames(room_id, cabinet_map);
                                    }
                                    
                                });
                            }
                        }
						
						@Override
						public void lookupServerNamesByCabinetID(final int cabinet_id) {
						    if (cabinet_id == -1) {
                                StringBuilder sb = new StringBuilder();
                                sb.append(new ClientMessage("Invalid Cabinet Name.", "机柜名称非法.")).append("\n");
                                sb.append(new ClientMessage("Please try again.", "请重试."));
                                Window.alert(sb.toString());
                            }
							else {
							    getBackendService().lookupDeviceServerNamesByCabinetID(getSession(), cabinet_id, new AsyncCallback<Map<String, Integer>>() {

                                    @Override
                                    public void onFailure(Throwable caught) {
                                        onBackendServiceFailure(caught);
                                    }

                                    @Override
                                    public void onSuccess(Map<String, Integer> server_map) {
                                        onBackendServiceFinished();
                                        cpuAddView.setServerNames(cabinet_id, server_map);
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
			    if (Window.confirm(new ClientMessage("Modify selected CPU.", "确认修改所选择的CPU.").toString())) {
				    if (cpuModifyView == null) {
				        cpuModifyView = new DeviceCPUModifyViewImpl();
				        cpuModifyView.setPresenter(new DeviceCPUModifyView.Presenter() {
                            
                            @Override
                            public boolean onOK(int cpu_id, String cpu_desc, int cpu_total, int cs_used, String cpu_vendor, String cpu_model, double cpu_ghz, double cpu_cache) {
                                if (cpu_total <= 0 || cpu_total < cs_used) {
                                    StringBuilder sb = new StringBuilder();
                                    sb.append(new ClientMessage("Invalid CPU Total: ", "CPU数量非法")).append(" = ").append(cpu_total).append(".").append("\n");
                                    sb.append(new ClientMessage("Please try again.", "请重试."));
                                    Window.alert(sb.toString());
                                    return false;
                                }
                                getBackendService().modifyDeviceCPU(getSession(), cpu_id, cpu_desc, cpu_total, cpu_vendor, cpu_model, cpu_ghz, cpu_cache, new AsyncCallback<Void>() {

                                    @Override
                                    public void onFailure(Throwable caught) {
                                        onBackendServiceFailure(caught);
                                        getView().clearSelection();
                                    }

                                    @Override
                                    public void onSuccess(Void result) {
                                        onBackendServiceFinished(new ClientMessage("Successfully modify selected CPU.", "CPU修改成功."));
                                        reloadCurrentRange();
                                        getView().clearSelection();
                                    }
                                    
                                });
                                return true;
                            }
                            
                        });
				    }
                    SearchResultRow row = getView().getSelectedSet().iterator().next();
                    final int cpu_id = Integer.parseInt(row.getField(CellTableColumns.CPU.CPU_ID));
                    final String server_name = row.getField(CellTableColumns.CPU.SERVER_NAME);
                    getBackendService().lookupDeviceCPUByID(getSession(), cpu_id, new AsyncCallback<CPUInfo>() {

                        @Override
                        public void onFailure(Throwable caught) {
                            onBackendServiceFailure(caught);
                            getView().clearSelection();
                        }

                        @Override
                        public void onSuccess(final CPUInfo info) {
                            cpuModifyView.popup(cpu_id, info.cpu_name, info.cpu_desc, info.cpu_total, info.cpu_total - info.cs_reserved, info.cpu_vendor, info.cpu_model, info.cpu_ghz, info.cpu_cache, server_name); 
                        }
                        
                    });
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
				List<Integer> cpu_ids = new ArrayList<Integer>();
				for (SearchResultRow row : getView().getSelectedSet()) {
					int cpu_id = Integer.parseInt(row.getField(CellTableColumns.CPU.CPU_ID));
					cpu_ids.add(cpu_id);
				}
				if (!cpu_ids.isEmpty()) {
				    if (Window.confirm(new ClientMessage("Delete selected CPU(s).", "确认删除所选择的CPU.").toString())) {
						getBackendService().deleteDeviceCPU(getSession(), cpu_ids, new AsyncCallback<Void>() {
		
							@Override
							public void onFailure(Throwable caught) {
							    onBackendServiceFailure(caught);
                                getView().clearSelection();
							}
		
							@Override
							public void onSuccess(Void result) {
							    onBackendServiceFinished(new ClientMessage("Successfully delete selected CPU(s).", "CPU删除成功."));
                                getView().clearSelection();
                                reloadCurrentRange();
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
			    if (Window.confirm(new ClientMessage("Create a new CPU Service.", "确认创建新CPU服务.").toString())) {
					if (cpuServiceAddView == null) {
						cpuServiceAddView = new DeviceCPUServiceAddViewImpl();
						cpuServiceAddView.setPresenter(new DeviceCPUServiceAddView.Presenter() {
							
							@Override
							public boolean onOK(int cpu_id, String cs_desc, int cs_reserved, int cs_used, Date cs_starttime, Date cs_endtime, int user_id) {
								if (cs_used <= 0 || cs_used > cs_reserved) {
                                    StringBuilder sb = new StringBuilder();
                                    sb.append(new ClientMessage("Invalid CPU Total: ", "CPU数量非法")).append(" = ").append(cs_used).append(".").append("\n");
                                    sb.append(new ClientMessage("Please try again.", "请重试."));
                                    Window.alert(sb.toString());
                                    return false;
                                }
							    if (cs_starttime == null) {
							        StringBuilder sb = new StringBuilder();
                                    sb.append(new ClientMessage("Invalid Start Time: ", "开始时间非法")).append(" = (null).").append("\n");
                                    sb.append(new ClientMessage("Please try again.", "请重试."));
                                    Window.alert(sb.toString());
                                    return false;
							    }
							    if (cs_endtime == null) {
                                    StringBuilder sb = new StringBuilder();
                                    sb.append(new ClientMessage("Invalid End Time: ", "结束时间非法")).append(" = (null).").append("\n");
                                    sb.append(new ClientMessage("Please try again.", "请重试."));
                                    Window.alert(sb.toString());
                                    return false;
                                }
							    int cs_life = DeviceDate.calcLife(cs_endtime, cs_starttime);
							    if (cs_life <= 0) {
                                    StringBuilder sb = new StringBuilder();
                                    sb.append(new ClientMessage("Invalid Service Life Time: ", "服务期限非法")).append(" = ").append(cs_life).append(".").append("\n");
                                    sb.append(new ClientMessage("Please try again.", "请重试."));
                                    Window.alert(sb.toString());
                                    return false;
								}
							    if (user_id == -1) {
	                                StringBuilder sb = new StringBuilder();
	                                sb.append(new ClientMessage("Invalid User Name.", "用户名称非法.")).append("\n");
	                                sb.append(new ClientMessage("Please try again.", "请重试."));
	                                Window.alert(sb.toString());
	                                return false;
	                            }
								getBackendService().createDeviceCPUService(getSession(), cs_desc, cs_used, CPUState.STOP, cs_starttime, cs_endtime, cpu_id, user_id, new AsyncCallback<Void>() {
									
									@Override
									public void onFailure(Throwable caught) {
									    onBackendServiceFailure(caught);
	                                    getView().clearSelection();
									}

									@Override
									public void onSuccess(Void result) {
									    onBackendServiceFinished(new ClientMessage("Successfully create CPU Service.", "CPU服务添加成功."));
	                                    getView().clearSelection();
	                                    reloadCurrentRange();
									}
											
								});
								return true;
							}
							
							@Override
							public void lookupAccountNames() {
							    getBackendService().lookupDeviceAccountNames(getSession(), new AsyncCallback<Map<String, Integer>>() {

	                                @Override
	                                public void onFailure(Throwable caught) {
	                                    onBackendServiceFailure(caught);
	                                }

	                                @Override
	                                public void onSuccess(Map<String, Integer> account_map) {
	                                    onBackendServiceFinished();
	                                    cpuServiceAddView.setAccountNames(account_map);
	                                }
	                                
	                            });
	                        }
	                        
	                        @Override
	                        public void lookupUserNamesByAccountID(final int account_id) {
	                            if (account_id == -1) {
	                                StringBuilder sb = new StringBuilder();
	                                sb.append(new ClientMessage("Invalid Account Name.", "账户名称非法.")).append("\n");
	                                sb.append(new ClientMessage("Please try again.", "请重试."));
	                                Window.alert(sb.toString());
	                            }
	                            else {
	                                getBackendService().lookupDeviceUserNamesByAccountID(getSession(), account_id, new AsyncCallback<Map<String, Integer>>() {
	    
	                                    @Override
	                                    public void onFailure(Throwable caught) {
	                                        onBackendServiceFailure(caught);
	                                    }
	    
	                                    @Override
	                                    public void onSuccess(Map<String, Integer> user_map) {
	                                        onBackendServiceFinished();
	                                        cpuServiceAddView.setUserNames(account_id, user_map);
	                                    }
	                                    
	                                });
	                            }
	                        }                   

						});
					}
					SearchResultRow row = getView().getSelectedSet().iterator().next();
                    final int cpu_id = Integer.parseInt(row.getField(CellTableColumns.CPU.CPU_ID));
                    final String server_name = row.getField(CellTableColumns.CPU.SERVER_NAME);
                    getBackendService().lookupDeviceCPUByID(getSession(), cpu_id, new AsyncCallback<CPUInfo>() {

                        @Override
                        public void onFailure(Throwable caught) {
                            onBackendServiceFailure(caught);
                            getView().clearSelection();
                        }

                        @Override
                        public void onSuccess(final CPUInfo info) {
                            cpuServiceAddView.popup(cpu_id, info.cpu_name, info.cs_reserved, server_name);
                        }
                        
                    });
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
			    if (Window.confirm(new ClientMessage("Modify selected CPU Service.", "确认修改所选择的CPU服务.").toString())) {
					if (cpuServiceModifyView == null) {
						cpuServiceModifyView = new DeviceCPUServiceModifyViewImpl();
						cpuServiceModifyView.setPresenter(new DeviceCPUServiceModifyView.Presenter() {
							
							@Override
							public boolean onOK(int cs_id, String cs_desc, int cs_reserved, int cs_used, Date cs_starttime, Date cs_endtime) {
							    if (cs_used <= 0 || cs_used > cs_reserved) {
                                    StringBuilder sb = new StringBuilder();
                                    sb.append(new ClientMessage("Invalid CPU Total: ", "CPU数量非法")).append(" = ").append(cs_used).append(".").append("\n");
                                    sb.append(new ClientMessage("Please try again.", "请重试."));
                                    Window.alert(sb.toString());
                                    return false;
                                }
							    if (cs_starttime == null) {
                                    StringBuilder sb = new StringBuilder();
                                    sb.append(new ClientMessage("Invalid Start Time: ", "开始时间非法")).append(" = (null).").append("\n");
                                    sb.append(new ClientMessage("Please try again.", "请重试."));
                                    Window.alert(sb.toString());
                                    return false;
                                }
                                if (cs_endtime == null) {
                                    StringBuilder sb = new StringBuilder();
                                    sb.append(new ClientMessage("Invalid End Time: ", "结束时间非法")).append(" = (null).").append("\n");
                                    sb.append(new ClientMessage("Please try again.", "请重试."));
                                    Window.alert(sb.toString());
                                    return false;
                                }
                                int cs_life = DeviceDate.calcLife(cs_endtime, cs_starttime);
                                if (cs_life <= 0) {
                                    StringBuilder sb = new StringBuilder();
                                    sb.append(new ClientMessage("Invalid Service Life Time: ", "服务期限非法")).append(" = ").append(cs_life).append(".").append("\n");
                                    sb.append(new ClientMessage("Please try again.", "请重试."));
                                    Window.alert(sb.toString());
                                    return false;
                                }
                                getBackendService().modifyDeviceCPUService(getSession(), cs_id, cs_desc, cs_used, cs_starttime, cs_endtime, new AsyncCallback<Void> () {

                                    @Override
                                    public void onFailure(Throwable caught) {
                                        onBackendServiceFailure(caught);
                                        getView().clearSelection();
                                    }

                                    @Override
                                    public void onSuccess(Void result) {
                                        onBackendServiceFinished(new ClientMessage("Successfully modify selected CPU Service.", "CPU服务修改成功."));
                                        reloadCurrentRange();
                                        getView().clearSelection();
                                    }
                                    
                                });
								return true;
							}
						});
					}
					SearchResultRow row = getView().getSelectedSet().iterator().next();
					final int cpu_id = Integer.parseInt(row.getField(CellTableColumns.CPU.CPU_ID));
                    final int cs_id = Integer.parseInt(row.getField(CellTableColumns.CPU.CPU_SERVICE_ID));
                    final String server_name = row.getField(CellTableColumns.CPU.SERVER_NAME);
                    final String account_name = row.getField(CellTableColumns.CPU.ACCOUNT_NAME);
                    final String user_name = row.getField(CellTableColumns.CPU.USER_NAME);
                    getBackendService().lookupDeviceCPUServiceByID(getSession(), cs_id, new AsyncCallback<CPUServiceInfo>() {

                        @Override
                        public void onFailure(Throwable caught) {
                            onBackendServiceFailure(caught);
                            getView().clearSelection();
                        }

                        @Override
                        public void onSuccess(final CPUServiceInfo cs_info) {
                            getBackendService().lookupDeviceCPUByID(getSession(), cpu_id, new AsyncCallback<CPUInfo>() {

                                @Override
                                public void onFailure(Throwable caught) {
                                    onBackendServiceFailure(caught);
                                    getView().clearSelection();
                                }

                                @Override
                                public void onSuccess(CPUInfo cpu_info) {
                                    cpuServiceModifyView.popup(cs_id, cpu_info.cpu_name, cs_info.cs_desc, cpu_info.cs_reserved, cs_info.cs_used, cs_info.cs_starttime, cs_info.cs_endtime, server_name, account_name, user_name);
                                }
                                
                            });
                        }
                        
                    });
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
				List<Integer> cs_ids = new ArrayList<Integer>();
				for (SearchResultRow row : getView().getSelectedSet()) {
					int cs_id = Integer.parseInt(row.getField(CellTableColumns.CPU.CPU_SERVICE_ID));
					cs_ids.add(cs_id);
				}
				if (!cs_ids.isEmpty()) {
				    if (Window.confirm(new ClientMessage("Delete selected CPU Service(s).", "确认删除所选择的CPU服务.").toString())) {
						getBackendService().deleteDeviceCPUService(getSession(), cs_ids, new AsyncCallback<Void>() {
		
							@Override
							public void onFailure(Throwable caught) {
							    onBackendServiceFailure(caught);
                                getView().clearSelection();
							}
		
							@Override
							public void onSuccess(Void result) {
							    onBackendServiceFinished(new ClientMessage("Successfully delete selected CPU Service(s).", "CPU服务删除成功."));
                                getView().clearSelection();
                                reloadCurrentRange();
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
