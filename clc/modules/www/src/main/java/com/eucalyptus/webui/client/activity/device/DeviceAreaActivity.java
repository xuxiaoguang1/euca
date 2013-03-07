package com.eucalyptus.webui.client.activity.device;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.eucalyptus.webui.client.ClientFactory;
import com.eucalyptus.webui.client.place.SearchPlace;
import com.eucalyptus.webui.client.service.SearchRange;
import com.eucalyptus.webui.client.service.SearchResult;
import com.eucalyptus.webui.client.service.SearchResultRow;
import com.eucalyptus.webui.client.view.DeviceAreaAddView;
import com.eucalyptus.webui.client.view.DeviceAreaAddViewImpl;
import com.eucalyptus.webui.client.view.DeviceAreaModifyView;
import com.eucalyptus.webui.client.view.DeviceAreaModifyViewImpl;
import com.eucalyptus.webui.client.view.DeviceAreaView;
import com.eucalyptus.webui.shared.message.ClientMessage;
import com.eucalyptus.webui.shared.resource.device.AreaInfo;
import com.eucalyptus.webui.shared.resource.device.CellTableColumns;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class DeviceAreaActivity extends DeviceActivity implements DeviceAreaView.Presenter {
    
    private static final ClientMessage title = new ClientMessage("Area", "区域");
    
    private DeviceAreaAddView areaAddView;
    private DeviceAreaModifyView areaModifyView;
    
    public DeviceAreaActivity(SearchPlace place, ClientFactory clientFactory) {
        super(place, clientFactory);
        super.pageSize = DevicePageSize.getPageSize();
    }
    
    private DeviceAreaView getView() {
        DeviceAreaView view = (DeviceAreaView)this.view;
        if (view == null) {
            view = clientFactory.getDeviceAreaView();
            view.setPresenter(this);
            container.setWidget(view);
            view.clear();
            this.view = view;
        }
        return view;
    }
    
    @Override
    protected void doSearch(String query, SearchRange range) {
        getBackendService().lookupDeviceArea(getSession(), range, new AsyncCallback<SearchResult>() {

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
            if (Window.confirm(new ClientMessage("Create a new Area.", "确认创建新区域.").toString())) {
                if (areaAddView == null) {
                    areaAddView = new DeviceAreaAddViewImpl();
                    areaAddView.setPresenter(new DeviceAreaAddView.Presenter() {
                        
                        @Override
                        public boolean onOK(String area_name, String area_desc) {
                            if (area_name == null || area_name.isEmpty()) {
                                StringBuilder sb = new StringBuilder();
                                sb.append(new ClientMessage("Invalid Area Name: ", "区域名称非法")).append(" = (null).").append("\n");
                                sb.append(new ClientMessage("Please try again.", "请重试."));
                                Window.alert(sb.toString());
                                return false;
                            }
                            getBackendService().createDeviceArea(getSession(), area_name, area_desc, new AsyncCallback<Void>() {

                                @Override
                                public void onFailure(Throwable caught) {
                                    onBackendServiceFailure(caught);
                                    getView().clearSelection();
                                }

                                @Override
                                public void onSuccess(Void result) {
                                    onBackendServiceFinished(new ClientMessage("Successfully create Area.", "区域添加成功."));
                                    getView().clearSelection();
                                    reloadCurrentRange();
                                }
                                
                            });
                            return true;
                        }
                        
                    });
                }
                areaAddView.popup();
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
                if (Window.confirm(new ClientMessage("Modify selected Area.", "确认修改所选择的区域.").toString())) {
                    if (areaModifyView == null) {
                        areaModifyView = new DeviceAreaModifyViewImpl();
                        areaModifyView.setPresenter(new DeviceAreaModifyView.Presenter() {
                            
                            @Override
                            public boolean onOK(int area_id, String area_desc) {
                                getBackendService().modifyDeviceArea(getSession(), area_id, area_desc, new AsyncCallback<Void>() {

                                    @Override
                                    public void onFailure(Throwable caught) {
                                        onBackendServiceFailure(caught);
                                        getView().clearSelection();
                                    }

                                    @Override
                                    public void onSuccess(Void result) {
                                        onBackendServiceFinished(new ClientMessage("Successfully modify selected Area.", "区域修改成功."));
                                        reloadCurrentRange();
                                        getView().clearSelection();
                                    }
                                    
                                });
                                return true;
                            }
                            
                        });
                    }
                    int area_id = Integer.parseInt(row.getField(CellTableColumns.AREA.AREA_ID));
                    getBackendService().lookupDeviceAreaByID(getSession(), area_id, new AsyncCallback<AreaInfo>() {

                        @Override
                        public void onFailure(Throwable caught) {
                            onBackendServiceFailure(caught);
                            getView().clearSelection();
                        }

                        @Override
                        public void onSuccess(AreaInfo info) {
                            areaModifyView.popup(info.area_id, info.area_name, info.area_desc);
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
                List<Integer> area_ids = new ArrayList<Integer>();
                for (SearchResultRow row : set) {
                    int area_id = Integer.parseInt(row.getField(CellTableColumns.AREA.AREA_ID));
                    area_ids.add(area_id);
                }
                if (!area_ids.isEmpty()) {
                    if (Window.confirm(new ClientMessage("Delete selected Area(s).", "确认删除所选择的区域.").toString())) {
                        getBackendService().deleteDeviceArea(getSession(), area_ids, new AsyncCallback<Void>() {
        
                            @Override
                            public void onFailure(Throwable caught) {
                                onBackendServiceFailure(caught);
                                getView().clearSelection();
                            }
        
                            @Override
                            public void onSuccess(Void result) {
                                onBackendServiceFinished(new ClientMessage("Successfully delete selected Area(s).", "区域删除成功."));
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
