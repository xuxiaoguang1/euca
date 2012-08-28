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
import com.eucalyptus.webui.client.view.SearchTableCellClickHandler;
import com.eucalyptus.webui.client.view.UserAppView;
import com.eucalyptus.webui.client.view.HasValueWidget;
import com.eucalyptus.webui.client.view.FooterView.StatusType;
import com.eucalyptus.webui.client.view.LogView.LogType;
import com.eucalyptus.webui.shared.resource.VMImageType;
import com.eucalyptus.webui.shared.user.EnumUserAppResult;
import com.eucalyptus.webui.shared.user.EnumUserAppState;
import com.eucalyptus.webui.shared.user.UserApp;
import com.eucalyptus.webui.shared.user.UserAppStateCount;
import com.google.common.collect.Lists;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class UserAppActivity extends AbstractSearchActivity
    implements ConfirmationView.Presenter, UserAppView.Presenter, DeviceTemplateListView.Presenter, SearchTableCellClickHandler {

public static final String[] TITLE = {"USER APPLICATION", "用户申请"};

  public static final String[] APPROVE_USER_APP_CAPTION = {"Approve user application", "通过申请"};
  public static final String[] APPROVE_USER_APP_SUBJECT = {"Are you sure to resume users?", "确定通过所选申请?"};
  
  public static final String[] REJECT_USER_APP_CAPTION = {"Pause users", "拒绝申请"};
  public static final String[] REJECT_USER_APP_SUBJECT = {"Are you sure to pause users?", "确定拒绝选择申请?"};
  
  public static final String[] DEL_USER_APP_CAPTION = {"Add users", "删除申请"};
  public static final String[] DEL_USER_APP_SUBJECT = {"Are you sure to add user?", "确定删除所选用户申请"};
  
  private final String[] FOOTERVIEW_USER_NO_SELECTION = {"Must select users", "必须选择至少一个用户"};
  private final String[] FOOTERVIEW_FAILED_TO_QUERY_TEMPLATES = {"Failed to query templates", "查询模板库失败"};
  private final String[] FOOTERVIEW_FAILED_TO_QUERY_VM_IMAGE_TYPE = {"Failed to query VM image type", "查询虚拟机镜像类型失败"};
  
  private final String[] FOOTERVIEW_FAILED_TO_ADD_USERAPP = {"Failed to add user app", "增加申请失败"};
  private final String[] FOOTERVIEW_ADD_USERAPP = {"Successfully add user apps", "增加申请成功"};
  
  private final String[] FOOTERVIEW_FAILED_TO_DEL_USERAPP = {"Failed to del user app", "删除申请失败"};
  private final String[] FOOTERVIEW_DEL_USERAPP = {"Successfully del user apps", "删除申请成功"};
  
  private final String[] FOOTERVIEW_FAILED_TO_APPROVE_USERAPP = {"Failed to approve user app", "批准申请失败"};
  private final String[] FOOTERVIEW_APPROVE_USERAPP = {"Successfully approve user apps", "批准申请成功"};
  
  private final String[] FOOTERVIEW_FAILED_TO_REJECT_USERAPP = {"Failed to reject user app", "拒绝申请失败"};
  private final String[] FOOTERVIEW_REJECT_USERAPP = {"Successfully reject user apps", "拒绝申请成功"};

  private static final Logger LOG = Logger.getLogger( UserAppActivity.class.getName( ) );
  
  private Set<SearchResultRow> currentSelected;
  
  private EnumUserAppState appState = EnumUserAppState.NONE;
  
  public UserAppActivity( SearchPlace place, ClientFactory clientFactory ) {
    super( place, clientFactory );
  }

  @Override
  protected void doSearch( String query, SearchRange range ) {
	  showUserAppByState(appState);
	  updateUserAppCountInfo();
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
    
    //Registering setCellClickProc must await UserAppView's table inited  
    ( ( UserAppView ) this.view ).setCellClickProc( this );
  }
  
  @Override
  public void onApproveUserApp() {
	  // TODO Auto-generated method stub
	  if (!selectionIsValid())
		  return;
  
	  ConfirmationView dialog = this.clientFactory.getConfirmationView( );
	  dialog.setPresenter( this );
	  dialog.display( APPROVE_USER_APP_CAPTION[1], APPROVE_USER_APP_SUBJECT[1]);
  }

  @Override
  public void onRejectUserApp() {
	  // TODO Auto-generated method stub
	  if (!selectionIsValid())
		  return;
  
	  ConfirmationView dialog = this.clientFactory.getConfirmationView( );
	  dialog.setPresenter( this );
	  dialog.display( REJECT_USER_APP_CAPTION[1], REJECT_USER_APP_SUBJECT[1]);
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
  	
  	this.clientFactory.getBackendService().queryVMImageType(clientFactory.getLocalSession().getSession(), new AsyncCallback<ArrayList<VMImageType>>() {

		@Override
		public void onFailure(Throwable caught) {
			// TODO Auto-generated method stub
			clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.ERROR, FOOTERVIEW_FAILED_TO_QUERY_VM_IMAGE_TYPE[1], FooterView.DEFAULT_STATUS_CLEAR_DELAY );
    		clientFactory.getShellView( ).getLogView( ).log( LogType.ERROR, FOOTERVIEW_FAILED_TO_QUERY_VM_IMAGE_TYPE[1] + ":" + caught.getMessage( ) );
		}

		@Override
		public void onSuccess(ArrayList<VMImageType> result) {
			// TODO Auto-generated method stub
			clientFactory.getDeviceTemplateListView().setVMImageTypeList(result);
		}
  		
  	});
  }
  	
  @Override
  public void onDeleteUserApp() {
	  // TODO Auto-generated method stub
	  if (!selectionIsValid())
		  return;
  
	  ConfirmationView dialog = this.clientFactory.getConfirmationView( );
	  dialog.setPresenter( this );
	  dialog.display( DEL_USER_APP_CAPTION[1], DEL_USER_APP_SUBJECT[1]);
  }
 
  @Override
  public void onShowAllApps() {
	  // TODO Auto-generated method stub
	  this.appState = EnumUserAppState.NONE;
	  showUserAppByState(appState);
  }

  @Override
  public void onSolvedApps() {
	  // TODO Auto-generated method stub
	  this.appState = EnumUserAppState.SOLVED;
	  showUserAppByState(appState);
  }

  @Override
  public void onSolvingApps() {
	  // TODO Auto-generated method stub
	  this.appState = EnumUserAppState.SOLVING;
	  showUserAppByState(appState);
  }

  @Override
  public void onToSolveApps() {
	  // TODO Auto-generated method stub
	  this.appState = EnumUserAppState.TOSOLVE;
	  showUserAppByState(appState);
  }
  
  private void showUserAppByState(EnumUserAppState state) {
	  this.clientFactory.getBackendService( ).lookupUserApp(this.clientFactory.getLocalSession( ).getSession( ), search, range, state, 
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

  private boolean selectionIsValid() {
	  if ( currentSelected == null || currentSelected.size( ) < 1 ) {
		  clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.ERROR, FOOTERVIEW_USER_NO_SELECTION[1], FooterView.DEFAULT_STATUS_CLEAR_DELAY );
		  return false;
	  }
	  
	  return true;
  }
  
  @Override
  public void confirm( String subject ) {
    if ( APPROVE_USER_APP_SUBJECT[1].equals( subject ) ) {
    	doApproveUserApp();
    } else if ( REJECT_USER_APP_SUBJECT[1].equals( subject ) ) {
    	doRejectUserApp();
    } else if ( DEL_USER_APP_SUBJECT[1].equals( subject ) ) {
    	doDelUserApp();
    } 
  }

  private void doApproveUserApp( ) {
	    final ArrayList<UserApp> apps = Lists.newArrayList( ); 
	    for ( SearchResultRow row : currentSelected ) {
	    	UserApp app = new UserApp();
	    	app.setUAId(Integer.valueOf(row.getField(0)));
	    	app.setState(EnumUserAppState.SOLVED);
	    	app.setResult(EnumUserAppResult.APPROVED);
	    	
	    	apps.add(app);
	    }

	    this.clientFactory.getBackendService().modifyUserApp(clientFactory.getLocalSession().getSession(), apps, 
					new AsyncCallback<Void>() {
						@Override
						public void onFailure(Throwable caught) {
						// TODO Auto-generated method stub
						clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.ERROR, FOOTERVIEW_FAILED_TO_APPROVE_USERAPP[1], FooterView.DEFAULT_STATUS_CLEAR_DELAY );
						clientFactory.getShellView( ).getLogView( ).log( LogType.ERROR, FOOTERVIEW_FAILED_TO_APPROVE_USERAPP[1] + ":" + caught.getMessage( ) );
						}
						@Override
						public void onSuccess(Void result) {
						// TODO Auto-generated method stub
						clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.NONE, FOOTERVIEW_APPROVE_USERAPP[1], FooterView.DEFAULT_STATUS_CLEAR_DELAY );
						clientFactory.getShellView( ).getLogView( ).log( LogType.ERROR, FOOTERVIEW_APPROVE_USERAPP[1]);

						reloadCurrentRange();
						updateUserAppCountInfo();
						
						clientFactory.getUserAppView().clearSelection();
						}
					});
	}

  private void doRejectUserApp( ) {
	  	final ArrayList<UserApp> apps = Lists.newArrayList( ); 
	    for ( SearchResultRow row : currentSelected ) {
	    	UserApp app = new UserApp();
	    	app.setUAId(Integer.valueOf(row.getField(0)));
	    	app.setState(EnumUserAppState.SOLVED);
	    	app.setResult(EnumUserAppResult.REJECTED);
	    	
	    	apps.add(app);
	    }
	    
	    this.clientFactory.getBackendService().modifyUserApp(clientFactory.getLocalSession().getSession(), apps, 
					new AsyncCallback<Void>() {
						@Override
						public void onFailure(Throwable caught) {
						// TODO Auto-generated method stub
						clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.ERROR, FOOTERVIEW_FAILED_TO_REJECT_USERAPP[1], FooterView.DEFAULT_STATUS_CLEAR_DELAY );
						clientFactory.getShellView( ).getLogView( ).log( LogType.ERROR, FOOTERVIEW_FAILED_TO_REJECT_USERAPP[1] + ":" + caught.getMessage( ) );
						}
						@Override
						public void onSuccess(Void result) {
						// TODO Auto-generated method stub
						clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.NONE, FOOTERVIEW_REJECT_USERAPP[1], FooterView.DEFAULT_STATUS_CLEAR_DELAY );
						clientFactory.getShellView( ).getLogView( ).log( LogType.ERROR, FOOTERVIEW_REJECT_USERAPP[1]);
						
						reloadCurrentRange();
						updateUserAppCountInfo();
						
						clientFactory.getUserAppView().clearSelection();
						}
					});
  }

  @Override
  public void doCreateUserApp(UserApp userApp) {
	  // TODO Auto-generated method stub
	  int userId = this.clientFactory.getSessionData().getLoginUser().getUserId();
	  userApp.setUserId(userId);
	  
	  this.clientFactory.getBackendService().addUserApp(clientFactory.getLocalSession().getSession(), 
			  											userApp,
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
	    		updateUserAppCountInfo();
			}
      });
  }
  
  private void doDelUserApp( ) {
	  
	  if ( currentSelected == null || currentSelected.size( ) < 1 ) {
	      return;
	  }
  
	  final ArrayList<String> ids = Lists.newArrayList( ); 
	  for ( SearchResultRow row : currentSelected ) {
	      ids.add( row.getField( 0 ) );
	  }
	    
	  clientFactory.getBackendService().deleteUserApp(clientFactory.getLocalSession().getSession(), ids,
								new AsyncCallback<Void>() {
									@Override
									public void onFailure(Throwable caught) {
									// TODO Auto-generated method stub
										clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.ERROR, FOOTERVIEW_FAILED_TO_DEL_USERAPP[1], FooterView.DEFAULT_STATUS_CLEAR_DELAY );
										clientFactory.getShellView( ).getLogView( ).log( LogType.ERROR, FOOTERVIEW_FAILED_TO_DEL_USERAPP[1] + ":" + caught.getMessage( ) );
									}
									@Override
									public void onSuccess(Void result) {
									// TODO Auto-generated method stub
										clientFactory.getShellView( ).getFooterView().showStatus( StatusType.NONE, FOOTERVIEW_DEL_USERAPP[1], FooterView.DEFAULT_STATUS_CLEAR_DELAY );
										clientFactory.getShellView( ).getLogView().log( LogType.ERROR, FOOTERVIEW_DEL_USERAPP[1]);
										
										reloadCurrentRange();
										updateUserAppCountInfo();
									}
	  						}
			  			);
  }
  
  private void updateUserAppCountInfo() {	  
	  clientFactory.getBackendService().countUserApp(clientFactory.getLocalSession().getSession(),
					new AsyncCallback<ArrayList<UserAppStateCount>>() {
							@Override
							public void onFailure(Throwable caught) {
							// TODO Auto-generated method stub
							}
							@Override
							public void onSuccess(ArrayList<UserAppStateCount> countInfo ) {
							// TODO Auto-generated method stub
								clientFactory.getUserAppView().updateCountInfo(countInfo);
							}
					}
			  );
  }
  
  @Override
  public void onClick(int rowIndex, int colIndex, SearchResultRow row) {
  	// TODO Auto-generated method stub
  	Window.alert(row.getLink(colIndex));
  }
}