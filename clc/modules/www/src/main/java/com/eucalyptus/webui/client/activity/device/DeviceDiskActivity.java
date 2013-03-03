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
import com.eucalyptus.webui.client.view.DeviceDiskAddView;
import com.eucalyptus.webui.client.view.DeviceDiskAddViewImpl;
import com.eucalyptus.webui.client.view.DeviceDiskModifyView;
import com.eucalyptus.webui.client.view.DeviceDiskModifyViewImpl;
import com.eucalyptus.webui.client.view.DeviceDiskView;
import com.eucalyptus.webui.shared.message.ClientMessage;
import com.eucalyptus.webui.shared.resource.device.CellTableColumns;
import com.eucalyptus.webui.shared.resource.device.DiskInfo;
import com.eucalyptus.webui.shared.resource.device.status.DiskState;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class DeviceDiskActivity extends DeviceActivity implements DeviceDiskView.Presenter {
	
	private static final ClientMessage title = new ClientMessage("Disk", "硬盘");
	
	private Date dateBegin;
	private Date dateEnd;
	private DiskState queryState = null;
	private Map<Integer, Long> diskCounts = new HashMap<Integer, Long>();
	
	private DeviceDiskAddView diskAddView;
	private DeviceDiskModifyView diskModifyView;
	
	public DeviceDiskActivity(SearchPlace place, ClientFactory clientFactory) {
		super(place, clientFactory);
		super.pageSize = DevicePageSize.getPageSize();
	}
	
	private DeviceDiskView getView() {
		DeviceDiskView view = (DeviceDiskView)this.view;
		if (view == null) {
			view = clientFactory.getDeviceDiskView();
			view.setPresenter(this);
			container.setWidget(view);
			view.clear();
			this.view = view;
		}
		return view;
	}
	
	@Override
	protected void doSearch(String query, SearchRange range) {
		getBackendService().lookupDeviceDiskByDate(getSession(), range, queryState, dateBegin, dateEnd, new AsyncCallback<SearchResult>() {

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
		getBackendService().lookupDeviceDiskCounts(getSession(), new AsyncCallback<Map<Integer, Long>>() {

			@Override
			public void onFailure(Throwable caught) {
			    onBackendServiceFailure(caught);
			}

			@Override
			public void onSuccess(Map<Integer, Long> result) {
			    onBackendServiceFinished();
				diskCounts = result;
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
    public void onAddDisk() {
        try {
            if (Window.confirm(new ClientMessage("Create a new Disk.", "确认创建新硬盘.").toString())) {
                if (diskAddView == null) {
                    diskAddView = new DeviceDiskAddViewImpl();
                    diskAddView.setPresenter(new DeviceDiskAddView.Presenter() {
                        
                        @Override
                        public boolean onOK(String disk_name, String disk_desc, long disk_size, int server_id) {
                            if (disk_name == null || disk_name.isEmpty()) {
                                StringBuilder sb = new StringBuilder();
                                sb.append(new ClientMessage("Invalid Disk Name: ", "硬盘名称非法")).append(" = (null).").append("\n");
                                sb.append(new ClientMessage("Please try again.", "请重试."));
                                Window.alert(sb.toString());
                                return false;
                            }
                            if (disk_size <= 0) {
                                StringBuilder sb = new StringBuilder();
                                sb.append(new ClientMessage("Invalid Disk Size: ", "硬盘大小非法")).append(" = ").append(disk_size).append(".").append("\n");
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
                            getBackendService().createDeviceDisk(getSession(), disk_name, disk_desc, disk_size, server_id, new AsyncCallback<Void>() {

                                @Override
                                public void onFailure(Throwable caught) {
                                    onBackendServiceFailure(caught);
                                    getView().clearSelection();
                                }

                                @Override
                                public void onSuccess(Void result) {
                                    onBackendServiceFinished(new ClientMessage("Successfully create Disk.", "硬盘添加成功."));
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
                                    diskAddView.setAreaNames(area_map);
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
                                        diskAddView.setRoomNames(area_id, room_map);
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
                                        diskAddView.setCabinetNames(room_id, cabinet_map);
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
                                        diskAddView.setServerNames(cabinet_id, server_map);
                                    }
                                    
                                });
                            }
                        }

                    });
                }
                diskAddView.popup();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            onFrontendServiceFailure(e);
        }
    }

    @Override
    public void onModifyDisk() {
        try {
            if (Window.confirm(new ClientMessage("Modify selected Disk.", "确认修改所选择的硬盘.").toString())) {
                if (diskModifyView == null) {
                    diskModifyView = new DeviceDiskModifyViewImpl();
                    diskModifyView.setPresenter(new DeviceDiskModifyView.Presenter() {
                        
                        @Override
                        public boolean onOK(int disk_id, String disk_desc, long disk_size, long ds_used) {
                            if (disk_size <= 0 || disk_size < ds_used) {
                                StringBuilder sb = new StringBuilder();
                                sb.append(new ClientMessage("Invalid Disk Size: ", "硬盘大小非法")).append(" = ").append(disk_size).append(".").append("\n");
                                sb.append(new ClientMessage("Please try again.", "请重试."));
                                Window.alert(sb.toString());
                                return false;
                            }
                            getBackendService().modifyDeviceDisk(getSession(), disk_id, disk_desc, disk_size, new AsyncCallback<Void> () {

                                @Override
                                public void onFailure(Throwable caught) {
                                    onBackendServiceFailure(caught);
                                    getView().clearSelection();
                                }

                                @Override
                                public void onSuccess(Void result) {
                                    onBackendServiceFinished(new ClientMessage("Successfully modify selected Disk.", "硬盘修改成功."));
                                    reloadCurrentRange();
                                    getView().clearSelection();
                                }
                                
                            });
                            return true;
                        }
                        
                    });
                }
                SearchResultRow row = getView().getSelectedSet().iterator().next();
                final int disk_id = Integer.parseInt(row.getField(CellTableColumns.DISK.DISK_ID));
                final String server_name = row.getField(CellTableColumns.DISK.SERVER_NAME);
                getBackendService().lookupDeviceDiskByID(getSession(), disk_id, new AsyncCallback<DiskInfo>() {

                    @Override
                    public void onFailure(Throwable caught) {
                        onBackendServiceFailure(caught);
                        getView().clearSelection();
                    }

                    @Override
                    public void onSuccess(DiskInfo info) {
                        diskModifyView.popup(disk_id, info.disk_name, info.disk_desc, info.disk_size, info.disk_size - info.ds_reserved, server_name);
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
    public void onDeleteDisk() {
        try {
            if (canDeleteDisk()) {
                List<Integer> disk_ids = new ArrayList<Integer>();
                for (SearchResultRow row : getView().getSelectedSet()) {
                    int disk_id = Integer.parseInt(row.getField(CellTableColumns.DISK.DISK_ID));
                    disk_ids.add(disk_id);
                }
                if (!disk_ids.isEmpty()) {
                    if (Window.confirm(new ClientMessage("Delete selected Disk(s).", "确认删除所选择的硬盘.").toString())) {
                        getBackendService().deleteDeviceDisk(getSession(), disk_ids, new AsyncCallback<Void>() {
        
                            @Override
                            public void onFailure(Throwable caught) {
                                onBackendServiceFailure(caught);
                                getView().clearSelection();
                            }
        
                            @Override
                            public void onSuccess(Void result) {
                                onBackendServiceFinished(new ClientMessage("Successfully delete selected Disk(s).", "硬盘删除成功."));
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
	public DiskState getQueryState() {
		return queryState;
	}

	@Override
	public void setQueryState(DiskState queryState) {
		if (this.queryState != queryState) {
	    	getView().clearSelection();
			this.queryState = queryState;
	    	range = new SearchRange(0, DevicePageSize.getPageSize(), -1, true);
	    	reloadCurrentRange();
		}
	}
	
	@Override
	public long getCounts(DiskState state) {
		Long count = diskCounts.get(state == null ? -1 : state.getValue());
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
	public boolean canDeleteDisk() {
		Set<SearchResultRow> set = getView().getSelectedSet();
		if (!set.isEmpty()) {
			for (SearchResultRow row : set) {
				try {
					DiskState ds_state = DiskState.parse(row.getField(CellTableColumns.DISK.DISK_SERVICE_STATE));
					if (ds_state != DiskState.RESERVED) {
						return false;
					}
					long disk_total = Long.parseLong(row.getField(CellTableColumns.DISK.DISK_TOTAL));
					long ds_used = Long.parseLong(row.getField(CellTableColumns.DISK.DISK_SERVICE_USED));
					if (disk_total != ds_used) {
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
	public boolean canModifyDisk() {
		Set<SearchResultRow> set = getView().getSelectedSet();
		if (set.size() == 1) {
			return true;
		}
		return false;
	}

}
