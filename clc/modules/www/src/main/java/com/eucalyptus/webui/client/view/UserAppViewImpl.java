package com.eucalyptus.webui.client.view;

import java.util.ArrayList;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.eucalyptus.webui.client.service.SearchResult;
import com.eucalyptus.webui.client.service.SearchResultFieldDesc;
import com.eucalyptus.webui.client.service.SearchResultRow;
import com.eucalyptus.webui.shared.user.EnumUserAppStatus;
import com.eucalyptus.webui.shared.user.LoginUserProfile;
import com.eucalyptus.webui.shared.user.UserAppStateCount;
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

public class UserAppViewImpl extends Composite implements UserAppView {

	private static UserAppViewImplUiBinder uiBinder = GWT
			.create(UserAppViewImplUiBinder.class);
	
	private Presenter presenter;
	private SearchResultTable table;
	private MultiSelectionModel<SearchResultRow> selectionModel;
	private static final Logger LOG = Logger.getLogger( UserAppViewImpl.class.getName( ) );
	
	@UiField
	LayoutPanel tablePanel;
	
	@UiField
	Anchor buttonApproveUserApp;
	@UiField 
	Anchor buttonRejectUserApp;
	@UiField 
	Anchor buttonOnDelUserApp;
	@UiField 
	Anchor buttonOnCreateUserApp;
	
	@UiField 
	Anchor labelAll;
	@UiField 
	Anchor labelApplying;
	@UiField 
	Anchor labelApproved;
	@UiField 
	Anchor labelRejected;

	interface UserAppViewImplUiBinder extends
		UiBinder<Widget, UserAppViewImpl> {
	}

	public UserAppViewImpl() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	public UserAppViewImpl(String firstName) {
		initWidget(uiBinder.createAndBindUi(this));
	}

	@UiHandler("buttonApproveUserApp")
	void onButtonApproveUserAppClick(ClickEvent event) {
		this.presenter.onApproveUserApp();
	}
	@UiHandler("buttonRejectUserApp")
	void onButtonRejectUserAppClick(ClickEvent event) {
		this.presenter.onRejectUserApp();
	}
	
	@UiHandler("buttonOnCreateUserApp")
	void onButtonOnCreateUserAppClick(ClickEvent event) {
		this.presenter.onCreateUserApp();
	}
	@UiHandler("buttonOnDelUserApp")
	void onButtonOnDelUserAppClick(ClickEvent event) {
		this.presenter.onDeleteUserApp();
	}

	@UiHandler("labelAll")
	void onLabelAllClick(ClickEvent event) {
		this.presenter.onShowAllApps();
	}
	@UiHandler("labelApproved")
	void onLabelSolvedClick(ClickEvent event) {
		this.presenter.onApprovedApps();
	}
	@UiHandler("labelApplying")
	void onLabelToSolveClick(ClickEvent event) {
		this.presenter.onApplyingApps();
	}
	@UiHandler("labelRejected")
	void onLabelSolvingClick(ClickEvent event) {
		this.presenter.onRejectedApps();
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
	public void clear() {
		// TODO Auto-generated method stub
		this.tablePanel.clear( );
		this.table = null;
	}

	@Override
	public void clearSelection() {
		// TODO Auto-generated method stub
		this.selectionModel.clear( );
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
		        presenter.onSelectionChange( rows );
		        System.out.println(rows.size());
			}
		} );
    
		table = new SearchResultTable( pageSize, fieldDescs, this.presenter, selectionModel );
		
		//table.setCellClickProc();
		
		tablePanel.add( table );
		table.load( );
	}

	@Override
	public void displayCtrl(LoginUserProfile curUser) {
		// TODO Auto-generated method stub
		if (!curUser.isSystemAdmin()) {
			this.buttonApproveUserApp.setVisible(false);
			this.buttonRejectUserApp.setVisible(false);
			
			this.buttonOnCreateUserApp.setVisible(true);
			this.buttonOnDelUserApp.setVisible(true);
		}
		else {
			this.buttonApproveUserApp.setVisible(true);
			this.buttonRejectUserApp.setVisible(true);
			
			this.buttonOnCreateUserApp.setVisible(false);
			this.buttonOnDelUserApp.setVisible(true);
		}
	}

	@Override
	public void updateCountInfo(ArrayList<UserAppStateCount> countInfo) {
		// TODO Auto-generated method stub
		int noneCount = 0, toSolveCount = 0, solvingCount = 0, solvedCount = 0;
		
		for (UserAppStateCount i : countInfo) {
			if (i.getAppState() == EnumUserAppStatus.NONE)
				noneCount = i.getCount();
			else if (i.getAppState() == EnumUserAppStatus.APPLYING)
				toSolveCount = i.getCount();
			else if (i.getAppState() == EnumUserAppStatus.APPROVED)
				solvedCount = i.getCount();
			else if (i.getAppState() == EnumUserAppStatus.REJECTED)
				solvedCount = i.getCount();
		}
		
		int count = noneCount + toSolveCount + solvingCount + solvedCount;
		
		this.labelAll.setText("总申请数量: " + count);
		this.labelApplying.setText("待解决申请数量: " + toSolveCount);
		this.labelApproved.setText("已批准申请数量: " + solvedCount);
		this.labelRejected.setText("已拒绝申请数量: " + solvingCount);
	}

	@Override
	public void setCellClickProc(SearchTableCellClickHandler clickHandler) {
		// TODO Auto-generated method stub
		this.table.setCellClickHandler(clickHandler);
	}
}
