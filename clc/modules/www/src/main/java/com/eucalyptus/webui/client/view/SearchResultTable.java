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
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;
import com.google.gwt.view.client.SelectionModel;

public class SearchResultTable extends Composite {

  private static SearchResultTableUiBinder uiBinder = GWT.create( SearchResultTableUiBinder.class );
  
  interface SearchResultTableUiBinder extends UiBinder<Widget, SearchResultTable> {}

  public static interface TableResources extends Resources {
    @Source( "SearchResultTable.css" )
    Style cellTableStyle( );
  }
  
  SearchTableCellClickHandler cellClickHandler = null;
  public void setCellClickHandler(SearchTableCellClickHandler handler) {
	  cellClickHandler = handler;
  }
  
  @UiField( provided = true )
  CellTable<SearchResultRow> cellTable;
  
  @UiField( provided = true )
  SimplePager pager;

  private ArrayList<SearchResultFieldDesc> fieldDescs;
  
  public ArrayList<SearchResultFieldDesc> getFieldDescs() {
	  return fieldDescs;
  }
  
  private final SearchRangeChangeHandler changeHandler;
  private SelectionModel<SearchResultRow> selectionModel;
  // Not all column are displayed in the table. This maps table column to data field index.
  private final ArrayList<Integer> tableColIdx = new ArrayList<Integer>( );
  
  public SearchResultTable( int pageSize, ArrayList<SearchResultFieldDesc> fieldDescs, SearchRangeChangeHandler changeHandler, SelectionModel<SearchResultRow> selectionModel ) {
    this.changeHandler = changeHandler;
    this.fieldDescs = fieldDescs;
    this.selectionModel = selectionModel;
    
    buildTable( pageSize );
    buildPager( );
    
    initWidget( uiBinder.createAndBindUi( this ) );
  }
  
  public void setData( SearchResult data ) {    
    if ( cellTable != null ) {
      cellTable.setRowCount( data.getTotalSize( ), true );
      //cellTable.setVisibleRange( data.getStart( ), data.getLength( ) );
      cellTable.setRowData( data.getRange( ).getStart( ), data.getRows( ) );
    }
  }
  
  private void buildTable( int pageSize ) {
    CellTable.Resources resources = GWT.create( TableResources.class );
    
    cellTable = new CellTable<SearchResultRow>( pageSize, resources );
    cellTable.setWidth( "100%", true );
    // Initialize columns
    for ( int i = 0; i < this.fieldDescs.size( ); i++ ) {
      SearchResultFieldDesc desc = this.fieldDescs.get( i );
      if ( desc.getTableDisplay( ) != TableDisplay.MANDATORY ) {
        continue;
      }
      SearchResultFieldDesc.Type colType = desc.getType();
      
      final int index = i;
      
      if (colType == SearchResultFieldDesc.Type.BOOLEAN) {
    	  Column<SearchResultRow, Boolean> checkBoxColumn = new Column<SearchResultRow, Boolean>(new CheckboxCell(false, true)) {
    		  @Override
    		  public Boolean getValue(SearchResultRow object) {
    			  if (object == null)
    				  return null;
    			  
    			  return selectionModel.isSelected(object);
    		  }
    	  };
    	  
    	  checkBoxColumn.setFieldUpdater(new FieldUpdater<SearchResultRow, Boolean> () {

				@Override
				public void update(int index, SearchResultRow object, Boolean value) {
					// TODO Auto-generated method stub
					selectionModel.setSelected(object, value);
				}
			}
    	  );
    	  
    	  checkBoxColumn.setSortable(false);
	      cellTable.addColumn( checkBoxColumn, desc.getTitle( ) );
      
	      cellTable.setColumnWidth(checkBoxColumn, desc.getWidth());
      }
      else if (colType == SearchResultFieldDesc.Type.LINK) {
        SearchTableClickableCell preview = new SearchTableClickableCell();
    	  preview.setColIndex(index);
    	  final Column<SearchResultRow, String> linkColumn = new Column<SearchResultRow, String>(preview) {
    	    public String getValue(SearchResultRow object) {
    	      //hack display content
    	      if (object.getField(index).startsWith("keypair"))
    	        return "下载";
    	      else
    	        return object.getField(index);
    	    }
    	  };
    	  
    	  linkColumn.setFieldUpdater(new FieldUpdater<SearchResultRow, String>() {
    		  @Override
    		  public void update(int index, SearchResultRow object, String value) {
    		    // The user clicked on the button for the passed auction.
    			if (cellClickHandler != null) {
    				int colIndex = ((SearchTableClickableCell)linkColumn.getCell()).getColIndex();
    				cellClickHandler.onClick(index, colIndex, object);
    			}
    		  }
    		});
    	  
    	  linkColumn.setSortable(false);
	      cellTable.addColumn( linkColumn, desc.getTitle( ) );
      
	      cellTable.setColumnWidth(linkColumn, desc.getWidth());
      }
      else {
	      TextColumn<SearchResultRow> col = new TextColumn<SearchResultRow>( ) {
		      @Override
		      public String getValue( SearchResultRow data ) {
		    	  if ( data == null ) {
		    		  return "";
		          }
		    	  else {
		        	  return data.getField( index );
		          }
		      }
	      };
	      col.setSortable( desc.getSortable( ) );
	      cellTable.addColumn( col, desc.getTitle( ) );
      
	      cellTable.setColumnWidth( col, desc.getWidth( ) );
      }      
      
      tableColIdx.add( i );
    }
    
    cellTable.setSelectionModel( selectionModel);
  }
  
  private void buildPager( ) {
    SimplePager.Resources pagerResources = GWT.create( SimplePager.Resources.class );
    pager = new SimplePager( TextLocation.CENTER, pagerResources, false, 0, true );
    pager.setDisplay( cellTable );
  }
  
  public void load( ) {
    AsyncDataProvider<SearchResultRow> dataProvider = new AsyncDataProvider<SearchResultRow>( ) {
      @Override
      protected void onRangeChanged( HasData<SearchResultRow> display ) {
        SearchRange sr = new SearchRange( -1 );
        Range range = display.getVisibleRange( );
        if ( range != null ) {
          sr.setStart( range.getStart( ) );
          sr.setLength( range.getLength( ) );
        }
        ColumnSortList sortList = cellTable.getColumnSortList( );
        if ( sortList != null && sortList.size( ) > 0 ) {
          ColumnSortInfo sort = sortList.get( 0 );
          if ( sort != null ) {
            sr.setSortField( tableColIdx.get( cellTable.getColumnIndex( ( Column<SearchResultRow, ?> ) sort.getColumn( ) ) ) );
            sr.setAscending( sort.isAscending( ) );
          }
        }
        changeHandler.handleRangeChange( sr );
      }
    };
    dataProvider.addDataDisplay( cellTable );
    
    AsyncHandler sortHandler = new AsyncHandler( cellTable );
    cellTable.addColumnSortHandler( sortHandler );
  }
  
  public void addDoublClickHandler(DoubleClickHandler handler) {
	  cellTable.addDomHandler(handler, DoubleClickEvent.getType());
  }

}
