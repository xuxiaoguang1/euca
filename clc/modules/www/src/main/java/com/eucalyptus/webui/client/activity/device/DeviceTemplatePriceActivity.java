package com.eucalyptus.webui.client.activity.device;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
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
import com.eucalyptus.webui.client.view.FooterView;
import com.eucalyptus.webui.client.view.FooterView.StatusType;
import com.eucalyptus.webui.client.view.LogView.LogType;
import com.eucalyptus.webui.client.view.DeviceTemplatePriceAddView;
import com.eucalyptus.webui.client.view.DeviceTemplatePriceAddViewImpl;
import com.eucalyptus.webui.client.view.DeviceTemplatePriceModifyView;
import com.eucalyptus.webui.client.view.DeviceTemplatePriceModifyViewImpl;
import com.eucalyptus.webui.client.view.DeviceTemplatePriceView;
import com.eucalyptus.webui.client.view.HasValueWidget;
import com.eucalyptus.webui.client.view.LogView;
import com.eucalyptus.webui.shared.resource.Template;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class DeviceTemplatePriceActivity extends AbstractSearchActivity implements DeviceTemplatePriceView.Presenter {
	
	private static final ClientMessage title = new ClientMessage("TemplatePrice", "模板定价");
	
	private Date creationtimeBegin;
	private Date creationtimeEnd;
	private Date modifiedtimeBegin;
	private Date modifiedtimeEnd;
	
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
	
	private final static int TEMPLATE_PRICE_ID = 0;
	private final static int TEMPLATE_NAME = 3;
	private final static int TEMPLATE_PRICE_DESC = 5;
	private final static int TEMPLATE_CPU = 6;
	private final static int TEMPLATE_NCPU = 7;
	private final static int TEMPLATE_MEM = 8;
	private final static int TEMPLATE_DISK = 9;
	private final static int TEMPLATE_BW = 10;
	private final static int TEMPLATE_PRICE_CPU = 11;
	private final static int TEMPLATE_PRICE_MEM = 12;
	private final static int TEMPLATE_PRICE_DISK = 13;
	private final static int TEMPLATE_PRICE_BW = 14;

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
		getBackendService().lookupDeviceTemplatePriceByDate(getSession(), range,
				creationtimeBegin, creationtimeEnd, modifiedtimeBegin, modifiedtimeEnd, 
				new AsyncCallback<SearchResult>() {

			@Override
			public void onFailure(Throwable caught) {
				if (caught instanceof EucalyptusServiceException) {
					onBackendServiceFailure((EucalyptusServiceException)caught);
				}
				displayData(null);
			}

			@Override
			public void onSuccess(SearchResult result) {
				showStatus(new ClientMessage("", "查询Template定价成功"));
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
	
	private double formatDouble(double value) {
		return (double)(value * 1000) / 1000;
	}
	
	private static final int DIV_BW = 1024;
	private static final int DIV_MEM = 1024 * 1024;
	private static final int DIV_DISK = 1000 * 1000;
	
	@Override
	public void onAdd() {
		try {
			if (Window.confirm(new ClientMessage("", "确认创建模板定价").toString())) {
				if (templatePriceAddView == null) {
					templatePriceAddView = new DeviceTemplatePriceAddViewImpl();
					templatePriceAddView.setPresenter(new DeviceTemplatePriceAddView.Presenter() {
						
						@Override
						public boolean onOK(int template_id, String template_price_desc, String cpu_price, String mem_price, String disk_price, String bw_price) {
							try {
                            	if (template_price_desc == null) {
                                    template_price_desc = "";
                                }
                                double template_price_cpu = convertPriceValue(cpu_price, new ClientMessage("", "CPU价格非法"));
                                double template_price_mem = convertPriceValue(mem_price, new ClientMessage("", "CPU价格非法"));
                                double template_price_disk = convertPriceValue(disk_price, new ClientMessage("", "磁盘价格非法"));
                                double template_price_bw = convertPriceValue(bw_price, new ClientMessage("", "带宽价格非法"));
                                getBackendService().createDeviceTemplatePriceByID(getSession(), template_id, template_price_desc, template_price_cpu, template_price_mem,
                                		template_price_disk, template_price_bw, new AsyncCallback<Void>() {

                                            @Override
                                            public void onFailure(Throwable caught) {
                                                if (caught instanceof EucalyptusServiceException) {
                                                    onBackendServiceFailure((EucalyptusServiceException)caught);
                                                }
                                                getView().clearSelection();
                                            }

                                            @Override
                                            public void onSuccess(Void result) {
                                                showStatus(new ClientMessage("", "添加模板定价成功"));
                                                reloadCurrentRange();
                                                getView().clearSelection();
                                            }
                                            
                                });
                            }
                            catch (Exception e) {
                                return false;
                            }
                            return true;
						}
						
						@Override
						public void lookupTemplateList() {
							getBackendService().lookupDeviceTemplateUnpriced(getSession(), new AsyncCallback<List<String>>() {

								@Override
								public void onFailure(Throwable caught) {
									if (caught instanceof EucalyptusServiceException) {
										onBackendServiceFailure((EucalyptusServiceException)caught);
									}
								}

								@Override
								public void onSuccess(List<String> list) {
									templatePriceAddView.setTemplateList(list);
								}
								
							});
						}
						
						@Override
						public void lookupTemplateDetailByName(String template_name) {
							throw new RuntimeException(" not finish yet!!");
							
//							getBackendService().lookupDeviceTemplateByName(getSession(), template_name, new AsyncCallback<Template>() {
//
//								@Override
//								public void onFailure(Throwable caught) {
//									if (caught instanceof EucalyptusServiceException) {
//										onBackendServiceFailure((EucalyptusServiceException)caught);
//									}
//								}
//
//								@Override
//								public void onSuccess(Template template) {
//									try {
//										int template_id = Integer.parseInt(template.getID());
//										String cpu_name = template.getCPU();
//										if (cpu_name == null) {
//											cpu_name = "";
//										}
//										int ncpus = Integer.parseInt(template.getNCPUs());
//										double mem_size = formatDouble((double)Long.parseLong(template.getMem()) / DIV_MEM);
//										double disk_size = formatDouble((double)Long.parseLong(template.getDisk()) / DIV_DISK);
//										double bw_size = formatDouble((double)Long.parseLong(template.getBw()) / DIV_BW);
//										templatePriceAddView.setTemplateDetails(template_id, template.getName(), cpu_name, ncpus, mem_size, disk_size, bw_size);
//									}
//									catch (Exception e) {
//									}
//								}
//								
//							});
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
	
    private double convertPriceValue(String price, ClientMessage error) throws Exception {
        try {
            if (!isEmpty(price)) {
                return Double.parseDouble(price);
            }
        }
        catch (Exception e) {
            StringBuilder sb = new StringBuilder();
            sb.append(error).append(" = '").append(price).append("' ");
            sb.append(new ClientMessage("", "请重新填写价格"));
            Window.alert(sb.toString());
            throw e;
        }
        return 0;
    }

	@Override
	public void onModify() {
	    Set<SearchResultRow> set = getView().getSelectedSet();
		try {
			if (set != null && !set.isEmpty()) {
				SearchResultRow row = new LinkedList<SearchResultRow>(set).getFirst();
				if (Window.confirm(new ClientMessage("", "确认修改所选择的模板定价").toString())) {
					int template_price_id = Integer.parseInt(row.getField(TEMPLATE_PRICE_ID));
					String template_name = row.getField(TEMPLATE_NAME);
					String template_price_desc = row.getField(TEMPLATE_PRICE_DESC);
					String cpu_name = row.getField(TEMPLATE_CPU);
					double cpu_price = Double.parseDouble(row.getField(TEMPLATE_PRICE_CPU));
					int ncpus = Integer.parseInt(row.getField(TEMPLATE_NCPU));
					double bw_size = Double.parseDouble(row.getField(TEMPLATE_BW));
					double mem_size = Double.parseDouble(row.getField(TEMPLATE_MEM));
					double disk_size = Double.parseDouble(row.getField(TEMPLATE_DISK));
					double bw_price = Double.parseDouble(row.getField(TEMPLATE_PRICE_BW));
					double mem_price = Double.parseDouble(row.getField(TEMPLATE_PRICE_MEM));
					double disk_price = Double.parseDouble(row.getField(TEMPLATE_PRICE_DISK));
					if (templatePriceModifyView == null) {
					    templatePriceModifyView = new DeviceTemplatePriceModifyViewImpl();
					    templatePriceModifyView.setPresenter(new DeviceTemplatePriceModifyView.Presenter() {
                            
                            @Override
                            public boolean onOK(int template_price_id, String template_price_desc, String cpu_price, String mem_price,
                                    String disk_price, String bw_price) {
                                try {
                                	if (template_price_desc == null) {
                                        template_price_desc = "";
                                    }
                                    double template_price_cpu = convertPriceValue(cpu_price, new ClientMessage("", "CPU价格非法"));
                                    double template_price_mem = convertPriceValue(mem_price, new ClientMessage("", "CPU价格非法"));
                                    double template_price_disk = convertPriceValue(disk_price, new ClientMessage("", "磁盘价格非法"));
                                    double template_price_bw = convertPriceValue(bw_price, new ClientMessage("", "带宽价格非法"));
                                    getBackendService().modifyDeviceTemplatePrice(getSession(), template_price_id, template_price_desc, template_price_cpu,
                                            template_price_mem, template_price_disk, template_price_bw, new AsyncCallback<Void>() {

                                                @Override
                                                public void onFailure(Throwable caught) {
                                                    if (caught instanceof EucalyptusServiceException) {
                                                        onBackendServiceFailure((EucalyptusServiceException)caught);
                                                    }
                                                    getView().clearSelection();
                                                }

                                                @Override
                                                public void onSuccess(Void result) {
                                                    showStatus(new ClientMessage("", "修改模板定价成功"));
                                                    reloadCurrentRange();
                                                    getView().clearSelection();
                                                }
                                                
                                    });
                                }
                                catch (Exception e) {
                                    return false;
                                }
                                return true;
                            }
                        });
					}
					templatePriceModifyView.popup(template_price_id, template_name, template_price_desc, cpu_name, ncpus, cpu_price, mem_size, mem_price, disk_size, disk_price, bw_size, bw_price);
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
				List<Integer> template_price_id_list = new ArrayList<Integer>();
				for (SearchResultRow row : set) {
					int template_price_id = Integer.parseInt(row.getField(TEMPLATE_PRICE_ID));
					template_price_id_list.add(template_price_id);
				}
				if (!template_price_id_list.isEmpty()) {
					if (Window.confirm(new ClientMessage("", "确认删除所选择的模板定价").toString())) {
						getBackendService().deleteDeviceTemplatePrice(getSession(), template_price_id_list, new AsyncCallback<Void>() {
		
							@Override
							public void onFailure(Throwable caught) {
								if (caught instanceof EucalyptusServiceException) {
									onBackendServiceFailure((EucalyptusServiceException)caught);
								}
								getView().clearSelection();
							}
		
							@Override
							public void onSuccess(Void result) {
								showStatus(new ClientMessage("", "删除模板定价成功"));
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
    public void updateSearchResult(Date creationtimeBegin, Date creationtimeEnd, Date modifiedtimeBegin, Date modifiedtimeEnd) {
    	getView().clearSelection();
    	this.creationtimeBegin = creationtimeBegin;
    	this.creationtimeEnd = creationtimeEnd;
    	this.modifiedtimeBegin = modifiedtimeBegin;
    	this.modifiedtimeEnd = modifiedtimeEnd;
    	range = new SearchRange(0, DeviceTemplatePriceView.DEFAULT_PAGESIZE, -1, true);
    	reloadCurrentRange();
    }
	
}
