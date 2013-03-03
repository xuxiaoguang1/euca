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
import com.eucalyptus.webui.client.view.DeviceTemplatePriceAddView;
import com.eucalyptus.webui.client.view.DeviceTemplatePriceAddViewImpl;
import com.eucalyptus.webui.client.view.DeviceTemplatePriceModifyView;
import com.eucalyptus.webui.client.view.DeviceTemplatePriceModifyViewImpl;
import com.eucalyptus.webui.client.view.DeviceTemplatePriceView;
import com.eucalyptus.webui.shared.message.ClientMessage;
import com.eucalyptus.webui.shared.resource.device.CellTableColumns;
import com.eucalyptus.webui.shared.resource.device.DevicePriceInfo;
import com.eucalyptus.webui.shared.resource.device.TemplateInfo;
import com.eucalyptus.webui.shared.resource.device.TemplatePriceInfo;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class DeviceTemplatePriceActivity extends DeviceActivity implements DeviceTemplatePriceView.Presenter {
	
	private static final ClientMessage title = new ClientMessage("TemplatePrice", "模板定价");
	
	private Date dateBegin;
    private Date dateEnd;
	
	private DeviceTemplatePriceAddView templatePriceAddView;
	private DeviceTemplatePriceModifyView templatePriceModifyView;
	
	public DeviceTemplatePriceActivity(SearchPlace place, ClientFactory clientFactory) {
		super(place, clientFactory);
	}
	
	private DeviceTemplatePriceView getView() {
		DeviceTemplatePriceView view = (DeviceTemplatePriceView)this.view;
		if (view == null) {
			view = clientFactory.getDeviceTemplatePriceView();
			view.setPresenter(this);
			container.setWidget(view);
			view.clear();
			this.view = view;
		}
		return view;
	}
	
	@Override
	protected void doSearch(String query, SearchRange range) {
	    getBackendService().lookupDeviceTemplatePriceByDate(getSession(), range, dateBegin, dateEnd, new AsyncCallback<SearchResult>() {

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
	
	private double default_tp_cpu = 0;
	private double default_tp_mem = 0;
	private double default_tp_disk = 0;
	private double default_tp_bw = 0;
	
	private void reload() {
        getBackendService().lookupDeviceCPUPrice(getSession(), new AsyncCallback<DevicePriceInfo>() {

            @Override
            public void onFailure(Throwable caught) {
                onBackendServiceFailure(caught);
            }

            @Override
            public void onSuccess(DevicePriceInfo info) {
                default_tp_cpu = info.op_price;
            }
            
        });
        getBackendService().lookupDeviceMemoryPrice(getSession(), new AsyncCallback<DevicePriceInfo>() {

            @Override
            public void onFailure(Throwable caught) {
                onBackendServiceFailure(caught);
            }

            @Override
            public void onSuccess(DevicePriceInfo info) {
                default_tp_mem = info.op_price;
            }
            
        });
        getBackendService().lookupDeviceDiskPrice(getSession(), new AsyncCallback<DevicePriceInfo>() {

            @Override
            public void onFailure(Throwable caught) {
                onBackendServiceFailure(caught);
            }

            @Override
            public void onSuccess(DevicePriceInfo info) {
                default_tp_disk = info.op_price;
            }
            
        });
        getBackendService().lookupDeviceBWPrice(getSession(), new AsyncCallback<DevicePriceInfo>() {

            @Override
            public void onFailure(Throwable caught) {
                onBackendServiceFailure(caught);
            }

            @Override
            public void onSuccess(DevicePriceInfo info) {
                default_tp_bw = info.op_price;
            }
            
        });
	}
	
	@Override
	public void onAddTemplatePrice() {
		try {
		    if (Window.confirm(new ClientMessage("Create a new Template Price.", "确认创建新模板定价.").toString())) {
		        reload();
				if (templatePriceAddView == null) {
					templatePriceAddView = new DeviceTemplatePriceAddViewImpl();
					templatePriceAddView.setPresenter(new DeviceTemplatePriceAddView.Presenter() {
						
						@Override
						public boolean onOK(int template_id, String tp_desc, double tp_cpu, double tp_mem, double tp_disk, double tp_bw) {
						    if (tp_cpu < 0) {
                                StringBuilder sb = new StringBuilder();
                                sb.append(new ClientMessage("Invalid CPU Price: ", "CPU价格非法")).append(" = ").append(tp_cpu).append(".").append("\n");
                                sb.append(new ClientMessage("Please try again.", "请重试."));
                                Window.alert(sb.toString());
                                return false;
                            }
						    if (tp_mem < 0) {
						        StringBuilder sb = new StringBuilder();
                                sb.append(new ClientMessage("Invalid Memory Price: ", "内存价格非法")).append(" = ").append(tp_mem).append(".").append("\n");
                                sb.append(new ClientMessage("Please try again.", "请重试."));
                                Window.alert(sb.toString());
                                return false;
						    }
						    if (tp_disk < 0) {
						        StringBuilder sb = new StringBuilder();
                                sb.append(new ClientMessage("Invalid Disk Price: ", "硬盘价格非法")).append(" = ").append(tp_disk).append(".").append("\n");
                                sb.append(new ClientMessage("Please try again.", "请重试."));
                                Window.alert(sb.toString());
                                return false;
						    }
						    if (tp_bw < 0) {
						        StringBuilder sb = new StringBuilder();
                                sb.append(new ClientMessage("Invalid Bandwidth Price: ", "带宽价格非法")).append(" = ").append(tp_bw).append(".").append("\n");
                                sb.append(new ClientMessage("Please try again.", "请重试."));
                                Window.alert(sb.toString());
                                return false;
						    }
						    getBackendService().createDeviceTemplatePriceByID(getSession(), template_id, tp_desc, tp_cpu, tp_mem, tp_disk, tp_bw, new AsyncCallback<Void>() {
						        
						        @Override
                                public void onFailure(Throwable caught) {
                                    onBackendServiceFailure(caught);
                                    getView().clearSelection();
                                }
						        
						        @Override
                                public void onSuccess(Void result) {
                                    onBackendServiceFinished(new ClientMessage("Successfully create Template Price.", "模板定价添加成功."));
                                    getView().clearSelection();
                                    reloadCurrentRange();
                                }
						        
						    });
                            return true;
						}
						
						@Override
						public void lookupTemplates() {
						    getBackendService().lookupDeviceTemplatesWithoutPrice(getSession(), new AsyncCallback<Map<String, Integer>>() {

                                @Override
                                public void onFailure(Throwable caught) {
                                    onBackendServiceFailure(caught);
                                }

                                @Override
                                public void onSuccess(Map<String, Integer> template_map) {
                                    templatePriceAddView.setTemplates(template_map);
                                }
                                
						    });
						}
						
						@Override
						public void lookupTemplate(final int template_id) {
						    getBackendService().lookupDeviceTemplateInfoByID(getSession(), template_id, new AsyncCallback<TemplateInfo>() {

                                @Override
                                public void onFailure(Throwable caught) {
                                    onBackendServiceFailure(caught);
                                }

                                @Override
                                public void onSuccess(TemplateInfo info) {
                                    templatePriceAddView.setTemplate(template_id, info.template_name, info.template_ncpus, default_tp_cpu, info.template_mem, default_tp_mem, info.template_disk, default_tp_disk, info.template_bw, default_tp_bw);
                                }
                                
						    });
						}
						
					});
				}
				templatePriceAddView.popup();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			onFrontendServiceFailure(e);
		}
	}
	
	@Override
	public void onModifyTemplatePrice() {
	    try {
            if (canModifyTemplatePrice()) {
                if (Window.confirm(new ClientMessage("Modify selected Template Price.", "确认修改所选择的模板定价.").toString())) {
					if (templatePriceModifyView == null) {
					    templatePriceModifyView = new DeviceTemplatePriceModifyViewImpl();
					    templatePriceModifyView.setPresenter(new DeviceTemplatePriceModifyView.Presenter() {
                            
                            @Override
                            public boolean onOK(int tp_id, String tp_desc, double tp_cpu, double tp_mem, double tp_disk, double tp_bw) {
                                if (tp_cpu < 0) {
                                    StringBuilder sb = new StringBuilder();
                                    sb.append(new ClientMessage("Invalid CPU Price: ", "CPU价格非法")).append(" = ").append(tp_cpu).append(".").append("\n");
                                    sb.append(new ClientMessage("Please try again.", "请重试."));
                                    Window.alert(sb.toString());
                                    return false;
                                }
                                if (tp_mem < 0) {
                                    StringBuilder sb = new StringBuilder();
                                    sb.append(new ClientMessage("Invalid Memory Price: ", "内存价格非法")).append(" = ").append(tp_mem).append(".").append("\n");
                                    sb.append(new ClientMessage("Please try again.", "请重试."));
                                    Window.alert(sb.toString());
                                    return false;
                                }
                                if (tp_disk < 0) {
                                    StringBuilder sb = new StringBuilder();
                                    sb.append(new ClientMessage("Invalid Disk Price: ", "硬盘价格非法")).append(" = ").append(tp_disk).append(".").append("\n");
                                    sb.append(new ClientMessage("Please try again.", "请重试."));
                                    Window.alert(sb.toString());
                                    return false;
                                }
                                if (tp_bw < 0) {
                                    StringBuilder sb = new StringBuilder();
                                    sb.append(new ClientMessage("Invalid Bandwidth Price: ", "带宽价格非法")).append(" = ").append(tp_bw).append(".").append("\n");
                                    sb.append(new ClientMessage("Please try again.", "请重试."));
                                    Window.alert(sb.toString());
                                    return false;
                                }
                                getBackendService().modifyDeviceTemplatePrice(getSession(), tp_id, tp_desc, tp_cpu, tp_mem, tp_disk, tp_bw, new AsyncCallback<Void>() {

                                    
                                    @Override
                                    public void onFailure(Throwable caught) {
                                        onBackendServiceFailure(caught);
                                        getView().clearSelection();
                                    }
                                    
                                    @Override
                                    public void onSuccess(Void result) {
                                        onBackendServiceFinished(new ClientMessage("Successfully modify Template Price.", "模板定价修改成功."));
                                        getView().clearSelection();
                                        reloadCurrentRange();
                                    }
                                    
                                });
                                return true;
                            }
                        });
					}
					SearchResultRow row = getView().getSelectedSet().iterator().next();
                    final int tp_id = Integer.parseInt(row.getField(CellTableColumns.TEMPLATE_PRICE.TEMPLATE_PRICE_ID));
                    final String template_name = row.getField(CellTableColumns.TEMPLATE_PRICE.TEMPLATE_NAME);
                    final int ncpus = Integer.parseInt(row.getField(CellTableColumns.TEMPLATE_PRICE.TEMPLATE_CPU_NCPUS));
                    final long mem_size = Long.parseLong(row.getField(CellTableColumns.TEMPLATE_PRICE.TEMPLATE_MEM_TOTAL));
                    final long disk_size = Long.parseLong(row.getField(CellTableColumns.TEMPLATE_PRICE.TEMPLATE_DISK_TOTAL));
                    final int bw_size = Integer.parseInt(row.getField(CellTableColumns.TEMPLATE_PRICE.TEMPLATE_BW_TOTAL));
                    System.out.println(row.getField(CellTableColumns.TEMPLATE_PRICE.TEMPLATE_MEM_TOTAL));
                    System.out.println(Long.parseLong(row.getField(CellTableColumns.TEMPLATE_PRICE.TEMPLATE_MEM_TOTAL)));
                    getBackendService().lookupDeviceTemplatePriceByID(getSession(), tp_id, new AsyncCallback<TemplatePriceInfo>() {

                        @Override
                        public void onFailure(Throwable caught) {
                            onBackendServiceFailure(caught);
                            getView().clearSelection();
                        }

                        @Override
                        public void onSuccess(TemplatePriceInfo info) {
                            templatePriceModifyView.popup(tp_id, template_name, info.tp_desc, ncpus, info.tp_cpu, mem_size, info.tp_mem, disk_size, info.tp_disk, bw_size, info.tp_bw);
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
	public void onDeleteTemplatePrice() {
	    try {
            if (canDeleteTemplatePrice()) {
                List<Integer> tp_ids = new ArrayList<Integer>();
                for (SearchResultRow row : getView().getSelectedSet()) {
                    int tp_id = Integer.parseInt(row.getField(CellTableColumns.TEMPLATE_PRICE.TEMPLATE_PRICE_ID));
                    tp_ids.add(tp_id);
                }
                if (!tp_ids.isEmpty()) {
                    if (Window.confirm(new ClientMessage("Delete selected Template Price(s).", "确认删除所选择的模板定价.").toString())) {
                        getBackendService().deleteDeviceTemplatePrice(getSession(), tp_ids, new AsyncCallback<Void>() {

                            @Override
                            public void onFailure(Throwable caught) {
                                onBackendServiceFailure(caught);
                                getView().clearSelection();
                            }

                            @Override
                            public void onSuccess(Void result) {
                                onBackendServiceFinished(new ClientMessage("Successfully delete selected Template Price(s).", "模板定价删除成功."));
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
    public boolean canDeleteTemplatePrice() {
        Set<SearchResultRow> set = getView().getSelectedSet();
        if (!set.isEmpty()) {
            return true;
        }
        return false;
    }
    
    @Override
    public boolean canModifyTemplatePrice() {
        Set<SearchResultRow> set = getView().getSelectedSet();
        if (set.size() == 1) {
            return true;
        }
        return false;
    }

	
}
