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
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;

public class GroupViewImpl extends Composite implements GroupView {
  
	private static final Logger LOG = Logger.getLogger( GroupViewImpl.class.getName( ) );
  
	private static GroupViewImplUiBinder uiBinder = GWT.create( GroupViewImplUiBinder.class );
  
	interface GroupViewImplUiBinder extends UiBinder<Widget, GroupViewImpl> {}
  
	@UiField
	LayoutPanel tablePanel;
  
	@UiField Anchor addGroupButton;
	@UiField Anchor removeGroupButton;
  
	@UiField Anchor resumeGroupButton;
	@UiField Anchor pauseGroupButton;
	@UiField Anchor banGroupButton;
  
	private MultiSelectionModel<SearchResultRow> selectionModel;
  
	private SearchResultTable table;
  
	private Presenter presenter;
  
	@UiHandler("addGroupButton")
	void onAddGroupButtonClick(ClickEvent event) {
		this.presenter.onAddGroup();
	}
	@UiHandler("modifyGroupButton")
	void onModifyGroupButtonClick(ClickEvent event) {
		this.presenter.onModifyGroup();
	}
	@UiHandler( "removeGroupButton" )
	void handleDelButtonClick( ClickEvent e ) {
		this.presenter.onDeleteGroup( );
	}
	@UiHandler("showGroupDetailsButton")
	void onShowGroupDetailsButtonClick(ClickEvent event) {
		this.presenter.showGroupDetails();
	}
	@UiHandler("resumeGroupButton")
	void onBUTTON_RESUMEClick(ClickEvent event) {
		this.presenter.onResumeGroup();
	}	
	@UiHandler("pauseGroupButton")
	void onBUTTON_PAUSEClick(ClickEvent event) {
		this.presenter.onPauseGroup();
	}
	@UiHandler("banGroupButton")
	void onBUTTON_BANClick(ClickEvent event) {
		this.presenter.onBanGroup();
	}
  
	public GroupViewImpl( ) {
		initWidget( uiBinder.createAndBindUi( this ) );
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
