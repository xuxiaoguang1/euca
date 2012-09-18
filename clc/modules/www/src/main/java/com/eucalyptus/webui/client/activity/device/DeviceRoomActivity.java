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
import com.eucalyptus.webui.client.view.DeviceRoomAddView;
import com.eucalyptus.webui.client.view.DeviceRoomAddViewImpl;
import com.eucalyptus.webui.client.view.DeviceRoomModifyView;
import com.eucalyptus.webui.client.view.DeviceRoomModifyViewImpl;
import com.eucalyptus.webui.client.view.DeviceRoomView;
import com.eucalyptus.webui.client.view.HasValueWidget;
import com.eucalyptus.webui.client.view.LogView;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class DeviceRoomActivity extends AbstractSearchActivity implements DeviceRoomView.Presenter {
	
	private static final ClientMessage title = new ClientMessage("Room", "机房");
	
	private Date creationtimeBegin;
	private Date creationtimeEnd;
	private Date modifiedtimeBegin;
	private Date modifiedtimeEnd;
	
	private DeviceRoomAddView roomAddView;
	private DeviceRoomModifyView roomModifyView;
	
	public DeviceRoomActivity(SearchPlace place, ClientFactory clientFactory) {
		super(place, clientFactory);
	}
	
	private DeviceRoomView getView() {
		DeviceRoomView view = (DeviceRoomView)this.view;
		if (view == null) {
			view = clientFactory.getDeviceRoomView();
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
	
	private final static int ROOM_ID = 0;
	private final static int ROOM_NAME = 3;
	private final static int ROOM_DESC = 4;
	
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
		getBackendService().lookupDeviceRoomByDate(getSession(), range,
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
				showStatus(new ClientMessage("", "查询机房成功"));
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
			if (Window.confirm(new ClientMessage("", "确认创建新机房").toString())) {
				if (roomAddView == null) {
					roomAddView = new DeviceRoomAddViewImpl();
					roomAddView.setPresenter(new DeviceRoomAddView.Presenter() {
						
						@Override
						public boolean onOK(String room_name, String room_desc, String area_name) {
							if (isEmpty(room_name)) {
								StringBuilder sb = new StringBuilder();
								sb.append(new ClientMessage("", "机房名称非法")).append(" = '").append(room_name).append("' ");
								sb.append(new ClientMessage("", "请重新选择机房"));
								Window.alert(sb.toString());
								return false;
							}
							if (isEmpty(area_name)) {
								StringBuilder sb = new StringBuilder();
								sb.append(new ClientMessage("", "区域名称非法")).append(" = '").append(area_name).append("' ");
								sb.append(new ClientMessage("", "请重新选择区域"));
								Window.alert(sb.toString());
								return false;
							}
							getBackendService().addDeviceRoom(getSession(), room_name, room_desc, area_name, new AsyncCallback<Void>() {

								@Override
								public void onFailure(Throwable caught) {
									if (caught instanceof EucalyptusServiceException) {
										onBackendServiceFailure((EucalyptusServiceException)caught);
									}
									getView().clearSelection();
								}

								@Override
								public void onSuccess(Void result) {
									showStatus(new ClientMessage("", "添加机房成功"));
									reloadCurrentRange();
									getView().clearSelection();
								}
								
							});
							return true;
						}

						@Override
						public void lookupAreaNames() {
							getBackendService().lookupDeviceAreaNames(getSession(), new AsyncCallback<List<String>>() {

								@Override
								public void onFailure(Throwable caught) {
									if (caught instanceof EucalyptusServiceException) {
										onBackendServiceFailure((EucalyptusServiceException)caught);
									}
								}

								@Override
								public void onSuccess(List<String> area_name_list) {
									showStatus(new ClientMessage("", "获取区域列表成功"));
									roomAddView.setAreaNameList(area_name_list);
								}
								
							});
						}
						
					});
				}
				roomAddView.popup();
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
				if (Window.confirm(new ClientMessage("", "确认修改所选择的机房").toString())) {
					int room_id = Integer.parseInt(row.getField(ROOM_ID));
					String room_name = row.getField(ROOM_NAME);
					String room_desc = row.getField(ROOM_DESC);
					if (roomModifyView == null) {
						roomModifyView = new DeviceRoomModifyViewImpl();
						roomModifyView.setPresenter(new DeviceRoomModifyView.Presenter() {
							
							@Override
							public boolean onOK(int room_id, String room_desc) {
								getBackendService().modifyDeviceRoom(getSession(), room_id, room_desc, new AsyncCallback<Void>() {

									@Override
									public void onFailure(Throwable caught) {
										if (caught instanceof EucalyptusServiceException) {
											onBackendServiceFailure((EucalyptusServiceException)caught);
										}
										getView().clearSelection();
									}

									@Override
									public void onSuccess(Void result) {
										showStatus(new ClientMessage("", "修改机房成功"));
										reloadCurrentRange();
										getView().clearSelection();
									}
									
								});
								return true;
							}
							
						});
					}
					roomModifyView.popup(room_id, room_name, room_desc);
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
				List<Integer> room_id_list = new ArrayList<Integer>();
				for (SearchResultRow row : set) {
					int room_id = Integer.parseInt(row.getField(ROOM_ID));
					room_id_list.add(room_id);
				}
				if (!room_id_list.isEmpty()) {
					if (Window.confirm(new ClientMessage("", "确认删除所选择的机房").toString())) {
						getBackendService().deleteDeviceRoom(getSession(), room_id_list, new AsyncCallback<Void>() {
		
							@Override
							public void onFailure(Throwable caught) {
								if (caught instanceof EucalyptusServiceException) {
									onBackendServiceFailure((EucalyptusServiceException)caught);
								}
								getView().clearSelection();
							}
		
							@Override
							public void onSuccess(Void result) {
								showStatus(new ClientMessage("", "删除机房成功"));
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
    	range = new SearchRange(0, DeviceRoomView.DEFAULT_PAGESIZE, -1, true);
    	reloadCurrentRange();
    }
	
}
