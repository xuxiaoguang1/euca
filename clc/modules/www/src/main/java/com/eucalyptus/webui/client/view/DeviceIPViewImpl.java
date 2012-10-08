package com.eucalyptus.webui.client.view;

import java.util.Date;
import java.util.Set;

import com.eucalyptus.webui.client.service.SearchResult;
import com.eucalyptus.webui.client.service.SearchResultRow;
import com.eucalyptus.webui.client.view.DeviceDateBox.Handler;
import com.eucalyptus.webui.shared.resource.device.status.IPState;
import com.eucalyptus.webui.shared.resource.device.status.IPType;
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

public class DeviceIPViewImpl extends Composite implements DeviceIPView {

	private static IPViewImplUiBinder uiBinder = GWT.create(IPViewImplUiBinder.class);

	interface IPViewImplUiBinder extends UiBinder<Widget, DeviceIPViewImpl> {
	}
	
	@UiField LayoutPanel resultPanel;
    @UiField Anchor buttonAddIP;
    @UiField Anchor buttonDeleteIP;
    @UiField Anchor buttonModifyIP;
    @UiField Anchor buttonAddIPService;
    @UiField Anchor buttonDeleteIPService;
    @UiField Anchor buttonModifyIPService;
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
    
    public DeviceIPViewImpl() {
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
    	final String[] prefix = {"全部IP地址数量： ", "预留IP地址数量： ", "使用中IP地址数量： ", "未使用IP地址数量： "};
        final IPState[] states = {null, IPState.RESERVED, IPState.INUSE, IPState.STOP};
        IPState state = presenter.getQueryState();
        Anchor[] labels = new Anchor[]{labelAll, labelReserved, labelInuse, labelStop};
        for (int i = 0; i < labels.length; i ++) {
            if (labels[i] != null) {
                double value = (double)presenter.getCounts(states[i]);
                labels[i].setHTML(getLabel(state == states[i], prefix[i] + value));
            }
        }
    }
    
    private void updateSearchResultButtonStatus() {
        int size = selection.getSelectedSet().size();
        buttonAddIP.setEnabled(true);
        buttonAddIPService.setEnabled(size == 1 && presenter.canAddIPService());
        buttonDeleteIP.setEnabled(size != 0 && presenter.canDeleteIP());
        buttonDeleteIPService.setEnabled(size != 0 && presenter.canDeleteIPService());
        buttonModifyIP.setEnabled(size == 1 && presenter.canModifyIP());
        buttonModifyIPService.setEnabled(size == 1 && presenter.canModifyIPService());
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
        presenter.setQueryState(IPState.STOP);
    }

    @UiHandler("labelInuse")
    void handleLabelInuse(ClickEvent event) {
        presenter.setQueryState(IPState.INUSE);
    }

    @UiHandler("labelReserved")
    void handleLabelStop(ClickEvent event) {
        presenter.setQueryState(IPState.RESERVED);
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
    
    @UiHandler("buttonAddIP")
    void onButtonAddIP(ClickEvent event) {
        if (buttonAddIP.isEnabled()) {
            presenter.onAddIP();
        }
    }
    
    @UiHandler("buttonAddIPService")
    void onButtonAddIPService(ClickEvent event) {
        if (buttonAddIPService.isEnabled()) {
            presenter.onAddIPService();
        }
    }
    
    @UiHandler("buttonDeleteIP")
    void onButtonDeleteIP(ClickEvent event) {
        if (buttonDeleteIP.isEnabled()) {
            presenter.onDeleteIP();
        }
    }
    
    @UiHandler("buttonDeleteIPService")
    void onButtonDeleteIPService(ClickEvent event) {
        if (buttonDeleteIPService.isEnabled()) {
            presenter.onDeleteIPService();
        }
    }
    
    @UiHandler("buttonModifyIP")
    void handleButtonModifyIP(ClickEvent event) {
        if (buttonModifyIP.isEnabled()) {
            presenter.onModifyIP();
        }
    }
    
    @UiHandler("buttonModifyIPService")
    void handleButtonModifyIPService(ClickEvent event) {
        if (buttonModifyIPService.isEnabled()) {
            presenter.onModifyIPService();
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
    
    @UiHandler("queryTypeAll")
	void handleQueryTypeAll(ClickEvent event) {
    	presenter.setQueryType(null);
    }
    
	@UiHandler("queryTypePublic")
	void handleQueryTypePublic(ClickEvent event) {
		presenter.setQueryType(IPType.PUBLIC);
	}
	
	@UiHandler("queryTypePrivate")
	void handleQueryTypePrivate(ClickEvent event) {
		presenter.setQueryType(IPType.PRIVATE);
	}

}
