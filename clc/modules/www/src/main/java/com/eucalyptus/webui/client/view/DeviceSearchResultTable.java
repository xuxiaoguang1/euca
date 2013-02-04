package com.eucalyptus.webui.client.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.eucalyptus.webui.client.service.SearchRange;
import com.eucalyptus.webui.client.service.SearchResultFieldDesc;
import com.eucalyptus.webui.client.service.SearchResultFieldDesc.TableDisplay;
import com.eucalyptus.webui.client.service.SearchResultRow;
import com.eucalyptus.webui.client.service.SearchResult;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.dom.client.TableCellElement;
import com.google.gwt.dom.client.TableRowElement;
import com.google.gwt.dom.client.TableSectionElement;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.cellview.client.CellTable.Resources;
import com.google.gwt.user.cellview.client.CellTable.Style;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.AsyncHandler;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.cellview.client.ColumnSortList.ColumnSortInfo;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.SimplePager.TextLocation;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.CellPreviewEvent;
import com.google.gwt.view.client.CellPreviewEvent.Handler;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;
import com.google.gwt.view.client.SelectionModel;

public class DeviceSearchResultTable extends Composite {
	
	private static DeviceSearchResultTableUiBinder uiBinder = GWT.create(DeviceSearchResultTableUiBinder.class);
	
	interface DeviceSearchResultTableUiBinder extends UiBinder<Widget, DeviceSearchResultTable> {
	}
	
	public interface DeviceSearchResultTableClickHandler {
		
		public void onClick(SearchResultRow row, int row_index, int column_index);
		public void onDoubleClick(SearchResultRow row, int row_index, int column_index);
		public void onHover(SearchResultRow row, int row_index, int columin_index);
		
	}
	
	public static interface TableResources extends Resources {
		
		@Source("DeviceSearchResultTable.css")
		Style cellTableStyle();
		
	}
	
	private DeviceSearchResultTableClickHandler clickHandler = null;
	
	public void setClickHandler(DeviceSearchResultTableClickHandler clickHandler) {
		this.clickHandler = clickHandler;
	}
	
	private SearchRangeChangeHandler rangeChangeHandler = null;
	
	public void setRangeChangeHandler(SearchRangeChangeHandler rangeChangeHandler) {
		this.rangeChangeHandler = rangeChangeHandler;
	}
	
	private class MyCellTable extends CellTable<SearchResultRow> {
        
        public MyCellTable(int pageSize, CellTable.Resources resources) {
            super(pageSize, resources);
        }
        
        public TableSectionElement getHeadElement() {
            return this.getTableHeadElement();
        }
        
    }
	
	@UiField(provided = true)
	MyCellTable cellTable;
	
	@UiField(provided = true)
	SimplePager pager;
	
	private final ArrayList<Integer> tableColIdx = new ArrayList<Integer>();
	
	private static final int DEFAULT_PAGESIZE = 10;
	private static final int MIN_WIDTH = 40;
	private static final int MIN_TITLE_WIDTH = 80;
	
	private Map<Integer, ResizableHeader> resizableHeaders = new HashMap<Integer, ResizableHeader>();
	
	public static boolean isVisible(String width) {
		if (width != null) {
			if (!width.equals("0") && !width.equals("0px") && !width.equals("0%") && !width.equals("0EM")) {
				return true;
			}
		}
		return false;
	}
	
	public DeviceSearchResultTable(ArrayList<SearchResultFieldDesc> descs, final SelectionModel<SearchResultRow> selection) {
	    CellTable.Resources resources = GWT.create(TableResources.class);
		cellTable = new MyCellTable(DEFAULT_PAGESIZE, resources);
		cellTable.setWidth("100%", true);
		
		for (int i = 0; i < descs.size(); i ++ ) {
			final int index = i;
			final SearchResultFieldDesc desc = descs.get(index);
			if (desc.getTableDisplay() != TableDisplay.MANDATORY) {
				continue;
			}
			SearchResultFieldDesc.Type type = desc.getType();
            ResizableHeader header = new ResizableHeader(desc.getTitle(), index, isVisible(desc.getWidth()));
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
                column.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
				column.setSortable(false);
                cellTable.addColumn(column, header);
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
				cellTable.addColumn(column, header);
				cellTable.setColumnWidth(column, desc.getWidth());
				resizableHeaders.put(index, header);
			}
			tableColIdx.add(i);
		}
		cellTable.setSelectionModel(selection);
		
