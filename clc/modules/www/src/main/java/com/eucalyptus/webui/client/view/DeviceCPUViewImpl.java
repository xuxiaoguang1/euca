package com.eucalyptus.webui.client.view;

import java.util.Date;
import java.util.Set;

import com.eucalyptus.webui.client.service.SearchResult;
import com.eucalyptus.webui.client.service.SearchResultRow;
import com.eucalyptus.webui.client.view.DeviceDateBox.Handler;
import com.eucalyptus.webui.shared.resource.device.status.CPUState;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.LayoutPanel;
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
	
	private Presenter presenter;
	private MultiSelectionModel<SearchResultRow> selection;
	private DBSearchResultTable table;
	private DevicePopupPanel popup = new DevicePopupPanel();
	
	public DeviceCPUViewImpl() {
		initWidget(uiBinder.createAndBindUi(this));
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
		clearSelection();
		if (row != null) {
			selection.setSelected(row, true);
		}
		updateSearchResultButtonStatus();
	}
	
	@Override
	public void showSearchResult(SearchResult result) {
		if (table == null) {
			table = new DBSearchResultTable(result.getDescs(), selection);
			table.setRangeChangeHandler(presenter);
			table.setClickHandler(presenter);
			table.load();
			resultPanel.add(table);
		}
		table.setData(result);
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
