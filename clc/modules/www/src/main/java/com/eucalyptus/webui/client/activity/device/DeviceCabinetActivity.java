package com.eucalyptus.webui.client.activity.device;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.eucalyptus.webui.client.ClientFactory;
import com.eucalyptus.webui.client.place.SearchPlace;
import com.eucalyptus.webui.client.service.SearchRange;
import com.eucalyptus.webui.client.service.SearchResult;
import com.eucalyptus.webui.client.service.SearchResultRow;
import com.eucalyptus.webui.client.view.DeviceCabinetAddView;
import com.eucalyptus.webui.client.view.DeviceCabinetAddViewImpl;
import com.eucalyptus.webui.client.view.DeviceCabinetModifyView;
import com.eucalyptus.webui.client.view.DeviceCabinetModifyViewImpl;
import com.eucalyptus.webui.client.view.DeviceCabinetView;
import com.eucalyptus.webui.shared.message.ClientMessage;
import com.eucalyptus.webui.shared.resource.device.CabinetInfo;
import com.eucalyptus.webui.shared.resource.device.CellTableColumns;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class DeviceCabinetActivity extends DeviceActivity implements DeviceCabinetView.Presenter {
	
	private static final ClientMessage title = new ClientMessage("Cabinet", "机柜");
	
	private Date dateBegin;
	private Date dateEnd;
	
	private DeviceCabinetAddView cabinetAddView;
	private DeviceCabinetModifyView cabinetModifyView;
	
	public DeviceCabinetActivity(SearchPlace place, ClientFactory clientFactory) {
		super(place, clientFactory);
		super.pageSize = DevicePageSize.getPageSize();
	}
	
	private DeviceCabinetView getView() {
		DeviceCabinetView view = (DeviceCabinetView)this.view;
		if (view == null) {
			view = clientFactory.getDeviceCabinetView();
			view.setPresenter(this);
			container.setWidget(view);
			view.clear();
			this.view = view;
		}
		return view;
	}
	
	@Override
	protected void doSearch(String query, SearchRange range) {
		getBackendService().lookupDeviceCabinetByDate(getSession(), range, dateBegin, dateEnd, new AsyncCallback<SearchResult>() {

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
		    if (Window.confirm(new ClientMessage("Create a new Cabinet.", "确认创建新机柜.").toString())) {
				if (cabinetAddView == null) {
					cabinetAddView = new DeviceCabinetAddViewImpl();
					cabinetAddView.setPresenter(new DeviceCabinetAddView.Presenter() {
						
						@Override
						public boolean onOK(String cabinet_name, String cabinet_desc, int room_id) {
						    if (cabinet_name == null || cabinet_name.isEmpty()) {
                                StringBuilder sb = new StringBuilder();
                                sb.append(new ClientMessage("Invalid Cabinet Name: ", "机柜名称非法")).append(" = (null).").append("\n");
                                sb.append(new ClientMessage("Please try again.", "请重试."));
                                Window.alert(sb.toString());
                                return false;
                            }
                            if (room_id == -1) {
                                StringBuilder sb = new StringBuilder();
                                sb.append(new ClientMessage("Invalid Room Name.", "机房名称非法.")).append("\n");
                                sb.append(new ClientMessage("Please try again.", "请重试."));
                                Window.alert(sb.toString());
                                return false;
                            }
							getBackendService().createDeviceCabinet(getSession(), cabinet_name, cabinet_desc, room_id, new AsyncCallback<Void>() {

								@Override
								public void onFailure(Throwable caught) {
								    onBackendServiceFailure(caught);
                                    getView().clearSelection();
								}

								@Override
								public void onSuccess(Void result) {
								    onBackendServiceFinished(new ClientMessage("Successfully create Cabinet.", "机柜添加成功."));
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
                                    cabinetAddView.setAreaNames(area_map);
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
                                        cabinetAddView.setRoomNames(area_id, room_map);
                                    }
									
								});
							}
						}
						
					});
				}
				cabinetAddView.popup();
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
				if (Window.confirm(new ClientMessage("Modify selected Cabinet.", "确认修改所选择的机柜.").toString())) {
					if (cabinetModifyView == null) {
						cabinetModifyView = new DeviceCabinetModifyViewImpl();
						cabinetModifyView.setPresenter(new DeviceCabinetModifyView.Presenter() {
							
							@Override
							public boolean onOK(int cabinet_id, String cabinet_desc) {
								getBackendService().modifyDeviceCabinet(getSession(), cabinet_id, cabinet_desc, new AsyncCallback<Void>() {

									@Override
									public void onFailure(Throwable caught) {
				                        onBackendServiceFailure(caught);
                                        getView().clearSelection();
									}

									@Override
									public void onSuccess(Void result) {
									    onBackendServiceFinished(new ClientMessage("Successfully modify selected Cabinet.", "机柜修改成功."));
                                        reloadCurrentRange();
                                        getView().clearSelection();
									}
									
								});
								return true;
							}
							
						});
					}
					int cabinet_id = Integer.parseInt(row.getField(CellTableColumns.CABINET.CABINET_ID));
					getBackendService().lookupDeviceCabinetByID(getSession(), cabinet_id, new AsyncCallback<CabinetInfo>() {

                        @Override
                        public void onFailure(Throwable caught) {
                            onBackendServiceFailure(caught);
                            getView().clearSelection();
                        }

                        @Override
                        public void onSuccess(CabinetInfo info) {
                            cabinetModifyView.popup(info.cabinet_id, info.cabinet_name, info.cabinet_desc);
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
				List<Integer> cabinet_ids = new ArrayList<Integer>();
				for (SearchResultRow row : set) {
					int cabinet_id = Integer.parseInt(row.getField(CellTableColumns.CABINET.CABINET_ID));
					cabinet_ids.add(cabinet_id);
				}
				if (!cabinet_ids.isEmpty()) {
				    if (Window.confirm(new ClientMessage("Delete selected Cabinet(s).", "确认删除所选择的机柜.").toString())) {
						getBackendService().deleteDeviceCabinet(getSession(), cabinet_ids, new AsyncCallback<Void>() {
		
							@Override
							public void onFailure(Throwable caught) {
							    onBackendServiceFailure(caught);
                                getView().clearSelection();
							}
		
							@Override
							public void onSuccess(Void result) {
							    onBackendServiceFinished(new ClientMessage("Successfully delete selected Cabinet(s).", "机柜删除成功."));
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
    public void updateSearchResult(Date dateBegin, Date dateEnd) {
    	getView().clearSelection();
    	this.dateBegin = dateBegin;
    	this.dateEnd = dateEnd;
    	range = new SearchRange(0, getView().getPageSize(), -1, true);
    	reloadCurrentRange();
    }
	
}
