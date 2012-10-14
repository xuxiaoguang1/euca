package com.eucalyptus.webui.client.view;

import java.util.ArrayList;
import java.util.Date;
import java.util.Set;

import com.eucalyptus.webui.client.activity.device.ClientMessage;
import com.eucalyptus.webui.client.activity.device.DevicePageSize;
import com.eucalyptus.webui.client.service.SearchResult;
import com.eucalyptus.webui.client.service.SearchResultFieldDesc;
import com.eucalyptus.webui.client.service.SearchResultRow;
import com.eucalyptus.webui.client.view.DeviceDateBox.Handler;
import com.eucalyptus.webui.shared.resource.device.CellTableColumns;
import com.eucalyptus.webui.shared.resource.device.status.CPUState;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent;

public class DeviceCPUViewImpl extends Composite implements DeviceCPUView {

	private static CPUViewImplUiBinder uiBinder = GWT.create(CPUViewImplUiBinder.class);

	interface CPUViewImplUiBinder extends UiBinder<Widget, DeviceCPUViewImpl> {
	}
	
	@UiField LayoutPanel resultPanel;
	@UiField Anchor buttonAddCPU;
	@UiField Anchor buttonDeleteCPU;
	@UiField Anchor buttonModifyCPU;
	@UiField Anchor buttonAddCPUService;
	@UiField Anchor buttonDeleteCPUService;
	@UiField Anchor buttonModifyCPUService;
	@UiField Anchor buttonClearSelection;
	@UiField DeviceDateBox dateBegin;
	@UiField DeviceDateBox dateEnd;
	@UiField Anchor buttonClearDate;
	@UiField Anchor labelAll;
	@UiField Anchor labelStop;
	@UiField Anchor labelInuse;
	@UiField Anchor labelReserved;
	@UiField Anchor columnButton;
	@UiField ListBox pageSizeList;
	
	private Presenter presenter;
	private MultiSelectionModel<SearchResultRow> selection;
	private DeviceSearchResultTable table;
	private DevicePopupPanel popup = new DevicePopupPanel();
	
	public DeviceCPUViewImpl() {
		initWidget(uiBinder.createAndBindUi(this));
		
		for (String pageSize : DevicePageSize.getPageSizeList()) {
			pageSizeList.addItem(pageSize);
		}
		pageSizeList.setSelectedIndex(DevicePageSize.getPageSizeSelectedIndex());
		pageSizeList.addChangeHandler(new ChangeHandler() {

			@Override
			public void onChange(ChangeEvent event) {
				DevicePageSize.setPageSizeSelectedIndex(pageSizeList.getSelectedIndex());
				if (table != null) {
					table.setPageSize(DevicePageSize.getPageSize());
				}
			}
			
		});
		
		selection = new MultiSelectionModel<SearchResultRow>(SearchResultRow.KEY_PROVIDER);
		selection.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {

			@Override
			public void onSelectionChange(SelectionChangeEvent event) {
				updateSearchResultButtonStatus();
				presenter.onSelectionChange(selection.getSelectedSet());
			}
			
		});
		
		updateSearchResultButtonStatus();
		
		popup.setAutoHideEnabled(true);
        
		for (final DeviceDateBox dateBox : new DeviceDateBox[]{dateBegin, dateEnd}) {
			dateBox.setErrorHandler(new Handler() {

				@Override
				public void onErrorHappens() {
					updateDateButtonStatus();
					int x = dateBox.getAbsoluteLeft();
		            int y = dateBox.getAbsoluteTop() + dateBox.getOffsetHeight();
					popup.setHTML(x, y, "15EM", "3EM", DeviceDateBox.getDateErrorHTML(dateBox));
				}

				@Override
				public void onValueChanged() {
					updateDateButtonStatus();
	            	int x = dateBox.getAbsoluteLeft();
		            int y = dateBox.getAbsoluteTop() + dateBox.getOffsetHeight();
	                DeviceDateBox pair;
	                pair = (dateBox != dateBegin ? dateBegin : dateEnd);
	                if (!pair.hasError()) {
	                	Date date0 = dateBegin.getValue(), date1 = dateEnd.getValue();
	                	if (date0 != null && date1 != null) {
	                		if (date0.getTime() > date1.getTime()) {
	                			popup.setHTML(x, y, "12EM", "2EM", DeviceDateBox.getDateErrorHTML(dateBegin, dateEnd));
	                			return;
	                		}
	                	}
	                	updateSearchRange();
	                }
				}
			});
		}
		
