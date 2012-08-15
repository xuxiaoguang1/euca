package com.eucalyptus.webui.client.activity;

import java.util.ArrayList;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.eucalyptus.webui.client.ClientFactory;
import com.eucalyptus.webui.client.place.SearchPlace;
import com.eucalyptus.webui.client.service.SearchRange;
import com.eucalyptus.webui.client.service.SearchResult;
import com.eucalyptus.webui.client.service.SearchResultRow;
import com.eucalyptus.webui.client.view.ConfirmationView;
import com.eucalyptus.webui.client.view.DeviceTemplateListView;
import com.eucalyptus.webui.client.view.FooterView;
import com.eucalyptus.webui.client.view.UserAppView;
import com.eucalyptus.webui.client.view.HasValueWidget;
import com.eucalyptus.webui.client.view.FooterView.StatusType;
import com.eucalyptus.webui.client.view.LogView.LogType;
import com.eucalyptus.webui.shared.user.EnumUserAppState;
import com.google.common.collect.Lists;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class UserAppActivity extends AbstractSearchActivity
    implements ConfirmationView.Presenter, UserAppView.Presenter, DeviceTemplateListView.Presenter {

public static final String[] TITLE = {"USER APPLICATION", "用户申请"};

  public static final String[] APPROVE_USER_APPLICATION_CAPTION = {"Approve user application", "通过申请"};
  public static final String[] APPROE_USER_APPLICATION_SUBJECT = {"Are you sure to resume users?", "确定激活所选用户?"};
  
  public static final String[] REJECT_USER_APPLICATION_CAPTION = {"Pause users", "暂停用户"};
  public static final String[] REJECT_USER_APPLICATION_SUBJECT = {"Are you sure to pause users?", "确定暂停所选用户?"};
  
  public static final String[] DEL_USER_APPLICATION_CAPTION = {"Add users", "添加用户"};
  public static final String[] DEL_USER_APPLICATION_SUBJECT = {"Are you sure to add user?", "确定添加该用户"};
  
  private final String[] FOOTERVIEW_USER_NO_SELECTION = {"Must select users", "必须选择至少一个用户"};
  private final String[] FOOTERVIEW_FAILED_TO_QUERY_TEMPLATES = {"Failed to query templates", "查询模板库失败"};
  
  private final String[] FOOTERVIEW_FAILED_TO_ADD_USERAPP = {"Failed to add user app", "增加用户申请失败"};
  private final String[] FOOTERVIEW_ADD_USERAPP = {"Successfully add user apps", "增加用户申请成功"};

  private static final Logger LOG = Logger.getLogger( UserAppActivity.class.getName( ) );
  
  private Set<SearchResultRow> currentSelected;
  
  public UserAppActivity( SearchPlace place, ClientFactory clientFactory ) {
    super( place, clientFactory );
  }

  @Override
  protected void doSearch( String query, SearchRange range ) {
    this.clientFactory.getBackendService( ).lookupUserApp(this.clientFactory.getLocalSession( ).getSession( ), search, range, EnumUserAppState.DEFAULT, 
                                                           new AsyncCallback<SearchResult>( ) {
      
      @Override
      public void onFailure( Throwable caught ) {
        ActivityUtil.logoutForInvalidSession( clientFactory, caught );
        LOG.log( Level.WARNING, "Search failed: " + caught );
        displayData( null );
      }
      
      @Override
      public void onSuccess( SearchResult result ) {
        LOG.log( Level.INFO, "Search success:" + result );
        displayData( result );
      }
      
    } );
  }
  
  @Override
  public void onSelectionChange( Set<SearchResultRow> selection ) {
    this.currentSelected = selection;
    
    if ( selection == null || selection.size( ) != 1 ) {
      LOG.log( Level.INFO, "Not a single selection" );      
      this.clientFactory.getShellView( ).hideDetail( );
    }
    else {
      LOG.log( Level.INFO, "Selection changed to " + selection );
    }
  }

  @Override
  public void saveValue( ArrayList<String> keys, ArrayList<HasValueWidget> values ) {
  }

  @Override
  protected String getTitle( ) {
    return TITLE[1];
  }

  @Override
  protected void showView( SearchResult result ) {
    if ( this.view == null ) {
      this.view = this.clientFactory.getUserAppView();
      ( ( UserAppView ) this.view ).setPresenter( this );
      ( ( UserAppView ) this.view ).displayCtrl(this.clientFactory.getSessionData().getLoginUser());
      container.setWidget( this.view );
      ( ( UserAppView ) this.view ).clear( );
    }
    
    ( ( UserAppView ) this.view ).showSearchResult( result );    
  }
  
  @Override
  public void onApproveUserApp() {
  	// TODO Auto-generated method stub
  	
  }

  @Override
  public void onRejectUserApp() {
  	// TODO Auto-generated method stub
  	
  }

  @Override
  public void onCreateUserApp() {
  	// TODO Auto-generated method stub
	clientFactory.getDeviceTemplateListView().setPresenter(this);
	
  	this.clientFactory.getBackendService().lookupDeviceTemplate(clientFactory.getLocalSession().getSession(), search, range, null, null, new AsyncCallback<SearchResult>() {

		        @Override
		        public void onFailure(Throwable caught) {
			        // TODO Auto-generated method stub
		        	clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.ERROR, FOOTERVIEW_FAILED_TO_QUERY_TEMPLATES[1], FooterView.DEFAULT_STATUS_CLEAR_DELAY );
		    		clientFactory.getShellView( ).getLogView( ).log( LogType.ERROR, FOOTERVIEW_FAILED_TO_QUERY_TEMPLATES[1] + ":" + caught.getMessage( ) );
		        }

		        @Override
		        public void onSuccess(SearchResult result) {
			        // TODO Auto-generated method stub;
			        clientFactory.getDeviceTemplateListView().display(result);
		        }
	        });
  }
  	
  @Override
  public void onDeleteUserApp() {
  	// TODO Auto-generated method stub
  	
  }
 
  @Override
  public void onShowAllApps() {
  	// TODO Auto-generated method stub
  	
  }

  @Override
  public void onSolvedApps() {
  	// TODO Auto-generated method stub
  	
  }

  @Override
  public void onSolvingApps() {
  	// TODO Auto-generated method stub
  	
  }

  @Override
  public void onToSolvingApps() {
  	// TODO Auto-generated method stub
  	
  }

  private boolean selectionIsValid() {
	  if ( currentSelected == null || currentSelected.size( ) < 1 ) {
		  clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.ERROR, FOOTERVIEW_USER_NO_SELECTION[1], FooterView.DEFAULT_STATUS_CLEAR_DELAY );
		  return false;
	  }
	  
	  return true;
  }
  
  @Override
  public void confirm( String subject ) {
    if ( APPROE_USER_APPLICATION_SUBJECT[1].equals( subject ) ) {
    	doApproveUserApp();
    } else if ( REJECT_USER_APPLICATION_SUBJECT[1].equals( subject ) ) {
    	doRejectUserApp();
    } else if ( DEL_USER_APPLICATION_SUBJECT[1].equals( subject ) ) {
    	doDelUserApp();
    } 
  }

  private void doApproveUserApp( ) {
	    final ArrayList<String> ids = Lists.newArrayList( ); 
	    for ( SearchResultRow row : currentSelected ) {
	      ids.add( row.getField( 0 ) );
	    }
	}

  private void doRejectUserApp( ) {
	    if ( currentSelected == null || currentSelected.size( ) != 1 ) {
	    	return;
	    }
  }

  @Override
  public void doCreateUserApp(String templateId) {
	  // TODO Auto-generated method stub
	  this.clientFactory.getBackendService().addUserApp(clientFactory.getLocalSession().getSession(), 
			  											Integer.toString(this.clientFactory.getSessionData().getLoginUser().getUserId()), 
			  											templateId,
			  											new AsyncCallback<Void>() {

	        @Override
	        public void onFailure(Throwable caught) {
		        // TODO Auto-generated method stub
	        	clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.ERROR, FOOTERVIEW_FAILED_TO_ADD_USERAPP[1], FooterView.DEFAULT_STATUS_CLEAR_DELAY );
	    		clientFactory.getShellView( ).getLogView( ).log( LogType.ERROR, FOOTERVIEW_FAILED_TO_ADD_USERAPP[1] + ":" + caught.getMessage( ) );
	        }
			@Override
			public void onSuccess(Void result) {
				// TODO Auto-generated method stub
				clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.NONE, FOOTERVIEW_ADD_USERAPP[1], FooterView.DEFAULT_STATUS_CLEAR_DELAY );
	    		clientFactory.getShellView( ).getLogView( ).log( LogType.ERROR, FOOTERVIEW_ADD_USERAPP[1]);
	    		
	    		reloadCurrentRange();
			}
      });
  }
  
  private void doDelUserApp( ) {
  }

}
  
