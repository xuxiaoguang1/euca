package com.eucalyptus.webui.client.activity.device;

import java.util.ArrayList;
import java.util.Date;
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
import com.eucalyptus.webui.client.view.DeviceTemplateAddView;
import com.eucalyptus.webui.client.view.DeviceTemplateAddViewImpl;
import com.eucalyptus.webui.client.view.DeviceTemplateModifyView;
import com.eucalyptus.webui.client.view.DeviceTemplateModifyViewImpl;
import com.eucalyptus.webui.client.view.DeviceTemplateView;
import com.eucalyptus.webui.client.view.FooterView;
import com.eucalyptus.webui.client.view.HasValueWidget;
import com.eucalyptus.webui.client.view.LogView;
import com.eucalyptus.webui.client.view.FooterView.StatusType;
import com.eucalyptus.webui.client.view.LogView.LogType;
import com.eucalyptus.webui.shared.message.ClientMessage;
import com.eucalyptus.webui.shared.resource.device.CellTableColumns;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class DeviceTemplateActivity extends AbstractSearchActivity implements DeviceTemplateView.Presenter {
	
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
		getBackendService().lookupDeviceTemplateByDate(getSession(), range, dateBegin, dateEnd, new AsyncCallback<SearchResult>() {

			@Override
			public void onFailure(Throwable caught) {
				if (caught instanceof EucalyptusServiceException) {
					onBackendServiceFailure((EucalyptusServiceException)caught);
				}
				displayData(null);
			}

			@Override
			public void onSuccess(SearchResult result) {
				showStatus(new ClientMessage("", "查询模板成功"));
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
			if (Window.confirm(new ClientMessage("", "确认添加的模板").toString())) {
				if (templateAddView == null) {
					templateAddView = new DeviceTemplateAddViewImpl();
					templateAddView.setPresenter(new DeviceTemplateAddView.Presenter() {
						
						@Override
						public boolean onOK(String template_name, String template_desc, String template_cpu, int template_ncpus, long template_mem, long template_disk, int template_bw, String template_image) {
							if (isEmpty(template_name)) {
								StringBuilder sb = new StringBuilder();
								sb.append(new ClientMessage("", "非法的模板名称")).append(" = '").append(template_name).append("' ");
								sb.append(new ClientMessage("", "请重新选择模板名称"));
								Window.alert(sb.toString());
								return false;
							}
							if (template_mem < 0) {
								StringBuilder sb = new StringBuilder();
								sb.append(new ClientMessage("", "非法的内存大小")).append(" = '").append(template_mem).append("' ");
								sb.append(new ClientMessage("", "请重新选择内存大小"));
								Window.alert(sb.toString());
								return false;
							}
							if (template_disk < 0) {
								StringBuilder sb = new StringBuilder();
								sb.append(new ClientMessage("", "非法的硬盘大小")).append(" = '").append(template_disk).append("' ");
								sb.append(new ClientMessage("", "请重新选择硬盘大小"));
								Window.alert(sb.toString());
								return false;
							}
							if (template_bw < 0) {
								StringBuilder sb = new StringBuilder();
								sb.append(new ClientMessage("", "非法的带宽")).append(" = '").append(template_bw).append("' ");
								sb.append(new ClientMessage("", "请重新选择带宽"));
								Window.alert(sb.toString());
								return false;
							}
							getBackendService().addDeviceTemplateService(getSession(), template_name, template_desc, template_cpu, template_ncpus, template_mem, template_disk, template_bw, template_image, new AsyncCallback<Void>() {

								@Override
								public void onFailure(Throwable caught) {
									if (caught instanceof EucalyptusServiceException) {
										onBackendServiceFailure((EucalyptusServiceException)caught);
									}
									getView().clearSelection();
								}

								@Override
								public void onSuccess(Void result) {
									showStatus(new ClientMessage("", "添加模板成功"));
									reloadCurrentRange();
									getView().clearSelection();
								}
								
							});
							return true;
						}

						@Override
						public void lookupCPUNames() {
							getBackendService().lookupDeviceCPUNamesUnpriced(getSession(), new AsyncCallback<List<String>>() {

								@Override
								public void onFailure(Throwable caught) {
									if (caught instanceof EucalyptusServiceException) {
										onBackendServiceFailure((EucalyptusServiceException)caught);
									}
								}

								@Override
								public void onSuccess(List<String> cpu_name_list) {
									showStatus(new ClientMessage("", "获取CPU列表成功"));
									templateAddView.setCPUNameList(cpu_name_list);
								}
								
							});
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
			if (Window.confirm(new ClientMessage("", "确认修改所选择的模板").toString())) {
				SearchResultRow row = getView().getSelectedSet().iterator().next();
				if (templateModifyView == null) {
					templateModifyView = new DeviceTemplateModifyViewImpl();
					templateModifyView.setPresenter(new DeviceTemplateModifyView.Presenter() {
						
						@Override
						public boolean onOK(int template_id, String template_desc, String template_cpu, int template_ncpus, String mem, String disk, String bw, String template_image) {
							long template_mem = 0;
							try {
								if (!isEmpty(mem)) {
									template_mem = Long.parseLong(mem);
								}
							}
							catch (Exception e) {
								StringBuilder sb = new StringBuilder();
								sb.append(new ClientMessage("", "非法的内存大小")).append(" = '").append(mem).append("' ");
								sb.append(new ClientMessage("", "请重新选择内存大小"));
								Window.alert(sb.toString());
								return false;
							}
							long template_disk = 0;
							try {
								if (!isEmpty(disk)) {
									template_disk = Long.parseLong(disk);
								}
							}
							catch (Exception e) {
								StringBuilder sb = new StringBuilder();
								sb.append(new ClientMessage("", "非法的硬盘大小")).append(" = '").append(disk).append("' ");
								sb.append(new ClientMessage("", "请重新选择硬盘大小"));
								Window.alert(sb.toString());
								return false;
							}
							int template_bw = 0;
							try {
								if (!isEmpty(bw)) {
									template_bw = Integer.parseInt(bw);
								}
							}
							catch (Exception e) {
								StringBuilder sb = new StringBuilder();
								sb.append(new ClientMessage("", "非法的带宽")).append(" = '").append(bw).append("' ");
								sb.append(new ClientMessage("", "请重新选择带宽"));
								Window.alert(sb.toString());
								return false;
							}
							getBackendService().modifyDeviceTemplateService(getSession(), template_id, template_desc, template_cpu, template_ncpus, template_mem, template_disk, template_bw, template_image, new AsyncCallback<Void>() {

								@Override
								public void onFailure(Throwable caught) {
									if (caught instanceof EucalyptusServiceException) {
										onBackendServiceFailure((EucalyptusServiceException)caught);
									}
									getView().clearSelection();
								}

								@Override
								public void onSuccess(Void result) {
									showStatus(new ClientMessage("", "变更模板成功"));
									reloadCurrentRange();
									getView().clearSelection();
								}
								
							});
							return true;
						}
						
					});
				}
				templateModifyView.popup(Integer.parseInt(row.getField(CellTableColumns.TEMPLATE.TEMPLATE_ID)), row.getField(CellTableColumns.TEMPLATE.TEMPLATE_NAME),
						row.getField(CellTableColumns.TEMPLATE.TEMPLATE_DESC), row.getField(CellTableColumns.TEMPLATE.TEMPLATE_CPU), 
						Integer.parseInt(row.getField(CellTableColumns.TEMPLATE.TEMPLATE_NCPUS)), row.getField(CellTableColumns.TEMPLATE.TEMPLATE_MEM),
						row.getField(CellTableColumns.TEMPLATE.TEMPLATE_DISK), row.getField(CellTableColumns.TEMPLATE.TEMPLATE_BW), row.getField(CellTableColumns.TEMPLATE.TEMPLATE_IMAGE));
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
				List<Integer> template_id_list = new ArrayList<Integer>();
				for (SearchResultRow row : getView().getSelectedSet()) {
					int template_id = Integer.parseInt(row.getField(CellTableColumns.TEMPLATE.TEMPLATE_ID));
					template_id_list.add(template_id);
				}
				if (!template_id_list.isEmpty()) {
					if (Window.confirm(new ClientMessage("", "确认删除所选择的模板").toString())) {
						getBackendService().deleteDeviceTemplateService(getSession(), template_id_list, new AsyncCallback<Void>() {

							@Override
							public void onFailure(Throwable caught) {
								if (caught instanceof EucalyptusServiceException) {
									onBackendServiceFailure((EucalyptusServiceException)caught);
								}
								getView().clearSelection();
							}

							@Override
							public void onSuccess(Void result) {
								showStatus(new ClientMessage("", "删除模板成功"));
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
