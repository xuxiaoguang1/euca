package com.eucalyptus.webui.client.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.eucalyptus.webui.client.activity.DeviceCPUActivity.CPUState;
import com.eucalyptus.webui.client.service.SearchRange;
import com.eucalyptus.webui.client.service.SearchResult;
import com.eucalyptus.webui.client.service.SearchResultFieldDesc;
import com.eucalyptus.webui.client.service.SearchResultRow;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.user.client.ui.DeckPanel;

public class DeviceCPUViewImpl extends Composite implements DeviceCPUView {

	private static CPUViewImplUiBinder uiBinder = GWT.create(CPUViewImplUiBinder.class);

	interface CPUViewImplUiBinder extends UiBinder<Widget, DeviceCPUViewImpl> {
	}

	@UiField LayoutPanel resultPanel;
	@UiField Anchor labelAll;
	@UiField Anchor labelReserved;
	@UiField Anchor labelInuse;
	@UiField Anchor labelStop;
	
	private String getLabel(boolean highlight, String prefix, int value, String suffix) {
		StringBuilder sb = new StringBuilder();
		String color = isMirrorMode() ? "#AAAAAA" : highlight ? "red" : "darkblue";
		sb.append("<font color='").append(color).append("'>").append(prefix);
		sb.append(" ").append(value).append(" ");
		sb.append(suffix).append("</font>");
		return sb.toString();
	}
	
	private String[] prefixes = new String[]{"全部CPU数量：", "预留CPU数量：", "使用中CPU数量：", "未使用CPU数量："};
	private String[] suffixes = new String[]{"台", "台", "台", "台"};
	private CPUState[] states = new CPUState[]{null, CPUState.RESERVED, CPUState.INUSE, CPUState.STOP};
	
	@Override
	public void updateLabels() {
		CPUState state = presenter.getQueryState();
		Anchor[] labels = new Anchor[]{labelAll, labelReserved, labelInuse, labelStop};
		for (int i = 0; i < labels.length; i ++) {
			if (labels[i] != null) {
				labels[i].setHTML(getLabel(state == states[i], prefixes[i], presenter.getCounts(states[i]), suffixes[i]));
			}
		}
	}
	
	private void updatePanel() {
		updateLabels();
		if (isMirrorMode()) {
			if (table != null) {
				resultPanel.remove(table);
			}
			if (mirrorTable != null) {
				resultPanel.add(mirrorTable);
			}
			deckPanel.showWidget(1);
		}
		else {
			if (mirrorTable != null) {
				resultPanel.remove(mirrorTable);
			}
			if (table != null) {
				resultPanel.add(table);
			}
			deckPanel.showWidget(0);
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
	}

	private Presenter presenter;

	private MultiSelectionModel<SearchResultRow> selection = new MultiSelectionModel<SearchResultRow>(SearchResultRow.KEY_PROVIDER);

	private SearchResultTable table;
	private DeviceMirrorSearchResultTable mirrorTable;
	
	@Override
	public Set<SearchResultRow> getSelectedSet() {
		return selection.getSelectedSet();
	}
	
	@Override
	public void showSearchResult(SearchResult result) {
		if (table == null) {
			updatePanel();
			resultPanel.clear();
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
		table.cellTable.redraw();
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}
	
	@UiField Anchor buttonExtendService;
	@UiField DeckPanel deckPanel;
	
	@UiHandler("buttonDelService")
	void handleButtonDelService(ClickEvent event) {
		presenter.onDelService();
	}
	
	@UiHandler("buttonExtendService")
	void handleButtonExtendService(ClickEvent event) {
		presenter.onExtendService();
	}
	
	@UiHandler("clearSelection")
	void handleButtonClearSelection(ClickEvent event) {
		presenter.onClearSelection();
	}
	
	@UiHandler("mirrorBack")
	void handleMirrorCancel(ClickEvent event) {
		presenter.onMirrorCancel();
	}
	
	private boolean mirrorMode = false;
	private MirrorModeType mirrorModeType;
	
	@Override
	public boolean isMirrorMode() {
		return mirrorMode;
	}
	
	@Override
	public void openMirrorMode(MirrorModeType type, List<SearchResultRow> data) {
		assert(!isMirrorMode() && type != null);
		mirrorMode = true;
		mirrorModeType = type;
		updatePanel();
		mirrorTable.setData(data);
	}
	
	@Override
	public void closeMirrorMode() {
		assert(isMirrorMode());
		mirrorMode = false;
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

	@UiHandler("buttonAddDevice")
	void onButtonAddDeviceClick(ClickEvent event) {
	}
	@UiHandler("buttonModifyDevice")
	void onButtonModifyDeviceClick(ClickEvent event) {
	}
	@UiHandler("buttonDelDevice")
	void onButtonDelDeviceClick(ClickEvent event) {
	}
	@UiHandler("buttonAddService")
	void onButtonAddServiceClick(ClickEvent event) {
	}
}
