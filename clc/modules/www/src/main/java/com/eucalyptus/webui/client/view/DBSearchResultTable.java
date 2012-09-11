package com.eucalyptus.webui.client.view;

import java.util.ArrayList;
import com.eucalyptus.webui.client.service.SearchRange;
import com.eucalyptus.webui.client.service.SearchResultFieldDesc;
import com.eucalyptus.webui.client.service.SearchResultFieldDesc.TableDisplay;
import com.eucalyptus.webui.client.service.SearchResultRow;
import com.eucalyptus.webui.client.service.SearchResult;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
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
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.CellPreviewEvent;
import com.google.gwt.view.client.CellPreviewEvent.Handler;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;
import com.google.gwt.view.client.SelectionModel;

public class DBSearchResultTable extends Composite {
	
	private static DBSearchResultTableUiBinder uiBinder = GWT.create(DBSearchResultTableUiBinder.class);
	
	interface DBSearchResultTableUiBinder extends UiBinder<Widget, DBSearchResultTable> {
	}
	
	public interface DBSearchResultTableClickHandler {
		
		public void onClick(SearchResultRow row, int row_index, int column_index);
		public void onDoubleClick(SearchResultRow row, int row_index, int column_index);
		
	}
	
	public static interface TableResources extends Resources {
		
		@Source("DBSearchResultTable.css")
		Style cellTableStyle();
		
	}
	
	private DBSearchResultTableClickHandler clickHandler = null;
	private SearchRangeChangeHandler rangeChangeHandler = null;
	
	public void setClickHandler(DBSearchResultTableClickHandler clickHandler) {
		this.clickHandler = clickHandler;
	}
	
	public void setRangeChangeHandler(SearchRangeChangeHandler rangeChangeHandler) {
		this.rangeChangeHandler = rangeChangeHandler;
	}
	
	@UiField(provided = true)
	CellTable<SearchResultRow> cellTable;
	
	@UiField(provided = true)
	SimplePager pager;
	
	private final ArrayList<Integer> tableColIdx = new ArrayList<Integer>();
	
	public DBSearchResultTable(int pageSize, ArrayList<SearchResultFieldDesc> descs, final SelectionModel<SearchResultRow> selection) {
		CellTable.Resources resources = GWT.create(TableResources.class);
		cellTable = new CellTable<SearchResultRow>(pageSize, resources);
		cellTable.setWidth("100%", true);
		for (int i = 0; i < descs.size(); i ++ ) {
			final int index = i;
			SearchResultFieldDesc desc = descs.get(index);
			if (desc.getTableDisplay() != TableDisplay.MANDATORY) {
				continue;
			}
			SearchResultFieldDesc.Type type = desc.getType();
			if (type == SearchResultFieldDesc.Type.BOOLEAN) {
				Column<SearchResultRow, Boolean> column = new Column<SearchResultRow, Boolean>(new CheckboxCell(false, true)) {
					
					@Override
					public Boolean getValue(SearchResultRow row) {
						if (row == null) {
							return false;
						}
						return selection.isSelected(row);
					}
				};
				
				column.setFieldUpdater(new FieldUpdater<SearchResultRow, Boolean>() {
					
					@Override
					public void update(int index, SearchResultRow object, Boolean value) {
						selection.setSelected(object, value);
					}
					
				});
				column.setSortable(false);
				cellTable.addColumn(column, desc.getTitle());
				cellTable.setColumnWidth(column, desc.getWidth());
			}
			else {
				Column<SearchResultRow, String> column = new TextColumn<SearchResultRow> () {

					@Override
					public String getValue(SearchResultRow row) {
						String value;
						if (row == null || (value = row.getField(index)) == null) {
							return "";
						}
						return value;
					}
					
				};
				column.setSortable(desc.getSortable());
				cellTable.addColumn(column, desc.getTitle());
				cellTable.setColumnWidth(column, desc.getWidth());
			}
			tableColIdx.add(i);
		}
		cellTable.setSelectionModel(selection);
		
		cellTable.addCellPreviewHandler(new Handler<SearchResultRow>() {
			
			private volatile int counter = 0;
			
			@Override
			public void onCellPreview(final CellPreviewEvent<SearchResultRow> event) {
				if ("click".equals(event.getNativeEvent().getType())) {
					final DBSearchResultTableClickHandler handler = clickHandler;
					if (handler != null && counter ++ == 0) {
						new Timer() {
	
							@Override
							public void run() {
								if (counter == 0) {
									return ;
								}
								SearchResultRow row = event.getValue();
								int row_index = event.getIndex();
								int col_index = event.getColumn();
								if (counter == 1) {
									handler.onClick(row, row_index, col_index);
								}
								else {
									handler.onDoubleClick(row, row_index, col_index);
								}
								counter = 0;
							}
							
						}.schedule(150);
					}
				}
			}
			
		});
		
		SimplePager.Resources pagerResources = GWT.create(SimplePager.Resources.class);
		pager = new SimplePager(TextLocation.CENTER, pagerResources, false, 0, true);
		pager.setDisplay(cellTable);
		
		initWidget(uiBinder.createAndBindUi(this));
	}
	
	public void setData(SearchResult data) {
		if (cellTable != null) {
			cellTable.setRowCount(data.getTotalSize(), true);
			cellTable.setRowData(data.getRange().getStart(), data.getRows());
		}
	}
	
	public void load() {
		AsyncDataProvider<SearchResultRow> dataProvider = new AsyncDataProvider<SearchResultRow>() {
			
			@SuppressWarnings("unchecked")
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
						sr.setSortField(tableColIdx.get(cellTable.getColumnIndex((Column<SearchResultRow, ?>) sort.getColumn())));
						sr.setAscending(sort.isAscending());
					}
				}
				rangeChangeHandler.handleRangeChange(sr);
			}
			
		};
		dataProvider.addDataDisplay(cellTable);
		cellTable.addColumnSortHandler(new AsyncHandler(cellTable));
	}
	
	public void addDoublClickHandler(DoubleClickHandler handler) {
		cellTable.addDomHandler(handler, DoubleClickEvent.getType());
	}
	
}
