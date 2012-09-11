package com.eucalyptus.webui.client.activity.device;

import java.util.ArrayList;
import java.util.Collection;
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
import com.eucalyptus.webui.client.view.device.DeviceCabinetAddView;
import com.eucalyptus.webui.client.view.device.DeviceCabinetAddViewImpl;
import com.eucalyptus.webui.client.view.device.DeviceCabinetModifyView;
import com.eucalyptus.webui.client.view.device.DeviceCabinetModifyViewImpl;
import com.eucalyptus.webui.client.view.device.DeviceCabinetView;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class DeviceCabinetActivity extends AbstractSearchActivity implements DeviceCabinetView.Presenter {
	
	private static final ClientMessage title = new ClientMessage("Cabinet", "机柜");
	
	private Date creationtimeBegin;
	private Date creationtimeEnd;
	private Date modifiedtimeBegin;
	private Date modifiedtimeEnd;
	
	private DeviceCabinetAddView cabinetAddView;
	private DeviceCabinetModifyView cabinetModifyView;
	
	public DeviceCabinetActivity(SearchPlace place, ClientFactory clientFactory) {
		super(place, clientFactory);
	}
	
	private DeviceCabinetView getView() {
		DeviceCabinetView view = (DeviceCabinetView)this.view;
		if (view == null) {
			view = clientFactory.getDeviceCabinetView();
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
	
	private final static int CABINET_ID = 0;
	private final static int CABINET_NAME = 3;
	private final static int CABINET_DESC = 4;
	
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
		getBackendService().lookupDeviceCabinetByDate(getSession(), range,
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
				showStatus(new ClientMessage("", "查询机柜成功"));
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
			if (Window.confirm(new ClientMessage("", "确认创建新机柜").toString())) {
				if (cabinetAddView == null) {
					cabinetAddView = new DeviceCabinetAddViewImpl();
					cabinetAddView.setPresenter(new DeviceCabinetAddView.Presenter() {
						
						@Override
						public boolean onOK(String cabinet_name, String cabinet_desc, String room_name) {
							if (isEmpty(cabinet_name)) {
								StringBuilder sb = new StringBuilder();
								sb.append(new ClientMessage("", "机柜名称非法")).append(" = '").append(cabinet_name).append("' ");
								sb.append(new ClientMessage("", "请重新选择机柜"));
								Window.alert(sb.toString());
								return false;
							}
							if (isEmpty(room_name)) {
								StringBuilder sb = new StringBuilder();
								sb.append(new ClientMessage("", "机房名称非法")).append(" = '").append(room_name).append("' ");
								sb.append(new ClientMessage("", "请重新选择机房"));
								Window.alert(sb.toString());
								return false;
							}
							getBackendService().addDeviceCabinet(getSession(), cabinet_name, cabinet_desc, room_name, new AsyncCallback<Void>() {

								@Override
								public void onFailure(Throwable caught) {
									if (caught instanceof EucalyptusServiceException) {
										onBackendServiceFailure((EucalyptusServiceException)caught);
									}
									getView().clearSelection();
								}

								@Override
								public void onSuccess(Void result) {
									showStatus(new ClientMessage("", "添加机柜成功"));
									reloadCurrentRange();
									getView().clearSelection();
								}
								
							});
							return true;
						}

						@Override
						public void lookupAreaNames() {
							getBackendService().lookupDeviceAreaNames(getSession(), new AsyncCallback<Collection<String>>() {

								@Override
								public void onFailure(Throwable caught) {
									if (caught instanceof EucalyptusServiceException) {
										onBackendServiceFailure((EucalyptusServiceException)caught);
									}
								}

								@Override
								public void onSuccess(Collection<String> area_name_list) {
									showStatus(new ClientMessage("", "获取区域列表成功"));
									cabinetAddView.setAreaNameList(area_name_list);
								}
								
							});
						}

						@Override
						public void lookupRoomNamesByAreaName(final String area_name) {
							if (isEmpty(area_name)) {
								StringBuilder sb = new StringBuilder();
								sb.append(new ClientMessage("", "区域名称非法")).append(" = '").append(area_name).append("'");
								Window.alert(sb.toString());
							}
							else {
								getBackendService().lookupDeviceRoomNamesByAreaName(getSession(), area_name, new AsyncCallback<Collection<String>>() {

									@Override
									public void onFailure(Throwable caught) {
										if (caught instanceof EucalyptusServiceException) {
											onBackendServiceFailure((EucalyptusServiceException)caught);
										}
									}

									@Override
									public void onSuccess(Collection<String> room_name_list) {
										showStatus(new ClientMessage("", "获取机房列表成功"));
										cabinetAddView.setRoomNameList(area_name, room_name_list);
									}
									
								});
							}
						}
						
					});
				}
				cabinetAddView.popup();
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
				if (Window.confirm(new ClientMessage("", "确认修改所选择的机柜").toString())) {
					int cabinet_id = Integer.parseInt(row.getField(CABINET_ID));
					String cabinet_name = row.getField(CABINET_NAME);
					String cabinet_desc = row.getField(CABINET_DESC);
					if (cabinetModifyView == null) {
						cabinetModifyView = new DeviceCabinetModifyViewImpl();
						cabinetModifyView.setPresenter(new DeviceCabinetModifyView.Presenter() {
							
							@Override
							public boolean onOK(int cabinet_id, String cabinet_desc) {
								getBackendService().modifyDeviceCabinet(getSession(), cabinet_id, cabinet_desc, new AsyncCallback<Void>() {

									@Override
									public void onFailure(Throwable caught) {
										if (caught instanceof EucalyptusServiceException) {
											onBackendServiceFailure((EucalyptusServiceException)caught);
										}
										getView().clearSelection();
									}

									@Override
									public void onSuccess(Void result) {
										showStatus(new ClientMessage("", "修改机柜成功"));
										reloadCurrentRange();
										getView().clearSelection();
									}
									
								});
								return true;
							}
							
						});
					}
					cabinetModifyView.popup(cabinet_id, cabinet_name, cabinet_desc);
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
				List<Integer> cabinet_id_list = new ArrayList<Integer>();
				for (SearchResultRow row : set) {
					int cabinet_id = Integer.parseInt(row.getField(CABINET_ID));
					cabinet_id_list.add(cabinet_id);
				}
				if (!cabinet_id_list.isEmpty()) {
					if (Window.confirm(new ClientMessage("", "确认删除所选择的机柜").toString())) {
						getBackendService().deleteDeviceCabinet(getSession(), cabinet_id_list, new AsyncCallback<Void>() {
		
							@Override
							public void onFailure(Throwable caught) {
								if (caught instanceof EucalyptusServiceException) {
									onBackendServiceFailure((EucalyptusServiceException)caught);
								}
								getView().clearSelection();
							}
		
							@Override
							public void onSuccess(Void result) {
								showStatus(new ClientMessage("", "删除机柜成功"));
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
    	range = new SearchRange(0, DeviceCabinetView.DEFAULT_PAGESIZE, -1, true);
    	reloadCurrentRange();
    }
	
}