		updateDateButtonStatus();
	}
	
	private String getLabel(boolean highlight, String msg) {
		StringBuilder sb = new StringBuilder();
		sb.append("<font color='").append(highlight ? "red" : "darkblue").append("'>").append(msg).append("</font>");
		return sb.toString();
	}

	@Override
	public void updateLabels() {
		final String[] prefix = {"全部CPU数量： ", "预留CPU数量： ", "使用中CPU数量： ", "未使用CPU数量： "};
		final String suffix = " 台";
		final CPUState[] states = {null, CPUState.RESERVED, CPUState.INUSE, CPUState.STOP};
		CPUState state = presenter.getQueryState();
		Anchor[] labels = new Anchor[]{labelAll, labelReserved, labelInuse, labelStop};
		for (int i = 0; i < labels.length; i ++) {
			if (labels[i] != null) {
				int value = presenter.getCounts(states[i]);
				labels[i].setHTML(getLabel(state == states[i], prefix[i] + value + suffix));
			}
		}
	}
	
	private void updateSearchResultButtonStatus() {
		int size = selection.getSelectedSet().size();
		buttonAddCPU.setEnabled(true);
		buttonAddCPUService.setEnabled(size == 1 && presenter.canAddCPUService());
		buttonDeleteCPU.setEnabled(size != 0 && presenter.canDeleteCPU());
		buttonDeleteCPUService.setEnabled(size != 0 && presenter.canDeleteCPUService());
		buttonModifyCPU.setEnabled(size == 1 && presenter.canModifyCPU());
		buttonModifyCPUService.setEnabled(size == 1 && presenter.canModifyCPUService());
		buttonClearSelection.setEnabled(size != 0);
	}
	
	private void updateDateButtonStatus() {
		if (isEmpty(dateBegin.getText()) && isEmpty(dateEnd.getText())) {
			buttonClearDate.setEnabled(false);
		}
		else {
            buttonClearDate.setEnabled(true);
		}
	}
	
	public boolean isEmpty(String s) {
		return s == null || s.length() == 0;
	}
	
	private void updateSearchRange() {
		if (!dateBegin.hasError() && !dateEnd.hasError()) {
			presenter.updateSearchResult(dateBegin.getValue(), dateEnd.getValue());
		}
	}
	
	@UiHandler("labelAll")
	void handleLabelAll(ClickEvent event) {
		presenter.setQueryState(null);
	}

	@UiHandler("labelStop")
	void handleLabelReserved(ClickEvent event) {
		presenter.setQueryState(CPUState.STOP);
	}

	@UiHandler("labelInuse")
	void handleLabelInuse(ClickEvent event) {
		presenter.setQueryState(CPUState.INUSE);
	}

	@UiHandler("labelReserved")
	void handleLabelStop(ClickEvent event) {
		presenter.setQueryState(CPUState.RESERVED);
	}
	
	@Override
	public Set<SearchResultRow> getSelectedSet() {
		return selection.getSelectedSet();
	}
	
	@Override
	public void setSelectedRow(SearchResultRow row) {
		boolean selected = true;
		Set<SearchResultRow> set = selection.getSelectedSet();
		if (set != null && set.contains(row)) {
			selected = false;
		}
		clearSelection();
		selection.setSelected(row, selected);
		updateSearchResultButtonStatus();
	}
	
	private DeviceColumnPopupPanel.Node addNode(ArrayList<SearchResultFieldDesc> descs, DeviceColumnPopupPanel.Node parent, ClientMessage msg, int column) {
	    return parent.addNode(msg, column, DeviceSearchResultTable.isVisible(descs.get(column).getWidth()));
	}
	
	private void initColumnPanel(ArrayList<SearchResultFieldDesc> descs, DeviceColumnPopupPanel panel) {
        DeviceColumnPopupPanel.Node account = panel.addNode(new ClientMessage("", "账户信息"));
        addNode(descs, account, new ClientMessage("", "账户名称"), CellTableColumns.CPU.ACCOUNT_NAME);
        addNode(descs, account, new ClientMessage("", "用户名称"), CellTableColumns.CPU.USER_NAME);
        DeviceColumnPopupPanel.Node service = panel.addNode(new ClientMessage("", "服务信息"));
        addNode(descs, service, new ClientMessage("", "CPU名称"), CellTableColumns.CPU.CPU_NAME);
        addNode(descs, service, new ClientMessage("", "服务状态"), CellTableColumns.CPU.CPU_SERVICE_STATE);
        addNode(descs, service, new ClientMessage("", "服务描述"), CellTableColumns.CPU.CPU_SERVICE_DESC);
        addNode(descs, service, new ClientMessage("", "占用数量"), CellTableColumns.CPU.CPU_SERVICE_USED);
        addNode(descs, service, new ClientMessage("", "开始时间"), CellTableColumns.CPU.CPU_SERVICE_STARTTIME);
        addNode(descs, service, new ClientMessage("", "结束时间"), CellTableColumns.CPU.CPU_SERVICE_ENDTIME);
        addNode(descs, service, new ClientMessage("", "剩余时间"), CellTableColumns.CPU.CPU_SERVICE_LIFE);
        addNode(descs, service, new ClientMessage("", "添加时间"), CellTableColumns.CPU.CPU_SERVICE_CREATIONTIME);
        addNode(descs, service, new ClientMessage("", "修改时间"), CellTableColumns.CPU.CPU_SERVICE_MODIFIEDTIME);
        DeviceColumnPopupPanel.Node device = panel.addNode(new ClientMessage("", "设备信息"));
        addNode(descs, device, new ClientMessage("", "总数量"), CellTableColumns.CPU.CPU_TOTAL);
        addNode(descs, device, new ClientMessage("", "硬件描述"), CellTableColumns.CPU.CPU_DESC);
        addNode(descs, device, new ClientMessage("", "所属服务器"), CellTableColumns.CPU.SERVER_NAME);
        addNode(descs, device, new ClientMessage("", "生产厂家"), CellTableColumns.CPU.CPU_VENDOR);
        addNode(descs, device, new ClientMessage("", "产品型号"), CellTableColumns.CPU.CPU_MODEL);
        addNode(descs, device, new ClientMessage("", "主频(GHz)"), CellTableColumns.CPU.CPU_GHZ);
        addNode(descs, device, new ClientMessage("", "缓存大小(MB)"), CellTableColumns.CPU.CPU_CACHE);
        addNode(descs, device, new ClientMessage("", "添加时间"), CellTableColumns.CPU.CPU_CREATIONTIME);
        addNode(descs, device, new ClientMessage("", "修改时间"), CellTableColumns.CPU.CPU_MODIFIEDTIME);
        panel.reload();
    }
	
	@Override
	public void showSearchResult(SearchResult result) {
		if (table == null) {
			table = new DeviceSearchResultTable(result.getDescs(), selection);
			table.setRangeChangeHandler(presenter);
			table.setClickHandler(presenter);
			table.load();
			resultPanel.add(table);
			final DeviceColumnPopupPanel panel = new DeviceColumnPopupPanel(new DeviceColumnPopupPanel.Presenter() {
	            
	            @Override
	            public void onValueChange(int column, boolean value) {
	            	if (table != null) {
	            		table.setVisible(column, value);
	            	}
	            }
	            
	        });
			columnButton.addClickHandler(new ClickHandler() {

                @Override
                public void onClick(ClickEvent event) {
                    panel.popup(columnButton);
                }
                
            });
			initColumnPanel(result.getDescs(), panel);
		}
		table.setData(result);
		if (table.getPageSize() != DevicePageSize.getPageSize()) {
			table.setPageSize(DevicePageSize.getPageSize());
			pageSizeList.setSelectedIndex(DevicePageSize.getPageSizeSelectedIndex());
		}
	}
	
	@Override
	public void clear() {
		resultPanel.clear();
		table = null;
	}
	
	@Override
	public void clearSelection() {
		selection.clear();
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}
	
	@UiHandler("buttonAddCPU")
	void onButtonAddCPU(ClickEvent event) {
		if (buttonAddCPU.isEnabled()) {
			presenter.onAddCPU();
		}
	}
	
	@UiHandler("buttonAddCPUService")
	void onButtonAddCPUService(ClickEvent event) {
		if (buttonAddCPUService.isEnabled()) {
			presenter.onAddCPUService();
		}
	}
	
	@UiHandler("buttonDeleteCPU")
	void onButtonDeleteCPU(ClickEvent event) {
		if (buttonDeleteCPU.isEnabled()) {
			presenter.onDeleteCPU();
		}
	}
	
	@UiHandler("buttonDeleteCPUService")
	void onButtonDeleteCPUService(ClickEvent event) {
		if (buttonDeleteCPUService.isEnabled()) {
			presenter.onDeleteCPUService();
		}
	}
	
	@UiHandler("buttonModifyCPU")
	void handleButtonModifyCPU(ClickEvent event) {
		if (buttonModifyCPU.isEnabled()) {
			presenter.onModifyCPU();
		}
	}
	
	@UiHandler("buttonModifyCPUService")
	void handleButtonModifyCPUService(ClickEvent event) {
		if (buttonModifyCPUService.isEnabled()) {
			presenter.onModifyCPUService();
		}
	}
	
	@UiHandler("buttonClearSelection")
	void handleButtonClearSelection(ClickEvent event) {
		if (buttonClearSelection.isEnabled()) {
			clearSelection();
		}
	}
	
	@UiHandler("buttonClearDate")
	void handleButtonClearDate(ClickEvent event) {
	    if (buttonClearDate.isEnabled()) {
	    	dateBegin.setValue(null);
    	    dateEnd.setValue(null);
    	    updateDateButtonStatus();
    	    updateSearchRange();
	    }
	}
	
}
