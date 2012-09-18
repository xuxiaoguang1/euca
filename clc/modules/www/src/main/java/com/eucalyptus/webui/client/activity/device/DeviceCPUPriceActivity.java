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
import com.eucalyptus.webui.client.view.DeviceCPUPriceAddView;
import com.eucalyptus.webui.client.view.DeviceCPUPriceAddViewImpl;
import com.eucalyptus.webui.client.view.DeviceCPUPriceModifyView;
import com.eucalyptus.webui.client.view.DeviceCPUPriceModifyViewImpl;
import com.eucalyptus.webui.client.view.DeviceCPUPriceView;
import com.eucalyptus.webui.client.view.HasValueWidget;
import com.eucalyptus.webui.client.view.LogView;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class DeviceCPUPriceActivity extends AbstractSearchActivity implements DeviceCPUPriceView.Presenter {
	
	private static final ClientMessage title = new ClientMessage("CPUPrice", "CPU定价");
	
	private Date creationtimeBegin;
	private Date creationtimeEnd;
	private Date modifiedtimeBegin;
	private Date modifiedtimeEnd;
	
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
	
	private final static int CPU_PRICE_ID = 0;
	private final static int CPU_NAME = 3;
	private final static int CPU_PRICE_DESC = 4;
	private final static int CPU_PRICE = 5;
	
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
		getBackendService().lookupDeviceCPUPriceByDate(getSession(), range,
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
				showStatus(new ClientMessage("", "查询CPU定价成功"));
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
			if (Window.confirm(new ClientMessage("", "确认创建CPU定价").toString())) {
				if (cpuPriceAddView == null) {
					cpuPriceAddView = new DeviceCPUPriceAddViewImpl();
					cpuPriceAddView.setPresenter(new DeviceCPUPriceAddView.Presenter() {
						
						@Override
						public boolean onOK(String cpu_name, String cpu_price_desc, String price) {
							if (isEmpty(cpu_name)) {
								StringBuilder sb = new StringBuilder();
								sb.append(new ClientMessage("", "CPU名称非法")).append(" = '").append(cpu_name).append("' ");
								sb.append(new ClientMessage("", "请重新选择CPU名称"));
								Window.alert(sb.toString());
								return false;
							}
							double cpu_price = 0;
							try {
							    if (!isEmpty(price)) {
							        cpu_price = Double.parseDouble(price);
							    }
							}
							catch (Exception e) {
								StringBuilder sb = new StringBuilder();
								sb.append(new ClientMessage("", "CPU价格非法")).append(" = '").append(price).append("' ");
								sb.append(new ClientMessage("", "请重新填写价格"));
								Window.alert(sb.toString());
								return false;
							}
							getBackendService().addDeviceCPUPrice(getSession(), cpu_name, cpu_price_desc, cpu_price, new AsyncCallback<Void>() {

								@Override
								public void onFailure(Throwable caught) {
									if (caught instanceof EucalyptusServiceException) {
										onBackendServiceFailure((EucalyptusServiceException)caught);
									}
									getView().clearSelection();
								}

								@Override
								public void onSuccess(Void result) {
									showStatus(new ClientMessage("", "添加CPU定价成功"));
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
								public void onSuccess(List<String> result) {
									showStatus(new ClientMessage("", "获取CPU列表成功"));
									cpuPriceAddView.setCPUNameList(result);
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
	public void onModify() {
		Set<SearchResultRow> set = getView().getSelectedSet();
		try {
			if (set != null && !set.isEmpty()) {
				SearchResultRow row = new LinkedList<SearchResultRow>(set).getFirst();
				if (Window.confirm(new ClientMessage("", "确认修改所选择的CPU定价").toString())) {
					int cpu_price_id = Integer.parseInt(row.getField(CPU_PRICE_ID));
					String cpu_name = row.getField(CPU_NAME);
					String cpu_price_desc = row.getField(CPU_PRICE_DESC);
					double cpu_price = Double.parseDouble(row.getField(CPU_PRICE));
					if (cpuPriceModifyView == null) {
						cpuPriceModifyView = new DeviceCPUPriceModifyViewImpl();
						cpuPriceModifyView.setPresenter(new DeviceCPUPriceModifyView.Presenter() {
							
							@Override
							public boolean onOK(int cpu_name, String cpu_price_desc, String price) {
								double cpu_price = 0;
								try{
								    if (!isEmpty(price)) {
								        cpu_price = Double.parseDouble(price);
								    }
								}
								catch (Exception e) {
									StringBuilder sb = new StringBuilder();
									sb.append(new ClientMessage("", "CPU价格非法")).append(" = '").append(price).append("' ");
									sb.append(new ClientMessage("", "请重新填写价格"));
									Window.alert(sb.toString());
									return false;
								}
								getBackendService().modifyDeviceCPUPrice(getSession(), cpu_name, cpu_price_desc, cpu_price, new AsyncCallback<Void>() {

									@Override
									public void onFailure(Throwable caught) {
										if (caught instanceof EucalyptusServiceException) {
											onBackendServiceFailure((EucalyptusServiceException)caught);
										}
										getView().clearSelection();
									}

									@Override
									public void onSuccess(Void result) {
										showStatus(new ClientMessage("", "修改CPU定价成功"));
										reloadCurrentRange();
										getView().clearSelection();
									}
									
								});
								return true;
							}
							
						});
					}
					cpuPriceModifyView.popup(cpu_price_id, cpu_name, cpu_price_desc, cpu_price);
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
				List<Integer> cpu_price_id_list = new ArrayList<Integer>();
				for (SearchResultRow row : set) {
					int cpu_price_id = Integer.parseInt(row.getField(CPU_PRICE_ID));
					cpu_price_id_list.add(cpu_price_id);
				}
				if (!cpu_price_id_list.isEmpty()) {
					if (Window.confirm(new ClientMessage("", "确认删除所选择的CPU定价").toString())) {
						getBackendService().deleteDeviceCPUPrice(getSession(), cpu_price_id_list, new AsyncCallback<Void>() {
		
							@Override
							public void onFailure(Throwable caught) {
								if (caught instanceof EucalyptusServiceException) {
									onBackendServiceFailure((EucalyptusServiceException)caught);
								}
								getView().clearSelection();
							}
		
							@Override
							public void onSuccess(Void result) {
								showStatus(new ClientMessage("", "删除CPU定价成功"));
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
    	range = new SearchRange(0, DeviceCPUPriceView.DEFAULT_PAGESIZE, -1, true);
    	reloadCurrentRange();
    }
	
}
