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
import com.eucalyptus.webui.client.view.DeviceTemplateAddView;
import com.eucalyptus.webui.client.view.DeviceTemplateAddViewImpl;
import com.eucalyptus.webui.client.view.DeviceTemplateModifyView;
import com.eucalyptus.webui.client.view.DeviceTemplateModifyViewImpl;
import com.eucalyptus.webui.client.view.DeviceTemplateView;
import com.eucalyptus.webui.shared.message.ClientMessage;
import com.eucalyptus.webui.shared.resource.device.CellTableColumns;
import com.eucalyptus.webui.shared.resource.device.TemplateInfo;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class DeviceTemplateActivity extends DeviceActivity implements DeviceTemplateView.Presenter {
	
	private static final ClientMessage title = new ClientMessage("Template", "模板");
	
	private Date dateBegin;
	private Date dateEnd;
	
	private DeviceTemplateAddView templateAddView;
	private DeviceTemplateModifyView templateModifyView;

	public DeviceTemplateActivity(SearchPlace place, ClientFactory clientFactory) {
		super(place, clientFactory);
	}

	private DeviceTemplateView getView() {
		DeviceTemplateView view = (DeviceTemplateView)this.view;
		if (view == null) {
			view = clientFactory.getDeviceTemplateView();
			view.setPresenter(this);
			container.setWidget(view);
			view.clear();
			this.view = view;
		}
		return view;
	}
	
	@Override
	protected void doSearch(String query, SearchRange range) {
		getBackendService().lookupDeviceTemplateByDate(getSession(), range, dateBegin, dateEnd, new AsyncCallback<SearchResult>() {

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
	public void onAddTemplate() {
		try {
			if (Window.confirm(new ClientMessage("Create a new Template.", "确认创建新模板.").toString())) {
				if (templateAddView == null) {
					templateAddView = new DeviceTemplateAddViewImpl();
					templateAddView.setPresenter(new DeviceTemplateAddView.Presenter() {
						
						@Override
						public boolean onOK(String template_name, String template_desc, int template_ncpus, long template_mem, long template_disk, int template_bw) {
							if (template_name == null || template_name.isEmpty()) {
                                StringBuilder sb = new StringBuilder();
                                sb.append(new ClientMessage("Invalid Template Name: ", "模板名称非法")).append(" = (null).").append("\n");
                                sb.append(new ClientMessage("Please try again.", "请重试."));
                                Window.alert(sb.toString());
                                return false;
                            }
							if (template_ncpus <= 0) {
								StringBuilder sb = new StringBuilder();
                                sb.append(new ClientMessage("Invalid CPU Total: ", "CPU数量非法")).append(" = ").append(template_ncpus).append(".").append("\n");
                                sb.append(new ClientMessage("Please try again.", "请重试."));
                                Window.alert(sb.toString());
                                return false;
							}
							if (template_mem <= 0) {
								StringBuilder sb = new StringBuilder();
								sb.append(new ClientMessage("Invalid Memory Size: ", "内存数量非法")).append(" = ").append(template_mem).append(".").append("\n");
                                sb.append(new ClientMessage("Please try again.", "请重试."));
								Window.alert(sb.toString());
								return false;
							}
							if (template_disk <= 0) {
								StringBuilder sb = new StringBuilder();
								sb.append(new ClientMessage("Invalid Disk Size: ", "硬盘数量非法")).append(" = ").append(template_disk).append(".").append("\n");
                                sb.append(new ClientMessage("Please try again.", "请重试."));
								Window.alert(sb.toString());
								return false;
							}
							if (template_bw < 0) {
								StringBuilder sb = new StringBuilder();
								sb.append(new ClientMessage("Invalid Bandwidth Value: ", "带宽数值非法")).append(" = ").append(template_bw).append(".").append("\n");
                                sb.append(new ClientMessage("Please try again.", "请重试."));
								Window.alert(sb.toString());
								return false;
							}
							getBackendService().createDeviceTemplateService(getSession(), template_name, template_desc, template_ncpus, template_mem, template_disk, template_bw, new AsyncCallback<Void>() {

								@Override
								public void onFailure(Throwable caught) {
									onBackendServiceFailure(caught);
                                    getView().clearSelection();
								}

								@Override
								public void onSuccess(Void result) {
									onBackendServiceFinished(new ClientMessage("Successfully create Template.", "模板添加成功."));
                                    getView().clearSelection();
                                    reloadCurrentRange();
								}
								
							});
							return true;
						}

					});
				}
				templateAddView.popup();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			onFrontendServiceFailure(e);
		}
	}
	
	@Override
	public void onModifyTemplate() {
		try {
			if (canModifyTemplate()) {
				if (Window.confirm(new ClientMessage("Modify selected Template.", "确认修改所选择的模板.").toString())) {
					if (templateModifyView == null) {
						templateModifyView = new DeviceTemplateModifyViewImpl();
						templateModifyView.setPresenter(new DeviceTemplateModifyView.Presenter() {
							
							@Override
							public boolean onOK(int template_id, String template_desc, int template_ncpus, long template_mem, long template_disk, int template_bw) {
							    if (template_ncpus <= 0) {
	                                StringBuilder sb = new StringBuilder();
	                                sb.append(new ClientMessage("Invalid CPU Total: ", "CPU数量非法")).append(" = ").append(template_ncpus).append(".").append("\n");
	                                sb.append(new ClientMessage("Please try again.", "请重试."));
	                                Window.alert(sb.toString());
	                                return false;
	                            }
	                            if (template_mem <= 0) {
	                                StringBuilder sb = new StringBuilder();
	                                sb.append(new ClientMessage("Invalid Memory Size: ", "内存数量非法")).append(" = ").append(template_mem).append(".").append("\n");
	                                sb.append(new ClientMessage("Please try again.", "请重试."));
	                                Window.alert(sb.toString());
	                                return false;
	                            }
	                            if (template_disk <= 0) {
	                                StringBuilder sb = new StringBuilder();
	                                sb.append(new ClientMessage("Invalid Disk Size: ", "硬盘数量非法")).append(" = ").append(template_disk).append(".").append("\n");
	                                sb.append(new ClientMessage("Please try again.", "请重试."));
	                                Window.alert(sb.toString());
	                                return false;
	                            }
	                            if (template_bw < 0) {
	                                StringBuilder sb = new StringBuilder();
	                                sb.append(new ClientMessage("Invalid Bandwidth Value: ", "带宽数值非法")).append(" = ").append(template_bw).append(".").append("\n");
	                                sb.append(new ClientMessage("Please try again.", "请重试."));
	                                Window.alert(sb.toString());
	                                return false;
	                            }
	                            getBackendService().modifyDeviceTemplateService(getSession(), template_id, template_desc, template_ncpus, template_mem, template_disk, template_bw, new AsyncCallback<Void>() {

                                    @Override
                                    public void onFailure(Throwable caught) {
                                        onBackendServiceFailure(caught);
                                        getView().clearSelection();
                                    }

                                    @Override
                                    public void onSuccess(Void result) {
                                        onBackendServiceFinished(new ClientMessage("Successfully modify selected Template.", "模板修改成功."));
                                        reloadCurrentRange();
                                        getView().clearSelection();
                                    }
                                    
	                            });
								return true;
							}
							
						});
					}
					SearchResultRow row = getView().getSelectedSet().iterator().next();
					final int template_id = Integer.parseInt(row.getField(CellTableColumns.TEMPLATE.TEMPLATE_ID));
					getBackendService().lookupDeviceTemplateInfoByID(getSession(), template_id, new AsyncCallback<TemplateInfo>() {

                        @Override
                        public void onFailure(Throwable caught) {
                            onBackendServiceFailure(caught);
                            getView().clearSelection();
                        }

                        @Override
                        public void onSuccess(TemplateInfo info) {
                            templateModifyView.popup(template_id, info.template_name, info.template_desc, info.template_ncpus, info.template_mem, info.template_disk, info.template_bw);
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
	public void onDeleteTemplate() {
		try {
			if (canDeleteTemplate()) {
				List<Integer> template_ids = new ArrayList<Integer>();
				for (SearchResultRow row : getView().getSelectedSet()) {
					int template_id = Integer.parseInt(row.getField(CellTableColumns.TEMPLATE.TEMPLATE_ID));
					template_ids.add(template_id);
				}
				if (!template_ids.isEmpty()) {
				    if (Window.confirm(new ClientMessage("Delete selected Template(s).", "确认删除所选择的模板.").toString())) {
						getBackendService().deleteDeviceTemplateService(getSession(), template_ids, new AsyncCallback<Void>() {

							@Override
							public void onFailure(Throwable caught) {
							    onBackendServiceFailure(caught);
                                getView().clearSelection();
							}

							@Override
							public void onSuccess(Void result) {
							    onBackendServiceFinished(new ClientMessage("Successfully delete selected Template(s).", "模板删除成功."));
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
	public boolean canDeleteTemplate() {
		Set<SearchResultRow> set = getView().getSelectedSet();
		if (!set.isEmpty()) {
			return true;
		}
		return false;
	}
	
	@Override
	public boolean canModifyTemplate() {
		Set<SearchResultRow> set = getView().getSelectedSet();
		if (set.size() == 1) {
			return true;
		}
		return false;
	}
	
}
