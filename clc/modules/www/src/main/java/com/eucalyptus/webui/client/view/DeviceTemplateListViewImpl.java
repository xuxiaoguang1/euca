package com.eucalyptus.webui.client.view;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.eucalyptus.webui.client.service.SearchResult;
import com.eucalyptus.webui.client.service.SearchResultFieldDesc;
import com.eucalyptus.webui.client.service.SearchResultRow;
import com.eucalyptus.webui.shared.resource.VMImageType;
import com.eucalyptus.webui.shared.user.UserApp;
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
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.datepicker.client.DateBox;
import com.google.gwt.user.client.ui.ListBox;

public class DeviceTemplateListViewImpl extends DialogBox implements DeviceTemplateListView {

	interface DeviceTemplateListViewImplUiBinder extends
			UiBinder<Widget, DeviceTemplateListViewImpl> {
	}

	public DeviceTemplateListViewImpl() {
		setWidget(uiBinder.createAndBindUi(this));
		this.currentSelected = null;
		
		this.startingTime.setFormat(new DateBox.DefaultFormat(DateTimeFormat.getShortDateFormat()));
		this.endingTime.setFormat(new DateBox.DefaultFormat(DateTimeFormat.getShortDateFormat()));
		
		setGlassEnabled(true);
	}

	@UiHandler("buttonOk")
	void onButtonOkClick(ClickEvent event) {
			
		if (this.currentSelected == null)
			return;
		
		if (this.startingTime.getValue() == null || this.endingTime.getValue() == null)
			return;
		
		if (this.startingTime.getValue().getTime() - this.endingTime.getValue().getTime() > 0)
			return;
		
		int templateId = Integer.parseInt(this.currentSelected.getField(0));
		int vmImageTypeId = Integer.parseInt(this.VMImageTypeList.getValue(this.VMImageTypeList.getSelectedIndex()));
		
		UserApp userApp = new UserApp();
		userApp.setTemplateId(templateId);
		userApp.setVmImageTypeId(vmImageTypeId);
		userApp.setSrvStartingTime(this.startingTime.getValue());
		userApp.setSrvEndingingTime(this.endingTime.getValue());
		
		this.presenter.doCreateUserApp(userApp);
		
		clearSelection();
		
		this.hide();
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
		this.VMImageTypeList.clear();
	}

	@Override
	public void setPresenter(Presenter presenter) {
		// TODO Auto-generated method stub
		this.presenter = presenter;
	}
	
	@Override
	public void setVMImageTypeList(ArrayList<VMImageType> vmTypeList) {
		// TODO Auto-generated method stub
		if (vmTypeList == null)
			return;
		
		for (VMImageType vm : vmTypeList) {
			String item = vm.getOs() + " (" + vm.getVer() + ")";
			this.VMImageTypeList.addItem(item, Integer.valueOf(vm.getId()).toString());
		}
	}
	
	@Override
	public void display(SearchResult deviceTemplateList) {
		// TODO Auto-generated method stub
		this.showSearchResult(deviceTemplateList);
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
	@UiField DateBox startingTime;
	@UiField DateBox endingTime;
	@UiField Button buttonOk;
	@UiField Button buttonCancle;
	@UiField ListBox VMImageTypeList;

	private SingleSelectionModel<SearchResultRow> selectionModel;
	private SearchResultTable table;
	
	private SearchResultRow currentSelected;
	
	private Presenter presenter;
}
