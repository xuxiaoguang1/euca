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
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;

public class CertViewImpl extends Composite implements CertView {
  
  private static final Logger LOG = Logger.getLogger( CertViewImpl.class.getName( ) );
  
  private static CertViewImplUiBinder uiBinder = GWT.create( CertViewImplUiBinder.class );
  
  interface CertViewImplUiBinder extends UiBinder<Widget, CertViewImpl> {}
  
  @UiField
  LayoutPanel tablePanel;
  
  private MultiSelectionModel<SearchResultRow> selectionModel;
  
  private SearchResultTable table;
  
  private Presenter presenter;
  
	public CertViewImpl() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	@UiHandler("delButton")
	void handleDelButtonClick(ClickEvent e) {
		this.presenter.onDeleteCert();
	}

	@UiHandler("activateButton")
	void handleActivateButtonClick(ClickEvent e) {
		this.presenter.onActivateCert();
	}

	@UiHandler("deactivateButton")
	void handleDeactivateButtonClick(ClickEvent e) {
		this.presenter.onDeactivateCert();
	}

	@UiHandler("revokeButton")
	void handleReovkeButtonClick(ClickEvent e) {
		this.presenter.onRevokeCert();
	}

	@UiHandler("authorizeButton")
	void handleAuthorizeButtonClick(ClickEvent e) {
		this.presenter.onAuthorizeCert();
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
    tablePanel.add( table );
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
