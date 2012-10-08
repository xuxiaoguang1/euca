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
						public boolean onOK(String template_name, String template_desc, String template_cpu, int template_ncpus, String mem, String disk, String bw, String template_image) {
							if (isEmpty(template_name)) {
								StringBuilder sb = new StringBuilder();
								sb.append(new ClientMessage("", "非法的模板名称")).append(" = '").append(template_name).append("' ");
								sb.append(new ClientMessage("", "请重新选择模板名称"));
								Window.alert(sb.toString());
								return false;
							}
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
	

//public class DeviceTemplateActivity extends AbstractSearchActivity implements DeviceTemplateView.Presenter {
//
//	private static final int LAN_SELECT = 1;
//
//	public static final String TITLE[] = {"Template", "模板"};
//
//	private DeviceTemplateModifyView templateModifyView;
//	private DeviceTemplateAddView templateAddView;
//
//	public DeviceTemplateActivity(SearchPlace place, ClientFactory clientFactory) {
//		super(place, clientFactory);
//		templateModifyView = new DeviceTemplateModifyViewImpl();
//		templateModifyView.setPresenter(new DeviceTemplateModifyView.Presenter() {
//
//			@Override
//			public boolean onOK(SearchResultRow row, String cpu, int ncpus, String mem, String disk, String bw, String image) {
//				if (isEmpty(cpu)) {
//					ncpus = 0;
//				}
//				if (ncpus == 0) {
//					cpu = "";
//				}
//				handleModifyService(row, cpu, ncpus, mem, disk, bw, image);
//				getView().getMirrorTable().clearSelection();
//				return true;
//			}
//
//			public void onCancel() {
//				getView().getMirrorTable().clearSelection();
//			}
//
//			@Override
//			public void lookupCPUNames() {
//				getBackendService().listDeviceTemplateCPUNames(getSession(), new AsyncCallback<List<String>>() {
//
//					@Override
//					public void onFailure(Throwable caught) {
//						log(QUERY_CPUNAMES_FAILURE[LAN_SELECT], caught);
//					}
//
//					@Override
//					public void onSuccess(List<String> result) {
//						if (result != null) {
//							templateModifyView.setCPUNameList(result);
//						}
//						else {
//							showStatus(QUERY_CPUNAMES_FAILURE[LAN_SELECT]);
//						}
//					}
//					
//				});
//			}
//
//		});
//
//		templateAddView = new DeviceTemplateAddViewImpl();
//		templateAddView.setPresenter(new DeviceTemplateAddView.Presenter() {
//
//			@Override
//			public boolean onOK(String mark, String cpu, int ncpus, String mem, String disk, String bw, String image) {
//				if (isEmpty(mark)) {
//					StringBuilder sb = new StringBuilder();
//					sb.append(ADD_TEMPLATE_FAILURE_INVALID_ARGS[LAN_SELECT]).append("\n");
//					sb.append("<mark='").append(mark).append("'").append(", ");
//					sb.append("cpu='").append(cpu).append("'").append("', ");
//					sb.append("ncpus='").append(ncpus).append("'").append("', ");
//					sb.append("mem='").append(cpu).append("'").append("', ");
//					sb.append("disk='").append(cpu).append("'").append("', ");
//					sb.append("bw='").append(cpu).append("'").append("', ");
//					sb.append("image='").append(image).append("'").append("'>");
//					Window.alert(sb.toString());
//					return false;
//				}
//				if (isEmpty(cpu)) {
//					ncpus = 0;
//				}
//				if (ncpus == 0) {
//					cpu = "";
//				}
//				handleAddTemplate(mark, cpu, ncpus, mem, disk, bw, image);
//				return true;
//			}
//
//			@Override
//			public void lookupCPUNames() {
//				getBackendService().listDeviceTemplateCPUNames(getSession(), new AsyncCallback<List<String>>() {
//
//					@Override
//					public void onFailure(Throwable caught) {
//						log(QUERY_CPUNAMES_FAILURE[LAN_SELECT], caught);
//					}
//
//					@Override
//					public void onSuccess(List<String> result) {
//						if (result != null) {
//							templateAddView.setCPUNameList(result);
//						}
//						else {
//							showStatus(QUERY_CPUNAMES_FAILURE[LAN_SELECT]);
//						}
//					}
//					
//				});
//			}
//
//		});
//	}
//	
//	private boolean isEmpty(String s) {
//		return s == null || s.length() == 0;
//	}
//	
//	private void handleAddTemplate(String mark, String cpu, int ncpus, String mem, String disk, String bw, String image) {
//		getBackendService().addDeviceTemplate(getSession(), 
//				mark, cpu, ncpus, mem, disk, bw, image, new AsyncCallback<Boolean>() {
//
//			        @Override
//			        public void onFailure(Throwable caught) {
//				        ActivityUtil.logoutForInvalidSession(clientFactory, caught);
//				        log(ADD_TEMPLATE_FAILURE[LAN_SELECT], caught);
//			        }
//
//			        @Override
//			        public void onSuccess(Boolean result) {
//				        if (result) {
//					        showStatus(ADD_TEMPLATE_SUCCESS[LAN_SELECT]);
//				        }
//				        else {
//					        showStatus(ADD_TEMPLATE_FAILURE[LAN_SELECT]);
//				        }
//			        	reloadCurrentRange();
//			        }
//
//		        });
//	}
//
//	private EucalyptusServiceAsync getBackendService() {
//		return clientFactory.getBackendService();
//	}
//
//	private FooterView getFooterView() {
//		return clientFactory.getShellView().getFooterView();
//	}
//
//	private LogView getLogView() {
//		return clientFactory.getShellView().getLogView();
//	}
//
//	private Session getSession() {
//		return clientFactory.getLocalSession().getSession();
//	}
//
//	private void log(String msg, Throwable caught) {
//		getFooterView().showStatus(StatusType.ERROR, msg, FooterView.CLEAR_DELAY_SECOND * 5);
//		getLogView().log(LogType.ERROR, msg + ": " + caught.getMessage());
//	}
//
//	private void showStatus(String msg) {
//		getFooterView().showStatus(StatusType.ERROR, msg, FooterView.CLEAR_DELAY_SECOND * 5);
//		getLogView().log(LogType.ERROR, msg);
//	}
//	
//	@Override
//	protected void doSearch(String query, SearchRange range) {
//		getBackendService().lookupDeviceTemplate(getSession(), query, range, starttime, endtime,
//		        new AsyncCallback<SearchResult>() {
//
//			        @Override
//			        public void onFailure(Throwable caught) {
//				        ActivityUtil.logoutForInvalidSession(clientFactory, caught);
//				        log(QUERY_TABLE_FAILURE[LAN_SELECT], caught);
//				        displayData(null);
//			        }
//
//			        @Override
//			        public void onSuccess(SearchResult result) {
//				        displayData(result);
//			        }
//
//		        });
//	}
//	
//	private DeviceTemplateView getView() {
//		DeviceTemplateView view = (DeviceTemplateView)this.view;
//		if (view == null) {
//			view = clientFactory.getDeviceTemplateView();
//			view.setPresenter(this);
//			container.setWidget(view);
//			view.clear();
//			this.view = view;
//		}
//		return view;
//	}
//
//	@Override
//	protected void showView(SearchResult result) {
//		getView().showSearchResult(result);
//	}
//
//	@Override
//	public void onSelectionChange(Set<SearchResultRow> selections) {
//		/* do nothing */
//	}
//
//	@Override
//	public void saveValue(ArrayList<String> keys, ArrayList<HasValueWidget> values) {
//		/* do nothing */
//	}
//
//	@Override
//	protected String getTitle() {
//		return TITLE[LAN_SELECT];
//	}
//
//	private static final String[] QUERY_TABLE_FAILURE = {"", "获取列表失败"};
//	private static final String[] UPDATE_TEMPLATE_FAILURE = {"", "更新模板失败"};
//	private static final String[] UPDATE_TEMPLATE_SUCCESS = {"", "更新模板成功"};
//	private static final String[] ADD_TEMPLATE_SUCCESS = {"", "添加模板成功"};
//	private static final String[] ADD_TEMPLATE_FAILURE = {"", "添加模板失败"};
//	private static final String[] ADD_TEMPLATE_FAILURE_INVALID_ARGS = {"", "添加模板失败：选择参数无效"};
//	private static final String[] DELETE_TEMPLATE_FAILURE = {"", "删除模板失败"};
//	private static final String[] DELETE_TEMPLATE_SUCCESS = {"", "删除模板成功"};
//	private static final String[] DELETE_TEMPLATE_CONFIRM = {"", "确认删除所选择的 模板？"};
//	private static final String[] DELETE_ALL_TEMPLATE_CONFIRM = {"", "确认删除所选择的 全部模板？"};
//	private static final String[] ACTION_SELECTED_FAILURE = {"", "请选择操作对象"};
//
//	private void prepareModifyService(SearchResultRow row) {
//		templateModifyView.popup(row);
//	}
//
//	private void prepareDeleteService(SearchResultRow row) {
//		if (!Window.confirm(DELETE_TEMPLATE_CONFIRM[LAN_SELECT])) {
//			getView().getMirrorTable().clearSelection();
//			return;
//		}
//		List<SearchResultRow> list = new ArrayList<SearchResultRow>();
//		list.add(row);
//		handleDeleteTemplate(list);
//	}
//
//	@Override
//	public void onMirrorSelectRow(SearchResultRow row) {
//		if (row == null) {
//			return;
//		}
//		switch (getView().getMirrorModeType()) {
//		case MODIFY_TEMPLATE:
//			prepareModifyService(row);
//			return;
//		case DELETE_TEMPLATE:
//			prepareDeleteService(row);
//			return;
//		default:
//			return;
//		}
//	}
//	
//	private static final int TABLE_COL_INDEX_TEMPLATE_ID = 0;
//	private static final int TABLE_COL_INDEX_CHECKBOX = 1;
//	private static final int TABLE_COL_INDEX_NO = 2;
//
//	private void handleModifyService(SearchResultRow row, String cpu, int ncpus, String mem, String disk, String bw, String image) {
//		assert (row != null && getView().getMirrorModeType() == MirrorModeType.MODIFY_TEMPLATE);
//		getBackendService().modifyDeviceTempate(getSession(), row, cpu, ncpus, mem, disk, bw, image, 
//		        new AsyncCallback<SearchResultRow>() {
//
//			        @Override
//			        public void onFailure(Throwable caught) {
//				        ActivityUtil.logoutForInvalidSession(clientFactory, caught);
//				        log(UPDATE_TEMPLATE_FAILURE[LAN_SELECT], caught);
//			        }
//
//			        @Override
//			        public void onSuccess(SearchResultRow result) {
//				        if (result != null) {
//					        showStatus(UPDATE_TEMPLATE_SUCCESS[LAN_SELECT]);
//					        if (getView().isMirrorMode()) {
//						        final int col = TABLE_COL_INDEX_TEMPLATE_ID;
//						        result.setField(TABLE_COL_INDEX_CHECKBOX, "+");
//						        final SearchResultRowMatcher matcher = new SearchResultRowMatcher() {
//
//							        @Override
//							        public boolean match(SearchResultRow row0, SearchResultRow row1) {
//								        return row0.getField(col).equals(row1.getField(col));
//							        }
//
//						        };
//						        getView().getMirrorTable().updateRow(result, matcher);
//					        }
//				        }
//				        else {
//					        showStatus(UPDATE_TEMPLATE_FAILURE[LAN_SELECT]);
//				        }
//			        	reloadCurrentRange();
//			        }
//
//		        });
//	}
//
//	private void handleDeleteTemplate(List<SearchResultRow> list) {
//		DeviceTemplateView view = getView();
//		assert (list.size() != 0 && view.getMirrorModeType() == MirrorModeType.DELETE_TEMPLATE);
//		getBackendService().deleteDeviceTemplate(getSession(), list, new AsyncCallback<List<SearchResultRow>>() {
//
//			@Override
//			public void onFailure(Throwable caught) {
//				ActivityUtil.logoutForInvalidSession(clientFactory, caught);
//				log(DELETE_TEMPLATE_FAILURE[LAN_SELECT], caught);
//			}
//
//			@Override
//			public void onSuccess(List<SearchResultRow> result) {
//				if (result != null) {
//					showStatus(DELETE_TEMPLATE_SUCCESS[LAN_SELECT]);
//					if (getView().isMirrorMode()) {
//						final int col = TABLE_COL_INDEX_TEMPLATE_ID;
//						final SearchResultRowMatcher matcher = new SearchResultRowMatcher() {
//
//							@Override
//							public boolean match(SearchResultRow row0, SearchResultRow row1) {
//								return row0.getField(col).equals(row1.getField(col));
//							}
//
//						};
//						for (SearchResultRow row : result) {
//							getView().getMirrorTable().deleteRow(row, matcher);
//						}
//			        }
//				}
//				else {
//					showStatus(DELETE_TEMPLATE_FAILURE[LAN_SELECT]);
//				}
//	        	reloadCurrentRange();
//			}
//
//		});
//	}
//
//	private SearchResultRow copyRow(SearchResultRow row) {
//		SearchResultRow tmp = row.copy();
//		tmp.setField(TABLE_COL_INDEX_CHECKBOX, "");
//		return tmp;
//	}
//
//	@Override
//	public void onAddTemplate() {
//		templateAddView.popup();
//	}
//
//	@Override
//	public void onModifyTemplate() {
//		Set<SearchResultRow> selected = getView().getSelectedSet();
//		if (selected == null || selected.isEmpty()) {
//			showStatus(ACTION_SELECTED_FAILURE[LAN_SELECT]);
//			return;
//		}
//		List<SearchResultRow> list = new ArrayList<SearchResultRow>();
//		for (SearchResultRow row : selected) {
//			list.add(copyRow(row));
//		}
//		getView().openMirrorMode(MirrorModeType.MODIFY_TEMPLATE, sortSearchResultRow(list));
//	}
//
//	@Override
//	public void onDeleteTemplate() {
//		Set<SearchResultRow> selected = getView().getSelectedSet();
//		if (selected == null || selected.isEmpty()) {
//			showStatus(ACTION_SELECTED_FAILURE[LAN_SELECT]);
//			return;
//		}
//		List<SearchResultRow> list = new ArrayList<SearchResultRow>();
//		for (SearchResultRow row : selected) {
//			list.add(copyRow(row));
//		}
//		getView().openMirrorMode(MirrorModeType.DELETE_TEMPLATE, sortSearchResultRow(list));
//	}
//
//	@Override
//	public void onClearSelection() {
//		getView().clearSelection();
//	}
//
//	@Override
//	public void onMirrorBack() {
//		getView().closeMirrorMode();
//	}
//
//	@Override
//	public void onMirrorDeleteAll() {
//		List<SearchResultRow> data;
//		switch (getView().getMirrorModeType()) {
//		case DELETE_TEMPLATE:
//			data = getView().getMirrorTable().getData();
//			if (data != null && data.size() != 0) {
//				if (Window.confirm(DELETE_ALL_TEMPLATE_CONFIRM[LAN_SELECT])) {
//					handleDeleteTemplate(data);
//				}
//			}
//			break;
//		default:
//			break;
//		}
//	}
//
//	private List<SearchResultRow> sortSearchResultRow(List<SearchResultRow> list) {
//		Collections.sort(list, new Comparator<SearchResultRow>() {
//
//			@Override
//			public int compare(SearchResultRow arg0, SearchResultRow arg1) {
//				String v0 = arg0.getField(TABLE_COL_INDEX_NO);
//				String v1 = arg1.getField(TABLE_COL_INDEX_NO);
//				if (v0 == null) {
//					return v1 == null ? 0 : 1;
//				}
//				if (v1 == null) {
//					return -1;
//				}
//				try {
//					return Integer.parseInt(v0) - Integer.parseInt(v1);
//				}
//				catch (Exception e) {
//					e.printStackTrace();
//				}
//				return 0;
//			}
//
//		});
//		return list;
//	}
//	
//	private Date starttime = null;
//	private Date endtime = null;
//
//	@Override
//    public void onSearch(Date starttime, Date endtime) {
//		this.starttime = starttime;
//		this.endtime = endtime;
//		reloadCurrentRange();
//    }
//	
//	private static final String[] QUERY_CPUNAMES_FAILURE = {"", "获取CPU列表失败"};
//
//}
