package com.eucalyptus.webui.client.view;

import java.util.ArrayList;
import java.util.List;

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
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.SimplePager.TextLocation;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;

public class DeviceMirrorSearchResultTable extends Composite {
	
	public interface SearchResultRowMatcher {
		
		public boolean match(SearchResultRow row0, SearchResultRow row1);
		
	}

	private static MirrorSearchResultTableUiBinder uiBinder = GWT.create(MirrorSearchResultTableUiBinder.class);

	interface MirrorSearchResultTableUiBinder extends UiBinder<Widget, DeviceMirrorSearchResultTable> {
	}

	public interface Presenter {

		void onMirrorSelectRow(SearchResultRow row);

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
	private int pageSize;

	public void setData(List<SearchResultRow> data) {
		if (cellTable != null) {
			dataCached = data;
			cellTable.setRowCount(dataCached.size(), true);
			int start = 0, length = Math.min(dataCached.size(), pageSize);
			cellTable.setVisibleRangeAndClearData(new Range(start, length), true);
		}
	}

	SingleSelectionModel<SearchResultRow> selection = new SingleSelectionModel<SearchResultRow>(
	        SearchResultRow.KEY_PROVIDER);

	private boolean doubleClickMode = true;

	public void setDoubleClick() {
		doubleClickMode = true;
	}

	public void setSingleClick() {
		doubleClickMode = false;
	}

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
		
		this.pageSize = pageSize;

		cellTable = new CellTable<SearchResultRow>(pageSize, resources) {

			private long last = 0;

			protected void onBrowserEvent2(Event event) {
				if (doubleClickMode) {
					if (event.getType().equals("click")) {
						long time = System.currentTimeMillis();
						if (time - last >= 200) {
							last = time;
							return;
						}
						last = 0;
					}
				}
				super.onBrowserEvent2(event);
			}

		};
		cellTable.setSelectionModel(selection);
		cellTable.setWidth("100%", true);
		// Initialize columns
		for (int i = 0; i < this.fieldDescs.size(); i ++) {
			SearchResultFieldDesc desc = this.fieldDescs.get(i);
			if (desc.getTableDisplay() != TableDisplay.MANDATORY) {
				continue;
			}

			final int index = i;

			Column<SearchResultRow, String> col = new TextColumn<SearchResultRow>() {

				@Override
				public String getValue(SearchResultRow data) {
					if (data == null) {
						return "";
					}
					return data.getField(index);
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
	
	private Range getLegalRange(Range range) {
		int start, length, totalSize = dataCached.size();
		if (range == null) {
			start = 0;
			length = Math.min(pageSize, totalSize);
		}
		else {
			start = range.getStart();
			length = range.getLength();
			if (start >= totalSize) {
				length = Math.min(pageSize, totalSize);
				start = totalSize - length;
			}
			else if (start + length > totalSize) {
				length = totalSize - start;
			}
			else {
				length = Math.min(pageSize, totalSize - start);
			}
		}
		return new Range(start, length);
	}
	
	public void load() {
		AsyncDataProvider<SearchResultRow> dataProvider = new AsyncDataProvider<SearchResultRow>() {
			
			@Override
			protected void onRangeChanged(HasData<SearchResultRow> display) {
				Range range = getLegalRange(display.getVisibleRange());
				int start = range.getStart(), length = range.getLength();
				cellTable.setRowData(start, dataCached.subList(start, start + length));
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

	public void updateRow(SearchResultRow result, SearchResultRowMatcher matcher) {
		if (dataCached != null) {
			for (SearchResultRow row : dataCached) {
				if (matcher.match(row, result)) {
					row.getRow().clear();
					row.getRow().addAll(result.getRow());
					redraw();
					return;
				}
			}
		}
	}

	public void deleteRow(SearchResultRow result, SearchResultRowMatcher matcher) {
		if (dataCached != null) {
			for (SearchResultRow row : dataCached) {
				if (matcher.match(result, row)) {
					dataCached.remove(row);
					redraw();
					return;
				}
			}
		}
	}
	
	private void redraw() {
		Range range = getLegalRange(cellTable.getVisibleRange());
		cellTable.setRowCount(dataCached.size(), true);
		cellTable.setVisibleRangeAndClearData(range, true);
	}
	
	public List<SearchResultRow> getData() {
		return dataCached;
	}

}
