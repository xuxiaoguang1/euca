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
import com.eucalyptus.webui.client.service.ViewSearchTableClientConfig;
import com.eucalyptus.webui.client.view.AccountView;
import com.eucalyptus.webui.client.view.ConfirmationView;
import com.eucalyptus.webui.client.view.FooterView;
import com.eucalyptus.webui.client.view.AccountAddView;
import com.eucalyptus.webui.client.view.InputField;
import com.eucalyptus.webui.client.view.InputView;
import com.eucalyptus.webui.client.view.FooterView.StatusType;
import com.eucalyptus.webui.client.view.HasValueWidget;
import com.eucalyptus.webui.client.view.LogView.LogType;
import com.eucalyptus.webui.shared.checker.ValueChecker;
import com.eucalyptus.webui.shared.checker.ValueCheckerFactory;
import com.eucalyptus.webui.shared.config.EnumService;
import com.eucalyptus.webui.shared.config.LanguageSelection;
import com.eucalyptus.webui.shared.dictionary.ConfDef;
import com.eucalyptus.webui.shared.dictionary.DBTableColName;
import com.eucalyptus.webui.shared.dictionary.Enum2String;
import com.eucalyptus.webui.shared.user.AccountInfo;
import com.eucalyptus.webui.shared.user.EnumState;
import com.eucalyptus.webui.shared.user.LoginUserProfile;
import com.google.common.collect.Lists;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class AccountActivity extends AbstractSearchActivity
    implements AccountView.Presenter, ConfirmationView.Presenter, AccountAddView.Presenter, InputView.Presenter {
  
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
  
  private final String[] ALERT_NOT_DEL_ROOT_ACCOUNT = {"Can not delete root account", "根账户不能删除"};
  
  public static final String[] ADD_POLICY_CAPTION = {"Add new policy", "增加策略"};
  public static final String[] ADD_POLICY_SUBJECT = {"Enter new policy to assign to the selected account:", "为选中的账户输入策略"};
  public static final String[] POLICY_NAME_INPUT_TITLE = {"Policy name", "策略名称"};
  public static final String[] POLICY_CONTENT_INPUT_TITLE = {"Policy content", "策略内容"};
  
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
  public int getPageSize() {
	  return ViewSearchTableClientConfig.instance().getPageSize(EnumService.ACCOUNT_SRV);
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
  		dialog.display( DELETE_ACCOUNTS_CAPTION[lan], DELETE_ACCOUNTS_SUBJECT[lan]);
	}
	
	@Override
	public void onAddPolicy() {
		// TODO Auto-generated method stub
		if (!oneSelectionIsValid())
			return;

		InputView dialog = this.clientFactory.getInputView();
		dialog.setPresenter(this);
		final int lan = LanguageSelection.instance().getCurLanguage().ordinal();
		dialog.display(ADD_POLICY_CAPTION[lan], ADD_POLICY_SUBJECT[lan],
				new ArrayList<InputField>(Arrays.asList(
					new InputField() {
	
						@Override
						public String getTitle() {
							return POLICY_NAME_INPUT_TITLE[lan];
						}
	
						@Override
						public ValueType getType() {
							return ValueType.TEXT;
						}
	
						@Override
						public ValueChecker getChecker() {
							return ValueCheckerFactory.createPolicyNameChecker();
						}
	
					}, 
					new InputField() {
	
						@Override
						public String getTitle() {
							return POLICY_CONTENT_INPUT_TITLE[lan];
						}
	
						@Override
						public ValueType getType() {
							return ValueType.TEXTAREA;
						}
	
						@Override
						public ValueChecker getChecker() {
							return ValueCheckerFactory.createNonEmptyValueChecker();
						}

				})));
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
		
		final int lan = LanguageSelection.instance().getCurLanguage().ordinal();
    
		final ArrayList<String> ids = Lists.newArrayList( ); 
		for ( SearchResultRow row : currentSelected ) {
			
			String accountName = row.getField(2);
		    	if (accountName.equals(ConfDef.ROOT_ACCOUNT)) {
		    		Window.alert(ALERT_NOT_DEL_ROOT_ACCOUNT[lan]);
		    		return;
		    }
		    	
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
		
		int idColIndex = ViewSearchTableClientConfig.instance().getSearchTableColIndex(EnumService.ACCOUNT_SRV, DBTableColName.ACCOUNT.ID);
		if (idColIndex >= 0) 
			account.setId(Integer.parseInt(row.getField(idColIndex)));
		
		int nameColIndex = ViewSearchTableClientConfig.instance().getSearchTableColIndex(EnumService.ACCOUNT_SRV, DBTableColName.ACCOUNT.NAME);
		if (nameColIndex >= 0) 
			account.setName(row.getField(nameColIndex));
		
		int emailColIndex = ViewSearchTableClientConfig.instance().getSearchTableColIndex(EnumService.ACCOUNT_SRV, DBTableColName.ACCOUNT.EMAIL);
		if (emailColIndex >= 0) 
			account.setEmail(row.getField(emailColIndex));
		
		int desColIndex = ViewSearchTableClientConfig.instance().getSearchTableColIndex(EnumService.ACCOUNT_SRV, DBTableColName.ACCOUNT.DES);
		if (desColIndex >= 0) 
			account.setDescription(row.getField(desColIndex));
		
		int stateColIndex = ViewSearchTableClientConfig.instance().getSearchTableColIndex(EnumService.ACCOUNT_SRV, DBTableColName.ACCOUNT.STATE);
		if (stateColIndex >= 0) {
			String state = row.getField(stateColIndex);
			EnumState accountState = Enum2String.getInstance().getEnumState(state);
			account.setState(accountState);
		}
		
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

	@Override
	public void process(String subject, ArrayList<String> values) {
		// TODO Auto-generated method stub
		final int lan = LanguageSelection.instance().getCurLanguage().ordinal();
		if (ADD_POLICY_SUBJECT[lan].equals(subject)) {
			doAddPolicy(values.get(0), values.get(1));
		}
	}
	
	private void doAddPolicy( final String name, final String document ) {
		if ( currentSelected == null || currentSelected.size( ) != 1 ) {
			return;
		}
		
		final String accountId = this.currentSelected.toArray( new SearchResultRow[0] )[0].getField( 0 );
		this.clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.LOADING, "Adding policy " + name + " ...", 0 );
		      
		this.clientFactory.getBackendService( ).addAccountPolicy(this.clientFactory.getLocalSession( ).getSession( ), accountId, name, document, new AsyncCallback<Void>() {  
			@Override
			public void onFailure( Throwable caught ) {
				ActivityUtil.logoutForInvalidSession( clientFactory, caught );
			    clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.ERROR, "Failed to add policy", FooterView.DEFAULT_STATUS_CLEAR_DELAY );
			    clientFactory.getShellView( ).getLogView( ).log( LogType.ERROR, "Failed to add policy " + name + " for user " + accountId + ": " + caught.getMessage( ) );
			}
			  
			@Override
			public void onSuccess( Void arg ) {
				clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.NONE, "Policy added", FooterView.DEFAULT_STATUS_CLEAR_DELAY );
			    clientFactory.getShellView( ).getLogView( ).log( LogType.INFO, "New policy " + name + " is added to user " + accountId );
			    reloadCurrentRange( );
			}
		});
	}
}
