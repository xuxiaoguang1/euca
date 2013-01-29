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
import com.eucalyptus.webui.client.view.DeviceBWServiceAddView;
import com.eucalyptus.webui.client.view.DeviceBWServiceAddViewImpl;
import com.eucalyptus.webui.client.view.DeviceBWServiceModifyView;
import com.eucalyptus.webui.client.view.DeviceBWServiceModifyViewImpl;
import com.eucalyptus.webui.client.view.DeviceBWView;
import com.eucalyptus.webui.client.view.FooterView;
import com.eucalyptus.webui.client.view.HasValueWidget;
import com.eucalyptus.webui.client.view.LogView;
import com.eucalyptus.webui.client.view.FooterView.StatusType;
import com.eucalyptus.webui.client.view.LogView.LogType;
import com.eucalyptus.webui.shared.message.ClientMessage;
import com.eucalyptus.webui.shared.resource.device.CellTableColumns;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class DeviceBWActivity extends AbstractSearchActivity implements DeviceBWView.Presenter {
	
	private static final ClientMessage title = new ClientMessage("BW", "带宽");
	
	private Date dateBegin;
	private Date dateEnd;
	
	private DeviceBWServiceAddView bwServiceAddView;
	private DeviceBWServiceModifyView bwServiceModifyView;

	public DeviceBWActivity(SearchPlace place, ClientFactory clientFactory) {
		super(place, clientFactory);
		super.pageSize = DevicePageSize.getPageSize();
	}
	
	private DeviceBWView getView() {
		DeviceBWView view = (DeviceBWView)this.view;
		if (view == null) {
			view = clientFactory.getDeviceBWView();
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
		getBackendService().lookupDeviceBWByDate(getSession(), range, dateBegin, dateEnd, new AsyncCallback<SearchResult>() {

			@Override
			public void onFailure(Throwable caught) {
				if (caught instanceof EucalyptusServiceException) {
					onBackendServiceFailure((EucalyptusServiceException)caught);
				}
				displayData(null);
			}

			@Override
			public void onSuccess(SearchResult result) {
				showStatus(new ClientMessage("", "查询带宽成功"));
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
	public void onAddBWService() {
		try {
			if (Window.confirm(new ClientMessage("", "确认添加的带宽服务").toString())) {
				if (bwServiceAddView == null) {
					bwServiceAddView = new DeviceBWServiceAddViewImpl();
					bwServiceAddView.setPresenter(new DeviceBWServiceAddView.Presenter() {
						
						@Override
						public boolean onOK(String ip_addr, String bs_desc, int bs_bw_max, Date bs_starttime, Date bs_endtime) {
							if (isEmpty(ip_addr)) {
								StringBuilder sb = new StringBuilder();
								sb.append(new ClientMessage("", "非法的IP地址")).append(" = '").append(ip_addr).append("' ");
								sb.append(new ClientMessage("", "请重新选择IP地址"));
								Window.alert(sb.toString());
								return false;
							}
							if (bs_bw_max < 0) {
								StringBuilder sb = new StringBuilder();
								sb.append(new ClientMessage("", "非法的带宽")).append(" = '").append(bs_bw_max).append("' ");
								sb.append(new ClientMessage("", "请重新选择带宽"));
								Window.alert(sb.toString());
								return false;
							}
							if (bs_starttime == null || bs_endtime == null || DeviceDate.calcLife(bs_endtime, bs_starttime) <= 0) {
								StringBuilder sb = new StringBuilder();
                                sb.append(new ClientMessage("", "非法的服务时间"));
                                if (bs_starttime != null && bs_endtime != null) {
                                	sb.append(" = '").append(DeviceDate.format(bs_starttime)).append("' >= '").append(DeviceDate.format(bs_endtime)).append("'");
                                }
                                sb.append(new ClientMessage("", "请重新选择时间"));
                                Window.alert(sb.toString());
                                return false;
							}
							getBackendService().addDeviceBWService(getSession(), bs_desc, bs_bw_max, bs_starttime, bs_endtime, ip_addr, new AsyncCallback<Void>() {

								@Override
								public void onFailure(Throwable caught) {
									if (caught instanceof EucalyptusServiceException) {
										onBackendServiceFailure((EucalyptusServiceException)caught);
									}
									getView().clearSelection();
								}

								@Override
								public void onSuccess(Void result) {
									showStatus(new ClientMessage("", "添加带宽服务成功"));
									reloadCurrentRange();
									getView().clearSelection();
								}
								
							});
							return true;
						}
						
						@Override
						public void lookupAccountNames() {
//							getBackendService().lookupDeviceAccountNames(getSession(), new AsyncCallback<List<String>>() {
//								
//								@Override
//								public void onFailure(Throwable caught) {
//									if (caught instanceof EucalyptusServiceException) {
//										onBackendServiceFailure((EucalyptusServiceException)caught);
//									}
//								}
//								
//								@Override
//								public void onSuccess(List<String> account_name_list) {
//									showStatus(new SharedMessage("", "获取账户列表成功"));
//									bwServiceAddView.setAccountNameList(account_name_list);
//								}
//	                              
//							});
						}
						
						@Override
						public void lookupUserNamesByAccountName(final String account_name) {
							if (isEmpty(account_name)) {
								StringBuilder sb = new StringBuilder();
								sb.append(new ClientMessage("", "账户名称非法")).append(" = '").append(account_name).append("'");
								Window.alert(sb.toString());
							}
							else {
//								getBackendService().lookupDeviceUserNamesByAccountName(getSession(), account_name, new AsyncCallback<List<String>>() {
//									
//									@Override
//									public void onFailure(Throwable caught) {
//										if (caught instanceof EucalyptusServiceException) {
//											onBackendServiceFailure((EucalyptusServiceException)caught);
//										}
//									}
//									
//									@Override
//									public void onSuccess(List<String> user_name_list) {
//										showStatus(new SharedMessage("", "获取用户列表成功"));
//										bwServiceAddView.setUserNameList(account_name, user_name_list);
//									}
//									
//								});
							}
						}

						@Override
						public void lookupAddrByUserName(final String account_name, final String user_name) {
							getBackendService().lookupDeviceUnusedIPAddrForBWService(getSession(), account_name, user_name, new AsyncCallback<List<String>>() {

								@Override
								public void onFailure(Throwable caught) {
									if (caught instanceof EucalyptusServiceException) {
										onBackendServiceFailure((EucalyptusServiceException)caught);
									}
								}

								@Override
								public void onSuccess(List<String> ip_addr_list) {
									showStatus(new ClientMessage("", "获取IP地址列表成功"));
									bwServiceAddView.setIPAddrList(ip_addr_list, account_name, user_name);
								}
								
							});
						}
						
					});
				}
				bwServiceAddView.popup();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			onFrontendServiceFailure(e);
		}
	}
	
	@Override
	public void onModifyBWService() {
		try {
			if (Window.confirm(new ClientMessage("", "确认修改所选择的带宽服务").toString())) {
				SearchResultRow row = getView().getSelectedSet().iterator().next();
				if (bwServiceModifyView == null) {
					bwServiceModifyView = new DeviceBWServiceModifyViewImpl();
					bwServiceModifyView.setPresenter(new DeviceBWServiceModifyView.Presenter() {
						
						@Override
						public boolean onOK(int bs_id, String bs_desc, int bs_bw_max, Date bs_starttime, Date bs_endtime) {
							if (bs_bw_max < 0) {
								StringBuilder sb = new StringBuilder();
								sb.append(new ClientMessage("", "非法的带宽")).append(" = '").append(bs_bw_max).append("' ");
								sb.append(new ClientMessage("", "请重新选择带宽"));
								Window.alert(sb.toString());
								return false;
							}
							if (bs_starttime == null || bs_endtime == null || DeviceDate.calcLife(bs_endtime, bs_starttime) <= 0) {
								StringBuilder sb = new StringBuilder();
                                sb.append(new ClientMessage("", "非法的服务时间"));
                                if (bs_starttime != null && bs_endtime != null) {
                                	sb.append(" = '").append(DeviceDate.format(bs_starttime)).append("' >= '").append(DeviceDate.format(bs_endtime)).append("'");
                                }
                                sb.append(new ClientMessage("", "请重新选择时间"));
                                Window.alert(sb.toString());
                                return false;
							}
							getBackendService().modifyDeviceBWService(getSession(), bs_id, bs_desc, bs_bw_max, bs_starttime, bs_endtime, new AsyncCallback<Void>() {

								@Override
								public void onFailure(Throwable caught) {
									if (caught instanceof EucalyptusServiceException) {
										onBackendServiceFailure((EucalyptusServiceException)caught);
									}
									getView().clearSelection();
								}
								
								@Override
								public void onSuccess(Void result) {
									showStatus(new ClientMessage("", "变更带宽服务成功"));
									reloadCurrentRange();
									getView().clearSelection();
								}
								
							});
							return true;
						}

					});
				}
				int bs_id = Integer.parseInt(row.getField(CellTableColumns.BW.BW_SERVICE_ID));
				String ip_addr = row.getField(CellTableColumns.BW.IP_ADDR);
				String bs_desc = row.getField(CellTableColumns.BW.BW_SERVICE_DESC);
				int bs_bw_max = Integer.parseInt(row.getField(CellTableColumns.BW.BW_SERVICE_BW_MAX));
				Date bs_starttime = DeviceDate.parse(row.getField(CellTableColumns.BW.BW_SERVICE_STARTTIME));
			    Date bs_endtime = DeviceDate.parse(row.getField(CellTableColumns.BW.BW_SERVICE_ENDTIME));
				String account_name = row.getField(CellTableColumns.BW.ACCOUNT_NAME);
			    String user_name = row.getField(CellTableColumns.BW.USER_NAME);
				bwServiceModifyView.popup(bs_id, ip_addr, bs_desc, bs_bw_max, bs_starttime, bs_endtime, account_name, user_name);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			onFrontendServiceFailure(e);
		}
	}
	
	@Override
	public void onDeleteBWService() {
		try {
			if (canDeleteBWService()) {
				List<Integer> bs_id_list = new ArrayList<Integer>();
				for (SearchResultRow row : getView().getSelectedSet()) {
					int bs_id = Integer.parseInt(row.getField(CellTableColumns.BW.BW_SERVICE_ID));
					bs_id_list.add(bs_id);
				}
				if (!bs_id_list.isEmpty()) {
					if (Window.confirm(new ClientMessage("", "确认删除所选择的带宽服务").toString())) {
						getBackendService().deleteDeviceBWService(getSession(), bs_id_list, new AsyncCallback<Void>() {
							
							@Override
							public void onFailure(Throwable caught) {
								if (caught instanceof EucalyptusServiceException) {
									onBackendServiceFailure((EucalyptusServiceException)caught);
								}
								getView().clearSelection();
							}
							
							@Override
							public void onSuccess(Void result) {
								showStatus(new ClientMessage("", "删除带宽服务成功"));
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
    	range = new SearchRange(0, DevicePageSize.getPageSize(), -1, true);
    	reloadCurrentRange();
	}

	@Override
	public boolean canDeleteBWService() {
		Set<SearchResultRow> set = getView().getSelectedSet();
		if (!set.isEmpty()) {
			return true;
		}
		return false;
	}
	
	@Override
	public boolean canModifyBWService() {
		Set<SearchResultRow> set = getView().getSelectedSet();
		if (set.size() == 1) {
			return true;
		}
		return false;
	}
	
}
