package com.eucalyptus.webui.client.activity.device;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.eucalyptus.webui.client.ClientFactory;
import com.eucalyptus.webui.client.place.SearchPlace;
import com.eucalyptus.webui.client.service.SearchRange;
import com.eucalyptus.webui.client.service.SearchResult;
import com.eucalyptus.webui.client.service.SearchResultRow;
import com.eucalyptus.webui.client.view.DeviceBWServiceAddView;
import com.eucalyptus.webui.client.view.DeviceBWServiceAddViewImpl;
import com.eucalyptus.webui.client.view.DeviceBWServiceModifyView;
import com.eucalyptus.webui.client.view.DeviceBWServiceModifyViewImpl;
import com.eucalyptus.webui.client.view.DeviceBWView;
import com.eucalyptus.webui.shared.message.ClientMessage;
import com.eucalyptus.webui.shared.resource.device.BWServiceInfo;
import com.eucalyptus.webui.shared.resource.device.CellTableColumns;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class DeviceBWActivity extends DeviceActivity implements DeviceBWView.Presenter {
    
    private static final ClientMessage title = new ClientMessage("BW", "带宽");
    
    private DeviceBWServiceAddView bwServiceAddView;
    private DeviceBWServiceModifyView bwServiceModifyView;

    public DeviceBWActivity(SearchPlace place, ClientFactory clientFactory) {
        super(place, clientFactory);
        super.pageSize = DevicePageSize.getPageSize();
    }
    
    private DeviceBWView getView() {
        DeviceBWView view = (DeviceBWView)this.view;
        if (view == null) {
            view = clientFactory.getDeviceBWView();
            view.setPresenter(this);
            container.setWidget(view);
            view.clear();
            this.view = view;
        }
        return view;
    }
    
    @Override
    protected void doSearch(String query, SearchRange range) {
        getBackendService().lookupDeviceBW(getSession(), range, new AsyncCallback<SearchResult>() {

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
    public void onAddBWService() {
        try {
            if (Window.confirm(new ClientMessage("Create a new Bandwidth Service.", "确认创建新带宽服务.").toString())) {
                if (bwServiceAddView == null) {
                    bwServiceAddView = new DeviceBWServiceAddViewImpl();
                    bwServiceAddView.setPresenter(new DeviceBWServiceAddView.Presenter() {
                        
                        @Override
                        public boolean onOK(String bs_desc, int bs_bw_max, Date bs_starttime, Date bs_endtime, int ip_id) {
                            if (bs_bw_max < 0) {
                                StringBuilder sb = new StringBuilder();
                                sb.append(new ClientMessage("Invalid Bandwidth Value: ", "带宽数值非法")).append(" = ").append(bs_bw_max).append(".").append("\n");
                                sb.append(new ClientMessage("Please try again.", "请重试."));
                                Window.alert(sb.toString());
                                return false;
                            }
                            if (bs_starttime == null) {
                                StringBuilder sb = new StringBuilder();
                                sb.append(new ClientMessage("Invalid Start Time: ", "开始时间非法")).append(" = (null).").append("\n");
                                sb.append(new ClientMessage("Please try again.", "请重试."));
                                Window.alert(sb.toString());
                                return false;
                            }
                            if (bs_endtime == null) {
                                StringBuilder sb = new StringBuilder();
                                sb.append(new ClientMessage("Invalid End Time: ", "结束时间非法")).append(" = (null).").append("\n");
                                sb.append(new ClientMessage("Please try again.", "请重试."));
                                Window.alert(sb.toString());
                                return false;
                            }
                            int bs_life = DeviceDate.calcLife(bs_endtime, bs_starttime);
                            if (bs_life <= 0) {
                                StringBuilder sb = new StringBuilder();
                                sb.append(new ClientMessage("Invalid Service Life Time: ", "服务期限非法")).append(" = ").append(bs_life).append(".").append("\n");
                                sb.append(new ClientMessage("Please try again.", "请重试."));
                                Window.alert(sb.toString());
                                return false;
                            }
                            if (ip_id == -1) {
                                StringBuilder sb = new StringBuilder();
                                sb.append(new ClientMessage("Invalid IP Address.", "IP地址非法.")).append("\n");
                                sb.append(new ClientMessage("Please try again.", "请重试."));
                                Window.alert(sb.toString());
                                return false;
                            }
                            getBackendService().createDeviceBWService(getSession(), bs_desc, bs_bw_max, bs_starttime, bs_endtime, ip_id, new AsyncCallback<Void>() {

                                @Override
                                public void onFailure(Throwable caught) {
                                    onBackendServiceFailure(caught);
                                    getView().clearSelection();
                                }

                                @Override
                                public void onSuccess(Void result) {
                                    onBackendServiceFinished(new ClientMessage("Successfully create Bandwidth Service.", "带宽服务添加成功."));
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
                                    bwServiceAddView.setAccountNames(account_map);
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
                                        bwServiceAddView.setUserNames(account_id, user_map);
                                    }
                                    
                                });
                            }
                        }

                        @Override
                        public void lookupIPsWithoutBWService(final int account_id, final int user_id) {
                            getBackendService().lookupDeviceIPsWihtoutBWService(getSession(), account_id, user_id, new AsyncCallback<Map<String,Integer>>() {

                                @Override
                                public void onFailure(Throwable caught) {
                                    onBackendServiceFailure(caught);
                                }

                                @Override
                                public void onSuccess(Map<String, Integer> ip_map) {
                                    onBackendServiceFinished();
                                    bwServiceAddView.setIPs(account_id, user_id, ip_map);
                                }
                                
                            });
                        }
                        
                    });
                }
                bwServiceAddView.popup();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            onFrontendServiceFailure(e);
        }
    }
    
    @Override
    public void onModifyBWService() {
        try {
            if (canModifyBWService()) {
                if (Window.confirm(new ClientMessage("Modify selected Bandwidth Service.", "确认修改所选择的带宽服务.").toString())) {
                    if (bwServiceModifyView == null) {
                        bwServiceModifyView = new DeviceBWServiceModifyViewImpl();
                        bwServiceModifyView.setPresenter(new DeviceBWServiceModifyView.Presenter() {
                            
                            @Override
                            public boolean onOK(int bs_id, String bs_desc, int bs_bw_max, Date bs_starttime, Date bs_endtime) {
                                if (bs_bw_max < 0) {
                                    StringBuilder sb = new StringBuilder();
                                    sb.append(new ClientMessage("Invalid Bandwidth Value: ", "带宽数值非法")).append(" = ").append(bs_bw_max).append(".").append("\n");
                                    sb.append(new ClientMessage("Please try again.", "请重试."));
                                    Window.alert(sb.toString());
                                    return false;
                                }
                                if (bs_starttime == null) {
                                    StringBuilder sb = new StringBuilder();
                                    sb.append(new ClientMessage("Invalid Start Time: ", "开始时间非法")).append(" = (null).").append("\n");
                                    sb.append(new ClientMessage("Please try again.", "请重试."));
                                    Window.alert(sb.toString());
                                    return false;
                                }
                                if (bs_endtime == null) {
                                    StringBuilder sb = new StringBuilder();
                                    sb.append(new ClientMessage("Invalid End Time: ", "结束时间非法")).append(" = (null).").append("\n");
                                    sb.append(new ClientMessage("Please try again.", "请重试."));
                                    Window.alert(sb.toString());
                                    return false;
                                }
                                int bs_life = DeviceDate.calcLife(bs_endtime, bs_starttime);
                                if (bs_life <= 0) {
                                    StringBuilder sb = new StringBuilder();
                                    sb.append(new ClientMessage("Invalid Service Life Time: ", "服务期限非法")).append(" = ").append(bs_life).append(".").append("\n");
                                    sb.append(new ClientMessage("Please try again.", "请重试."));
                                    Window.alert(sb.toString());
                                    return false;
                                }
                                getBackendService().modifyDeviceBWService(getSession(), bs_id, bs_desc, bs_bw_max, bs_starttime, bs_endtime, new AsyncCallback<Void>() {

                                    @Override
                                    public void onFailure(Throwable caught) {
                                        onBackendServiceFailure(caught);
                                        getView().clearSelection();
                                    }

                                    @Override
                                    public void onSuccess(Void result) {
                                        onBackendServiceFinished(new ClientMessage("Successfully modify selected Bandwidth Service.", "带宽服务修改成功."));
                                        reloadCurrentRange();
                                        getView().clearSelection();
                                    }
                                    
                                });
                                return true;
                            }
    
                        });
                    }
                    SearchResultRow row = getView().getSelectedSet().iterator().next();
                    final int bs_id = Integer.parseInt(row.getField(CellTableColumns.BW.BW_SERVICE_ID));
                    final String ip_addr = row.getField(CellTableColumns.BW.IP_ADDR);
                    final String account_name = row.getField(CellTableColumns.BW.ACCOUNT_NAME);
                    final String user_name = row.getField(CellTableColumns.BW.USER_NAME);
                    getBackendService().lookupDeviceBWServiceByID(getSession(), bs_id, new AsyncCallback<BWServiceInfo>() {

                        @Override
                        public void onFailure(Throwable caught) {
                            onBackendServiceFailure(caught);
                            getView().clearSelection();
                        }

                        @Override
                        public void onSuccess(BWServiceInfo info) {
                            bwServiceModifyView.popup(bs_id, ip_addr, info.bs_desc, info.bs_bw_max, info.bs_starttime, info.bs_endtime, account_name, user_name);
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
    public void onDeleteBWService() {
        try {
            if (canDeleteBWService()) {
                List<Integer> bs_ids = new ArrayList<Integer>();
                for (SearchResultRow row : getView().getSelectedSet()) {
                    int bs_id = Integer.parseInt(row.getField(CellTableColumns.BW.BW_SERVICE_ID));
                    bs_ids.add(bs_id);
                }
                if (!bs_ids.isEmpty()) {
                    if (Window.confirm(new ClientMessage("Delete selected Bandwidth Service(s).", "确认删除所选择的带宽服务.").toString())) {
                        getBackendService().deleteDeviceBWService(getSession(), bs_ids, new AsyncCallback<Void>() {
                            
                            @Override
                            public void onFailure(Throwable caught) {
                                onBackendServiceFailure(caught);
                                getView().clearSelection();
                            }
                            
                            @Override
                            public void onSuccess(Void result) {
                                onBackendServiceFinished(new ClientMessage("Successfully delete selected Bandwidth Service(s).", "带宽服务删除成功."));
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
    public void updateSearchResult() {
        getView().clearSelection();
        range = new SearchRange(0, DevicePageSize.getPageSize(), -1, true);
        reloadCurrentRange();
    }

    @Override
    public boolean canDeleteBWService() {
        Set<SearchResultRow> set = getView().getSelectedSet();
        if (!set.isEmpty()) {
            return true;
        }
        return false;
    }
    
    @Override
    public boolean canModifyBWService() {
        Set<SearchResultRow> set = getView().getSelectedSet();
        if (set.size() == 1) {
            return true;
        }
        return false;
    }
    
}
