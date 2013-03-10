package com.eucalyptus.webui.client.activity.device;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.eucalyptus.webui.client.ClientFactory;
import com.eucalyptus.webui.client.place.SearchPlace;
import com.eucalyptus.webui.client.service.SearchRange;
import com.eucalyptus.webui.client.service.SearchResult;
import com.eucalyptus.webui.client.service.SearchResultRow;
import com.eucalyptus.webui.client.view.DeviceServerAddView;
import com.eucalyptus.webui.client.view.DeviceServerAddViewImpl;
import com.eucalyptus.webui.client.view.DeviceServerModifyView;
import com.eucalyptus.webui.client.view.DeviceServerModifyViewImpl;
import com.eucalyptus.webui.client.view.DeviceServerOperateView;
import com.eucalyptus.webui.client.view.DeviceServerOperateViewImpl;
import com.eucalyptus.webui.client.view.DeviceServerView;
import com.eucalyptus.webui.shared.message.ClientMessage;
import com.eucalyptus.webui.shared.resource.device.CellTableColumns;
import com.eucalyptus.webui.shared.resource.device.ServerInfo;
import com.eucalyptus.webui.shared.resource.device.status.ServerState;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class DeviceServerActivity extends DeviceActivity implements DeviceServerView.Presenter {

    private static final ClientMessage title = new ClientMessage("Server", "服务器");
    
    private ServerState queryState = null;
    private Map<Integer, Integer> serverCounts = new HashMap<Integer, Integer>();

    private DeviceServerAddView serverAddView;
    private DeviceServerModifyView serverModifyView;
    private DeviceServerOperateView serverOperateView;
    
    public DeviceServerActivity(SearchPlace place, ClientFactory clientFactory) {
        super(place, clientFactory);
        super.pageSize = DevicePageSize.getPageSize();
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
    
    @Override
    protected void doSearch(String query, SearchRange range) {
        getBackendService().lookupDeviceServer(getSession(), range, queryState, new AsyncCallback<SearchResult>() {

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
        getBackendService().lookupDeviceServerCounts(getSession(), new AsyncCallback<Map<Integer, Integer>>() {

            @Override
            public void onFailure(Throwable caught) {
                onBackendServiceFailure(caught);
            }

            @Override
            public void onSuccess(Map<Integer, Integer> result) {
                onBackendServiceFinished();
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
            if (Window.confirm(new ClientMessage("Create a new Server.", "确认创建新服务器.").toString())) {
                if (serverAddView == null) {
                    serverAddView = new DeviceServerAddViewImpl();
                    serverAddView.setPresenter(new DeviceServerAddView.Presenter() {
                        
                        @Override
                        public boolean onOK(String server_name, String server_desc, String server_euca, String server_ip, String bandwidth, ServerState server_state, int cabinet_id) {
                            if (server_name == null || server_name.isEmpty()) {
                                StringBuilder sb = new StringBuilder();
                                sb.append(new ClientMessage("Invalid Server Name: ", "服务器名称非法")).append(" = (null).").append("\n");
                                sb.append(new ClientMessage("Please try again.", "请重试."));
                                Window.alert(sb.toString());
                                return false;
                            }
                            if (server_euca == null || server_euca.isEmpty()) {
                                StringBuilder sb = new StringBuilder();
                                sb.append(new ClientMessage("Invalid Server Euca: ", "服务器映射非法")).append(" = (null).").append("\n");
                                sb.append(new ClientMessage("Please try again.", "请重试."));
                                Window.alert(sb.toString());
                                return false;
                            }
                            if (cabinet_id == -1) {
                                StringBuilder sb = new StringBuilder();
                                sb.append(new ClientMessage("Invalid Cabinet Name.", "机柜名称非法.")).append("\n");
                                sb.append(new ClientMessage("Please try again.", "请重试."));
                                Window.alert(sb.toString());
                                return false;
                            }
                            int server_bw = 0;
                            try {
                                if (bandwidth != null && !bandwidth.isEmpty()) {
                                    server_bw = Integer.parseInt(bandwidth);
                                }
                            }
                            catch (Exception e) {
                                StringBuilder sb = new StringBuilder();
                                sb.append(new ClientMessage("Invalid Bandwidth value.", "带宽数值非法.")).append("\n");
                                sb.append(new ClientMessage("Please try again.", "请重试."));
                                Window.alert(sb.toString());
                                return false;
                            }
                            getBackendService().createDeviceServer(getSession(), server_name, server_desc, server_euca, server_ip, server_bw, server_state, cabinet_id, new AsyncCallback<Void>() {

                                @Override
                                public void onFailure(Throwable caught) {
                                    onBackendServiceFailure(caught);
                                    getView().clearSelection();
                                }

                                @Override
                                public void onSuccess(Void result) {
                                    onBackendServiceFinished(new ClientMessage("Successfully create Server.", "服务器添加成功."));
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
                                    serverAddView.setAreaNames(area_map);
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
                                        serverAddView.setRoomNames(area_id, room_map);
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
                                        serverAddView.setCabinetNames(room_id, cabinet_map);
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
                if (Window.confirm(new ClientMessage("Modify selected Server.", "确认修改所选择的服务器.").toString())) {
                    if (serverModifyView == null) {
                        serverModifyView = new DeviceServerModifyViewImpl();
                        serverModifyView.setPresenter(new DeviceServerModifyView.Presenter() {
                            
                            @Override
                            public boolean onOK(int server_id, String server_desc, String server_ip, String bandwidth) {
                                int server_bw = 0;
                                try {
                                    if (bandwidth != null && !bandwidth.isEmpty()) {
                                        server_bw = Integer.parseInt(bandwidth);
                                    }
                                }
                                catch (Exception e) {
                                    StringBuilder sb = new StringBuilder();
                                    sb.append(new ClientMessage("Invalid Bandwidth value.", "带宽数值非法.")).append("\n");
                                    sb.append(new ClientMessage("Please try again.", "请重试."));
                                    Window.alert(sb.toString());
                                    return false;
                                }
                                getBackendService().modifyDeviceServer(getSession(), server_id, server_desc, server_ip, server_bw, new AsyncCallback<Void>() {

                                    @Override
                                    public void onFailure(Throwable caught) {
                                        onBackendServiceFailure(caught);
                                        getView().clearSelection();
                                    }

                                    @Override
                                    public void onSuccess(Void result) {
                                        onBackendServiceFinished(new ClientMessage("Successfully modify selected Server.", "服务器修改成功."));
                                        reloadCurrentRange();
                                        getView().clearSelection();
                                    }
                                    
                                });
                                return true;
                            }
                            
                        });
                    }
                    int server_id = Integer.parseInt(row.getField(CellTableColumns.SERVER.SERVER_ID));
                    getBackendService().lookupDeviceServerByID(getSession(), server_id, new AsyncCallback<ServerInfo>() {

                        @Override
                        public void onFailure(Throwable caught) {
                            onBackendServiceFailure(caught);
                            getView().clearSelection();
                        }

                        @Override
                        public void onSuccess(ServerInfo info) {
                            serverModifyView.popup(info.server_id, info.server_name, info.server_desc, info.server_euca, info.server_ip, info.server_bw, info.server_state);
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
    public void onOperateServer() {
        Set<SearchResultRow> set = getView().getSelectedSet();
        try {
            if (set != null && !set.isEmpty()) {
                SearchResultRow row = new LinkedList<SearchResultRow>(set).getFirst();
                if (Window.confirm(new ClientMessage("Modify selected Server.", "确认修改所选择的服务器.").toString())) {
                    if (serverOperateView == null) {
                        serverOperateView = new DeviceServerOperateViewImpl();
                        serverOperateView.setPresenter(new DeviceServerOperateView.Presenter() {
                            
                            @Override
                            public void onOK(int server_id, ServerState server_state) {
                                getBackendService().modifyDeviceServerState(getSession(), server_id, server_state, new AsyncCallback<Void>() {

                                    @Override
                                    public void onFailure(Throwable caught) {
                                        onBackendServiceFailure(caught);
                                        getView().clearSelection();
                                    }

                                    @Override
                                    public void onSuccess(Void result) {
                                        onBackendServiceFinished(new ClientMessage("Successfully modify selected Server.", "服务器修改成功."));
                                        reloadCurrentRange();
                                        getView().clearSelection();
                                    }
                                    
                                });
                            }
                            
                        });
                    }
                    int server_id = Integer.parseInt(row.getField(CellTableColumns.SERVER.SERVER_ID));
                    getBackendService().lookupDeviceServerByID(getSession(), server_id, new AsyncCallback<ServerInfo>() {

                        @Override
                        public void onFailure(Throwable caught) {
                            onBackendServiceFailure(caught);
                            getView().clearSelection();
                        }

                        @Override
                        public void onSuccess(ServerInfo info) {
                            serverOperateView.popup(info.server_id, info.server_name, info.server_state);
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
    public void onDeleteServer() {
        Set<SearchResultRow> set = getView().getSelectedSet();
        try {
            if (set != null && !set.isEmpty()) {
                List<Integer> server_ids = new ArrayList<Integer>();
                for (SearchResultRow row : set) {
                    int server_id = Integer.parseInt(row.getField(CellTableColumns.SERVER.SERVER_ID));
                    server_ids.add(server_id);
                }
                if (!server_ids.isEmpty()) {
                    if (Window.confirm(new ClientMessage("Delete selected Server(s).", "确认删除所选择的服务器.").toString())) {
                        getBackendService().deleteDeviceServer(getSession(), server_ids, new AsyncCallback<Void>() {
        
                            @Override
                            public void onFailure(Throwable caught) {
                                onBackendServiceFailure(caught);
                                getView().clearSelection();
                            }
        
                            @Override
                            public void onSuccess(Void result) {
                                onBackendServiceFinished(new ClientMessage("Successfully delete selected Server(s).", "服务器删除成功."));
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
    public void updateSearchResult() {
        getView().clearSelection();
        range = new SearchRange(0, getView().getPageSize(), -1, true);
        reloadCurrentRange();
    }
    
}
