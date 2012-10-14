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
import com.eucalyptus.webui.client.view.DeviceDiskAddView;
import com.eucalyptus.webui.client.view.DeviceDiskAddViewImpl;
import com.eucalyptus.webui.client.view.DeviceDiskModifyView;
import com.eucalyptus.webui.client.view.DeviceDiskModifyViewImpl;
import com.eucalyptus.webui.client.view.DeviceDiskServiceAddView;
import com.eucalyptus.webui.client.view.DeviceDiskServiceAddViewImpl;
import com.eucalyptus.webui.client.view.DeviceDiskServiceModifyView;
import com.eucalyptus.webui.client.view.DeviceDiskServiceModifyViewImpl;
import com.eucalyptus.webui.client.view.DeviceDiskView;
import com.eucalyptus.webui.client.view.FooterView;
import com.eucalyptus.webui.client.view.HasValueWidget;
import com.eucalyptus.webui.client.view.LogView;
import com.eucalyptus.webui.client.view.FooterView.StatusType;
import com.eucalyptus.webui.client.view.LogView.LogType;
import com.eucalyptus.webui.shared.resource.device.CellTableColumns;
import com.eucalyptus.webui.shared.resource.device.status.DiskState;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class DeviceDiskActivity extends AbstractSearchActivity implements DeviceDiskView.Presenter {
	
	private static final ClientMessage title = new ClientMessage("Disk", "硬盘");
	
	private Date dateBegin;
	private Date dateEnd;
	private DiskState queryState = null;
	private Map<Integer, Long> diskCounts = new HashMap<Integer, Long>();
	
	private DeviceDiskAddView diskAddView;
	private DeviceDiskModifyView diskModifyView;
	private DeviceDiskServiceAddView diskServiceAddView;
	private DeviceDiskServiceModifyView diskServiceModifyView;
	
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
		getBackendService().lookupDeviceDiskByDate(getSession(), range, queryState, dateBegin, dateEnd, new AsyncCallback<SearchResult>() {

			@Override
			public void onFailure(Throwable caught) {
				if (caught instanceof EucalyptusServiceException) {
					onBackendServiceFailure((EucalyptusServiceException)caught);
				}
				displayData(null);
			}

			@Override
			public void onSuccess(SearchResult result) {
				showStatus(new ClientMessage("", "查询硬盘成功"));
				displayData(result);
			}
			
		});
		getBackendService().lookupDeviceDiskCounts(getSession(), new AsyncCallback<Map<Integer, Long>>() {

			@Override
			public void onFailure(Throwable caught) {
				if (caught instanceof EucalyptusServiceException) {
					onBackendServiceFailure((EucalyptusServiceException)caught);
				}
			}

			@Override
			public void onSuccess(Map<Integer, Long> result) {
				showStatus(new ClientMessage("", "查询硬盘数量成功"));
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
            if (Window.confirm(new ClientMessage("", "确认创建新硬盘设备").toString())) {
                if (diskAddView == null) {
                    diskAddView = new DeviceDiskAddViewImpl();
                    diskAddView.setPresenter(new DeviceDiskAddView.Presenter() {
                        
                        @Override
                        public boolean onOK(String disk_name, String disk_desc, long disk_size, String server_name) {
                            if (isEmpty(disk_name)) {
                                StringBuilder sb = new StringBuilder();
                                sb.append(new ClientMessage("", "硬盘名称非法")).append(" = '").append(disk_name).append("' ");
                                sb.append(new ClientMessage("", "请重新选择硬盘名称"));
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
                            if (disk_size <= 0) {
                                StringBuilder sb = new StringBuilder();
                                sb.append(new ClientMessage("", "硬盘大小非法")).append(" = '").append(disk_size).append("' ");
                                sb.append(new ClientMessage("", "请重新选择硬盘大小"));
                                Window.alert(sb.toString());
                                return false;
                            }
                            getBackendService().addDeviceDisk(getSession(), disk_name, disk_desc, disk_size, server_name, new AsyncCallback<Void>() {

                                @Override
                                public void onFailure(Throwable caught) {
                                    if (caught instanceof EucalyptusServiceException) {
                                        onBackendServiceFailure((EucalyptusServiceException)caught);
                                    }
                                    getView().clearSelection();
                                }

                                @Override
                                public void onSuccess(Void result) {
                                    showStatus(new ClientMessage("", "添加硬盘成功"));
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
                                    diskAddView.setAreaNameList(area_name_list);
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
                                        diskAddView.setRoomNameList(area_name, room_name_list);
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
                                        diskAddView.setCabinetNameList(room_name, cabinet_name_list);
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
                                        diskAddView.setServerNameList(cabinet_name, cabinet_name_list);
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
            if (Window.confirm(new ClientMessage("", "确认修改所选择的硬盘").toString())) {
                SearchResultRow row = getView().getSelectedSet().iterator().next();
                if (diskModifyView == null) {
                    diskModifyView = new DeviceDiskModifyViewImpl();
                    diskModifyView.setPresenter(new DeviceDiskModifyView.Presenter() {
                        
                        @Override
                        public boolean onOK(int disk_id, String disk_desc, long disk_size) {
                            if (disk_size <= 0) {
                                StringBuilder sb = new StringBuilder();
                                sb.append(new ClientMessage("", "硬盘大小非法")).append(" = '").append(disk_size).append("' ");
                                sb.append(new ClientMessage("", "请重新选择硬盘大小"));
                                Window.alert(sb.toString());
                                return false;
                            }
                            getBackendService().modifyDeviceDisk(getSession(), disk_id, disk_desc, disk_size, new AsyncCallback<Void> () {

                                @Override
                                public void onFailure(Throwable caught) {
                                    if (caught instanceof EucalyptusServiceException) {
                                        onBackendServiceFailure((EucalyptusServiceException)caught);
                                    }
                                    getView().clearSelection();
                                }

                                @Override
                                public void onSuccess(Void result) {
                                    showStatus(new ClientMessage("", "修改硬盘成功"));
                                    reloadCurrentRange();
                                    getView().clearSelection();
                                }
                                
                            });
                            return true;
                        }
                        
                    });
                }
                int disk_id = Integer.parseInt(row.getField(CellTableColumns.DISK.DISK_ID));
			    String disk_name = row.getField(CellTableColumns.DISK.DISK_NAME);
			    String disk_desc = row.getField(CellTableColumns.DISK.DISK_DESC);
			    long disk_size = Long.parseLong(row.getField(CellTableColumns.DISK.DISK_TOTAL));
			    String server_name = row.getField(CellTableColumns.DISK.SERVER_NAME);
			    diskModifyView.popup(disk_id, disk_name, disk_desc, disk_size, server_name);
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
                List<Integer> disk_id_list = new ArrayList<Integer>();
                for (SearchResultRow row : getView().getSelectedSet()) {
                    int disk_id = Integer.parseInt(row.getField(CellTableColumns.DISK.DISK_ID));
                    disk_id_list.add(disk_id);
                }
                if (!disk_id_list.isEmpty()) {
                    if (Window.confirm(new ClientMessage("", "确认删除所选择的硬盘").toString())) {
                        getBackendService().deleteDeviceDisk(getSession(), disk_id_list, new AsyncCallback<Void>() {
        
                            @Override
                            public void onFailure(Throwable caught) {
                                if (caught instanceof EucalyptusServiceException) {
                                    onBackendServiceFailure((EucalyptusServiceException)caught);
                                }
                                getView().clearSelection();
                            }
        
                            @Override
                            public void onSuccess(Void result) {
                                showStatus(new ClientMessage("", "删除硬盘成功"));
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
    public void onAddDiskService() {
        try {
            if (Window.confirm(new ClientMessage("", "确认添加的硬盘服务").toString())) {
                SearchResultRow row = getView().getSelectedSet().iterator().next();
                if (diskServiceAddView == null) {
                    diskServiceAddView = new DeviceDiskServiceAddViewImpl();
                    diskServiceAddView.setPresenter(new DeviceDiskServiceAddView.Presenter() {
                        
                        @Override
                        public boolean onOK(int disk_id, String ds_desc, long ds_used, Date ds_starttime, Date ds_endtime, String account_name, String user_name) {
                            if (ds_used <= 0) {
                                StringBuilder sb = new StringBuilder();
                                sb.append(new ClientMessage("", "硬盘数量非法")).append(" = '").append(ds_used).append("' ");
                                sb.append(new ClientMessage("", "请重新选择数量"));
                                Window.alert(sb.toString());
                                return false;
                            }
                            if (ds_starttime == null || ds_endtime == null || DeviceDate.calcLife(ds_endtime, ds_starttime) <= 0) {
								StringBuilder sb = new StringBuilder();
                                sb.append(new ClientMessage("", "非法的服务时间"));
                                if (ds_starttime != null && ds_endtime != null) {
                                	sb.append(" = '").append(DeviceDate.format(ds_starttime)).append("' >= '").append(DeviceDate.format(ds_endtime)).append("'");
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
                            getBackendService().addDeviceDiskService(getSession(), ds_desc, ds_used, ds_starttime, ds_endtime, disk_id, account_name, user_name, new AsyncCallback<Void>() {

                                @Override
                                public void onFailure(Throwable caught) {
                                    if (caught instanceof EucalyptusServiceException) {
                                        onBackendServiceFailure((EucalyptusServiceException)caught);
                                    }
                                    getView().clearSelection();
                                }

                                @Override
                                public void onSuccess(Void result) {
                                    showStatus(new ClientMessage("", "添加硬盘服务成功"));
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
                                        onBackendServiceFailure((EucalyptusServiceException)caught);
                                    }
                                }

                                @Override
                                public void onSuccess(List<String> account_name_list) {
                                    showStatus(new ClientMessage("", "获取账户列表成功"));
                                    diskServiceAddView.setAccountNameList(account_name_list);
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
                                            onBackendServiceFailure((EucalyptusServiceException)caught);
                                        }
                                    }

                                    @Override
                                    public void onSuccess(List<String> user_name_list) {
                                        showStatus(new ClientMessage("", "获取用户列表成功"));
                                        diskServiceAddView.setUserNameList(account_name, user_name_list);
                                    }
                                    
                                });
                            }
                        }
                        
                    });
                }
                int disk_id = Integer.parseInt(row.getField(CellTableColumns.DISK.DISK_ID));
			    String disk_name = row.getField(CellTableColumns.DISK.DISK_NAME);
			    String server_name = row.getField(CellTableColumns.DISK.SERVER_NAME);
			    long ds_reserved = Long.parseLong(row.getField(CellTableColumns.DISK.DISK_SERVICE_USED));
			    diskServiceAddView.popup(disk_id, disk_name, server_name, ds_reserved);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            onFrontendServiceFailure(e);
        }
    }

    @Override
    public void onModifyDiskService() {
        try {
            if (Window.confirm(new ClientMessage("", "确认修改所选择的硬盘服务").toString())) {
                SearchResultRow row = getView().getSelectedSet().iterator().next();
                if (diskServiceModifyView == null) {
                    diskServiceModifyView = new DeviceDiskServiceModifyViewImpl();
                    diskServiceModifyView.setPresenter(new DeviceDiskServiceModifyView.Presenter() {
                        
                        @Override
                        public boolean onOK(int ds_id, String ds_desc, Date ds_starttime, Date ds_endtime) {
                        	if (ds_starttime == null || ds_endtime == null || DeviceDate.calcLife(ds_endtime, ds_starttime) <= 0) {
								StringBuilder sb = new StringBuilder();
                                sb.append(new ClientMessage("", "非法的服务时间"));
                                if (ds_starttime != null && ds_endtime != null) {
                                	sb.append(" = '").append(DeviceDate.format(ds_starttime)).append("' >= '").append(DeviceDate.format(ds_endtime)).append("'");
                                }
                                sb.append(new ClientMessage("", "请重新选择时间"));
                                Window.alert(sb.toString());
                                return false;
							}
                            getBackendService().modifyDeviceDiskService(getSession(), ds_id, ds_desc, ds_starttime, ds_endtime, new AsyncCallback<Void>() {

                                @Override
                                public void onFailure(Throwable caught) {
                                    if (caught instanceof EucalyptusServiceException) {
                                        onBackendServiceFailure((EucalyptusServiceException)caught);
                                    }
                                    getView().clearSelection();
                                }

                                @Override
                                public void onSuccess(Void result) {
                                    showStatus(new ClientMessage("", "变更硬盘服务成功"));
                                    reloadCurrentRange();
                                    getView().clearSelection();
                                }
                                
                            });
                            return true;
                        }
                        
                    });
                }
				int ds_id = Integer.parseInt(row.getField(CellTableColumns.DISK.DISK_SERVICE_ID));
				String ds_desc = row.getField(CellTableColumns.DISK.DISK_SERVICE_DESC);
			    String disk_name = row.getField(CellTableColumns.DISK.DISK_NAME);
			    int ds_used = Integer.parseInt(row.getField(CellTableColumns.DISK.DISK_SERVICE_USED));
			    Date ds_starttime = DeviceDate.parse(row.getField(CellTableColumns.DISK.DISK_SERVICE_STARTTIME));
			    Date ds_endtime = DeviceDate.parse(row.getField(CellTableColumns.DISK.DISK_SERVICE_ENDTIME));
			    String server_name = row.getField(CellTableColumns.DISK.SERVER_NAME);
			    String account_name = row.getField(CellTableColumns.DISK.ACCOUNT_NAME);
			    String user_name = row.getField(CellTableColumns.DISK.USER_NAME);
			    diskServiceModifyView.popup(ds_id, disk_name, ds_desc, ds_used, ds_starttime, ds_endtime, server_name, account_name, user_name);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            onFrontendServiceFailure(e);
        }
    }

    @Override
    public void onDeleteDiskService() {
        try {
            if (canDeleteDiskService()) {
                List<Integer> ds_id_list = new ArrayList<Integer>();
                for (SearchResultRow row : getView().getSelectedSet()) {
                    int ds_id = Integer.parseInt(row.getField(CellTableColumns.DISK.DISK_SERVICE_ID));
                    ds_id_list.add(ds_id);
                }
                if (!ds_id_list.isEmpty()) {
                    if (Window.confirm(new ClientMessage("", "确认删除所选择的硬盘服务").toString())) {
                        getBackendService().deleteDeviceDiskService(getSession(), ds_id_list, new AsyncCallback<Void>() {
        
                            @Override
                            public void onFailure(Throwable caught) {
                                if (caught instanceof EucalyptusServiceException) {
                                    onBackendServiceFailure((EucalyptusServiceException)caught);
                                }
                                getView().clearSelection();
                            }
        
                            @Override
                            public void onSuccess(Void result) {
                                showStatus(new ClientMessage("", "删除硬盘服务成功"));
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
					DiskState disk_state = DiskState.parse(row.getField(CellTableColumns.DISK.DISK_SERVICE_STATE));
					if (disk_state != DiskState.RESERVED) {
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

	@Override
	public boolean canAddDiskService() {
		Set<SearchResultRow> set = getView().getSelectedSet();
		if (set.size() == 1) {
			try {
				SearchResultRow row = set.iterator().next();
				DiskState disk_state = DiskState.parse(row.getField(CellTableColumns.DISK.DISK_SERVICE_STATE));
				if (disk_state != DiskState.RESERVED) {
					return false;
				}
				long ds_used = Integer.parseInt(row.getField(CellTableColumns.DISK.DISK_SERVICE_USED));
				if (ds_used == 0) {
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
	public boolean canDeleteDiskService() {
		Set<SearchResultRow> set = getView().getSelectedSet();
		if (!set.isEmpty()) {
			for (SearchResultRow row : set) {
				try {
					DiskState disk_state = DiskState.parse(row.getField(CellTableColumns.DISK.DISK_SERVICE_STATE));
					if (disk_state != DiskState.INUSE && disk_state != DiskState.STOP) {
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
	public boolean canModifyDiskService() {
		Set<SearchResultRow> set = getView().getSelectedSet();
		if (set.size() == 1) {
			try {
				SearchResultRow row = set.iterator().next();
				DiskState disk_state = DiskState.parse(row.getField(CellTableColumns.DISK.DISK_SERVICE_STATE));
				if (disk_state != DiskState.INUSE && disk_state != DiskState.STOP) {
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