		cellTable.addCellPreviewHandler(new Handler<SearchResultRow>() {
			
			@Override
			public void onCellPreview(final CellPreviewEvent<SearchResultRow> event) {
				final DeviceSearchResultTableClickHandler handler = clickHandler;
				if (handler != null) {
					if ("click".equals(event.getNativeEvent().getType())) {
					    handler.onClick(event.getValue(), event.getIndex(), event.getColumn());
					}
					else if ("dblclick".equals(event.getNativeEvent().getType())) {
					    handler.onDoubleClick(event.getValue(), event.getIndex(), event.getColumn());
					}
				}
			}
			
		});
		
		cellTable.addCellPreviewHandler(new Handler<SearchResultRow>() {
			
			private Timer timer = null;

			@Override
			public void onCellPreview(final CellPreviewEvent<SearchResultRow> event) {
				final DeviceSearchResultTableClickHandler handler = clickHandler;
				if (handler != null) {
					String type = event.getNativeEvent().getType();
					if (timer != null) {
						timer.cancel();
						timer = null;
					}
					if ("mouseover".equals(type)) {
						timer = new Timer() {

							@Override
							public void run() {
								SearchResultRow row = event.getValue();
								int row_index = event.getIndex();
								int col_index = event.getColumn();
								handler.onHover(row, row_index, col_index);
							}
							
						};
						timer.schedule(1500);
					}
				}
			}
			
		});
		
		updateCellTableMinWidth();
		
		SimplePager.Resources pagerResources = GWT.create(SimplePager.Resources.class);
		pager = new SimplePager(TextLocation.CENTER, pagerResources, false, 0, true) {
			
			@Override
			public void previousPage() {
				setPage(getPage() - 1);
			}
			
			@Override
			public void nextPage() {
				setPage(getPage() + 1);
			}
			
			@Override
			public void firstPage() {
				setPageStart(0);
			}
			
			@Override
			public void lastPage() {
				lastPageStart();
			}
			
		};
		pager.setDisplay(cellTable);
		
