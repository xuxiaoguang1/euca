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
import com.eucalyptus.webui.client.view.HasValueWidget;
import com.eucalyptus.webui.client.view.LogView;
import com.eucalyptus.webui.client.view.device.DeviceAreaAddView;
import com.eucalyptus.webui.client.view.device.DeviceAreaAddViewImpl;
import com.eucalyptus.webui.client.view.device.DeviceAreaModifyView;
import com.eucalyptus.webui.client.view.device.DeviceAreaModifyViewImpl;
import com.eucalyptus.webui.client.view.device.DeviceAreaView;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class DeviceAreaActivity extends AbstractSearchActivity implements DeviceAreaView.Presenter {
	
	private static final ClientMessage title = new ClientMessage("Area", "区域");
	
	private Date creationtimeBegin;
	private Date creationtimeEnd;
	private Date modifiedtimeBegin;
	private Date modifiedtimeEnd;
	
	private DeviceAreaAddView areaAddView;
	private DeviceAreaModifyView areaModifyView;
	
	public DeviceAreaActivity(SearchPlace place, ClientFactory clientFactory) {
		super(place, clientFactory);
	}
	
	private DeviceAreaView getView() {
		DeviceAreaView view = (DeviceAreaView)this.view;
		if (view == null) {
			view = clientFactory.getDeviceAreaView();
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
	
	private final static int AREA_ID = 0;
	private final static int AREA_NAME = 3;
	private final static int AREA_DESC = 4;
	
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
		getBackendService().lookupDeviceAreaByDate(getSession(), range,
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
				showStatus(new ClientMessage("", "查询区域成功"));
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
		System.out.println("single click " + row_index + " " + column_index + " " + row);
	}

	@Override
	public void onDoubleClick(SearchResultRow row, int row_index, int column_index) {
		getView().setSelectedRow(row);
	}
	
	@Override
	public void onAdd() {
		try {
			if (Window.confirm(new ClientMessage("", "确认创建新区域").toString())) {
				if (areaAddView == null) {
					areaAddView = new DeviceAreaAddViewImpl();
					areaAddView.setPresenter(new DeviceAreaAddView.Presenter() {
						
						@Override
						public boolean onOK(String area_name, String area_desc) {
							if (isEmpty(area_name)) {
								StringBuilder sb = new StringBuilder();
								sb.append(new ClientMessage("", "区域名称非法")).append(" = '").append(area_name).append("' ");
								sb.append(new ClientMessage("", "请重新选择区域"));
								Window.alert(sb.toString());
								return false;
							}
							getBackendService().addDeviceArea(getSession(), area_name, area_desc, new AsyncCallback<Void>() {

								@Override
								public void onFailure(Throwable caught) {
									if (caught instanceof EucalyptusServiceException) {
										onBackendServiceFailure((EucalyptusServiceException)caught);
									}
									getView().clearSelection();
								}

								@Override
								public void onSuccess(Void result) {
									showStatus(new ClientMessage("", "添加区域成功"));
									reloadCurrentRange();
									getView().clearSelection();
								}
								
							});
							return true;
						}
						
					});
				}
				areaAddView.popup();
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
				if (Window.confirm(new ClientMessage("", "确认修改所选择的区域").toString())) {
					int area_id = Integer.parseInt(row.getField(AREA_ID));
					String area_name = row.getField(AREA_NAME);
					String area_desc = row.getField(AREA_DESC);
					if (areaModifyView == null) {
						areaModifyView = new DeviceAreaModifyViewImpl();
						areaModifyView.setPresenter(new DeviceAreaModifyView.Presenter() {
							
							@Override
							public boolean onOK(int area_id, String area_desc) {
								getBackendService().modifyDeviceArea(getSession(), area_id, area_desc, new AsyncCallback<Void>() {

									@Override
									public void onFailure(Throwable caught) {
										if (caught instanceof EucalyptusServiceException) {
											onBackendServiceFailure((EucalyptusServiceException)caught);
										}
										getView().clearSelection();
									}

									@Override
									public void onSuccess(Void result) {
										showStatus(new ClientMessage("", "修改区域成功"));
										reloadCurrentRange();
										getView().clearSelection();
									}
									
								});
								return true;
							}
							
						});
					}
					areaModifyView.popup(area_id, area_name, area_desc);
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
				List<Integer> area_id_list = new ArrayList<Integer>();
				for (SearchResultRow row : set) {
					int area_id = Integer.parseInt(row.getField(AREA_ID));
					area_id_list.add(area_id);
				}
				if (!area_id_list.isEmpty()) {
					if (Window.confirm(new ClientMessage("", "确认删除所选择的区域").toString())) {
						getBackendService().deleteDeviceArea(getSession(), area_id_list, new AsyncCallback<Void>() {
		
							@Override
							public void onFailure(Throwable caught) {
								if (caught instanceof EucalyptusServiceException) {
									onBackendServiceFailure((EucalyptusServiceException)caught);
								}
								getView().clearSelection();
							}
		
							@Override
							public void onSuccess(Void result) {
								showStatus(new ClientMessage("", "删除区域成功"));
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
    	range = new SearchRange(0, DeviceAreaView.DEFAULT_PAGESIZE, -1, true);
    	reloadCurrentRange();
    }
	
}
