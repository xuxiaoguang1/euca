package com.eucalyptus.webui.client.activity.device;

import java.util.ArrayList;
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
import com.eucalyptus.webui.client.view.DeviceMemoryView;
import com.eucalyptus.webui.shared.message.ClientMessage;
import com.eucalyptus.webui.shared.resource.device.CellTableColumns;
import com.eucalyptus.webui.shared.resource.device.MemoryInfo;
import com.eucalyptus.webui.shared.resource.device.status.MemoryState;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class DeviceMemoryActivity extends DeviceActivity implements DeviceMemoryView.Presenter {

    private static final ClientMessage title = new ClientMessage("Memory", "内存");

    private MemoryState queryState = null;
    private Map<Integer, Long> memCounts = new HashMap<Integer, Long>();

    private DeviceMemoryAddView memAddView;
    private DeviceMemoryModifyView memModifyView;

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
        getBackendService().lookupDeviceMemory(getSession(), range, queryState, new AsyncCallback<SearchResult>() {

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
                        public boolean onOK(String mem_desc, long mem_total, int server_id) {
                            if (mem_total <= 0) {
                                StringBuilder sb = new StringBuilder();
                                sb.append(new ClientMessage("Invalid Memory Size: ", "内存大小非法")).append(" = ").append(mem_total).append(".").append("\n");
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
                            getBackendService().createDeviceMemory(getSession(), mem_desc, mem_total, server_id, new AsyncCallback<Void>() {
                                
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
                        public boolean onOK(int mem_id, String mem_desc, long mem_total, long ms_used) {
                            if (mem_total <= 0 || mem_total < ms_used) {
                                StringBuilder sb = new StringBuilder();
                                sb.append(new ClientMessage("Invalid Memory Size: ", "内存大小非法")).append(" = ").append(mem_total).append(".").append("\n");
                                sb.append(new ClientMessage("Please try again.", "请重试."));
                                Window.alert(sb.toString());
                                return false;
                            }
                            getBackendService().modifyDeviceMemory(getSession(), mem_id, mem_desc, mem_total, new AsyncCallback<Void>() {

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
                        memModifyView.popup(mem_id, info.mem_desc, info.mem_total, info.mem_total - info.ms_reserved, server_name);
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
    public void updateSearchResult() {
        getView().clearSelection();
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

}
