package com.eucalyptus.webui.client.activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;

import com.eucalyptus.webui.client.ClientFactory;
import com.eucalyptus.webui.client.place.SearchPlace;
import com.eucalyptus.webui.client.service.EucalyptusServiceAsync;
import com.eucalyptus.webui.client.service.SearchRange;
import com.eucalyptus.webui.client.service.SearchResult;
import com.eucalyptus.webui.client.service.SearchResultRow;
import com.eucalyptus.webui.client.session.Session;
import com.eucalyptus.webui.client.view.DeviceTemplateAddView;
import com.eucalyptus.webui.client.view.DeviceTemplateAddViewImpl;
import com.eucalyptus.webui.client.view.DeviceTemplateModifyView;
import com.eucalyptus.webui.client.view.DeviceTemplateModifyViewImpl;
import com.eucalyptus.webui.client.view.DeviceTemplateView;
import com.eucalyptus.webui.client.view.DeviceMirrorSearchResultTable.SearchResultRowMatcher;
import com.eucalyptus.webui.client.view.FooterView;
import com.eucalyptus.webui.client.view.HasValueWidget;
import com.eucalyptus.webui.client.view.LogView;
import com.eucalyptus.webui.client.view.DeviceTemplateView.MirrorModeType;
import com.eucalyptus.webui.client.view.FooterView.StatusType;
import com.eucalyptus.webui.client.view.LogView.LogType;
import com.eucalyptus.webui.server.DeviceTemplateServiceProcImpl;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class DeviceTemplateActivity extends AbstractSearchActivity implements DeviceTemplateView.Presenter {

	private static final int LAN_SELECT = 1;

	public static final String TITLE[] = {"Template", "模板"};

	private DeviceTemplateModifyView templateModifyView;
	private DeviceTemplateAddView templateAddView;

	public DeviceTemplateActivity(SearchPlace place, ClientFactory clientFactory) {
		super(place, clientFactory);
		templateModifyView = new DeviceTemplateModifyViewImpl();
		templateModifyView.setPresenter(new DeviceTemplateModifyView.Presenter() {

			@Override
			public boolean onOK(SearchResultRow row, String cpu, String mem, String disk, String bw, String image) {
				handleModifyService(row, cpu, mem, disk, bw, image);
				getView().getMirrorTable().clearSelection();
				return true;
			}

			public void onCancel() {
				getView().getMirrorTable().clearSelection();
			}

		});

		templateAddView = new DeviceTemplateAddViewImpl();
		templateAddView.setPresenter(new DeviceTemplateAddView.Presenter() {

			@Override
			public boolean onOK(String mark, String cpu, String mem, String disk, String bw, String image) {
				if (mark == null || mark.length() == 0) {
					StringBuilder sb = new StringBuilder();
					sb.append(ADD_TEMPLATE_FAILURE_INVALID_ARGS[LAN_SELECT]).append("\n");
					sb.append("<mark='").append(mark).append("'").append(", ");
					sb.append("cpu='").append(cpu).append("'").append("', ");
					sb.append("mem='").append(cpu).append("'").append("', ");
					sb.append("disk='").append(cpu).append("'").append("', ");
					sb.append("bw='").append(cpu).append("'").append("', ");
					sb.append("image='").append(image).append("'").append("'>");
					Window.alert(sb.toString());
					return false;
				}
				handleAddTemplate(mark, cpu, mem, disk, bw, image);
				return true;
			}

		});
	}
	
	private void handleAddTemplate(String mark, String cpu, String mem, String disk, String bw, String image) {
		getBackendService().addDeviceTemplate(getSession(), 
				mark, cpu, mem, disk, bw, image, new AsyncCallback<Boolean>() {

			        @Override
			        public void onFailure(Throwable caught) {
				        ActivityUtil.logoutForInvalidSession(clientFactory, caught);
				        log(ADD_TEMPLATE_FAILURE[LAN_SELECT], caught);
			        }

			        @Override
			        public void onSuccess(Boolean result) {
				        if (result) {
					        showStatus(ADD_TEMPLATE_SUCCESS[LAN_SELECT]);
				        	reloadCurrentRange();
				        }
				        else {
					        showStatus(ADD_TEMPLATE_FAILURE[LAN_SELECT]);
				        }
			        }

		        });
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

	private void log(String msg, Throwable caught) {
		getFooterView().showStatus(StatusType.ERROR, msg, FooterView.CLEAR_DELAY_SECOND * 5);
		getLogView().log(LogType.ERROR, msg + ": " + caught.getMessage());
	}

	private void showStatus(String msg) {
		getFooterView().showStatus(StatusType.ERROR, msg, FooterView.CLEAR_DELAY_SECOND * 5);
		getLogView().log(LogType.ERROR, msg);
	}
	
	@Override
	protected void doSearch(String query, SearchRange range) {
		getBackendService().lookupDeviceTemplate(getSession(), query, range, starttime, endtime,
		        new AsyncCallback<SearchResult>() {

			        @Override
			        public void onFailure(Throwable caught) {
				        ActivityUtil.logoutForInvalidSession(clientFactory, caught);
				        log(QUERY_TABLE_FAILURE[LAN_SELECT], caught);
				        displayData(null);
			        }

			        @Override
			        public void onSuccess(SearchResult result) {
				        displayData(result);
			        }

		        });
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
	protected void showView(SearchResult result) {
		getView().showSearchResult(result);
	}

	@Override
	public void onSelectionChange(Set<SearchResultRow> selections) {
		/* do nothing */
	}

	@Override
	public void saveValue(ArrayList<String> keys, ArrayList<HasValueWidget> values) {
		/* do nothing */
	}

	@Override
	protected String getTitle() {
		return TITLE[LAN_SELECT];
	}

	private static final String[] QUERY_TABLE_FAILURE = {"", "获取列表失败"};
	private static final String[] UPDATE_TEMPLATE_FAILURE = {"", "更新模板失败"};
	private static final String[] UPDATE_TEMPLATE_SUCCESS = {"", "更新模板成功"};
	private static final String[] ADD_TEMPLATE_SUCCESS = {"", "添加模板成功"};
	private static final String[] ADD_TEMPLATE_FAILURE = {"", "添加模板失败"};
	private static final String[] ADD_TEMPLATE_FAILURE_INVALID_ARGS = {"", "添加模板失败：选择参数无效"};
	private static final String[] DELETE_TEMPLATE_FAILURE = {"", "删除模板失败"};
	private static final String[] DELETE_TEMPLATE_SUCCESS = {"", "删除模板成功"};
	private static final String[] DELETE_TEMPLATE_CONFIRM = {"", "确认删除所选择的 模板？"};
	private static final String[] DELETE_ALL_TEMPLATE_CONFIRM = {"", "确认删除所选择的 全部模板？"};
	private static final String[] ACTION_SELECTED_FAILURE = {"", "请选择操作对象"};

	private void prepareModifyService(SearchResultRow row) {
		templateModifyView.popup(row);
	}

	private void prepareDeleteService(SearchResultRow row) {
		if (!Window.confirm(DELETE_TEMPLATE_CONFIRM[LAN_SELECT])) {
			getView().getMirrorTable().clearSelection();
			return;
		}
		List<SearchResultRow> list = new ArrayList<SearchResultRow>();
		list.add(row);
		handleDeleteTemplate(list);
	}

	@Override
	public void onMirrorSelectRow(SearchResultRow row) {
		if (row == null) {
			return;
		}
		switch (getView().getMirrorModeType()) {
		case MODIFY_TEMPLATE:
			prepareModifyService(row);
			return;
		case DELETE_TEMPLATE:
			prepareDeleteService(row);
			return;
		default:
			return;
		}
	}

	private void handleModifyService(SearchResultRow row, String cpu, String mem, String disk, String bw, String image) {
		assert (row != null && getView().getMirrorModeType() == MirrorModeType.MODIFY_TEMPLATE);
		getBackendService().modifyDeviceTempate(getSession(), row, cpu, mem, disk, bw, image, 
		        new AsyncCallback<SearchResultRow>() {

			        @Override
			        public void onFailure(Throwable caught) {
				        ActivityUtil.logoutForInvalidSession(clientFactory, caught);
				        log(UPDATE_TEMPLATE_FAILURE[LAN_SELECT], caught);
			        }

			        @Override
			        public void onSuccess(SearchResultRow result) {
				        if (result != null) {
					        showStatus(UPDATE_TEMPLATE_SUCCESS[LAN_SELECT]);
					        if (getView().isMirrorMode()) {
						        final int col = DeviceTemplateServiceProcImpl.TABLE_COL_INDEX_TEMPLATE_ID;
						        result.setField(DeviceTemplateServiceProcImpl.TABLE_COL_INDEX_CHECKBOX, "+");
						        final SearchResultRowMatcher matcher = new SearchResultRowMatcher() {

							        @Override
							        public boolean match(SearchResultRow row0, SearchResultRow row1) {
								        return row0.getField(col) != null
								                && row0.getField(col).equals(row1.getField(col));
							        }

						        };
						        getView().getMirrorTable().updateRow(result, matcher);
					        }
					        else {
					        	reloadCurrentRange();
					        }
				        }
				        else {
					        showStatus(UPDATE_TEMPLATE_FAILURE[LAN_SELECT]);
				        }
			        }

		        });
	}

	private void handleDeleteTemplate(List<SearchResultRow> list) {
		DeviceTemplateView view = getView();
		assert (list.size() != 0 && view.getMirrorModeType() == MirrorModeType.DELETE_TEMPLATE);
		getBackendService().deleteDeviceTemplate(getSession(), list, new AsyncCallback<List<SearchResultRow>>() {

			@Override
			public void onFailure(Throwable caught) {
				ActivityUtil.logoutForInvalidSession(clientFactory, caught);
				log(DELETE_TEMPLATE_FAILURE[LAN_SELECT], caught);
			}

			@Override
			public void onSuccess(List<SearchResultRow> result) {
				if (result != null) {
					showStatus(DELETE_TEMPLATE_SUCCESS[LAN_SELECT]);
					if (getView().isMirrorMode()) {
						final int col = DeviceTemplateServiceProcImpl.TABLE_COL_INDEX_TEMPLATE_ID;
						final SearchResultRowMatcher matcher = new SearchResultRowMatcher() {

							@Override
							public boolean match(SearchResultRow row0, SearchResultRow row1) {
								return row0.getField(col) != null && row0.getField(col).equals(row1.getField(col));
							}

						};
						for (SearchResultRow row : result) {
							getView().getMirrorTable().deleteRow(row, matcher);
						}
			        }
			        else {
			        	reloadCurrentRange();
			        }
				}
				else {
					showStatus(DELETE_TEMPLATE_FAILURE[LAN_SELECT]);
				}
			}

		});
	}

	private SearchResultRow copyRow(SearchResultRow row) {
		SearchResultRow tmp = row.copy();
		tmp.setField(DeviceTemplateServiceProcImpl.TABLE_COL_INDEX_CHECKBOX, "");
		return tmp;
	}

	@Override
	public void onAddTemplate() {
		templateAddView.popup();
	}

	@Override
	public void onModifyTemplate() {
		Set<SearchResultRow> selected = getView().getSelectedSet();
		if (selected == null || selected.isEmpty()) {
			showStatus(ACTION_SELECTED_FAILURE[LAN_SELECT]);
			return;
		}
		List<SearchResultRow> list = new ArrayList<SearchResultRow>();
		for (SearchResultRow row : selected) {
			list.add(copyRow(row));
		}
		getView().openMirrorMode(MirrorModeType.MODIFY_TEMPLATE, sortSearchResultRow(list));
	}

	@Override
	public void onDeleteTemplate() {
		Set<SearchResultRow> selected = getView().getSelectedSet();
		if (selected == null || selected.isEmpty()) {
			showStatus(ACTION_SELECTED_FAILURE[LAN_SELECT]);
			return;
		}
		List<SearchResultRow> list = new ArrayList<SearchResultRow>();
		for (SearchResultRow row : selected) {
			list.add(copyRow(row));
		}
		getView().openMirrorMode(MirrorModeType.DELETE_TEMPLATE, sortSearchResultRow(list));
	}

	@Override
	public void onClearSelection() {
		getView().clearSelection();
	}

	@Override
	public void onMirrorBack() {
		getView().closeMirrorMode();
		reloadCurrentRange();
	}

	@Override
	public void onMirrorDeleteAll() {
		List<SearchResultRow> data;
		switch (getView().getMirrorModeType()) {
		case DELETE_TEMPLATE:
			data = getView().getMirrorTable().getData();
			if (data != null && data.size() != 0) {
				if (Window.confirm(DELETE_ALL_TEMPLATE_CONFIRM[LAN_SELECT])) {
					handleDeleteTemplate(data);
				}
			}
			break;
		default:
			break;
		}
	}

	private List<SearchResultRow> sortSearchResultRow(List<SearchResultRow> list) {
		Collections.sort(list, new Comparator<SearchResultRow>() {

			@Override
			public int compare(SearchResultRow arg0, SearchResultRow arg1) {
				String v0 = arg0.getField(DeviceTemplateServiceProcImpl.TABLE_COL_INDEX_NO);
				String v1 = arg1.getField(DeviceTemplateServiceProcImpl.TABLE_COL_INDEX_NO);
				if (v0 == null) {
					return v1 == null ? 0 : 1;
				}
				if (v1 == null) {
					return -1;
				}
				try {
					return Integer.parseInt(v0) - Integer.parseInt(v1);
				}
				catch (Exception e) {
					e.printStackTrace();
				}
				return 0;
			}

		});
		return list;
	}
	
	private Date starttime = null;
	private Date endtime = null;

	@Override
    public void onSearch(Date starttime, Date endtime) {
		this.starttime = starttime;
		this.endtime = endtime;
		reloadCurrentRange();
    }

}
