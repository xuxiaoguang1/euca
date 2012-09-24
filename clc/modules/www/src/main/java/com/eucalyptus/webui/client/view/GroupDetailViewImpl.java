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
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.google.gwt.user.client.ui.Button;

public class GroupDetailViewImpl extends DialogBox implements GroupDetailView {
  
	public GroupDetailViewImpl( ) {
		setWidget( uiBinder.createAndBindUi( this ) );
		
		setGlassEnabled( true );
	}
  
	@UiHandler("buttonAddUsers")
	void onButtonAddUsersClick(ClickEvent event) {
		this.presenter.onAddUsers(this.accountId, this.groupId);
	}
	@UiHandler("buttonRemoveUsers")
	void onButtonRemoveUsersClick(ClickEvent event) {
		this.presenter.onRemoveUsers();
	}
	@UiHandler("cancleButton")
	void onCancleButtonClick(ClickEvent event) {
		this.hide();
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
		    System.out.println(rows.size());
		  }
	    } );
	    
	    table = new SearchResultTable( pageSize, fieldDescs, this.presenter, selectionModel );
	    tablePanel.add( table );
	    table.load( );
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
	public void showSearchResult(SearchResult result) {
		// TODO Auto-generated method stub
		if ( this.table == null ) {
			initializeTable( this.presenter.getPageSize( ), result.getDescs( ) );
		}
	
		table.setData( result );
		this.center();
		this.show();
	}

	@Override
	public void setTitle(String title) {
		// TODO Auto-generated method stub
		this.setTitle(title);
	}
	
	@Override
	public void setAccountId(int accountId) {
		// TODO Auto-generated method stub
		this.accountId = accountId;
	}
	
	@Override
	public void setGroupId(int groupId) {
		// TODO Auto-generated method stub
		this.groupId = groupId;
	}

	private static final Logger LOG = Logger.getLogger( GroupDetailViewImpl.class.getName( ) );
	
	private static GroupDetailViewImplUiBinder uiBinder = GWT.create( GroupDetailViewImplUiBinder.class );
	
	interface GroupDetailViewImplUiBinder extends UiBinder<Widget, GroupDetailViewImpl> {}
	
	@UiField
	LayoutPanel tablePanel;
	@UiField Button buttonAddUsers;
	@UiField Button buttonRemoveUsers;
	@UiField Button cancleButton;
	
	private MultiSelectionModel<SearchResultRow> selectionModel;
	
	private SearchResultTable table;
	
	private Presenter presenter;
	
	private int accountId;
	private int groupId;
}
