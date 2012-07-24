package com.eucalyptus.webui.client.activity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.eucalyptus.webui.client.ClientFactory;
import com.eucalyptus.webui.client.place.AccountPlace;
import com.eucalyptus.webui.client.service.SearchRange;
import com.eucalyptus.webui.client.service.SearchResultRow;
import com.eucalyptus.webui.client.service.SearchResult;
import com.eucalyptus.webui.client.view.AccountView;
import com.eucalyptus.webui.client.view.ConfirmationView;
import com.eucalyptus.webui.client.view.FooterView;
import com.eucalyptus.webui.client.view.InputField;
import com.eucalyptus.webui.client.view.InputView;
import com.eucalyptus.webui.client.view.FooterView.StatusType;
import com.eucalyptus.webui.client.view.HasValueWidget;
import com.eucalyptus.webui.client.view.LogView.LogType;
import com.eucalyptus.webui.shared.checker.ValueChecker;
import com.eucalyptus.webui.shared.checker.ValueCheckerFactory;
import com.eucalyptus.webui.shared.user.EnumState;
import com.google.common.collect.Lists;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class AccountActivity extends AbstractSearchActivity
    implements AccountView.Presenter, ConfirmationView.Presenter, InputView.Presenter {
  
  public static final String TITLE = "ACCOUNTS";
  
  public static final String[] CREATE_ACCOUNT_CAPTION = {"Create a new account", "增加一个账户"};
  public static final String[] CREATE_ACCOUNT_SUBJECT = {"Enter information to create a new account:", "输入新账户的信息"};
  public static final String[] ACCOUNT_NAME_INPUT_TITLE = {"Account name", "账户名称"};
  
  public static final String[] DELETE_ACCOUNTS_CAPTION = {"Delete selected accounts", "删除所选账户"};
  public static final String[] DELETE_ACCOUNTS_SUBJECT = {"Are you sure you want to delete following selected accounts?", "确定要删除所选账户?"};

  public static final String[] RESUME_ACCOUNTS_CAPTION = {"Approve selected accounts", "激活账户"};
  public static final String[] RESUME_ACCOUNTS_SUBJECT = {"Are you sure you want to approve following selected accounts?", "确定要激活所选择的账户？"};

  public static final String[] PAUSE_ACCOUNTS_CAPTION = {"Pause selected accounts", "暂停账户"};
  public static final String[] PAUSE_ACCOUNTS_SUBJECT = {"Are you sure you want to pause following selected accounts?", "确定要暂停所选择的账户？"};

  public static final String[] BAN_ACCOUNTS_CAPTION = {"Reject selected accounts", "禁止账户"};
  public static final String[] BAN_ACCOUNTS_SUBJECT = {"Are you sure you want to reject following selected accounts?", "确定要禁止所选择的账户？"};

  private String[] ACCOUNT_ACTIVITY_NO_SELECTION = {"Select at least one account", "至少选择一个账户"};
  
  private String[] ACCOUNT_ACTIVITY_FOOTVIEW_RESUME_ACCOUNT = {"Select accounts to resume", "激活账户"};
  private String[] ACCOUNT_ACTIVITY_FOOTVIEW_PAUSE_ACCOUNT = {"Select accounts to pause", "暂停账户"};
  private String[] ACCOUNT_ACTIVITY_FOOTVIEW_BAN_ACCOUNT = {"Select accounts to ban", "禁止账户"};
    
  public static final String[] EMAIL_INPUT_TITLE = {"Admin email", "管理员电子邮件"};
  public static final String[] DESCRIPTION_INPUT_TITLE = {"Admin description", "备注"};
  
  private static final Logger LOG = Logger.getLogger( AccountActivity.class.getName( ) );

  private Set<SearchResultRow> currentSelected;
    
  public AccountActivity( AccountPlace place, ClientFactory clientFactory ) {
    super( place, clientFactory );
  }

  protected void doSearch( final String search, SearchRange range ) {    
    this.clientFactory.getBackendService( ).lookupAccount( this.clientFactory.getLocalSession( ).getSession( ), search, range,
                                                           new AsyncCallback<SearchResult>( ) {
      
      @Override
      public void onFailure( Throwable caught ) {
        ActivityUtil.logoutForInvalidSession( clientFactory, caught );
        LOG.log( Level.WARNING, "Search failed: " + caught );
        clientFactory.getShellView( ).getLogView( ).log( LogType.ERROR, "Account search " + search + " failed: " + caught.getMessage( ) );
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
    } else {
      LOG.log( Level.INFO, "Selection changed to " + selection );
      this.clientFactory.getShellView( ).showDetail( DETAIL_PANE_SIZE );
      showSingleSelectedDetails( selection.toArray( new SearchResultRow[0] )[0] );
    }
  }

  private static String[] ACCOUNT_ACTIVITY_FOOTERVIEW_MODIFY_GROUP_SUCCEED = {"Successfully modified account", "修改账户成功"};
  private static String[] ACCOUNT_ACTIVITY_FOOTERVIEW_MODIFY_GROUP_FAIL = {"Failed to modify account", "修改账户失败"};
  
  @Override
  public void saveValue( ArrayList<String> keys, ArrayList<HasValueWidget> values ) {
    final ArrayList<String> newVals = Lists.newArrayList( );
    for ( HasValueWidget w : values ) {
      newVals.add( w.getValue( ) );
    }
    
    final String accountId = emptyForNull( getField( newVals, 0 ) );
    this.clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.LOADING, "Modifying account " + accountId + " ...", 0 );
    
    String name = getField(newVals, 1);
    String email = getField(newVals, 2);
    
    clientFactory.getBackendService( ).modifyAccount( clientFactory.getLocalSession( ).getSession( ), Integer.valueOf(accountId), name, email, new AsyncCallback<Void>( ) {

      @Override
      public void onFailure( Throwable caught ) {
        ActivityUtil.logoutForInvalidSession( clientFactory, caught );
        clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.ERROR, ACCOUNT_ACTIVITY_FOOTERVIEW_MODIFY_GROUP_FAIL[1], FooterView.DEFAULT_STATUS_CLEAR_DELAY );
        clientFactory.getShellView( ).getLogView( ).log( LogType.ERROR, "Failed to modify account " + accountId  + ": " + caught.getMessage( ) );
      }

      @Override
      public void onSuccess( Void arg0 ) {
        clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.NONE, ACCOUNT_ACTIVITY_FOOTERVIEW_MODIFY_GROUP_SUCCEED[1], FooterView.DEFAULT_STATUS_CLEAR_DELAY );
        clientFactory.getShellView( ).getLogView( ).log( LogType.INFO, "Modified account " + accountId );
        //clientFactory.getShellView( ).getDetailView( ).disableSave( );
        reloadCurrentRange( );
      }
      
    } );
  }

  @Override
  protected String getTitle( ) {
    return TITLE;
  }

  @Override
  protected void showView( SearchResult result ) {
    if ( this.view == null ) {
      this.view = this.clientFactory.getAccountView( );
      ( ( AccountView ) this.view ).setPresenter( this );
      container.setWidget( this.view );
      ( ( AccountView ) this.view ).clear( );
    }
    // Turn on/off account action buttons based on if the user is system admin
    boolean isSystemAdmin = this.clientFactory.getSessionData( ).getLoginUser( ).isSystemAdmin( );
    ( ( AccountView ) this.view ).enableNewButton( isSystemAdmin );
    ( ( AccountView ) this.view ).enableDelButton( isSystemAdmin );
    
    ( ( AccountView ) this.view ).showSearchResult( result );    
  }

  @Override
  public void onCreateAccount( ) {
    InputView dialog = this.clientFactory.getInputView( );
    dialog.setPresenter( this );
    dialog.display( CREATE_ACCOUNT_CAPTION[1], CREATE_ACCOUNT_SUBJECT[1], new ArrayList<InputField>( Arrays.asList( new InputField( ) {

      @Override
      public String getTitle( ) {
        return ACCOUNT_NAME_INPUT_TITLE[1];
      }

      @Override
      public ValueType getType( ) {
        return ValueType.TEXT;
      }

      @Override
      public ValueChecker getChecker( ) {
        return ValueCheckerFactory.createAccountNameChecker( );
      }
      
    }, new InputField( ) {

      @Override
      public String getTitle( ) {
        return EMAIL_INPUT_TITLE[1];
      }

      @Override
      public ValueType getType( ) {
        return ValueType.TEXT;
      }

      @Override
      public ValueChecker getChecker( ) {
        return ValueCheckerFactory.createEmailChecker( );
      }
      
    },
    new InputField( ) {

        @Override
        public String getTitle( ) {
          return DESCRIPTION_INPUT_TITLE[1];
        }

        @Override
        public ValueType getType( ) {
          return ValueType.TEXT;
        }

        @Override
        public ValueChecker getChecker( ) {
          return null;
        }
        
      }) ) );
  }

  @Override
  public void process( String subject, ArrayList<String> values ) {
    if ( CREATE_ACCOUNT_SUBJECT[1].equals( subject ) ) {
    	doCreateAccount( values );
    }
  }

  private void doCreateAccount( final ArrayList<String> values ) {
    this.clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.LOADING, "Creating account " + values.get(0) + " ...", 0 );
    
    this.clientFactory.getBackendService( ).createAccount( this.clientFactory.getLocalSession( ).getSession( ), values, new AsyncCallback<String>( ) {

      @Override
      public void onFailure( Throwable caught ) {
        ActivityUtil.logoutForInvalidSession( clientFactory, caught );
        clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.ERROR, "Failed to create account", FooterView.DEFAULT_STATUS_CLEAR_DELAY );
        clientFactory.getShellView( ).getLogView( ).log( LogType.ERROR, "Creating account " + values.get(0) + " failed: " + caught.getMessage( ) );
      }

      @Override
      public void onSuccess( String accountId ) {
        clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.NONE, "Account " + accountId + " created", FooterView.DEFAULT_STATUS_CLEAR_DELAY );
        clientFactory.getShellView( ).getLogView( ).log( LogType.INFO, "New account " + accountId + " created" );
        reloadCurrentRange( );
      }
      
    } );
  }
  
  	@Override
  	public void onDeleteAccounts( ) {
  		if (!selectionIsValid())
  			return;
    
  		ConfirmationView dialog = this.clientFactory.getConfirmationView( );
  		dialog.setPresenter( this );
  		dialog.display( DELETE_ACCOUNTS_CAPTION[1], DELETE_ACCOUNTS_SUBJECT[1], currentSelected, new ArrayList<Integer>( Arrays.asList( 0, 1 ) ) );
  }
  
	@Override
	public void onResume( ) {
		// TODO Auto-generated method stub
		if (!selectionIsValid())
			return;
	  
		ConfirmationView confirmView = this.clientFactory.getConfirmationView();
		confirmView.setPresenter(this);
	  
		confirmView.display(RESUME_ACCOUNTS_CAPTION[1], RESUME_ACCOUNTS_SUBJECT[1]);
  }
  
	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		if (!selectionIsValid())
			return;
	  
		ConfirmationView confirmView = this.clientFactory.getConfirmationView();
		confirmView.setPresenter(this);
	  
		confirmView.display(PAUSE_ACCOUNTS_CAPTION[1], PAUSE_ACCOUNTS_SUBJECT[1]);
	}
  
	@Override
	public void onBan( ) {
		// TODO Auto-generated method stub
		if (!selectionIsValid())
			return;
	  
		ConfirmationView confirmView = this.clientFactory.getConfirmationView();
		confirmView.setPresenter(this);
	  
		confirmView.display(BAN_ACCOUNTS_CAPTION[1], BAN_ACCOUNTS_SUBJECT[1]);
	}
  

	@Override
	public void confirm( String subject ) {
		if ( DELETE_ACCOUNTS_SUBJECT[1].equals( subject ) ) {
			doDeleteAccounts( );
		}
		else if (RESUME_ACCOUNTS_SUBJECT[1].equals(subject)) {
			doUpdateUserStateByAccounts(ACCOUNT_ACTIVITY_FOOTVIEW_RESUME_ACCOUNT[1], EnumState.NORMAL);
		}
		else if (PAUSE_ACCOUNTS_SUBJECT[1].equals(subject)) {
			doUpdateUserStateByAccounts(ACCOUNT_ACTIVITY_FOOTVIEW_PAUSE_ACCOUNT[1], EnumState.PAUSE);
		}
		else if (BAN_ACCOUNTS_SUBJECT[1].equals(subject)) {
			doUpdateUserStateByAccounts(ACCOUNT_ACTIVITY_FOOTVIEW_BAN_ACCOUNT[1], EnumState.BAN);
		}
	}
  
	private void doDeleteAccounts( ) {
		if ( currentSelected == null || currentSelected.size( ) < 1 ) {
			return;
		}
    
		final ArrayList<String> ids = Lists.newArrayList( ); 
		for ( SearchResultRow row : currentSelected ) {
			ids.add( row.getField( 0 ) );
		}
    
		clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.LOADING, "Deleting accounts ...", 0 );
		clientFactory.getBackendService( ).deleteAccounts( clientFactory.getLocalSession( ).getSession( ), ids, new AsyncCallback<Void>( ) {

			@Override
			public void onFailure( Throwable caught ) {
				ActivityUtil.logoutForInvalidSession( clientFactory, caught );
				clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.ERROR, "Failed to delete accounts", FooterView.DEFAULT_STATUS_CLEAR_DELAY );
				clientFactory.getShellView( ).getLogView( ).log( LogType.ERROR, "Failed to delete accounts " + ids + ": " + caught.getMessage( ) );
			}

			@Override
			public void onSuccess( Void arg0 ) {
				clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.NONE, "Accounts deleted", FooterView.DEFAULT_STATUS_CLEAR_DELAY );
				clientFactory.getShellView( ).getLogView( ).log( LogType.INFO, "Accounts " + ids + " deleted" );
				reloadCurrentRange( );
			}
		} );
	}

	private void doUpdateUserStateByAccounts(final String footerViewMsg, EnumState userState) {
		// Pick account names
		final ArrayList<String> ids = Lists.newArrayList( ); 
		for ( SearchResultRow row : currentSelected ) {
			ids.add( row.getField(0) );
		}
	  
		clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.LOADING, footerViewMsg + "...", 0 );
		clientFactory.getBackendService( ).updateAccountState( clientFactory.getLocalSession( ).getSession( ), 
			  														ids,
			  														userState,
			  														new AsyncCallback<Void>( ) {

			@Override
			public void onFailure( Throwable caught ) {
				ActivityUtil.logoutForInvalidSession( clientFactory, caught );
				clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.ERROR, footerViewMsg, FooterView.DEFAULT_STATUS_CLEAR_DELAY );
				clientFactory.getShellView( ).getLogView( ).log( LogType.ERROR, footerViewMsg + ids + ": " + caught.getMessage( ) );
			}

			@Override
			public void onSuccess( Void result) {
				clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.NONE, footerViewMsg, FooterView.DEFAULT_STATUS_CLEAR_DELAY );
				clientFactory.getShellView( ).getLogView( ).log( LogType.INFO, footerViewMsg + ids );
				reloadCurrentRange( );
			}
      
		} );
	}
 
	private boolean selectionIsValid() {
		if ( currentSelected == null || currentSelected.size( ) < 1 ) {
			clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.ERROR, ACCOUNT_ACTIVITY_NO_SELECTION[1], FooterView.DEFAULT_STATUS_CLEAR_DELAY );
			return false;
		}
	  
		return true;
	}
}
