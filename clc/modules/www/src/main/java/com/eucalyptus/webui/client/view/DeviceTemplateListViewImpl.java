package com.eucalyptus.webui.client.view;

import java.util.ArrayList;
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
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.LayoutPanel;

public class DeviceTemplateListViewImpl extends DialogBox implements DeviceTemplateListView {

	interface DeviceTemplateListViewImplUiBinder extends
			UiBinder<Widget, DeviceTemplateListViewImpl> {
	}

	public DeviceTemplateListViewImpl() {
		setWidget(uiBinder.createAndBindUi(this));
		this.currentSelected = null;
		
		setGlassEnabled(true);
	}

	@UiHandler("buttonOk")
	void onButtonOkClick(ClickEvent event) {
		this.hide();
		
		if (this.currentSelected == null)
			return;
		
		String templateId = this.currentSelected.getField(0);
		this.presenter.doCreateUserApp(templateId);
		
		clearSelection();
	}
	
	@UiHandler("buttonCancle")
	void onButtonCancleClick(ClickEvent event) {
		this.hide();
		clearSelection();
	}

	@Override
	public void showSearchResult(SearchResult result) {
		// TODO Auto-generated method stub
		if ( this.table == null ) {
			initializeTable( this.presenter.getPageSize( ), result.getDescs( ) );
		}
    
		table.setData( result );
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
	
	@Override
	public void display(SearchResult result) {
		// TODO Auto-generated method stub
		this.showSearchResult(result);
		this.center();
		this.show();
	}
		
	private void initializeTable( int pageSize,  ArrayList<SearchResultFieldDesc> fieldDescs ) {
		tablePanel.clear( );
		selectionModel = new SingleSelectionModel<SearchResultRow>( SearchResultRow.KEY_PROVIDER );
		selectionModel.addSelectionChangeHandler( new Handler( ) {
			@Override
			public void onSelectionChange( SelectionChangeEvent event ) {
				currentSelected = selectionModel.getSelectedObject();
		        LOG.log( Level.INFO, "Selection changed: " + currentSelected );
			}
		} );
    
		table = new SearchResultTable( pageSize, fieldDescs, this.presenter, selectionModel );
		tablePanel.add( table );
		table.load( );
	}
	
	private static DeviceTemplateListViewImplUiBinder uiBinder = GWT
			.create(DeviceTemplateListViewImplUiBinder.class);
	
	private static final Logger LOG = Logger.getLogger( UserViewImpl.class.getName( ) );
	
	@UiField LayoutPanel tablePanel;
	@UiField Button buttonOk;
	@UiField Button buttonCancle;

	private SingleSelectionModel<SearchResultRow> selectionModel;
	private SearchResultTable table;
	
	private SearchResultRow currentSelected;
	
	private Presenter presenter;
}
