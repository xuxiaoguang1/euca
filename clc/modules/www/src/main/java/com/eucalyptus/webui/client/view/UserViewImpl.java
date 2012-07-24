package com.eucalyptus.webui.client.view;

import java.util.ArrayList;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.eucalyptus.webui.client.service.SearchResult;
import com.eucalyptus.webui.client.service.SearchResultFieldDesc;
import com.eucalyptus.webui.client.service.SearchResultRow;
import com.eucalyptus.webui.shared.user.LoginUserProfile;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
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

public class UserViewImpl extends Composite implements UserView {
  
  private static final Logger LOG = Logger.getLogger( UserViewImpl.class.getName( ) );
  
  private static UserViewImplUiBinder uiBinder = GWT.create( UserViewImplUiBinder.class );
  
  interface UserViewImplUiBinder extends UiBinder<Widget, UserViewImpl> {}
  
  @UiField
  LayoutPanel tablePanel;
  
  @UiField Anchor buttonAddToGroup;
  @UiField Anchor buttonDelFromGroup;

  private MultiSelectionModel<SearchResultRow> selectionModel;
  
  private SearchResultTable table;
  
  private Presenter presenter;
  
  public UserViewImpl( ) {
    initWidget( uiBinder.createAndBindUi( this ) );
  }
  
	@UiHandler("buttonOnAddUser")
	void onButtonOnAddUserClick(ClickEvent event) {
		this.presenter.onAddUser();
	}
	@UiHandler("buttonOnDelUser")
	void onButtonOnDelUserClick(ClickEvent event) {
		this.presenter.onDeleteUsers();
	}
	
	@UiHandler("buttonAddToGroup")
	void onButtonAddToGroupClick(ClickEvent event) {
		this.presenter.onAddToGroups();
	}
	@UiHandler("buttonDelFromGroup")
	void onButtonDelFromGroupClick(ClickEvent event) {
		this.presenter.onRemoveFromGroups();
	}
	
	@UiHandler("addPolicyButton")
	void onAddPolicyButtonClick(ClickEvent event) {
		this.presenter.onAddPolicy();
	}
	@UiHandler("addKeyButton")
	void onAddKeyButtonClick(ClickEvent event) {
		this.presenter.onAddKey();
	}
	
	@UiHandler("buttonResumeUser")
	void onButtonResumeUserClick(ClickEvent event) {
		this.presenter.onResumeUses();
	}
	@UiHandler("buttonPauseUser")
	void onButtonPauseUserClick(ClickEvent event) {
		this.presenter.onPauseUsers();
	}
	@UiHandler("buttonBanUser")
	void onButtonBanUserClick(ClickEvent event) {
		this.presenter.onBanUsers();
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

	@Override
	public void setPresenter(LoginUserProfile curUser) {
		// TODO Auto-generated method stub
		if (curUser.isSystemAdmin()) {
			this.buttonAddToGroup.setVisible(false);
			this.buttonDelFromGroup.setVisible(false);
		}
		else {
			this.buttonAddToGroup.setVisible(true);
			this.buttonDelFromGroup.setVisible(true);
		}
	}
}
