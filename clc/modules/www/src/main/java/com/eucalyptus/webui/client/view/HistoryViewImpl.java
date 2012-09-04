package com.eucalyptus.webui.client.view;

import java.util.ArrayList;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.eucalyptus.webui.client.service.SearchResult;
import com.eucalyptus.webui.client.service.SearchResultFieldDesc;
import com.eucalyptus.webui.client.service.SearchResultRow;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;

public class HistoryViewImpl extends Composite implements HistoryView {
  
  private static final Logger LOG = Logger.getLogger( HistoryViewImpl.class.getName( ) );
  
  private static HistoryViewImplUiBinder uiBinder = GWT.create( HistoryViewImplUiBinder.class );
  
  interface HistoryViewImplUiBinder extends UiBinder<Widget, HistoryViewImpl> {}
  
  @UiField
  LayoutPanel tablePanel;

  private MultiSelectionModel<SearchResultRow> selectionModel;
  
  private SearchResultTable table;
  
  private Presenter presenter;
  
  public HistoryViewImpl( ) {
    initWidget( uiBinder.createAndBindUi( this ) );
  }
  
	@UiHandler("buttonOnRefresh")
	void onButtonOnAddUserClick(ClickEvent event) {
		this.presenter.onRefresh();
	}
	@UiHandler("buttonOnSetting")
	void onButtonOnModifyUserClick(ClickEvent event) {
	}


	private void initializeTable( int pageSize,  ArrayList<SearchResultFieldDesc> fieldDescs ) {
		tablePanel.clear( );
		selectionModel = new MultiSelectionModel<SearchResultRow>( SearchResultRow.KEY_PROVIDER );
		selectionModel.addSelectionChangeHandler( new Handler( ) {
			@Override
			public void onSelectionChange( SelectionChangeEvent event ) {
		        Set<SearchResultRow> rows = selectionModel.getSelectedSet( );
		        LOG.log( Level.INFO, "Selection changed: " + rows );
		        presenter.onSelectionChange( rows );
		        System.out.println(rows.size());
			}
		} );
    
		table = new SearchResultTable( pageSize, fieldDescs, this.presenter, selectionModel );
		tablePanel.add( table );
		
		DoubleClickHandler handler = new DoubleClickHandler() {

			@Override
			public void onDoubleClick(DoubleClickEvent event) {
				presenter.onDoubleClick(event);
			}
		};
		table.addDoublClickHandler(handler);
		
		table.load( );
	}
  
	@Override
	public void showSearchResult( SearchResult result ) {
		if ( this.table == null ) {
			initializeTable( this.presenter.getPageSize( ), result.getDescs( ) );
		}
    
		table.setData( result );
	}

	@Override
	public void clear( ) {
		this.tablePanel.clear( );
		this.table = null;
	}

	@Override
	public void setPresenter( Presenter presenter ) {
		this.presenter = presenter;
	}

	@Override
	public void clearSelection( ) {
		this.selectionModel.clear( );
	}

}
