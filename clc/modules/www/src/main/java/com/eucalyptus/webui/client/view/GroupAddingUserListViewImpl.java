package com.eucalyptus.webui.client.view;

import java.util.ArrayList;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.eucalyptus.webui.client.service.SearchResult;
import com.eucalyptus.webui.client.service.SearchResultFieldDesc;
import com.eucalyptus.webui.client.service.SearchResultRow;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.LayoutPanel;

public class GroupAddingUserListViewImpl extends DialogBox implements GroupAddingUserListView {

	interface GroupAddingUserListViewImplUiBinder extends
			UiBinder<Widget, GroupAddingUserListViewImpl> {
	}

	public GroupAddingUserListViewImpl() {
		setWidget(uiBinder.createAndBindUi(this));
		this.currentSelected = null;
		
		setGlassEnabled( true );
	}

	@UiHandler("buttonOk")
	void onButtonOkClick(ClickEvent event) {
		this.hide();
		
		if (this.currentSelected == null || this.currentSelected.size() == 0)
			return;
		
		ArrayList<String> ids = new ArrayList<String>();
		for (SearchResultRow row : this.currentSelected) {
			ids.add(row.getField(0));
		}
		
		this.presenter.process(ids);
		
		clearSelection();
	}
	@UiHandler("buttonCancle")
	void onButtonCancleClick(ClickEvent event) {
		clearSelection();
		this.hide();
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
	public void clearSelection() {
		// TODO Auto-generated method stub
		this.tablePanel.clear( );
		this.table = null;
	}

	@Override
	public void setPresenter(Presenter presenter) {
		// TODO Auto-generated method stub
		this.presenter = presenter;
	}
		
	private void initializeTable( int pageSize,  ArrayList<SearchResultFieldDesc> fieldDescs ) {
		tablePanel.clear( );
		selectionModel = new MultiSelectionModel<SearchResultRow>( SearchResultRow.KEY_PROVIDER );
		selectionModel.addSelectionChangeHandler( new Handler( ) {
			@Override
			public void onSelectionChange( SelectionChangeEvent event ) {
		        Set<SearchResultRow> rows = selectionModel.getSelectedSet( );
		        LOG.log( Level.INFO, "Selection changed: " + rows );
		        currentSelected = rows;
		        
		        System.out.println(rows.size());
			}
		} );
    
		table = new SearchResultTable( pageSize, fieldDescs, this.presenter, selectionModel );
		tablePanel.add( table );
		table.load( );
	}
	
	private static GroupAddingUserListViewImplUiBinder uiBinder = GWT
			.create(GroupAddingUserListViewImplUiBinder.class);
	
	private static final Logger LOG = Logger.getLogger( UserViewImpl.class.getName( ) );
	
	@UiField LayoutPanel tablePanel;
	@UiField Button buttonOk;
	@UiField Button buttonCancle;

	private MultiSelectionModel<SearchResultRow> selectionModel;
	private SearchResultTable table;
	
	private Set<SearchResultRow> currentSelected;
	
	private Presenter presenter;
}
