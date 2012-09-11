package com.eucalyptus.webui.client.view.device;

import java.util.List;
import java.util.Set;

import com.eucalyptus.webui.client.service.SearchResult;
import com.eucalyptus.webui.client.service.SearchResultRow;
import com.eucalyptus.webui.client.view.SearchResultTable;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.user.client.ui.DeckPanel;

public class DeviceBWViewImpl extends Composite implements DeviceBWView {

	private static BWViewImplUiBinder uiBinder = GWT.create(BWViewImplUiBinder.class);

	interface BWViewImplUiBinder extends UiBinder<Widget, DeviceBWViewImpl> {
	}

	@UiField
	LayoutPanel resultPanel;

	private void updatePanel() {
		if (!isMirrorMode()) {
			if (mirrorTable != null) {
				resultPanel.remove(mirrorTable);
			}
			if (table != null) {
				resultPanel.add(table);
			}
			deckPanel.showWidget(0);
		}
		else {
			if (table != null) {
				resultPanel.remove(table);
			}
			if (mirrorTable != null) {
				resultPanel.add(mirrorTable);
			}
			switch (getMirrorModeType()) {
			case MODIFY_SERVICE:
				deckPanel.showWidget(1);
				break;
			case DELETE_SERVICE:
				deckPanel.showWidget(2);
				break;
			}
		}
	}

	public DeviceBWViewImpl() {
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
		presenter.onAddService();
	}

	@UiHandler("buttonModifyService")
	void handleButtonModifyService(ClickEvent event) {
		presenter.onModifyService();
	}

	@UiHandler("buttonDeleteService")
	void handleButtonDeleteService(ClickEvent event) {
		presenter.onDeleteService();
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
