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
import com.eucalyptus.webui.client.view.DeviceIPServiceAddView;
import com.eucalyptus.webui.client.view.DeviceIPServiceAddViewImpl;
import com.eucalyptus.webui.client.view.DeviceIPView;
import com.eucalyptus.webui.shared.message.ClientMessage;
import com.eucalyptus.webui.shared.resource.device.CellTableColumns;
import com.eucalyptus.webui.shared.resource.device.status.IPState;
import com.eucalyptus.webui.shared.resource.device.status.IPType;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class DeviceIPActivity extends DeviceActivity implements DeviceIPView.Presenter {
    
    private static final ClientMessage title = new ClientMessage("IP", "IP地址");
    
    private IPType queryType = null;
    private IPState queryState = null;
    private Map<Integer, Integer> ipCounts = new HashMap<Integer, Integer>();
    
    private DeviceIPServiceAddView ipServiceAddView;

    public DeviceIPActivity(SearchPlace place, ClientFactory clientFactory) {
        super(place, clientFactory);
        super.pageSize = DevicePageSize.getPageSize();
    }
    
    private DeviceIPView getView() {
        DeviceIPView view = (DeviceIPView)this.view;
        if (view == null) {
            view = clientFactory.getDeviceIPView();
            view.setPresenter(this);
            container.setWidget(view);
            view.clear();
            this.view = view;
        }
        return view;
    }
    
    @Override
    protected void doSearch(String query, SearchRange range) {
        getBackendService().lookupDeviceIP(getSession(), range, queryType, queryState, new AsyncCallback<SearchResult>() {

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
        getBackendService().lookupDeviceIPCounts(getSession(), queryType, new AsyncCallback<Map<Integer, Integer>>() {

            @Override
            public void onFailure(Throwable caught) {
                onBackendServiceFailure(caught);
            }

            @Override
            public void onSuccess(Map<Integer, Integer> result) {
                onBackendServiceFinished();
                ipCounts = result;
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
    public void onAddIPService() {
        try {
            if (Window.confirm(new ClientMessage("Create a new IP Address.", "确认创建新IP地址.").toString())) {
                if (ipServiceAddView == null) {
                    ipServiceAddView = new DeviceIPServiceAddViewImpl();
                    ipServiceAddView.setPresenter(new DeviceIPServiceAddView.Presenter() {
                        
                        @Override
                        public boolean onOK(String is_desc, Date is_starttime, Date is_endtime, IPType ip_type, int count, int user_id) {
                            if (ip_type == null) {
                                StringBuilder sb = new StringBuilder();
                                sb.append(new ClientMessage("Invalid IP Address Type: ", "IP地址类型非法")).append(" = (null).").append("\n");
                                sb.append(new ClientMessage("Please try again.", "请重试."));
                                Window.alert(sb.toString());
                                return false;
                            }
                            if (is_starttime == null) {
                                StringBuilder sb = new StringBuilder();
                                sb.append(new ClientMessage("Invalid Start Time: ", "开始时间非法")).append(" = (null).").append("\n");
                                sb.append(new ClientMessage("Please try again.", "请重试."));
                                Window.alert(sb.toString());
                                return false;
                            }
                            if (is_endtime == null) {
                                StringBuilder sb = new StringBuilder();
                                sb.append(new ClientMessage("Invalid End Time: ", "结束时间非法")).append(" = (null).").append("\n");
                                sb.append(new ClientMessage("Please try again.", "请重试."));
                                Window.alert(sb.toString());
                                return false;
                            }
                            int is_life = DeviceDate.calcLife(is_endtime, is_starttime);
                            if (is_life <= 0) {
                                StringBuilder sb = new StringBuilder();
                                sb.append(new ClientMessage("Invalid Service Life Time: ", "服务期限非法")).append(" = ").append(is_life).append(".").append("\n");
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
                            getBackendService().createDeviceIPService(getSession(), ip_type, is_desc, is_starttime, is_endtime, count, user_id, new AsyncCallback<Void>() {

                                @Override
                                public void onFailure(Throwable caught) {
                                    onBackendServiceFailure(caught);
                                    getView().clearSelection();
                                }

                                @Override
                                public void onSuccess(Void result) {
                                    onBackendServiceFinished(new ClientMessage("Successfully create IP Address Service.", "IP地址服务添加成功."));
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
                                    ipServiceAddView.setAccountNames(account_map);
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
                                        ipServiceAddView.setUserNames(account_id, user_map);
                                    }
                                    
                                });
                            }
                        }                   
                    });
                }
                ipServiceAddView.popup();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            onFrontendServiceFailure(e);
        }
    }
    
    @Override
    public void onDeleteIPService() {
        try {
            if (canDeleteIPService()) {
                List<Integer> ip_ids = new ArrayList<Integer>();
                for (SearchResultRow row : getView().getSelectedSet()) {
                    int ip_id = Integer.parseInt(row.getField(CellTableColumns.IP.IP_ID));
                    ip_ids.add(ip_id);
                }
                if (!ip_ids.isEmpty()) {
                    if (Window.confirm(new ClientMessage("Delete selected IP Address Service(s).", "确认删除所选择的IP地址服务.").toString())) {
                        getBackendService().deleteDeviceIPService(getSession(), ip_ids, new AsyncCallback<Void>() {

                            @Override
                            public void onFailure(Throwable caught) {
                                onBackendServiceFailure(caught);
                                getView().clearSelection();
                            }

                            @Override
                            public void onSuccess(Void result) {
                                onBackendServiceFinished(new ClientMessage("Successfully delete selected IP Address Service(s).", "IP地址服务删除成功."));
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
    public IPState getQueryState() {
        return queryState;
    }

    @Override
    public void setQueryState(IPState queryState) {
        if (this.queryState != queryState) {
            getView().clearSelection();
            this.queryState = queryState;
            range = new SearchRange(0, DevicePageSize.getPageSize(), -1, true);
            reloadCurrentRange();
        }
    }
    
    @Override
    public void setQueryType(IPType queryType) {
        if (this.queryType != queryType) {
            getView().clearSelection();
            this.queryType = queryType;
            range = new SearchRange(0, DevicePageSize.getPageSize(), -1, true);
            reloadCurrentRange();
        }
    }
    
    @Override
    public int getCounts(IPState state) {
        Integer count = ipCounts.get(state == null ? -1 : state.getValue());
        if (count == null) {
            return 0;
        }
        return count;
    }
    
    @Override
    public void updateSearchResult() {
        getView().clearSelection();
        range = new SearchRange(0, DevicePageSize.getPageSize(), -1, true);
        reloadCurrentRange();
    }

    @Override
    public boolean canDeleteIPService() {
        Set<SearchResultRow> set = getView().getSelectedSet();
        if (!set.isEmpty()) {
            for (SearchResultRow row : set) {
                try {
                    IPState ip_state = IPState.parse(row.getField(CellTableColumns.IP.IP_SERVICE_STATE));
                    if (ip_state != IPState.RESERVED) {
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
    
}
