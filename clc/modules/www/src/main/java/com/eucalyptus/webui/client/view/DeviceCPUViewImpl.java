package com.eucalyptus.webui.client.view;

import java.util.List;
import java.util.Set;

import com.eucalyptus.webui.client.activity.device.DeviceCPUActivity.CPUState;
import com.eucalyptus.webui.client.service.SearchResult;
import com.eucalyptus.webui.client.service.SearchResultRow;
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
import com.google.gwt.user.client.ui.DeckPanel;

public class DeviceCPUViewImpl extends Composite implements DeviceCPUView {

	private static CPUViewImplUiBinder uiBinder = GWT.create(CPUViewImplUiBinder.class);

	interface CPUViewImplUiBinder extends UiBinder<Widget, DeviceCPUViewImpl> {
	}

	@UiField
	LayoutPanel resultPanel;
	@UiField
	Anchor labelAll;
	@UiField
	Anchor labelReserved;
	@UiField
	Anchor labelInuse;
	@UiField
	Anchor labelStop;

	private String getLabel(boolean highlight, String msg) {
		StringBuilder sb = new StringBuilder();
		String color = isMirrorMode() ? "#AAAAAA" : highlight ? "red" : "darkblue";
		sb.append("<font color='").append(color).append("'>").append(msg).append("</font>");
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

	private void updatePanel() {
		updateLabels();
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
			case ADD_SERVICE:
				deckPanel.showWidget(1);
				break;
			case MODIFY_SERVICE:
				deckPanel.showWidget(2);
				break;
			case DELETE_SERVICE:
				deckPanel.showWidget(3);
				break;
			case DELETE_DEVICE:
				deckPanel.showWidget(4);
				break;
			}
		}
	}

	@UiHandler("labelAll")
	void handleLabelAll(ClickEvent event) {
		presenter.setQueryState(null);
	}

	@UiHandler("labelReserved")
	void handleLabelReserved(ClickEvent event) {
		presenter.setQueryState(CPUState.RESERVED);
	}

	@UiHandler("labelInuse")
	void handleLabelInuse(ClickEvent event) {
		presenter.setQueryState(CPUState.INUSE);
	}

	@UiHandler("labelStop")
	void handleLabelStop(ClickEvent event) {
		presenter.setQueryState(CPUState.STOP);
	}

	public DeviceCPUViewImpl() {
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
	
	@UiHandler("buttonAddDevice")
	void onButtonAddDevice(ClickEvent event) {
		presenter.onAddDevice();
	}
	
	@UiHandler("buttonDeleteDevice")
	void onButtonDeleteDevice(ClickEvent event) {
		presenter.onDeleteDevice();
	}
	
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
	
	@UiHandler("mirrorAddServiceBack")
	void handleMirrorAddServiceBack(ClickEvent event) {
		presenter.onMirrorBack();
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
	
	@UiHandler("mirrorDeleteDeviceBack")
	void handleMirrorDeleteDeviceBack(ClickEvent event) {
		presenter.onMirrorBack();
	}
	
	@UiHandler("mirrorDeleteDeviceDeleteAll")
	void handleMirrorDeleteDeviceDeleteAll(ClickEvent event) {
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
