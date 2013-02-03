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
import com.eucalyptus.webui.client.view.DeviceMemoryAddView;
import com.eucalyptus.webui.client.view.DeviceMemoryAddViewImpl;
import com.eucalyptus.webui.client.view.DeviceMemoryModifyView;
import com.eucalyptus.webui.client.view.DeviceMemoryModifyViewImpl;
import com.eucalyptus.webui.client.view.DeviceMemoryServiceAddView;
import com.eucalyptus.webui.client.view.DeviceMemoryServiceAddViewImpl;
import com.eucalyptus.webui.client.view.DeviceMemoryServiceModifyView;
import com.eucalyptus.webui.client.view.DeviceMemoryServiceModifyViewImpl;
import com.eucalyptus.webui.client.view.DeviceMemoryView;
import com.eucalyptus.webui.shared.message.ClientMessage;
import com.eucalyptus.webui.shared.resource.device.CellTableColumns;
import com.eucalyptus.webui.shared.resource.device.MemoryInfo;
import com.eucalyptus.webui.shared.resource.device.MemoryServiceInfo;
import com.eucalyptus.webui.shared.resource.device.status.MemoryState;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class DeviceMemoryActivity extends DeviceActivity implements DeviceMemoryView.Presenter {

	private static final ClientMessage title = new ClientMessage("Memory", "内存");

	private Date dateBegin;
	private Date dateEnd;
	private MemoryState queryState = null;
	private Map<Integer, Long> memCounts = new HashMap<Integer, Long>();

	private DeviceMemoryAddView memAddView;
	private DeviceMemoryModifyView memModifyView;
	private DeviceMemoryServiceAddView memServiceAddView;
	private DeviceMemoryServiceModifyView memServiceModifyView;

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

	@Override
	protected void doSearch(String query, SearchRange range) {
		getBackendService().lookupDeviceMemoryByDate(getSession(), range, queryState, dateBegin, dateEnd, new AsyncCallback<SearchResult>() {

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
		getBackendService().lookupDeviceMemoryCounts(getSession(), new AsyncCallback<Map<Integer, Long>>() {

			@Override
			public void onFailure(Throwable caught) {
			    onBackendServiceFailure(caught);
			}

			@Override
			public void onSuccess(Map<Integer, Long> result) {
			    onBackendServiceFinished();
				memCounts = result;
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
		    if (Window.confirm(new ClientMessage("Create a new Memory.", "确认创建新内存.").toString())) {
				if (memAddView == null) {
					memAddView = new DeviceMemoryAddViewImpl();
					memAddView.setPresenter(new DeviceMemoryAddView.Presenter() {

						@Override
						public boolean onOK(String mem_name, String mem_desc, long mem_size, int server_id) {
						    if (mem_name == null || mem_name.isEmpty()) {
                                StringBuilder sb = new StringBuilder();
                                sb.append(new ClientMessage("Invalid Memory Name: ", "内存名称非法")).append(" = (null).").append("\n");
                                sb.append(new ClientMessage("Please try again.", "请重试."));
                                Window.alert(sb.toString());
                                return false;
                            }
						    if (mem_size <= 0) {
                                StringBuilder sb = new StringBuilder();
                                sb.append(new ClientMessage("Invalid Memory Size: ", "内存大小非法")).append(" = ").append(mem_size).append(".").append("\n");
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
						    getBackendService().createDeviceMemory(getSession(), mem_name, mem_desc, mem_size, server_id, new AsyncCallback<Void>() {
								
								@Override
								public void onFailure(Throwable caught) {
								    onBackendServiceFailure(caught);
                                    getView().clearSelection();
								}

								@Override
								public void onSuccess(Void result) {
								    onBackendServiceFinished(new ClientMessage("Successfully create Memory.", "内存添加成功."));
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
                                    memAddView.setAreaNames(area_map);
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
                                        memAddView.setRoomNames(area_id, room_map);
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
                                        memAddView.setCabinetNames(room_id, cabinet_map);
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
                                        memAddView.setServerNames(cabinet_id, server_map);
                                    }
                                    
                                });
                            }
                        }

					});
				}
				memAddView.popup();
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
		    if (Window.confirm(new ClientMessage("Modify selected Memory.", "确认修改所选择的内存.").toString())) {
				if (memModifyView == null) {
					memModifyView = new DeviceMemoryModifyViewImpl();
					memModifyView.setPresenter(new DeviceMemoryModifyView.Presenter() {

						@Override
					    public boolean onOK(int mem_id, String mem_desc, long mem_size, long ms_used) {
						    if (mem_size <= 0 || mem_size < ms_used) {
                                StringBuilder sb = new StringBuilder();
                                sb.append(new ClientMessage("Invalid Memory Size: ", "内存大小非法")).append(" = ").append(mem_size).append(".").append("\n");
                                sb.append(new ClientMessage("Please try again.", "请重试."));
                                Window.alert(sb.toString());
                                return false;
                            }
						    getBackendService().modifyDeviceMemory(getSession(), mem_id, mem_desc, mem_size, new AsyncCallback<Void>() {

								@Override
								public void onFailure(Throwable caught) {
                                    onBackendServiceFailure(caught);
                                    getView().clearSelection();
								}

								@Override
								public void onSuccess(Void result) {
                                    onBackendServiceFinished(new ClientMessage("Successfully modify selected Memory.", "内存修改成功."));
                                    reloadCurrentRange();
                                    getView().clearSelection();
								}

							});
							return true;
						}

					});
				}
				SearchResultRow row = getView().getSelectedSet().iterator().next();
                final int mem_id = Integer.parseInt(row.getField(CellTableColumns.MEMORY.MEMORY_ID));
                final String server_name = row.getField(CellTableColumns.MEMORY.SERVER_NAME);
                getBackendService().lookupDeviceMemoryByID(getSession(), mem_id, new AsyncCallback<MemoryInfo>() {
                    
                    @Override
                    public void onFailure(Throwable caught) {
                        onBackendServiceFailure(caught);
                        getView().clearSelection();
                    }

                    @Override
                    public void onSuccess(MemoryInfo info) {
                        memModifyView.popup(mem_id, info.mem_name, info.mem_desc, info.mem_size, info.mem_size - info.ms_reserved, server_name);
                    }
                    
                });
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
				List<Integer> mem_ids = new ArrayList<Integer>();
				for (SearchResultRow row : getView().getSelectedSet()) {
					int mem_id = Integer.parseInt(row.getField(CellTableColumns.MEMORY.MEMORY_ID));
					mem_ids.add(mem_id);
				}
				if (!mem_ids.isEmpty()) {
				    if (Window.confirm(new ClientMessage("Delete selected Memory(s).", "确认删除所选择的内存.").toString())) {
						getBackendService().deleteDeviceMemory(getSession(), mem_ids, new AsyncCallback<Void>() {

							@Override
							public void onFailure(Throwable caught) {
							    onBackendServiceFailure(caught);
                                getView().clearSelection();
							}

							@Override
							public void onSuccess(Void result) {
							    onBackendServiceFinished(new ClientMessage("Successfully delete selected Memory(s).", "内存删除成功."));
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
	public void onAddMemoryService() {
		try {
		    if (Window.confirm(new ClientMessage("Create a new Memory Service.", "确认创建新内存服务.").toString())) {
				if (memServiceAddView == null) {
					memServiceAddView = new DeviceMemoryServiceAddViewImpl();
					memServiceAddView.setPresenter(new DeviceMemoryServiceAddView.Presenter() {

						@Override
						public boolean onOK(int mem_id, String ms_desc, long ms_reserved, long ms_used, Date ms_starttime, Date ms_endtime, int user_id) {
							if (ms_used <= 0 || ms_used > ms_reserved) {
                                StringBuilder sb = new StringBuilder();
                                sb.append(new ClientMessage("Invalid Memory Size: ", "内存数量非法")).append(" = ").append(ms_used).append(".").append("\n");
                                sb.append(new ClientMessage("Please try again.", "请重试."));
                                Window.alert(sb.toString());
                                return false;
                            }
                            if (ms_starttime == null) {
                                StringBuilder sb = new StringBuilder();
                                sb.append(new ClientMessage("Invalid Start Time: ", "开始时间非法")).append(" = (null).").append("\n");
                                sb.append(new ClientMessage("Please try again.", "请重试."));
                                Window.alert(sb.toString());
                                return false;
                            }
                            if (ms_endtime == null) {
                                StringBuilder sb = new StringBuilder();
                                sb.append(new ClientMessage("Invalid End Time: ", "结束时间非法")).append(" = (null).").append("\n");
                                sb.append(new ClientMessage("Please try again.", "请重试."));
                                Window.alert(sb.toString());
                                return false;
                            }
                            int ms_life = DeviceDate.calcLife(ms_endtime, ms_starttime);
                            if (ms_life <= 0) {
                                StringBuilder sb = new StringBuilder();
                                sb.append(new ClientMessage("Invalid Service Life Time: ", "服务期限非法")).append(" = ").append(ms_life).append(".").append("\n");
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
                            getBackendService().createDeviceMemoryService(getSession(), ms_desc, ms_used, MemoryState.STOP, ms_starttime, ms_endtime, mem_id, user_id, new AsyncCallback<Void>() {

								@Override
								public void onFailure(Throwable caught) {
								    onBackendServiceFailure(caught);
                                    getView().clearSelection();
								}

								@Override
								public void onSuccess(Void result) {
								    onBackendServiceFinished(new ClientMessage("Successfully create Memory Service.", "内存服务添加成功."));
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
                                    memServiceAddView.setAccountNames(account_map);
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
                                        memServiceAddView.setUserNames(account_id, user_map);
                                    }
                                    
                                });
                            }
                        }                   

					});
				}
				SearchResultRow row = getView().getSelectedSet().iterator().next();
				final int mem_id = Integer.parseInt(row.getField(CellTableColumns.MEMORY.MEMORY_ID));
                final String server_name = row.getField(CellTableColumns.MEMORY.SERVER_NAME);
                getBackendService().lookupDeviceMemoryByID(getSession(), mem_id, new AsyncCallback<MemoryInfo>() {

                    @Override
                    public void onFailure(Throwable caught) {
                        onBackendServiceFailure(caught);
                        getView().clearSelection();
                    }

                    @Override
                    public void onSuccess(MemoryInfo info) {
                        memServiceAddView.popup(mem_id, info.mem_name, info.ms_reserved, server_name);
                    }
                    
                });
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
		    if (Window.confirm(new ClientMessage("Modify selected Memory Service.", "确认修改所选择的内存服务.").toString())) {
				if (memServiceModifyView == null) {
					memServiceModifyView = new DeviceMemoryServiceModifyViewImpl();
					memServiceModifyView.setPresenter(new DeviceMemoryServiceModifyView.Presenter() {

						@Override
						public boolean onOK(int ms_id, String ms_desc, long ms_reserved, long ms_used, Date ms_starttime, Date ms_endtime) {
						    if (ms_used <= 0 || ms_used > ms_reserved) {
                                StringBuilder sb = new StringBuilder();
                                sb.append(new ClientMessage("Invalid Memory Size: ", "内存大小非法")).append(" = ").append(ms_used).append(".").append("\n");
                                sb.append(new ClientMessage("Please try again.", "请重试."));
                                Window.alert(sb.toString());
                                return false;
                            }
						    if (ms_starttime == null) {
                                StringBuilder sb = new StringBuilder();
                                sb.append(new ClientMessage("Invalid Start Time: ", "开始时间非法")).append(" = (null).").append("\n");
                                sb.append(new ClientMessage("Please try again.", "请重试."));
                                Window.alert(sb.toString());
                                return false;
                            }
                            if (ms_endtime == null) {
                                StringBuilder sb = new StringBuilder();
                                sb.append(new ClientMessage("Invalid End Time: ", "结束时间非法")).append(" = (null).").append("\n");
                                sb.append(new ClientMessage("Please try again.", "请重试."));
                                Window.alert(sb.toString());
                                return false;
                            }
                            int ms_life = DeviceDate.calcLife(ms_endtime, ms_starttime);
                            if (ms_life <= 0) {
                                StringBuilder sb = new StringBuilder();
                                sb.append(new ClientMessage("Invalid Service Life Time: ", "服务期限非法")).append(" = ").append(ms_life).append(".").append("\n");
                                sb.append(new ClientMessage("Please try again.", "请重试."));
                                Window.alert(sb.toString());
                                return false;
                            }
                            getBackendService().modifyDeviceMemoryService(getSession(), ms_id, ms_desc, ms_used, ms_starttime, ms_endtime, new AsyncCallback<Void>() {
                                
                                @Override
                                public void onFailure(Throwable caught) {
                                    onBackendServiceFailure(caught);
                                    getView().clearSelection();
                                }

                                @Override
                                public void onSuccess(Void result) {
                                    onBackendServiceFinished(new ClientMessage("Successfully modify selected Memory Service.", "内存服务修改成功."));
                                    reloadCurrentRange();
                                    getView().clearSelection();
                                }
                                
                            });
							return true;
						}

					});
				}
				SearchResultRow row = getView().getSelectedSet().iterator().next();
                final int mem_id = Integer.parseInt(row.getField(CellTableColumns.MEMORY.MEMORY_ID));
                final int ms_id = Integer.parseInt(row.getField(CellTableColumns.MEMORY.MEMORY_SERVICE_ID));
                final String server_name = row.getField(CellTableColumns.DISK.SERVER_NAME);
                final String account_name = row.getField(CellTableColumns.DISK.ACCOUNT_NAME);
                final String user_name = row.getField(CellTableColumns.DISK.USER_NAME);
                getBackendService().lookupDeviceMemoryServiceByID(getSession(), ms_id, new AsyncCallback<MemoryServiceInfo>() {
                    
                    @Override
                    public void onFailure(Throwable caught) {
                        onBackendServiceFailure(caught);
                        getView().clearSelection();
                    }
                    
                    @Override
                    public void onSuccess(final MemoryServiceInfo ms_info) {
                        getBackendService().lookupDeviceMemoryByID(getSession(), mem_id, new AsyncCallback<MemoryInfo>() {
                            
                            @Override
                            public void onFailure(Throwable caught) {
                                onBackendServiceFailure(caught);
                                getView().clearSelection();
                            }

                            @Override
                            public void onSuccess(MemoryInfo mem_info) {
                                memServiceModifyView.popup(ms_id, mem_info.mem_name, ms_info.ms_desc, mem_info.ms_reserved, ms_info.ms_used, ms_info.ms_starttime, ms_info.ms_endtime, server_name, account_name, user_name);
                            }
                            
                        });
                    }
                    
                });
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
				List<Integer> ms_ids = new ArrayList<Integer>();
				for (SearchResultRow row : getView().getSelectedSet()) {
					int ms_id = Integer.parseInt(row.getField(CellTableColumns.MEMORY.MEMORY_SERVICE_ID));
					ms_ids.add(ms_id);
				}
				if (!ms_ids.isEmpty()) {
				    if (Window.confirm(new ClientMessage("Delete selected Memory Service(s).", "确认删除所选择的内存服务.").toString())) {
						getBackendService().deleteDeviceMemoryService(getSession(), ms_ids, new AsyncCallback<Void>() {

							@Override
							public void onFailure(Throwable caught) {
							    onBackendServiceFailure(caught);
                                getView().clearSelection();
							}

							@Override
							public void onSuccess(Void result) {
							    onBackendServiceFinished(new ClientMessage("Successfully delete selected Memory Service(s).", "内存服务删除成功."));
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
		Long count = memCounts.get(state == null ? -1 : state.getValue());
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
					MemoryState ms_state = MemoryState.parse(row.getField(CellTableColumns.MEMORY.MEMORY_SERVICE_STATE));
					if (ms_state != MemoryState.RESERVED) {
						return false;
					}
					long mem_total = Long.parseLong(row.getField(CellTableColumns.MEMORY.MEMORY_TOTAL));
					long ms_used = Long.parseLong(row.getField(CellTableColumns.MEMORY.MEMORY_SERVICE_USED));
					if (mem_total != ms_used) {
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
				MemoryState ms_state = MemoryState.parse(row.getField(CellTableColumns.MEMORY.MEMORY_SERVICE_STATE));
				if (ms_state != MemoryState.RESERVED) {
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
					MemoryState ms_state = MemoryState.parse(row.getField(CellTableColumns.MEMORY.MEMORY_SERVICE_STATE));
					if (ms_state != MemoryState.INUSE && ms_state != MemoryState.STOP) {
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
				MemoryState ms_state = MemoryState.parse(row.getField(CellTableColumns.MEMORY.MEMORY_SERVICE_STATE));
				if (ms_state != MemoryState.INUSE && ms_state != MemoryState.STOP) {
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
