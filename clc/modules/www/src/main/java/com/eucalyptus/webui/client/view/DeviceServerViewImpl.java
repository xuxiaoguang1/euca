package com.eucalyptus.webui.client.view;

import java.util.Date;
import java.util.Set;

import com.eucalyptus.webui.client.activity.device.DevicePageSize;
import com.eucalyptus.webui.client.service.SearchResult;
import com.eucalyptus.webui.client.service.SearchResultRow;
import com.eucalyptus.webui.client.view.DeviceDateBox.Handler;
import com.eucalyptus.webui.shared.resource.device.status.ServerState;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
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

public class DeviceServerViewImpl extends Composite implements DeviceServerView {

	private static DeviceServerViewImplUiBinder uiBinder = GWT.create(DeviceServerViewImplUiBinder.class);

	interface DeviceServerViewImplUiBinder extends UiBinder<Widget, DeviceServerViewImpl> {
	}
	
	@UiField LayoutPanel resultPanel;
	@UiField Anchor buttonAddServer;
	@UiField Anchor buttonDeleteServer;
	@UiField Anchor buttonModifyServer;
	@UiField Anchor buttonOperateServer;
	@UiField Anchor buttonClearSelection;
	@UiField DeviceDateBox dateBegin;
	@UiField DeviceDateBox dateEnd;
	@UiField Anchor buttonClearDate;
	@UiField Anchor labelAll;
	@UiField Anchor labelStop;
	@UiField Anchor labelInuse;
	@UiField Anchor labelError;
	@UiField ListBox pageSizeList;
	
	private Presenter presenter;
	private MultiSelectionModel<SearchResultRow> selection;
	private DeviceSearchResultTable table;
	private DevicePopupPanel popup = new DevicePopupPanel();
	
	public DeviceServerViewImpl() {
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
					popup.setHTML(x, y, "30EM", "3EM", DeviceDateBox.getDateErrorHTML(dateBox));
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
	                			popup.setHTML(x, y, "20EM", "2EM", DeviceDateBox.getDateErrorHTML(dateBegin, dateEnd));
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
		final String[] prefix = {"全部服务器数量： ", "停止中服务器数量： ", "使用中服务器数量： ", "故障中服务器数量： "};
		final String suffix = " 台";
		final ServerState[] states = {null, ServerState.STOP, ServerState.INUSE, ServerState.ERROR};
		ServerState state = presenter.getQueryState();
		Anchor[] labels = new Anchor[]{labelAll, labelStop, labelInuse, labelError};
		for (int i = 0; i < labels.length; i ++) {
			if (labels[i] != null) {
				int value = presenter.getCounts(states[i]);
				labels[i].setHTML(getLabel(state == states[i], prefix[i] + value + suffix));
			}
		}
	}
	
	private void updateSearchResultButtonStatus() {
		int size = selection.getSelectedSet().size();
		buttonAddServer.setEnabled(true);
		buttonDeleteServer.setEnabled(size != 0);
		buttonModifyServer.setEnabled(size == 1);
		buttonOperateServer.setEnabled(size == 1);
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
		presenter.setQueryState(ServerState.STOP);
	}

	@UiHandler("labelInuse")
	void handleLabelInuse(ClickEvent event) {
		presenter.setQueryState(ServerState.INUSE);
	}

	@UiHandler("labelError")
	void handleLabelStop(ClickEvent event) {
		presenter.setQueryState(ServerState.ERROR);
	}

	@Override
	public Set<SearchResultRow> getSelectedSet() {
		return selection.getSelectedSet();
	}
	
	@Override
	public void setSelectedRow(SearchResultRow row) {
		clearSelection();
		if (row != null) {
			selection.setSelected(row, true);
		}
		updateSearchResultButtonStatus();
	}

	@Override
	public void showSearchResult(SearchResult result) {
		if (table == null) {
			table = new DeviceSearchResultTable(result.getDescs(), selection);
			table.setRangeChangeHandler(presenter);
			table.setClickHandler(presenter);
			table.load();
			resultPanel.add(table);
		}
		table.setData(result);
		if (table.getPageSize() != DevicePageSize.getPageSize()) {
            table.setPageSize(DevicePageSize.getPageSize());
            pageSizeList.setSelectedIndex(DevicePageSize.getPageSizeSelectedIndex());
        }
	}
	
	@Override
	public int getPageSize() {
		return table.getPageSize();
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

	@UiHandler("buttonAddServer")
	void onButtonAddServer(ClickEvent event) {
		if (buttonAddServer.isEnabled()) {
			presenter.onAddServer();
		}
	}
	
	@UiHandler("buttonDeleteServer")
	void onButtonDeleteServer(ClickEvent event) {
		if (buttonDeleteServer.isEnabled()) {
			presenter.onDeleteServer();
		}
	}
	
	@UiHandler("buttonModifyServer")
	void handleButtonModifyServer(ClickEvent event) {
		if (buttonModifyServer.isEnabled()) {
			presenter.onModifyServer();
		}
	}
	
	@UiHandler("buttonOperateServer")
	void handleButtonOperateServer(ClickEvent event) {
		if (buttonOperateServer.isEnabled()) {
			presenter.onOperateServer();
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
