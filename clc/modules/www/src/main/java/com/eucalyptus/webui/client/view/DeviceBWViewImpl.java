package com.eucalyptus.webui.client.view;

import java.util.ArrayList;
import java.util.Set;

import com.eucalyptus.webui.client.activity.device.DevicePageSize;
import com.eucalyptus.webui.client.service.SearchResult;
import com.eucalyptus.webui.client.service.SearchResultFieldDesc;
import com.eucalyptus.webui.client.service.SearchResultRow;
import com.eucalyptus.webui.shared.message.ClientMessage;
import com.eucalyptus.webui.shared.resource.device.CellTableColumns;
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

public class DeviceBWViewImpl extends Composite implements DeviceBWView {

    private static BWViewImplUiBinder uiBinder = GWT.create(BWViewImplUiBinder.class);

    interface BWViewImplUiBinder extends UiBinder<Widget, DeviceBWViewImpl> {
    }

    @UiField LayoutPanel resultPanel;
    @UiField Anchor buttonAddBWService;
    @UiField Anchor buttonDeleteBWService;
    @UiField Anchor buttonModifyBWService;
    @UiField Anchor buttonClearSelection;
    @UiField Anchor columnButton;
    @UiField ListBox pageSizeList;
    
    private Presenter presenter;
    private MultiSelectionModel<SearchResultRow> selection;
    private DeviceSearchResultTable table;
    private DevicePopupPanel popup = new DevicePopupPanel();
    
    public DeviceBWViewImpl() {
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
        buttonAddBWService.setEnabled(true);
        buttonDeleteBWService.setEnabled(size != 0 && presenter.canDeleteBWService());
        buttonModifyBWService.setEnabled(size == 1 && presenter.canModifyBWService());
        buttonClearSelection.setEnabled(size != 0);
    }
    
    public boolean isEmpty(String s) {
        return s == null || s.length() == 0;
    }
    
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
        addNode(descs, account, new ClientMessage("", "账户名称"), CellTableColumns.BW.ACCOUNT_NAME);
        addNode(descs, account, new ClientMessage("", "用户名称"), CellTableColumns.BW.USER_NAME);
        DeviceColumnPopupPanel.Node service = panel.addNode(new ClientMessage("", "服务信息"));
        addNode(descs, service, new ClientMessage("", "BW地址"), CellTableColumns.BW.IP_ADDR);
        addNode(descs, service, new ClientMessage("", "地址类型"), CellTableColumns.BW.IP_TYPE);
        addNode(descs, service, new ClientMessage("", "带宽上限(KB)"), CellTableColumns.BW.BW_SERVICE_BW_MAX);
        addNode(descs, service, new ClientMessage("", "带宽(KB)"), CellTableColumns.BW.BW_SERVICE_BW);
        addNode(descs, service, new ClientMessage("", "服务描述"), CellTableColumns.BW.BW_SERVICE_DESC);
        addNode(descs, service, new ClientMessage("", "开始时间"), CellTableColumns.BW.BW_SERVICE_STARTTIME);
        addNode(descs, service, new ClientMessage("", "结束时间"), CellTableColumns.BW.BW_SERVICE_ENDTIME);
        addNode(descs, service, new ClientMessage("", "剩余时间"), CellTableColumns.BW.BW_SERVICE_LIFE);
        addNode(descs, service, new ClientMessage("", "添加时间"), CellTableColumns.BW.BW_SERVICE_CREATIONTIME);
        addNode(descs, service, new ClientMessage("", "修改时间"), CellTableColumns.BW.BW_SERVICE_MODIFIEDTIME);
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
    
    @UiHandler("buttonAddBWService")
    void onButtonAddBWService(ClickEvent event) {
        if (buttonAddBWService.isEnabled()) {
            presenter.onAddBWService();
        }
    }
    
    @UiHandler("buttonDeleteBWService")
    void onButtonDeleteBWService(ClickEvent event) {
        if (buttonDeleteBWService.isEnabled()) {
            presenter.onDeleteBWService();
        }
    }
    
    @UiHandler("buttonModifyBWService")
    void handleButtonModifyBWService(ClickEvent event) {
        if (buttonModifyBWService.isEnabled()) {
            presenter.onModifyBWService();
        }
    }
    
    @UiHandler("buttonClearSelection")
    void handleButtonClearSelection(ClickEvent event) {
        if (buttonClearSelection.isEnabled()) {
            clearSelection();
        }
    }
    
}
