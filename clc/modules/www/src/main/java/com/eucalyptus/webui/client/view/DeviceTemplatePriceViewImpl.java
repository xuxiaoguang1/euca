package com.eucalyptus.webui.client.view;

import java.util.Date;
import java.util.Set;

import com.eucalyptus.webui.client.service.SearchResult;
import com.eucalyptus.webui.client.service.SearchResultRow;
import com.eucalyptus.webui.client.view.DeviceDateBox.Handler;
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

public class DeviceTemplatePriceViewImpl extends Composite implements DeviceTemplatePriceView {
	
	private static DeviceTemplatePriceViewImplUiBinder uiBinder = GWT.create(DeviceTemplatePriceViewImplUiBinder.class);
	
	interface DeviceTemplatePriceViewImplUiBinder extends UiBinder<Widget, DeviceTemplatePriceViewImpl> {
	}
	
	@UiField LayoutPanel resultPanel;
	@UiField Anchor buttonAddTemplatePrice;
	@UiField Anchor buttonDeleteTemplatePrice;
	@UiField Anchor buttonModifyTemplatePrice;
	@UiField Anchor buttonClearSelection;
	@UiField DeviceDateBox dateBegin;
    @UiField DeviceDateBox dateEnd;
	@UiField Anchor buttonClearDate;
	
	private Presenter presenter;
	private MultiSelectionModel<SearchResultRow> selection;
	private DeviceSearchResultTable table;
	private DevicePopupPanel popup = new DevicePopupPanel();
	
	public DeviceTemplatePriceViewImpl() {
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
	
	private void updateSearchResultButtonStatus() {
        int size = selection.getSelectedSet().size();
        buttonAddTemplatePrice.setEnabled(true);
        buttonDeleteTemplatePrice.setEnabled(size != 0 && presenter.canDeleteTemplatePrice());
        buttonModifyTemplatePrice.setEnabled(size == 1 && presenter.canModifyTemplatePrice());
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
	
	@UiHandler("buttonAddTemplatePrice")
	void handleButtonAdd(ClickEvent event) {
		if (buttonAddTemplatePrice.isEnabled()) {
			presenter.onAddTemplatePrice();
		}
	}
	
	@UiHandler("buttonDeleteTemplatePrice")
	void handleButtonDelete(ClickEvent event) {
		if (buttonDeleteTemplatePrice.isEnabled()) {
			presenter.onDeleteTemplatePrice();
		}
	}
	
	@UiHandler("buttonModifyTemplatePrice")
	void handleButtonModify(ClickEvent event) {
		if (buttonModifyTemplatePrice.isEnabled()) {
			presenter.onModifyTemplatePrice();
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
