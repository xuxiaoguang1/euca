package com.eucalyptus.webui.client.view;

import java.util.List;
import java.util.Set;

import com.eucalyptus.webui.client.activity.DeviceServerActivity.ServerState;
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

public class DeviceServerViewImpl extends Composite implements DeviceServerView {

	private static CPUViewImplUiBinder uiBinder = GWT.create(CPUViewImplUiBinder.class);

	interface CPUViewImplUiBinder extends UiBinder<Widget, DeviceServerViewImpl> {
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
		final String[] prefix = {"全部服务器数量： ", "停止中服务器数量： ", "使用中服务器数量： ", "故障中服务器数量： "};
		final String suffix = " 台";
		final ServerState[] states = {null, ServerState.STOP, ServerState.INUSE, ServerState.ERROR};
		ServerState state = presenter.getQueryState();
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
			case MODIFY_STATE:
				deckPanel.showWidget(1);
				break;
			case DELETE_DEVICE:
				deckPanel.showWidget(2);
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
		presenter.setQueryState(ServerState.STOP);
	}

	@UiHandler("labelInuse")
	void handleLabelInuse(ClickEvent event) {
		presenter.setQueryState(ServerState.INUSE);
	}

	@UiHandler("labelStop")
	void handleLabelStop(ClickEvent event) {
		presenter.setQueryState(ServerState.ERROR);
	}

	public DeviceServerViewImpl() {
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
	
	@UiHandler("buttonModifyServer")
	void handleButtonModifyState(ClickEvent event) {
		presenter.onModifyState();
	}

	@UiHandler("clearSelection")
	void handleButtonClearSelection(ClickEvent event) {
		presenter.onClearSelection();
	}
	
	@UiHandler("mirrorModifyStateBack")
	void handleMirrorModifyStateBack(ClickEvent event) {
		presenter.onMirrorBack();
	}

	@UiHandler("mirrorDeleteDeviceDeleteAll")
	void handleMirrorDeleteDeviceDeleteAll(ClickEvent event) {
		presenter.onMirrorDeleteAll();
	}
	
	@UiHandler("mirrorDeleteDeviceBack")
	void handleMirrorDeleteDeviceBack(ClickEvent event) {
		presenter.onMirrorBack();
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
