package com.eucalyptus.webui.client.view;

import java.util.ArrayList;
import java.util.List;

import com.eucalyptus.webui.client.service.SearchRange;
import com.eucalyptus.webui.client.service.SearchResultFieldDesc;
import com.eucalyptus.webui.client.service.SearchResultFieldDesc.TableDisplay;
import com.eucalyptus.webui.client.service.SearchResultRow;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.CellTable.Resources;
import com.google.gwt.user.cellview.client.CellTable.Style;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.AsyncHandler;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.cellview.client.ColumnSortList.ColumnSortInfo;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.SimplePager.TextLocation;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;

public class DeviceMirrorSearchResultTable extends Composite {

	private static MirrorSearchResultTableUiBinder uiBinder = GWT.create(MirrorSearchResultTableUiBinder.class);

	interface MirrorSearchResultTableUiBinder extends UiBinder<Widget, DeviceMirrorSearchResultTable> {
	}
	
	public interface Presenter {
		
		void onMirrorSelectRow(SearchResultRow row);
		
		boolean updateRow(SearchResultRow row, SearchResultRow result);
		
	};

	public static interface TableResources extends Resources {
		@Source("SearchResultTable.css")
		Style cellTableStyle();
	}

	@UiField(provided = true)
	CellTable<SearchResultRow> cellTable;
	
	@UiField(provided = true)
	SimplePager pager;

	private ArrayList<SearchResultFieldDesc> fieldDescs;
	// Not all column are displayed in the table. This maps table column to data
	// field index.
	private final ArrayList<Integer> tableColIdx = new ArrayList<Integer>();
	
	private Presenter presenter;

	public DeviceMirrorSearchResultTable(int pageSize, ArrayList<SearchResultFieldDesc> fieldDescs, Presenter presenter) {
		this.fieldDescs = fieldDescs;
		this.presenter = presenter;

		buildTable(pageSize);
		buildPager();

		initWidget(uiBinder.createAndBindUi(this));
	}
	
	private List<SearchResultRow> dataCached;
	
	public void setData(List<SearchResultRow> data) {
		if (cellTable != null) {
			dataCached = data;
			cellTable.setRowCount(dataCached.size(), true);
			cellTable.setRowData(0, dataCached);
		}
	}
	
	private void handleRangeChange(SearchRange sr) {
		if (cellTable != null) {
			cellTable.setRowData(0, dataCached.subList(sr.getStart(), sr.getStart() + sr.getLength()));
		}
	}
	
	SingleSelectionModel<SearchResultRow> selection = new SingleSelectionModel<SearchResultRow>(SearchResultRow.KEY_PROVIDER);
	
	private void buildTable(int pageSize) {
		CellTable.Resources resources = GWT.create(TableResources.class);
		
		selection.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {

			@Override
            public void onSelectionChange(SelectionChangeEvent event) {
				SearchResultRow selected = selection.getSelectedObject();
				if (selected != null) {
					presenter.onMirrorSelectRow(selected);
				}
            }
			
		});
		
		cellTable = new CellTable<SearchResultRow>(pageSize, resources);
		cellTable.setSelectionModel(selection);
		cellTable.setWidth("100%", true);
		// Initialize columns
		for (int i = 0; i < this.fieldDescs.size(); i ++) {
			SearchResultFieldDesc desc = this.fieldDescs.get(i);
			if (desc.getTableDisplay() != TableDisplay.MANDATORY) {
				continue;
			}
			
			final int index = i;
			
			TextColumn<SearchResultRow> col = new TextColumn<SearchResultRow>() {
				@Override
				public String getValue(SearchResultRow data) {
					if (data == null) {
						return "";
					}
					else {
						return data.getField(index);
					}
				}
			};
			col.setSortable(false);
			cellTable.addColumn(col, desc.getTitle());
			cellTable.setColumnWidth(col, desc.getWidth());
			tableColIdx.add(i);
		}
	}
	
	private void buildPager() {
		SimplePager.Resources pagerResources = GWT.create(SimplePager.Resources.class);
		pager = new SimplePager(TextLocation.CENTER, pagerResources, false, 0, true);
		pager.setDisplay(cellTable);
	}

	public void load() {
		AsyncDataProvider<SearchResultRow> dataProvider = new AsyncDataProvider<SearchResultRow>() {
			@Override
			protected void onRangeChanged(HasData<SearchResultRow> display) {
				SearchRange sr = new SearchRange(-1);
				Range range = display.getVisibleRange();
				if (range != null) {
					sr.setStart(range.getStart());
					sr.setLength(range.getLength());
				}
				ColumnSortList sortList = cellTable.getColumnSortList();
				if (sortList != null && sortList.size() > 0) {
					ColumnSortInfo sort = sortList.get(0);
					if (sort != null) {
						sr.setSortField(tableColIdx.get(cellTable.getColumnIndex((Column<SearchResultRow, ?>)sort
						        .getColumn())));
						sr.setAscending(sort.isAscending());
					}
				}
				handleRangeChange(sr);
			}
		};
		dataProvider.addDataDisplay(cellTable);

		AsyncHandler sortHandler = new AsyncHandler(cellTable);
		cellTable.addColumnSortHandler(sortHandler);
	}
	
	public void clearSelection() {
		SearchResultRow selected = selection.getSelectedObject();
		if (selected != null) {
			selection.setSelected(selected, false);
		}
	}
	
	public void updateRow(SearchResultRow result) {
		if (dataCached != null) {
			for (SearchResultRow row : dataCached) {
				if (presenter.updateRow(row, result)) {
					cellTable.redraw();
					return ;
				}
			}
		}
	}

}
