package com.eucalyptus.webui.client.view;

import java.util.Set;

import com.eucalyptus.webui.client.activity.device.DevicePageSize;
import com.eucalyptus.webui.client.service.SearchResult;
import com.eucalyptus.webui.client.service.SearchResultRow;
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

public class DeviceCabinetViewImpl extends Composite implements DeviceCabinetView {
    
    private static DeviceCabinetViewImplUiBinder uiBinder = GWT.create(DeviceCabinetViewImplUiBinder.class);
    
    interface DeviceCabinetViewImplUiBinder extends UiBinder<Widget, DeviceCabinetViewImpl> {
    }
    
    @UiField LayoutPanel resultPanel;
    @UiField Anchor buttonAdd;
    @UiField Anchor buttonDelete;
    @UiField Anchor buttonModify;
    @UiField Anchor buttonClearSelection;
    @UiField ListBox pageSizeList;
    
    private Presenter presenter;
    private MultiSelectionModel<SearchResultRow> selection;
    private DeviceSearchResultTable table;
    private DevicePopupPanel popup = new DevicePopupPanel();
    
    public DeviceCabinetViewImpl() {
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
    }
    
    private void updateSearchResultButtonStatus() {
        int size = selection.getSelectedSet().size();
        buttonAdd.setEnabled(true);
        buttonDelete.setEnabled(size != 0);
        buttonModify.setEnabled(size == 1);
        buttonClearSelection.setEnabled(size != 0);
    }
    
    public boolean isEmpty(String s) {
        return s == null || s.length() == 0;
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
    public void clearSelection() {
        selection.clear();
    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }
    
    @UiHandler("buttonAdd")
    void handleButtonAdd(ClickEvent event) {
        if (buttonAdd.isEnabled()) {
            presenter.onAdd();
        }
    }
    
    @UiHandler("buttonDelete")
    void handleButtonDelete(ClickEvent event) {
        if (buttonDelete.isEnabled()) {
            presenter.onDelete();
        }
    }
    
    @UiHandler("buttonModify")
    void handleButtonModify(ClickEvent event) {
        if (buttonModify.isEnabled()) {
            presenter.onModify();
        }
    }
    
    @UiHandler("buttonClearSelection")
    void handleButtonClearSelection(ClickEvent event) {
        if (buttonClearSelection.isEnabled()) {
            clearSelection();
        }
    }
    
}
