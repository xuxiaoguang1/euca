package com.eucalyptus.webui.client.view;

import java.util.ArrayList;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.eucalyptus.webui.client.service.SearchResultFieldDesc;
import com.eucalyptus.webui.client.service.SearchResult;
import com.eucalyptus.webui.client.service.SearchResultRow;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
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

public class AccountViewImpl extends Composite implements AccountView {
  
  private static final Logger LOG = Logger.getLogger( AccountViewImpl.class.getName( ) );
  
  private static AccountViewImplUiBinder uiBinder = GWT.create( AccountViewImplUiBinder.class );
  
  interface AccountViewImplUiBinder extends UiBinder<Widget, AccountViewImpl> {}
  
  @UiField
  LayoutPanel tablePanel;
  
  @UiField
  Anchor createAccountButton;
  @UiField
  Anchor delButton;
  
  private MultiSelectionModel<SearchResultRow> selectionModel;
  
  private SearchResultTable table;
  
  private Presenter presenter;
  
  public AccountViewImpl( ) {
    initWidget( uiBinder.createAndBindUi( this ) );
  }

  @UiHandler( "createAccountButton" )
  void onCreateAccountButtonClick( ClickEvent e ) {
    this.presenter.onCreateAccount( );
  }
  @UiHandler("modifyAccountButton")
  void onModifyAccountButtonClick(ClickEvent event) {
	  this.presenter.onModifyAccount();
  }
  @UiHandler( "delButton" )
  void handleDelButtonClick( ClickEvent e ) {
	  this.presenter.onDeleteAccounts( );
  }
  
  @UiHandler("addPolicyButton")
  void onAddPolicyButtonClick(ClickEvent event) {
	  this.presenter.onAddPolicy();
  }

  @UiHandler( "resumeButton" )
  void handleResumeButtonClick( ClickEvent e ) {
	  this.presenter.onResume();
  }
  
  @UiHandler( "pauseButton" )
  void handlePauseButtonClick( ClickEvent e ) {
	  this.presenter.onPause();
  }

  @UiHandler( "banButton" )
  void handleBanButtonClick( ClickEvent e ) {
	  this.presenter.onBan();
  }

  public void initializeTable( int pageSize,  ArrayList<SearchResultFieldDesc> fieldDescs ) {
    tablePanel.clear( );
    selectionModel = new MultiSelectionModel<SearchResultRow>( SearchResultRow.KEY_PROVIDER );
    selectionModel.addSelectionChangeHandler( new Handler( ) {
      @Override
      public void onSelectionChange( SelectionChangeEvent event ) {
        Set<SearchResultRow> rows = selectionModel.getSelectedSet( );
        LOG.log( Level.INFO, "Selection changed: " + rows );
        presenter.onSelectionChange( rows );
      }
    } );
    table = new SearchResultTable( pageSize, fieldDescs, this.presenter, selectionModel );
    
    DoubleClickHandler handler = new DoubleClickHandler() {

		@Override
		public void onDoubleClick(DoubleClickEvent event) {
			presenter.onDoubleClick(event);
		}
	};
	table.addDoublClickHandler(handler);
	
    tablePanel.add( table );
    table.load( );
  }

  @Override
  public void setPresenter( Presenter presenter ) {
    this.presenter = presenter;
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
  public void enableNewButton(boolean enabled) {
	this.createAccountButton.setVisible( enabled );
  }

  @Override
  public void enableDelButton(boolean enabled) {
	delButton.setVisible( enabled );
  }

  @Override
  public void clearSelection( ) {
    selectionModel.clear( );
  }
}
