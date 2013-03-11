package com.eucalyptus.webui.client.view;

import java.util.ArrayList;
import java.util.Set;

import com.eucalyptus.webui.client.activity.device.DevicePageSize;
import com.eucalyptus.webui.client.service.SearchResult;
import com.eucalyptus.webui.client.service.SearchResultFieldDesc;
import com.eucalyptus.webui.client.service.SearchResultRow;
import com.eucalyptus.webui.shared.message.ClientMessage;
import com.eucalyptus.webui.shared.resource.device.CellTableColumns;
import com.eucalyptus.webui.shared.resource.device.status.IPState;
import com.eucalyptus.webui.shared.resource.device.status.IPType;
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

public class DeviceIPViewImpl extends Composite implements DeviceIPView {

    private static IPViewImplUiBinder uiBinder = GWT.create(IPViewImplUiBinder.class);

    interface IPViewImplUiBinder extends UiBinder<Widget, DeviceIPViewImpl> {
    }
    
    @UiField LayoutPanel resultPanel;
    @UiField Anchor buttonAddIPService;
    @UiField Anchor buttonDeleteIPService;
    @UiField Anchor buttonClearSelection;
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
    
    public DeviceIPViewImpl() {
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
                labels[i].setHTML(getLabel(state == states[i], prefix[i] + presenter.getCounts(states[i])));
            }
        }
    }
    
    private void updateSearchResultButtonStatus() {
        int size = selection.getSelectedSet().size();
        buttonAddIPService.setEnabled(true);
        buttonDeleteIPService.setEnabled(size != 0 && presenter.canDeleteIPService());
        buttonClearSelection.setEnabled(size != 0);
    }
    
    public boolean isEmpty(String s) {
        return s == null || s.length() == 0;
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
        addNode(descs, account, new ClientMessage("", "账户名称"), CellTableColumns.IP.ACCOUNT_NAME);
        addNode(descs, account, new ClientMessage("", "用户名称"), CellTableColumns.IP.USER_NAME);
        DeviceColumnPopupPanel.Node service = panel.addNode(new ClientMessage("", "服务信息"));
        addNode(descs, service, new ClientMessage("", "IP地址"), CellTableColumns.IP.IP_ADDR);
        addNode(descs, service, new ClientMessage("", "地址类型"), CellTableColumns.IP.IP_TYPE);
        addNode(descs, service, new ClientMessage("", "服务状态"), CellTableColumns.IP.IP_SERVICE_STATE);
        addNode(descs, service, new ClientMessage("", "服务描述"), CellTableColumns.IP.IP_SERVICE_DESC);
        addNode(descs, service, new ClientMessage("", "开始时间"), CellTableColumns.IP.IP_SERVICE_STARTTIME);
        addNode(descs, service, new ClientMessage("", "结束时间"), CellTableColumns.IP.IP_SERVICE_ENDTIME);
        addNode(descs, service, new ClientMessage("", "剩余时间"), CellTableColumns.IP.IP_SERVICE_LIFE);
        addNode(descs, service, new ClientMessage("", "添加时间"), CellTableColumns.IP.IP_SERVICE_CREATIONTIME);
        addNode(descs, service, new ClientMessage("", "修改时间"), CellTableColumns.IP.IP_SERVICE_MODIFIEDTIME);
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
    
    @UiHandler("buttonAddIPService")
    void onButtonAddIP(ClickEvent event) {
        if (buttonAddIPService.isEnabled()) {
            presenter.onAddIPService();
        }
    }
    
    @UiHandler("buttonDeleteIPService")
    void onButtonDeleteIPService(ClickEvent event) {
        if (buttonDeleteIPService.isEnabled()) {
            presenter.onDeleteIPService();
        }
    }
    
    @UiHandler("buttonClearSelection")
    void handleButtonClearSelection(ClickEvent event) {
        if (buttonClearSelection.isEnabled()) {
            clearSelection();
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
