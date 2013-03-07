package com.eucalyptus.webui.client.activity.device;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.eucalyptus.webui.client.ClientFactory;
import com.eucalyptus.webui.client.place.SearchPlace;
import com.eucalyptus.webui.client.service.SearchRange;
import com.eucalyptus.webui.client.service.SearchResult;
import com.eucalyptus.webui.client.service.SearchResultRow;
import com.eucalyptus.webui.client.view.DeviceRoomAddView;
import com.eucalyptus.webui.client.view.DeviceRoomAddViewImpl;
import com.eucalyptus.webui.client.view.DeviceRoomModifyView;
import com.eucalyptus.webui.client.view.DeviceRoomModifyViewImpl;
import com.eucalyptus.webui.client.view.DeviceRoomView;
import com.eucalyptus.webui.shared.message.ClientMessage;
import com.eucalyptus.webui.shared.resource.device.CellTableColumns;
import com.eucalyptus.webui.shared.resource.device.RoomInfo;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class DeviceRoomActivity extends DeviceActivity implements DeviceRoomView.Presenter {
    
    private static final ClientMessage title = new ClientMessage("Room", "机房");
    
    private DeviceRoomAddView roomAddView;
    private DeviceRoomModifyView roomModifyView;
    
    public DeviceRoomActivity(SearchPlace place, ClientFactory clientFactory) {
        super(place, clientFactory);
        super.pageSize = DevicePageSize.getPageSize();
    }
    
    private DeviceRoomView getView() {
        DeviceRoomView view = (DeviceRoomView)this.view;
        if (view == null) {
            view = clientFactory.getDeviceRoomView();
            view.setPresenter(this);
            container.setWidget(view);
            view.clear();
            this.view = view;
        }
        return view;
    }
    
    @Override
    protected void doSearch(String query, SearchRange range) {
        getBackendService().lookupDeviceRoom(getSession(), range, new AsyncCallback<SearchResult>() {

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
    public void onAdd() {
        try {
            if (Window.confirm(new ClientMessage("Create a new Room.", "确认创建新机房.").toString())) {
                if (roomAddView == null) {
                    roomAddView = new DeviceRoomAddViewImpl();
                    roomAddView.setPresenter(new DeviceRoomAddView.Presenter() {
                        
                        @Override
                        public boolean onOK(String room_name, String room_desc, int area_id) {
                            if (room_name == null || room_name.isEmpty()) {
                                StringBuilder sb = new StringBuilder();
                                sb.append(new ClientMessage("Invalid Room Name: ", "机房名称非法")).append(" = (null).").append("\n");
                                sb.append(new ClientMessage("Please try again.", "请重试."));
                                Window.alert(sb.toString());
                                return false;
                            }
                            if (area_id == -1) {
                                StringBuilder sb = new StringBuilder();
                                sb.append(new ClientMessage("Invalid Area Name.", "区域名称非法.")).append("\n");
                                sb.append(new ClientMessage("Please try again.", "请重试."));
                                Window.alert(sb.toString());
                                return false;
                            }
                            getBackendService().createDeviceRoom(getSession(), room_name, room_desc, area_id, new AsyncCallback<Void>() {

                                @Override
                                public void onFailure(Throwable caught) {
                                    onBackendServiceFailure(caught);
                                    getView().clearSelection();
                                }

                                @Override
                                public void onSuccess(Void result) {
                                    onBackendServiceFinished(new ClientMessage("Successfully create Room.", "机房添加成功."));
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
                                    roomAddView.setAreaNames(area_map);
                                }
                                
                            });
                        }
                        
                    });
                }
                roomAddView.popup();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            onFrontendServiceFailure(e);
        }
    }

    @Override
    public void onModify() {
        Set<SearchResultRow> set = getView().getSelectedSet();
        try {
            if (set != null && !set.isEmpty()) {
                SearchResultRow row = new LinkedList<SearchResultRow>(set).getFirst();
                if (Window.confirm(new ClientMessage("Modify selected Room.", "确认修改所选择的机房.").toString())) {
                    if (roomModifyView == null) {
                        roomModifyView = new DeviceRoomModifyViewImpl();
                        roomModifyView.setPresenter(new DeviceRoomModifyView.Presenter() {
                            
                            @Override
                            public boolean onOK(int room_id, String room_desc) {
                                getBackendService().modifyDeviceRoom(getSession(), room_id, room_desc, new AsyncCallback<Void>() {

                                    @Override
                                    public void onFailure(Throwable caught) {
                                        onBackendServiceFailure(caught);
                                        getView().clearSelection();
                                    }

                                    @Override
                                    public void onSuccess(Void result) {
                                        onBackendServiceFinished(new ClientMessage("Successfully modify selected Room.", "机房修改成功."));
                                        reloadCurrentRange();
                                        getView().clearSelection();
                                    }
                                    
                                });
                                return true;
                            }
                            
                        });
                    }
                    int room_id = Integer.parseInt(row.getField(CellTableColumns.ROOM.ROOM_ID));
                    getBackendService().lookupDeviceRoomByID(getSession(), room_id, new AsyncCallback<RoomInfo>() {

                        @Override
                        public void onFailure(Throwable caught) {
                            onBackendServiceFailure(caught);
                            getView().clearSelection();
                        }

                        @Override
                        public void onSuccess(RoomInfo info) {
                            roomModifyView.popup(info.room_id, info.room_name, info.room_desc);
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
    public void onDelete() {
        Set<SearchResultRow> set = getView().getSelectedSet();
        try {
            if (set != null && !set.isEmpty()) {
                List<Integer> room_ids = new ArrayList<Integer>();
                for (SearchResultRow row : set) {
                    int room_id = Integer.parseInt(row.getField(CellTableColumns.ROOM.ROOM_ID));
                    room_ids.add(room_id);
                }
                if (!room_ids.isEmpty()) {
                    if (Window.confirm(new ClientMessage("Delete selected Room(s).", "确认删除所选择的机房.").toString())) {
                        getBackendService().deleteDeviceRoom(getSession(), room_ids, new AsyncCallback<Void>() {
        
                            @Override
                            public void onFailure(Throwable caught) {
                                onBackendServiceFailure(caught);
                                getView().clearSelection();
                            }
        
                            @Override
                            public void onSuccess(Void result) {
                                onBackendServiceFinished(new ClientMessage("Successfully delete selected Room(s).", "机房删除成功."));
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
        range = new SearchRange(0, getView().getPageSize(), -1, true);
        reloadCurrentRange();
    }
    
}
