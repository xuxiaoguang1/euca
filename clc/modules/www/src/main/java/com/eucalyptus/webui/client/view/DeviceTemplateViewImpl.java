package com.eucalyptus.webui.client.view;

import java.util.Date;
import java.util.List;
import java.util.Set;

import com.eucalyptus.webui.client.service.SearchResult;
import com.eucalyptus.webui.client.service.SearchResultRow;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.user.client.ui.DeckPanel;

public class DeviceTemplateViewImpl extends Composite implements DeviceTemplateView {

	private static TemplateViewImplUiBinder uiBinder = GWT.create(TemplateViewImplUiBinder.class);

	interface TemplateViewImplUiBinder extends UiBinder<Widget, DeviceTemplateViewImpl> {
	}
	
	@UiField
	Label labelFrom;
	@UiField
	Label labelTo;

	@UiField
	LayoutPanel resultPanel;
	
	private DeviceDatePickerView picker = new DeviceDatePickerViewImpl(new DeviceDatePickerView.Presenter() {
		
		@Override
		public void onOK() {
			doSearch();
		}
		
		@Override
		public void onCancel() {
		}
		
	});

	private void updatePanel() {
		resultPanel.clear();
		if (!isMirrorMode()) {
			if (table != null) {
				resultPanel.add(table);
			}
			deckPanel.showWidget(0);
		}
		else {
			if (mirrorTable != null) {
				resultPanel.add(mirrorTable);
			}
			switch (getMirrorModeType()) {
			case MODIFY_TEMPLATE:
				deckPanel.showWidget(1);
				break;
			case DELETE_TEMPLATE:
				deckPanel.showWidget(2);
				break;
			}
		}
	}

	public DeviceTemplateViewImpl() {
		initWidget(uiBinder.createAndBindUi(this));
		selection = new MultiSelectionModel<SearchResultRow>(SearchResultRow.KEY_PROVIDER);
	}

	private Presenter presenter;

	private MultiSelectionModel<SearchResultRow> selection;

	private SearchResultTable table;
	private DeviceMirrorSearchResultTable mirrorTable;

	@Override
	public Set<SearchResultRow> getSelectedSet() {
		return selection.getSelectedSet();
	}

	@Override
	public void showSearchResult(SearchResult result) {
		if (table == null) {
			// int pageSize = presenter.getPageSize();
			table = new SearchResultTable(DEFAULT_PAGESIZE, result.getDescs(), presenter, selection);
			table.load();
			updatePanel();
		}
		if (mirrorTable == null) {
			mirrorTable = new DeviceMirrorSearchResultTable(DEFAULT_PAGESIZE, result.getDescs(), presenter);
			mirrorTable.load();
			updatePanel();
		}
		table.setData(result);
	}

	@Override
	public void clear() {
		resultPanel.clear();
		table = null;
		mirrorTable = null;
	}

	@Override
	public void clearSelection() {
		selection.clear();
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	@UiField
	DeckPanel deckPanel;
	
	@UiHandler("buttonAddService")
	void handleButtonAddService(ClickEvent event) {
		presenter.onAddTemplate();
	}

	@UiHandler("buttonModifyService")
	void handleButtonModifyService(ClickEvent event) {
		presenter.onModifyTemplate();
	}

	@UiHandler("buttonDeleteService")
	void handleButtonDeleteService(ClickEvent event) {
		presenter.onDeleteTemplate();
	}

	@UiHandler("clearSelection")
	void handleButtonClearSelection(ClickEvent event) {
		presenter.onClearSelection();
	}
	
	@UiHandler("mirrorModifyServiceBack")
	void handleMirrorModifyServiceBack(ClickEvent event) {
		presenter.onMirrorBack();
	}
	
	@UiHandler("mirrorDeleteServiceBack")
	void handleMirrorDeleteServiceBack(ClickEvent event) {
		presenter.onMirrorBack();
	}
	
	@UiHandler("mirrorDeleteServiceDeleteAll")
	void handleMirrorDeleteServiceDeleteAll(ClickEvent event) {
		presenter.onMirrorDeleteAll();
	}
	
	@UiHandler("setDateFrom")
	void handleSetDateFrom(ClickEvent event) {
		if (!isMirrorMode()) {
			picker.setValue(labelFrom);
		}
	}
	
	@UiHandler("clearFrom")
	void handleClearFrom(ClickEvent event) {
		if (!isMirrorMode()) {
			labelFrom.setText("");
			doSearch();
		}
	}
	
	@UiHandler("setDateTo")
	void handleSetDateTo(ClickEvent event) {
		if (!isMirrorMode()) {
			picker.setValue(labelTo);
		}
	}
	
	@UiHandler("clearTo")
	void handleClearTo(ClickEvent event) {
		if (!isMirrorMode()) {
			labelTo.setText("");
			doSearch();
		}
	}
	
	void doSearch() {
		Date starttime = DeviceServiceDatePicker.parse(labelFrom.getText());
		Date endtime = DeviceServiceDatePicker.parse(labelTo.getText());
		presenter.onSearch(starttime, endtime);
	}
	
	private MirrorModeType mirrorModeType = null;

	@Override
	public boolean isMirrorMode() {
		return mirrorModeType != null;
	}

	@Override
	public void openMirrorMode(MirrorModeType type, List<SearchResultRow> data) {
		assert (!isMirrorMode() && type != null);
		clearSelection();
		mirrorModeType = type;
		mirrorTable.setData(data);
		updatePanel();
	}

	@Override
	public void closeMirrorMode() {
		assert (isMirrorMode());
		mirrorModeType = null;
		mirrorTable.clearSelection();
		updatePanel();
	}

	@Override
	public DeviceMirrorSearchResultTable getMirrorTable() {
		return mirrorTable;
	}

	@Override
	public MirrorModeType getMirrorModeType() {
		return mirrorModeType;
	}

}
