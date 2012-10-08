package com.eucalyptus.webui.client.activity.device;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import com.eucalyptus.webui.client.view.DeviceDateBox;
import com.eucalyptus.webui.client.view.DeviceIPAddView;
import com.eucalyptus.webui.client.view.DeviceIPAddViewImpl;
import com.eucalyptus.webui.client.view.DeviceIPModifyView;
import com.eucalyptus.webui.client.view.DeviceIPModifyViewImpl;
import com.eucalyptus.webui.client.view.DeviceIPServiceAddView;
import com.eucalyptus.webui.client.view.DeviceIPServiceAddViewImpl;
import com.eucalyptus.webui.client.view.DeviceIPServiceModifyView;
import com.eucalyptus.webui.client.view.DeviceIPServiceModifyViewImpl;
import com.eucalyptus.webui.client.view.DeviceIPView;
import com.eucalyptus.webui.client.view.FooterView;
import com.eucalyptus.webui.client.view.HasValueWidget;
import com.eucalyptus.webui.client.view.LogView;
import com.eucalyptus.webui.client.view.FooterView.StatusType;
import com.eucalyptus.webui.client.view.LogView.LogType;
import com.eucalyptus.webui.shared.resource.device.CellTableColumns;
import com.eucalyptus.webui.shared.resource.device.status.IPState;
import com.eucalyptus.webui.shared.resource.device.status.IPType;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class DeviceIPActivity extends AbstractSearchActivity implements DeviceIPView.Presenter {
	
	private static final ClientMessage title = new ClientMessage("IP", "IP地址");
	
	private Date dateBegin;
	private Date dateEnd;
	private IPType queryType = null;
	private IPState queryState = null;
	private Map<Integer, Integer> ipCounts = new HashMap<Integer, Integer>();
	
	private DeviceIPAddView ipAddView;
	private DeviceIPModifyView ipModifyView;
	private DeviceIPServiceAddView ipServiceAddView;
	private DeviceIPServiceModifyView ipServiceModifyView;

	public DeviceIPActivity(SearchPlace place, ClientFactory clientFactory) {
		super(place, clientFactory);
	}
	
	private DeviceIPView getView() {
		DeviceIPView view = (DeviceIPView)this.view;
		if (view == null) {
			view = clientFactory.getDeviceIPView();
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
		getBackendService().lookupDeviceIPByDate(getSession(), range, queryType, queryState, dateBegin, dateEnd, new AsyncCallback<SearchResult>() {

			@Override
			public void onFailure(Throwable caught) {
				if (caught instanceof EucalyptusServiceException) {
					onBackendServiceFailure((EucalyptusServiceException)caught);
				}
				displayData(null);
			}

			@Override
			public void onSuccess(SearchResult result) {
				showStatus(new ClientMessage("", "查询IP地址成功"));
				displayData(result);
			}
			
		});
		getBackendService().lookupDeviceIPCounts(getSession(), queryType, new AsyncCallback<Map<Integer, Integer>>() {

			@Override
			public void onFailure(Throwable caught) {
				if (caught instanceof EucalyptusServiceException) {
					onBackendServiceFailure((EucalyptusServiceException)caught);
				}
			}

			@Override
			public void onSuccess(Map<Integer, Integer> result) {
				showStatus(new ClientMessage("", "查询IP地址数量成功"));
				ipCounts = result;
				getView().updateLabels();
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
	public void onAddIP() {
		try {
			if (Window.confirm(new ClientMessage("", "确认创建新IP地址").toString())) {
				if (ipAddView == null) {
					ipAddView = new DeviceIPAddViewImpl();
					ipAddView.setPresenter(new DeviceIPAddView.Presenter() {
						
						@Override
						public boolean onOK(String ip_addr, String ip_desc, IPType ip_type) {
							if (isEmpty(ip_addr)) {
								StringBuilder sb = new StringBuilder();
								sb.append(new ClientMessage("", "IP地址非法")).append(" = '").append(ip_addr).append("' ");
								sb.append(new ClientMessage("", "请重新选择IP地址"));
								Window.alert(sb.toString());
								return false;
							}
							if (ip_type == null) {
								StringBuilder sb = new StringBuilder();
								sb.append(new ClientMessage("", "IP地址类型非法"));
								sb.append(new ClientMessage("", "请重新选择IP地址类型"));
								Window.alert(sb.toString());
								return false;
							}
							getBackendService().addDeviceIP(getSession(), ip_addr, ip_desc, ip_type, new AsyncCallback<Void>() {
								
								@Override
								public void onFailure(Throwable caught) {
									if (caught instanceof EucalyptusServiceException) {
										onBackendServiceFailure((EucalyptusServiceException)caught);
									}
									getView().clearSelection();
								}

								@Override
								public void onSuccess(Void result) {
									showStatus(new ClientMessage("", "添加IP地址成功"));
									reloadCurrentRange();
									getView().clearSelection();
								}
								
							});
							return true;
						}
						
					});
				}
				ipAddView.popup();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			onFrontendServiceFailure(e);
		}
	}
	
	@Override
	public void onModifyIP() {
		try {
			if (Window.confirm(new ClientMessage("", "确认修改所选择的IP地址").toString())) {
				SearchResultRow row = getView().getSelectedSet().iterator().next();
				if (ipModifyView == null) {
					ipModifyView = new DeviceIPModifyViewImpl();
					ipModifyView.setPresenter(new DeviceIPModifyView.Presenter() {
						
						@Override
						public boolean onOK(int ip_id, String ip_desc, IPType ip_type) {
							if (ip_type == null) {
								StringBuilder sb = new StringBuilder();
								sb.append(new ClientMessage("", "IP地址类型非法"));
								sb.append(new ClientMessage("", "请重新选择IP地址类型"));
								Window.alert(sb.toString());
								return false;
							}
							getBackendService().modifyDeviceIP(getSession(), ip_id, ip_desc, ip_type, new AsyncCallback<Void>() {

								@Override
								public void onFailure(Throwable caught) {
									if (caught instanceof EucalyptusServiceException) {
										onBackendServiceFailure((EucalyptusServiceException)caught);
									}
									getView().clearSelection();
								}

								@Override
								public void onSuccess(Void result) {
									showStatus(new ClientMessage("", "修改IP地址成功"));
									reloadCurrentRange();
									getView().clearSelection();
								}
								
							});
							return true;
						}
						
					});
				}
				ipModifyView.popup(Integer.parseInt(row.getField(CellTableColumns.IP.IP_ID)), row.getField(CellTableColumns.IP.IP_ADDR),
						row.getField(CellTableColumns.IP.IP_DESC), IPType.parse(row.getField(CellTableColumns.IP.IP_TYPE)));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			onFrontendServiceFailure(e);
		}
	}
	
	@Override
	public void onDeleteIP() {
		try {
			if (canDeleteIP()) {
				List<Integer> ip_id_list = new ArrayList<Integer>();
				for (SearchResultRow row : getView().getSelectedSet()) {
					int ip_id = Integer.parseInt(row.getField(CellTableColumns.IP.IP_ID));
					ip_id_list.add(ip_id);
				}
				if (!ip_id_list.isEmpty()) {
					if (Window.confirm(new ClientMessage("", "确认删除所选择的IP地址").toString())) {
						getBackendService().deleteDeviceIP(getSession(), ip_id_list, new AsyncCallback<Void>() {

							@Override
							public void onFailure(Throwable caught) {
								if (caught instanceof EucalyptusServiceException) {
									onBackendServiceFailure((EucalyptusServiceException)caught);
								}
								getView().clearSelection();
							}
							
							@Override
							public void onSuccess(Void result) {
								showStatus(new ClientMessage("", "删除IP地址成功"));
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
	public void onAddIPService() {
		try {
			if (Window.confirm(new ClientMessage("", "确认添加的IP地址服务").toString())) {
				SearchResultRow row = getView().getSelectedSet().iterator().next();
				if (ipServiceAddView == null) {
					ipServiceAddView = new DeviceIPServiceAddViewImpl();
					ipServiceAddView.setPresenter(new DeviceIPServiceAddView.Presenter() {
						
						@Override
						public boolean onOK(int ip_id, String is_desc, String starttime, String endtime, String account_name, String user_name) {
							Date is_starttime = null;
							try {
								if (!isEmpty(starttime)) {
									is_starttime = DeviceDateBox.parse(starttime);
								}
							}
							catch (Exception e) {
							}
							if (is_starttime == null) {
								StringBuilder sb = new StringBuilder();
								sb.append(new ClientMessage("", "非法的服务起始时间")).append(" = '").append(starttime).append("' ");
								sb.append(new ClientMessage("", "请重新选择时间"));
								Window.alert(sb.toString());
								return false;
							}
							Date is_endtime = null;
							try {
								if (!isEmpty(endtime)) {
									is_endtime = DeviceDateBox.parse(endtime);
								}
							}
							catch (Exception e) {
							}
							if (is_endtime == null) {
								StringBuilder sb = new StringBuilder();
								sb.append(new ClientMessage("", "非法的服务结束时间")).append(" = '").append(endtime).append("' ");
								sb.append(new ClientMessage("", "请重新选择时间"));
								Window.alert(sb.toString());
								return false;
							}
							if (is_starttime.getTime() >= is_endtime.getTime()) {
								StringBuilder sb = new StringBuilder();
								sb.append(new ClientMessage("", "非法的服务时间")).append(" = '").append(is_starttime).append("' >= '").append(is_endtime).append("'");
								sb.append(new ClientMessage("", "请重新选择时间"));
								Window.alert(sb.toString());
								return false;
							}
							if (isEmpty(account_name)) {
								StringBuilder sb = new StringBuilder();
								sb.append(new ClientMessage("", "非法的账户名称")).append(" = '").append(account_name).append("' ");
								sb.append(new ClientMessage("", "请重新选择账户"));
								Window.alert(sb.toString());
								return false;
							}
							if (isEmpty(user_name)) {
								StringBuilder sb = new StringBuilder();
								sb.append(new ClientMessage("", "非法的用户名称")).append(" = '").append(user_name).append("' ");
								sb.append(new ClientMessage("", "请重新选择账户"));
								Window.alert(sb.toString());
								return false;
							}
							getBackendService().addDeviceIPService(getSession(), is_desc, is_starttime, is_endtime, ip_id, account_name, user_name, new AsyncCallback<Void>() {

								@Override
								public void onFailure(Throwable caught) {
									if (caught instanceof EucalyptusServiceException) {
										onBackendServiceFailure((EucalyptusServiceException)caught);
									}
									getView().clearSelection();
								}

								@Override
								public void onSuccess(Void result) {
									showStatus(new ClientMessage("", "添加IP地址服务成功"));
									reloadCurrentRange();
									getView().clearSelection();
								}
								
							});
							return true;
						}
						
						@Override
						public void lookupAccountNames() {
							getBackendService().lookupDeviceAccountNames(getSession(), new AsyncCallback<List<String>>() {
								
								@Override
								public void onFailure(Throwable caught) {
									if (caught instanceof EucalyptusServiceException) {
										onBackendServiceFailure((EucalyptusServiceException)caught);
									}
								}
								
								@Override
								public void onSuccess(List<String> account_name_list) {
									showStatus(new ClientMessage("", "获取账户列表成功"));
									ipServiceAddView.setAccountNameList(account_name_list);
								}
	                              
							});
						}
						
						@Override
						public void lookupUserNamesByAccountName(final String account_name) {
							if (isEmpty(account_name)) {
								StringBuilder sb = new StringBuilder();
								sb.append(new ClientMessage("", "账户名称非法")).append(" = '").append(account_name).append("'");
								Window.alert(sb.toString());
							}
							else {
								getBackendService().lookupDeviceUserNamesByAccountName(getSession(), account_name, new AsyncCallback<List<String>>() {
									
									@Override
									public void onFailure(Throwable caught) {
										if (caught instanceof EucalyptusServiceException) {
											onBackendServiceFailure((EucalyptusServiceException)caught);
										}
									}
									
									@Override
									public void onSuccess(List<String> user_name_list) {
										showStatus(new ClientMessage("", "获取用户列表成功"));
										ipServiceAddView.setUserNameList(account_name, user_name_list);
									}
									
								});
							}
						}
						
					});
				}
				ipServiceAddView.popup(Integer.parseInt(row.getField(CellTableColumns.IP.IP_ID)), row.getField(CellTableColumns.IP.IP_ADDR));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			onFrontendServiceFailure(e);
		}
	}
	
	@Override
	public void onModifyIPService() {
		try {
			if (Window.confirm(new ClientMessage("", "确认修改所选择的IP地址服务").toString())) {
				SearchResultRow row = getView().getSelectedSet().iterator().next();
				if (ipServiceModifyView == null) {
					ipServiceModifyView = new DeviceIPServiceModifyViewImpl();
					ipServiceModifyView.setPresenter(new DeviceIPServiceModifyView.Presenter() {
						
						@Override
						public boolean onOK(int is_id, String is_desc, String starttime, String endtime) {
							Date is_starttime = null;
							try {
								if (!isEmpty(starttime)) {
									is_starttime = DeviceDateBox.parse(starttime);
								}
							}
							catch (Exception e) {
							}
							if (is_starttime == null) {
								StringBuilder sb = new StringBuilder();
								sb.append(new ClientMessage("", "非法的服务起始时间")).append(" = '").append(starttime).append("' ");
								sb.append(new ClientMessage("", "请重新选择时间"));
								Window.alert(sb.toString());
								return false;
							}
							Date is_endtime = null;
							try {
								if (!isEmpty(endtime)) {
									is_endtime = DeviceDateBox.parse(endtime);
								}
							}
							catch (Exception e) {
							}
							if (is_endtime == null) {
								StringBuilder sb = new StringBuilder();
								sb.append(new ClientMessage("", "非法的服务结束时间")).append(" = '").append(endtime).append("' ");
								sb.append(new ClientMessage("", "请重新选择时间"));
								Window.alert(sb.toString());
								return false;
							}
							if (is_starttime.getTime() >= is_endtime.getTime()) {
								StringBuilder sb = new StringBuilder();
								sb.append(new ClientMessage("", "非法的服务时间")).append(" = '").append(is_starttime).append("' >= '").append(is_endtime).append("'");
								sb.append(new ClientMessage("", "请重新选择时间"));
								Window.alert(sb.toString());
								return false;
							}
							getBackendService().modifyDeviceIPService(getSession(), is_id, is_desc, is_starttime, is_endtime, new AsyncCallback<Void>() {

								@Override
								public void onFailure(Throwable caught) {
									if (caught instanceof EucalyptusServiceException) {
										onBackendServiceFailure((EucalyptusServiceException)caught);
									}
									getView().clearSelection();
								}
								
								@Override
								public void onSuccess(Void result) {
									showStatus(new ClientMessage("", "变更IP地址服务成功"));
									reloadCurrentRange();
									getView().clearSelection();
								}
								
							});
							return true;
						}
						
					});
				}
				ipServiceModifyView.popup(Integer.parseInt(row.getField(CellTableColumns.IP.IP_SERVICE_ID)), row.getField(CellTableColumns.IP.IP_ADDR), row.getField(CellTableColumns.IP.IP_SERVICE_DESC),
						row.getField(CellTableColumns.IP.IP_SERVICE_STARTTIME), row.getField(CellTableColumns.IP.IP_SERVICE_ENDTIME), row.getField(CellTableColumns.IP.ACCOUNT_NAME), row.getField(CellTableColumns.IP.USER_NAME));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			onFrontendServiceFailure(e);
		}
	}
	
	@Override
	public void onDeleteIPService() {
		try {
			if (canDeleteIPService()) {
				List<Integer> is_id_list = new ArrayList<Integer>();
				for (SearchResultRow row : getView().getSelectedSet()) {
					int is_id = Integer.parseInt(row.getField(CellTableColumns.IP.IP_SERVICE_ID));
					is_id_list.add(is_id);
				}
				if (!is_id_list.isEmpty()) {
					if (Window.confirm(new ClientMessage("", "确认删除所选择的IP地址服务").toString())) {
						getBackendService().deleteDeviceIPService(getSession(), is_id_list, new AsyncCallback<Void>() {
							
							@Override
							public void onFailure(Throwable caught) {
								if (caught instanceof EucalyptusServiceException) {
									onBackendServiceFailure((EucalyptusServiceException)caught);
								}
								getView().clearSelection();
							}
							
							@Override
							public void onSuccess(Void result) {
								showStatus(new ClientMessage("", "删除IP地址服务成功"));
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
	public IPState getQueryState() {
		return queryState;
	}

	@Override
	public void setQueryState(IPState queryState) {
		if (this.queryState != queryState) {
	    	getView().clearSelection();
			this.queryState = queryState;
	    	range = new SearchRange(0, getView().getPageSize(), -1, true);
	    	reloadCurrentRange();
		}
	}
	
	@Override
	public void setQueryType(IPType queryType) {
		if (this.queryType != queryType) {
			getView().clearSelection();
			this.queryType = queryType;
			range = new SearchRange(0, getView().getPageSize(), -1, true);
			reloadCurrentRange();
		}
	}
	
	@Override
	public int getCounts(IPState state) {
		Integer count = ipCounts.get(state == null ? -1 : state.getValue());
		if (count == null) {
			return 0;
		}
		return count;
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
	public boolean canDeleteIP() {
		Set<SearchResultRow> set = getView().getSelectedSet();
		if (!set.isEmpty()) {
			for (SearchResultRow row : set) {
				try {
					IPState ip_state = IPState.parse(row.getField(CellTableColumns.IP.IP_SERVICE_STATE));
					if (ip_state != IPState.RESERVED) {
						return false;
					}
				}
				catch (Exception e) {
					e.printStackTrace();
					return false;
				}
			}
			return true;
		}
		return false;
	}
	
	@Override
	public boolean canModifyIP() {
		Set<SearchResultRow> set = getView().getSelectedSet();
		if (set.size() == 1) {
			return true;
		}
		return false;
	}

	@Override
	public boolean canAddIPService() {
		Set<SearchResultRow> set = getView().getSelectedSet();
		if (set.size() == 1) {
			try {
				SearchResultRow row = set.iterator().next();
				IPState ip_state = IPState.parse(row.getField(CellTableColumns.IP.IP_SERVICE_STATE));
				if (ip_state != IPState.RESERVED) {
					return false;
				}
			}
			catch (Exception e) {
				e.printStackTrace();
				return false;
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean canDeleteIPService() {
		Set<SearchResultRow> set = getView().getSelectedSet();
		if (!set.isEmpty()) {
			for (SearchResultRow row : set) {
				try {
					IPState ip_state = IPState.parse(row.getField(CellTableColumns.IP.IP_SERVICE_STATE));
					if (ip_state != IPState.INUSE && ip_state != IPState.STOP) {
						return false;
					}
				}
				catch (Exception e) {
					e.printStackTrace();
					return false;
				}
			}
			return true;
		}
		return false;
	}
	
	@Override
	public boolean canModifyIPService() {
		Set<SearchResultRow> set = getView().getSelectedSet();
		if (set.size() == 1) {
			try {
				SearchResultRow row = set.iterator().next();
				IPState ip_state = IPState.parse(row.getField(CellTableColumns.IP.IP_SERVICE_STATE));
				if (ip_state != IPState.INUSE && ip_state != IPState.STOP) {
					return false;
				}
			}
			catch (Exception e) {
				e.printStackTrace();
				return false;
			}
			return true;
		}
		return false;
	}
	
}
