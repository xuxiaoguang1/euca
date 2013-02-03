package com.eucalyptus.webui.client.activity.device;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import com.eucalyptus.webui.client.ClientFactory;
import com.eucalyptus.webui.client.place.SearchPlace;
import com.eucalyptus.webui.client.service.SearchRange;
import com.eucalyptus.webui.client.service.SearchResult;
import com.eucalyptus.webui.client.service.SearchResultRow;
import com.eucalyptus.webui.client.view.DeviceCPUPriceAddView;
import com.eucalyptus.webui.client.view.DeviceCPUPriceAddViewImpl;
import com.eucalyptus.webui.client.view.DeviceCPUPriceModifyView;
import com.eucalyptus.webui.client.view.DeviceCPUPriceModifyViewImpl;
import com.eucalyptus.webui.client.view.DeviceCPUPriceView;
import com.eucalyptus.webui.shared.message.ClientMessage;
import com.eucalyptus.webui.shared.resource.device.CPUPriceInfo;
import com.eucalyptus.webui.shared.resource.device.CellTableColumns;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class DeviceCPUPriceActivity extends DeviceActivity implements DeviceCPUPriceView.Presenter {
	
	private static final ClientMessage title = new ClientMessage("CPUPrice", "CPU定价");
	
	private Date dateBegin;
    private Date dateEnd;
	
	private DeviceCPUPriceAddView cpuPriceAddView;
	private DeviceCPUPriceModifyView cpuPriceModifyView;
	
	public DeviceCPUPriceActivity(SearchPlace place, ClientFactory clientFactory) {
		super(place, clientFactory);
	}
	
	private DeviceCPUPriceView getView() {
		DeviceCPUPriceView view = (DeviceCPUPriceView)this.view;
		if (view == null) {
			view = clientFactory.getDeviceCPUPriceView();
			view.setPresenter(this);
			container.setWidget(view);
			view.clear();
			this.view = view;
		}
		return view;
	}
	
	@Override
	protected void doSearch(String query, SearchRange range) {
	    getBackendService().lookupDeviceCPUPriceByDate(getSession(), range, dateBegin, dateEnd, new AsyncCallback<SearchResult>() {

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
	public void onAddCPUPrice() {
		try {
		    if (Window.confirm(new ClientMessage("Create a new CPU Price.", "确认创建新CPU定价.").toString())) {
				if (cpuPriceAddView == null) {
					cpuPriceAddView = new DeviceCPUPriceAddViewImpl();
					cpuPriceAddView.setPresenter(new DeviceCPUPriceAddView.Presenter() {
						
						@Override
						public boolean onOK(String cpu_name, String cp_desc, double cp_price) {
						    if (cpu_name == null || cpu_name.isEmpty()) {
						        StringBuilder sb = new StringBuilder();
                                sb.append(new ClientMessage("Invalid CPU Name: ", "CPU名称非法")).append(" = (null).").append("\n");
                                sb.append(new ClientMessage("Please try again.", "请重试."));
                                Window.alert(sb.toString());
                                return false;
							}
						    if (cp_price < 0) {
                                StringBuilder sb = new StringBuilder();
                                sb.append(new ClientMessage("Invalid CPU Price: ", "CPU价格非法")).append(" = ").append(cp_price).append(".").append("\n");
                                sb.append(new ClientMessage("Please try again.", "请重试."));
                                Window.alert(sb.toString());
                                return false;
                            }
						    getBackendService().createDeviceCPUPrice(getSession(), cpu_name, cp_desc, cp_price, new AsyncCallback<Void>() {

                                @Override
                                public void onFailure(Throwable caught) {
                                    onBackendServiceFailure(caught);
                                    getView().clearSelection();
                                }

                                @Override
                                public void onSuccess(Void result) {
                                    onBackendServiceFinished(new ClientMessage("Successfully create CPU Price.", "CPU价格添加成功."));
                                    getView().clearSelection();
                                    reloadCurrentRange();
                                }
                                
						    });
							return true;
						}

						@Override
						public void lookupCPUNames() {
						    getBackendService().lookupDeviceCPUNamesWithoutPrice(getSession(), new AsyncCallback<List<String>>() {

                                @Override
                                public void onFailure(Throwable caught) {
                                    onBackendServiceFailure(caught);
                                }

                                @Override
                                public void onSuccess(List<String> cpu_name_list) {
                                    cpuPriceAddView.setCPUNameList(cpu_name_list);
                                }
                                
						    });
						}
						
					});
				}
				cpuPriceAddView.popup();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			onFrontendServiceFailure(e);
		}
	}

	@Override
	public void onModifyCPUPrice() {
		try {
		    if (canModifyCPUPrice()) {
		        if (Window.confirm(new ClientMessage("Modify selected CPU Price.", "确认修改所选择的CPU定价.").toString())) {
		            if (cpuPriceModifyView == null) {
						cpuPriceModifyView = new DeviceCPUPriceModifyViewImpl();
						cpuPriceModifyView.setPresenter(new DeviceCPUPriceModifyView.Presenter() {
							
							@Override
							public boolean onOK(int cp_id, String cp_desc, double cp_price) {
							    if (cp_price < 0) {
	                                StringBuilder sb = new StringBuilder();
	                                sb.append(new ClientMessage("Invalid CPU Price: ", "CPU价格非法")).append(" = ").append(cp_price).append(".").append("\n");
	                                sb.append(new ClientMessage("Please try again.", "请重试."));
	                                Window.alert(sb.toString());
	                                return false;
	                            }
							    getBackendService().modifyDeviceCPUPrice(getSession(), cp_id, cp_desc, cp_price, new AsyncCallback<Void>() {

                                    @Override
                                    public void onFailure(Throwable caught) {
                                        onBackendServiceFailure(caught);
                                        getView().clearSelection();
                                    }

                                    @Override
                                    public void onSuccess(Void result) {
                                        onBackendServiceFinished(new ClientMessage("Successfully modify selected CPU Price.", "CPU定价修改成功."));
                                        reloadCurrentRange();
                                        getView().clearSelection();
                                    }
                                    
							    });
								return true;
							}
							
						});
					}
		            SearchResultRow row = getView().getSelectedSet().iterator().next();
		            final int cp_id = Integer.parseInt(row.getField(CellTableColumns.CPU_PRICE.CPU_PRICE_ID));
		            getBackendService().lookupDeviceCPUPriceByID(getSession(), cp_id, new AsyncCallback<CPUPriceInfo>() {

                        @Override
                        public void onFailure(Throwable caught) {
                            onBackendServiceFailure(caught);
                            getView().clearSelection();
                        }

                        @Override
                        public void onSuccess(CPUPriceInfo info) {
                            cpuPriceModifyView.popup(cp_id, info.cpu_name, info.cp_desc, info.cp_price);
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
	public void onDeleteCPUPrice() {
	    try {
            if (canDeleteCPUPrice()) {
                List<Integer> cp_ids = new ArrayList<Integer>();
                for (SearchResultRow row : getView().getSelectedSet()) {
                    int cp_id = Integer.parseInt(row.getField(CellTableColumns.CPU_PRICE.CPU_PRICE_ID));
                    cp_ids.add(cp_id);
                }
                if (!cp_ids.isEmpty()) {
                    if (Window.confirm(new ClientMessage("Delete selected CPU Price(s).", "确认删除所选择的CPU定价.").toString())) {
                        getBackendService().deleteDeviceCPUPrice(getSession(), cp_ids, new AsyncCallback<Void>() {

                            @Override
                            public void onFailure(Throwable caught) {
                                onBackendServiceFailure(caught);
                                getView().clearSelection();
                            }

                            @Override
                            public void onSuccess(Void result) {
                                onBackendServiceFinished(new ClientMessage("Successfully delete selected CPU Price(s).", "CPU定价删除成功."));
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
    
    @Override
    public boolean canDeleteCPUPrice() {
        Set<SearchResultRow> set = getView().getSelectedSet();
        if (!set.isEmpty()) {
            return true;
        }
        return false;
    }
    
    @Override
    public boolean canModifyCPUPrice() {
        Set<SearchResultRow> set = getView().getSelectedSet();
        if (set.size() == 1) {
            return true;
        }
        return false;
    }
	
}