		initWidget(uiBinder.createAndBindUi(this));
	}
	
	private void updateCellTableMinWidth() {
	    int counts = 0;
	    for (Map.Entry<Integer, ResizableHeader> entry : resizableHeaders.entrySet()) {
	        if (entry.getValue().isVisible()) {
	            counts ++;
            }
	    }
	    int minWidth = (counts + 1) * MIN_TITLE_WIDTH;
	    cellTable.getElement().getStyle().setProperty("minWidth", minWidth + "px");
    }
	
	public void setVisible(int column, boolean visible) {
	    ResizableHeader header = resizableHeaders.get(column);
	    if (header != null && header.isVisible() != visible) {
	        header.setVisible(visible);
	        updateCellTableMinWidth();
	    }
	}
	
    private class ResizableHeaderCell extends AbstractCell<String> {
        
        public ResizableHeaderCell() {
            super("click", "mousedown", "mousemove", "dblclick");
        }
        
        @Override
        public void render(Context context, String value, SafeHtmlBuilder sb) {
            sb.append(SafeHtmlUtils.fromString(value));
        }
        
    }
    
	private class ResizableHeader extends Header<String> {
	    
	    private String title = "";
	    private int column;
	    private boolean visible;
	    
	    public ResizableHeader(String title, int column, boolean visible) {
	        super(new ResizableHeaderCell());
	        if (title != null) {
	            this.title = title;
	        }
	        this.column = column;
	        this.visible = visible;
	    }
	    
	    @Override
	    public String getValue() {
	        return title;
	    }
	    
	    public String getInnerHTML() {
	        NodeList<TableCellElement> headers = cellTable.getHeadElement().getRows().getItem(0).getCells();
	        return headers.getItem(column).getInnerHTML();
	    }
	    
	    public boolean isVisible() {
	        return visible;
	    }
	    
	    public void setVisible(boolean visible) {
	        if (this.visible != visible) {
                this.visible = visible;
                resize();
	        }
	    }
	    
	    public void resize() {
	        int width = 0;
	        if (visible) {
	            List<String> htmls = new LinkedList<String>();
                htmls.add(getInnerHTML());
                int i = 0;
                TableRowElement row;
                while (i < cellTable.getVisibleItemCount() && (row = cellTable.getRowElement(i ++)) != null) {
                    htmls.add(row.getCells().getItem(column).getInnerHTML());
                }
                width = Math.max(MIN_WIDTH, DeviceMeasure.getMaxHTMLWidth(htmls));
	        }
	        cellTable.setColumnWidth(cellTable.getColumn(column), width + "px");
	    }
	    
	    @Override
	    public void onBrowserEvent(Context context, final Element target, NativeEvent event) {
	        String eventType = event.getType();
	        if (eventType.equals("mousemove")) {
	            new NativePreviewHandler() {
	                
	                private HandlerRegistration handler = Event.addNativePreviewHandler(this);
	                private boolean mousedown = false;
	                
	                private void removeHandler() {
	                    handler.removeHandler();
	                    target.getStyle().setCursor(Cursor.DEFAULT);
	                }

                    @Override
                    public void onPreviewNativeEvent(NativePreviewEvent event) {
                        NativeEvent nativeEvent = event.getNativeEvent();
                        nativeEvent.preventDefault();
                        nativeEvent.stopPropagation();
                        
                        String eventType = nativeEvent.getType();
                        int clientX = nativeEvent.getClientX();
                        if (eventType.equals("mousemove") && mousedown) {
                            int absoluteLeft = target.getAbsoluteLeft();
                            int width = Math.max(clientX - absoluteLeft, DeviceMeasure.getHTMLWidth(getInnerHTML()));
                            cellTable.setColumnWidth(cellTable.getColumn(column), Math.max(MIN_WIDTH, width) + "px");
                            return;
                        }
                        
                        if (eventType.equals("mousemove") || eventType.equals("mousedown")) {
                            int absoluteLeft = target.getAbsoluteLeft();
                            int offsetWidth = target.getOffsetWidth();
                            if (absoluteLeft + offsetWidth - clientX < Math.min(MIN_WIDTH, offsetWidth) / 2) { 
                                if (eventType.equals("mousedown")) {
                                    mousedown = true;
                                }
                                else {
                                    target.getStyle().setCursor(Cursor.COL_RESIZE);
                                }
                            }
                            else {
                                removeHandler();
                                return;
                            }
                        }
                        else if (eventType.equals("mouseup")) {
                            mousedown = false;
                        }
                        else if (eventType.equals("mouseout") && !mousedown) {
                            removeHandler();
                            return;
                        }
                        
                        if (eventType.equals("dblclick")) {
                            nativeEvent.preventDefault();
                            nativeEvent.stopPropagation();
                            resize();
                            removeHandler();
                        }
                    }
                    
	            };
	        }
	    }
	    
	}
		
	public void setPageSize(int pageSize) {
		if (cellTable != null) {
			cellTable.setPageSize(pageSize);
		}
	}
	
	public int getPageSize() {
		return cellTable.getPageSize();
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
				SearchRangeChangeHandler handler = rangeChangeHandler;
				if (handler != null) {
				    handler.handleRangeChange(sr);
				}
			}
			
		};
		dataProvider.addDataDisplay(cellTable);
		cellTable.addColumnSortHandler(new AsyncHandler(cellTable));
	}
	
	public void addDoublClickHandler(DoubleClickHandler handler) {
		cellTable.addDomHandler(handler, DoubleClickEvent.getType());
	}
		
}
