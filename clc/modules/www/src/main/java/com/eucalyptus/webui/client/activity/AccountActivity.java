package com.eucalyptus.webui.client.activity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.eucalyptus.webui.client.ClientFactory;
import com.eucalyptus.webui.client.place.AccountPlace;
import com.eucalyptus.webui.client.service.LanguageSelection;
import com.eucalyptus.webui.client.service.SearchRange;
import com.eucalyptus.webui.client.service.SearchResultRow;
import com.eucalyptus.webui.client.service.SearchResult;
import com.eucalyptus.webui.client.view.AccountView;
import com.eucalyptus.webui.client.view.ConfirmationView;
import com.eucalyptus.webui.client.view.FooterView;
import com.eucalyptus.webui.client.view.AccountAddView;
import com.eucalyptus.webui.client.view.FooterView.StatusType;
import com.eucalyptus.webui.client.view.HasValueWidget;
import com.eucalyptus.webui.client.view.LogView.LogType;
import com.eucalyptus.webui.shared.dictionary.Enum2String;
import com.eucalyptus.webui.shared.user.AccountInfo;
import com.eucalyptus.webui.shared.user.EnumState;
import com.eucalyptus.webui.shared.user.LoginUserProfile;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class AccountActivity extends AbstractSearchActivity
    implements AccountView.Presenter, ConfirmationView.Presenter, AccountAddView.Presenter {
  
  public static final String[] TITLE = {"ACCOUNTS", "账户"};
  
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
  private String[] ACCOUNT_ACTIVITY_ONE_SELECTION = {"Select one account", "必须选择一个账户"};
  
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
  
  @Override
  public void saveValue( ArrayList<String> keys, ArrayList<HasValueWidget> values ) {
  }

  @Override
  protected String getTitle( ) {
	  int lan = LanguageSelection.instance().getCurLanguage().ordinal();
	  return TITLE[lan];
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
	LoginUserProfile curUser = this.clientFactory.getSessionData().getLoginUser();
		
	if (curUser.isSystemAdmin()) {
		final AccountAddView window = clientFactory.getAccountAddView();
		window.setPresenter(this);
		window.display(this.clientFactory);
	}
  }
  
	@Override
	public void onModifyAccount() {
		// TODO Auto-generated method stub
		if (oneSelectionIsValid())
			this.doModifyAccount();
	}
	
	@Override
  	public void onDeleteAccounts( ) {
  		if (!selectionIsValid())
  			return;
    
  		ConfirmationView dialog = this.clientFactory.getConfirmationView( );
  		dialog.setPresenter( this );
  		int lan = LanguageSelection.instance().getCurLanguage().ordinal();
  		dialog.display( DELETE_ACCOUNTS_CAPTION[lan], DELETE_ACCOUNTS_SUBJECT[lan], currentSelected, new ArrayList<Integer>( Arrays.asList( 0, 1 ) ) );
  }
  
	@Override
	public void onResume( ) {
		// TODO Auto-generated method stub
		if (!selectionIsValid())
			return;
	  
		ConfirmationView confirmView = this.clientFactory.getConfirmationView();
		confirmView.setPresenter(this);
	  
		int lan = LanguageSelection.instance().getCurLanguage().ordinal();
		confirmView.display(RESUME_ACCOUNTS_CAPTION[lan], RESUME_ACCOUNTS_SUBJECT[lan]);
  }
  
	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		if (!selectionIsValid())
			return;
	  
		ConfirmationView confirmView = this.clientFactory.getConfirmationView();
		confirmView.setPresenter(this);
	  
		int lan = LanguageSelection.instance().getCurLanguage().ordinal();
		confirmView.display(PAUSE_ACCOUNTS_CAPTION[lan], PAUSE_ACCOUNTS_SUBJECT[lan]);
	}
  
	@Override
	public void onBan( ) {
		// TODO Auto-generated method stub
		if (!selectionIsValid())
			return;
	  
		ConfirmationView confirmView = this.clientFactory.getConfirmationView();
		confirmView.setPresenter(this);
	  
		int lan = LanguageSelection.instance().getCurLanguage().ordinal();
		confirmView.display(BAN_ACCOUNTS_CAPTION[lan], BAN_ACCOUNTS_SUBJECT[lan]);
	}
  

	@Override
	public void confirm( String subject ) {
		int lan = LanguageSelection.instance().getCurLanguage().ordinal();
		
		if ( DELETE_ACCOUNTS_SUBJECT[lan].equals( subject ) ) {
			doDeleteAccounts( );
		}
		else if (RESUME_ACCOUNTS_SUBJECT[lan].equals(subject)) {
			doUpdateUserStateByAccounts(ACCOUNT_ACTIVITY_FOOTVIEW_RESUME_ACCOUNT[lan], EnumState.NORMAL);
		}
		else if (PAUSE_ACCOUNTS_SUBJECT[lan].equals(subject)) {
			doUpdateUserStateByAccounts(ACCOUNT_ACTIVITY_FOOTVIEW_PAUSE_ACCOUNT[lan], EnumState.PAUSE);
		}
		else if (BAN_ACCOUNTS_SUBJECT[lan].equals(subject)) {
			doUpdateUserStateByAccounts(ACCOUNT_ACTIVITY_FOOTVIEW_BAN_ACCOUNT[lan], EnumState.BAN);
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
		
		final int lan = LanguageSelection.instance().getCurLanguage().ordinal();
	  
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
			int lan = LanguageSelection.instance().getCurLanguage().ordinal();
			clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.ERROR, ACCOUNT_ACTIVITY_NO_SELECTION[lan], FooterView.DEFAULT_STATUS_CLEAR_DELAY );
			return false;
		}
	  
		return true;
	}
	
	private boolean oneSelectionIsValid() {
		if ( currentSelected == null || currentSelected.size( ) != 1 ) {
			int lan = LanguageSelection.instance().getCurLanguage().ordinal();
			clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.ERROR, ACCOUNT_ACTIVITY_ONE_SELECTION[lan], FooterView.DEFAULT_STATUS_CLEAR_DELAY );
			return false;
		}
	  
		return true;
	}
	
	private void doModifyAccount() {
		if ( this.currentSelected == null || this.currentSelected.size( ) != 1 )
	    	return;
	    
		SearchResultRow row = this.currentSelected.toArray(new SearchResultRow[0])[0];
		
		final AccountInfo account = new AccountInfo();
		account.setId(Integer.parseInt(row.getField(0)));
		account.setName(row.getField(2));
		account.setEmail(row.getField(3));
		account.setDescription(row.getField(4));
		
		String groupState = row.getField(5);		
		EnumState state = Enum2String.getInstance().getEnumState(groupState);
		account.setState(state);
		
		String accountId = row.getField(6);

		if (!Strings.isNullOrEmpty(accountId))
			account.setId(Integer.parseInt(accountId));
		
		final AccountAddView window = clientFactory.getAccountAddView();
  		window.setPresenter(this);
  		window.display(this.clientFactory);
  		window.setAccount(account);
	}

	@Override
	public void onDoubleClick(DoubleClickEvent event) {
		// TODO Auto-generated method stub
		if (oneSelectionIsValid())
			this.doModifyAccount();
	}

	private static String[] ACCOUNT_ACTIVITY_FOOTERVIEW_MODIFY_GROUP_SUCCEED = {"Successfully modified account", "修改账户成功"};
	private static String[] ACCOUNT_ACTIVITY_FOOTERVIEW_MODIFY_GROUP_FAIL = {"Failed to modify account", "修改账户失败"};
	@Override
	public void process(final AccountInfo account) {
		// TODO Auto-generated method stub
		this.clientFactory.getBackendService( ).createAccount( this.clientFactory.getLocalSession( ).getSession( ), account, new AsyncCallback<Void>( ) {

		      @Override
		      public void onFailure( Throwable caught ) {
		    	  ActivityUtil.logoutForInvalidSession( clientFactory, caught );
		    	  int lan = LanguageSelection.instance().getCurLanguage().ordinal();
		    	  if (account.getId() == 0) {
		    		  clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.ERROR, "Failed to create account", FooterView.DEFAULT_STATUS_CLEAR_DELAY );
		    		  clientFactory.getShellView( ).getLogView( ).log( LogType.ERROR, "Creating account " + account.getName() + " failed: " + caught.getMessage( ) );
		    	  }
		    	  else {
		    		  clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.ERROR, ACCOUNT_ACTIVITY_FOOTERVIEW_MODIFY_GROUP_FAIL[lan], FooterView.DEFAULT_STATUS_CLEAR_DELAY );
		    		  clientFactory.getShellView( ).getLogView( ).log( LogType.ERROR, "Failed to modify account " + account.getName()  + ": " + caught.getMessage( ) );
			      }
		      }

		      @Override
		      public void onSuccess( Void result) {
		    	  if (account.getId() == 0) {
		    		  clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.NONE, "Account " + account.getName() + " created", FooterView.DEFAULT_STATUS_CLEAR_DELAY );
		    		  clientFactory.getShellView( ).getLogView( ).log( LogType.INFO, "New account " + account.getName() + " created" );
		    	  }
		    	  else {
		    		  int lan = LanguageSelection.instance().getCurLanguage().ordinal();
		    		  clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.NONE, ACCOUNT_ACTIVITY_FOOTERVIEW_MODIFY_GROUP_SUCCEED[lan], FooterView.DEFAULT_STATUS_CLEAR_DELAY );
		    		  clientFactory.getShellView( ).getLogView( ).log( LogType.INFO, "Modified account " + account.getName());
		    	  }
		    	  
		    	  reloadCurrentRange( );
		      }
		    } );
	}
}
