package com.eucalyptus.webui.client.view;

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
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;

public class DeviceDiskViewImpl extends Composite implements DeviceDiskView {

	private static DiskViewImplUiBinder uiBinder = GWT.create(DiskViewImplUiBinder.class);

	interface DiskViewImplUiBinder extends UiBinder<Widget, DeviceDiskViewImpl> {
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

	Anchor[] labelList = {labelAll, labelReserved, labelInuse, labelStop};

	int numAll = 0;
	int numReserved = 0;
	int numInuse = 0;
	int numStop = 0;

	@UiHandler("labelAll")
	void handleLabelAll(ClickEvent event) {
		labelAll.setHTML("全部硬盘数量：<font color='red'>" + numAll + "</font> 台");
	}

	@UiHandler("labelReserved")
	void handleLabelReserved(ClickEvent event) {
		labelReserved.setHTML("预留硬盘数量：<font color='red'>" + numAll + "</font> 台");
	}

	@UiHandler("labelInuse")
	void handleLabelInuse(ClickEvent event) {
		labelInuse.setHTML("使用中硬盘数量：<font color='red'>" + numAll + "</font> 台");
	}

	@UiHandler("labelStop")
	void handleLabelStop(ClickEvent event) {
		labelStop.setHTML("未使用硬盘数量：<font color='red'>" + numAll + "</font> 台");
	}

	public DeviceDiskViewImpl() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	private Presenter presenter;

	private MultiSelectionModel<SearchResultRow> selection;

	private SearchResultTable table;

	@UiHandler("buttonAdd")
	void handleButtonAdd(ClickEvent event) {
		presenter.onAdd();
	}

	@UiHandler("buttonExtend")
	void handleButtonExtend(ClickEvent event) {
		presenter.onExtend();
	}

	@UiHandler("buttonModify")
	void handleButtonModify(ClickEvent event) {
		presenter.onModify();
	}

	@UiHandler("buttonDelete")
	void handleButtonDelete(ClickEvent event) {
		presenter.onDelete();
	}

	@Override
	public void showSearchResult(SearchResult result) {
		if (table == null) {
			resultPanel.clear();
			selection = new MultiSelectionModel<SearchResultRow>(SearchResultRow.KEY_PROVIDER);
			selection.addSelectionChangeHandler(new Handler() {

				@Override
				public void onSelectionChange(SelectionChangeEvent event) {
					System.err.println("OnSelectionChange");
				}

			});

			// int pageSize = presenter.getPageSize();
			table = new SearchResultTable(DEFAULT_PAGESIZE, result.getDescs(), presenter, selection);
			table.load();
			resultPanel.add(table);
		}
		table.setData(result);
	}

	private static final int DEFAULT_PAGESIZE = 12;

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

}
