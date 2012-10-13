package com.eucalyptus.webui.client.view;

import java.util.Date;
import java.util.Set;

import com.eucalyptus.webui.client.service.SearchResult;
import com.eucalyptus.webui.client.service.SearchResultRow;
import com.eucalyptus.webui.client.view.DeviceDateBox.Handler;
import com.eucalyptus.webui.shared.resource.device.status.MemoryState;
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

public class DeviceMemoryViewImpl extends Composite implements DeviceMemoryView {

	private static MemoryViewImplUiBinder uiBinder = GWT.create(MemoryViewImplUiBinder.class);

	interface MemoryViewImplUiBinder extends UiBinder<Widget, DeviceMemoryViewImpl> {
	}
	
	@UiField LayoutPanel resultPanel;
    @UiField Anchor buttonAddMemory;
    @UiField Anchor buttonDeleteMemory;
    @UiField Anchor buttonModifyMemory;
    @UiField Anchor buttonAddMemoryService;
    @UiField Anchor buttonDeleteMemoryService;
    @UiField Anchor buttonModifyMemoryService;
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
    private DeviceSearchResultTable table;
    private DevicePopupPanel popup = new DevicePopupPanel();
    
    public DeviceMemoryViewImpl() {
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
    
    private static final long MEMORY_UNIT = 1024;
    
    private double format(double value) {
        return (double)(int)(value * 1000) / 1000;
    }

    @Override
    public void updateLabels() {
        final String[] prefix = {"全部内存数量： ", "预留内存数量： ", "使用中内存数量： ", "未使用内存数量： "};
        final String suffix = " GB";
        final MemoryState[] states = {null, MemoryState.RESERVED, MemoryState.INUSE, MemoryState.STOP};
        MemoryState state = presenter.getQueryState();
        Anchor[] labels = new Anchor[]{labelAll, labelReserved, labelInuse, labelStop};
        for (int i = 0; i < labels.length; i ++) {
            if (labels[i] != null) {
                double value = format((double)presenter.getCounts(states[i]) / MEMORY_UNIT);
                labels[i].setHTML(getLabel(state == states[i], prefix[i] + value + suffix));
            }
        }
    }
    
    private void updateSearchResultButtonStatus() {
        int size = selection.getSelectedSet().size();
        buttonAddMemory.setEnabled(true);
        buttonAddMemoryService.setEnabled(size == 1 && presenter.canAddMemoryService());
        buttonDeleteMemory.setEnabled(size != 0 && presenter.canDeleteMemory());
        buttonDeleteMemoryService.setEnabled(size != 0 && presenter.canDeleteMemoryService());
        buttonModifyMemory.setEnabled(size == 1 && presenter.canModifyMemory());
        buttonModifyMemoryService.setEnabled(size == 1 && presenter.canModifyMemoryService());
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
        presenter.setQueryState(MemoryState.STOP);
    }

    @UiHandler("labelInuse")
    void handleLabelInuse(ClickEvent event) {
        presenter.setQueryState(MemoryState.INUSE);
    }

    @UiHandler("labelReserved")
    void handleLabelStop(ClickEvent event) {
        presenter.setQueryState(MemoryState.RESERVED);
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
    
    @UiHandler("buttonAddMemory")
    void onButtonAddMemory(ClickEvent event) {
        if (buttonAddMemory.isEnabled()) {
            presenter.onAddMemory();
        }
    }
    
    @UiHandler("buttonAddMemoryService")
    void onButtonAddMemoryService(ClickEvent event) {
        if (buttonAddMemoryService.isEnabled()) {
            presenter.onAddMemoryService();
        }
    }
    
    @UiHandler("buttonDeleteMemory")
    void onButtonDeleteMemory(ClickEvent event) {
        if (buttonDeleteMemory.isEnabled()) {
            presenter.onDeleteMemory();
        }
    }
    
    @UiHandler("buttonDeleteMemoryService")
    void onButtonDeleteMemoryService(ClickEvent event) {
        if (buttonDeleteMemoryService.isEnabled()) {
            presenter.onDeleteMemoryService();
        }
    }
    
    @UiHandler("buttonModifyMemory")
    void handleButtonModifyMemory(ClickEvent event) {
        if (buttonModifyMemory.isEnabled()) {
            presenter.onModifyMemory();
        }
    }
    
    @UiHandler("buttonModifyMemoryService")
    void handleButtonModifyMemoryService(ClickEvent event) {
        if (buttonModifyMemoryService.isEnabled()) {
            presenter.onModifyMemoryService();
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
