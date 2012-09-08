package com.eucalyptus.webui.client.activity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.eucalyptus.webui.client.ClientFactory;
import com.eucalyptus.webui.client.place.SearchPlace;
import com.eucalyptus.webui.client.service.LanguageSelection;
import com.eucalyptus.webui.client.service.SearchRange;
import com.eucalyptus.webui.client.service.SearchResult;
import com.eucalyptus.webui.client.service.SearchResultRow;
import com.eucalyptus.webui.client.view.ConfirmationView;
import com.eucalyptus.webui.client.view.FooterView;
import com.eucalyptus.webui.client.view.GroupAddView;
import com.eucalyptus.webui.client.view.GroupView;
import com.eucalyptus.webui.client.view.HasValueWidget;
import com.eucalyptus.webui.client.view.FooterView.StatusType;
import com.eucalyptus.webui.client.view.LogView.LogType;
import com.eucalyptus.webui.client.view.InputView;
import com.eucalyptus.webui.shared.dictionary.Enum2String;
import com.eucalyptus.webui.shared.user.AccountInfo;
import com.eucalyptus.webui.shared.user.EnumState;
import com.eucalyptus.webui.shared.user.GroupInfo;
import com.eucalyptus.webui.shared.user.LoginUserProfile;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class GroupActivity extends AbstractSearchActivity
    implements GroupView.Presenter, ConfirmationView.Presenter, InputView.Presenter, GroupAddView.Presenter {
  
  public static final String TITLE[] = {"GROUPS", "用户组"};
  
  public static final String DELETE_GROUPS_CAPTION[] = {"Delete selected groups", "删除所选的用户组"};
  public static final String DELETE_GROUPS_SUBJECT[] = {"Are you sure you want to delete following selected groups?", "确定要删除所选择的用户组"};
  
  private final static String[] RESUME_GROUPS_CAPTION = {"Resume the group", "激活组"};
  private final static String[] RESUME_GROUPS_SUBJECT = {"Are you sure you resume the group?", "确定要激活该组？"};
  
  private final static String[] PAUSE_GROUPS_CAPTION = {"Pause the group", "暂停组"};
  private final static String[] PAUSE_GROUPS_SUBJECT = {"Are you sure you pause the group?", "确定要暂停该组？"};
  
  private final static String[] BAN_GROUPS_CAPTION = {"Ban the group", "禁止组"};
  private final static String[] BAN_GROUPS_SUBJECT = {"Are you sure you ban the group?", "确定要禁止该组？"};
  
  public static final String ADD_USERS_CAPTION[] = {"Add users to selected groups", "将用户加入所选择的组"};
  public static final String ADD_USERS_SUBJECT[] = {"Enter users to add to selected groups (using space to separate names):", "输入需要加入组的用户姓名（用空格隔开）"};
  public static final String USER_NAMES_INPUT_TITLE[] = {"User names", "用户姓名"};

  public static final String REMOVE_USERS_CAPTION[] = {"Remove users from selected groups", "从所选的组中删除用户"};
  public static final String REMOVE_USERS_SUBJECT[] = {"Enter users to remove from selected groups (using space to separate names):", "输入需要从组中删除的用户姓名（用空格隔开）"};

  public static final String ADD_POLICY_CAPTION[] = {"Add new policy", "增加新的策略"};
  public static final String ADD_POLICY_SUBJECT[] = {"Enter new policy to assign to the selected group:", "给选定的组增加新的策略"};
  public static final String POLICY_NAME_INPUT_TITLE[] = {"Policy name", "策略名称"};
  public static final String POLICY_CONTENT_INPUT_TITLE[] = {"Policy content", "策略内容"};

  private static final Logger LOG = Logger.getLogger( GroupActivity.class.getName( ) );
  
  private Set<SearchResultRow> currentSelected;
  
  private GroupDetailActivity groupDetailActivity;
  
  public GroupActivity( SearchPlace place, ClientFactory clientFactory ) {
    super( place, clientFactory );
    
    groupDetailActivity = new GroupDetailActivity(place, clientFactory);
  }

  @Override
  protected void doSearch( String query, SearchRange range ) {
    this.clientFactory.getBackendService( ).lookupGroup( this.clientFactory.getLocalSession( ).getSession( ), search, range,
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
    } else {
      LOG.log( Level.INFO, "Selection changed to " + selection );
    }
  }

	static String[] GROUP_VIEW_DETAIL = {"Group View", "组视图"};
	
	protected void showSingleSelectedDetails( SearchResultRow selected ) {
		String groupId = selected.getField(0);
		String accountId = selected.getField(1);
		
		this.groupDetailActivity.setGroupId(Integer.valueOf(groupId));

		//clientFactory.getGroupDetailView().setTitle(GROUP_VIEW_DETAIL[lan]);
		clientFactory.getGroupDetailView().setPresenter(this.groupDetailActivity);
		clientFactory.getGroupDetailView().setAccountId(Integer.valueOf(accountId));
		clientFactory.getGroupDetailView().setGroupId(Integer.valueOf(groupId));
	  
		this.clientFactory.getBackendService().lookupUserByGroupId(this.clientFactory.getLocalSession().getSession(), 
			  														Integer.valueOf(groupId), 
			  														range, 
			  														new AsyncCallback<SearchResult>  () {

																		@Override
																		public void onFailure(Throwable caught) {
																			// TODO Auto-generated method stub
																			
																		}

																		@Override
																		public void onSuccess(SearchResult result) {
																			// TODO Auto-generated method stub
																			
																			clientFactory.getGroupDetailView().showSearchResult(result);
																		}});
	}
  
  	


  public void saveValue( ArrayList<String> keys, ArrayList<HasValueWidget> values ) {
    final ArrayList<String> newVals = Lists.newArrayList( );
    for ( HasValueWidget w : values ) {
      newVals.add( w.getValue( ) );
    }
    
    final String groupId = emptyForNull( getField( newVals, 0 ) );
    this.clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.LOADING, "Modifying group " + groupId + " ...", 0 );
    
    clientFactory.getBackendService( ).modifyGroup( clientFactory.getLocalSession( ).getSession( ), newVals, new AsyncCallback<Void>( ) {

      @Override
      public void onFailure( Throwable caught ) {
        ActivityUtil.logoutForInvalidSession( clientFactory, caught );
        clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.ERROR, "Failed to modify group", FooterView.DEFAULT_STATUS_CLEAR_DELAY );
        clientFactory.getShellView( ).getLogView( ).log( LogType.ERROR, "Failed to modify group " + groupId  + ": " + caught.getMessage( ) );
      }

      @Override
      public void onSuccess( Void arg0 ) {
        clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.NONE, "Successfully modified group", FooterView.DEFAULT_STATUS_CLEAR_DELAY );
        clientFactory.getShellView( ).getLogView( ).log( LogType.INFO, "Modified group " + groupId );
        //clientFactory.getShellView( ).getDetailView( ).disableSave( );
        reloadCurrentRange( );
      }
      
    } );
  }


  @Override
  protected String getTitle( ) {
	  int lan = LanguageSelection.instance().getCurLanguage().ordinal();
	  return TITLE[lan];
  }

  @Override
  protected void showView( SearchResult result ) {
    if ( this.view == null ) {
      this.view = this.clientFactory.getGroupView( );
      ( ( GroupView ) this.view ).setPresenter( this );
      container.setWidget( this.view );
      ( ( GroupView ) this.view ).clear( );
    }
    ( ( GroupView ) this.view ).showSearchResult( result );    
  }
  
  @Override
  public void process(GroupInfo group) {
  	// TODO Auto-generated method stub
  	clientFactory.getBackendService().createGroup(this.clientFactory.getLocalSession( ).getSession( ), group, new AsyncCallback<Void> () {
			@Override
			public void onFailure(Throwable caught) {
				// TODO Auto-generated method stub
				ActivityUtil.logoutForInvalidSession( clientFactory, caught );
				clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.ERROR, "Failed to create group", FooterView.DEFAULT_STATUS_CLEAR_DELAY );
				clientFactory.getShellView( ).getLogView( ).log( LogType.ERROR, "Failed to create group " + ": " + caught.getMessage( ) );
			}
	
			@Override
			public void onSuccess(Void result) {
				// TODO Auto-generated method stub
				clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.NONE, "Successfully create group", FooterView.DEFAULT_STATUS_CLEAR_DELAY );
				clientFactory.getShellView( ).getLogView( ).log( LogType.INFO, "Create group");
				reloadCurrentRange( );
			}
		}
  	);
  }
  
  	@Override
  	public void onAddGroup() {
  		// TODO Auto-generated method stub
  		final GroupAddView window = clientFactory.getGroupAddView();
  		window.setPresenter(this);
  		window.display();
  		
  		LoginUserProfile curUser = this.clientFactory.getSessionData().getLoginUser();
  		
  		if (curUser.isSystemAdmin()) {
  			this.clientFactory.getBackendService().listAccounts(this.clientFactory.getLocalSession().getSession(), new AsyncCallback<ArrayList<AccountInfo>> () {
	
			@Override
			public void onFailure(Throwable caught) {
				// TODO Auto-generated method stub
			}
			@Override
			public void onSuccess(ArrayList<AccountInfo> result) {
				// TODO Auto-generated method stub
				window.setAccountsInfo(result);
			}});
  		}
 	}
  
  	@Override
	public void onModifyGroup() {
		// TODO Auto-generated method stub
		this.doModifyGroup();
	}
  	
  	@Override
  	public void onDeleteGroup( ) {
  		if ( currentSelected == null || currentSelected.size( ) < 1 ) {
  			clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.ERROR, "Select groups to delete", FooterView.DEFAULT_STATUS_CLEAR_DELAY );
  			return;
  		}
  		ConfirmationView dialog = this.clientFactory.getConfirmationView( );
  		dialog.setPresenter( this );
  		
  		int lan = LanguageSelection.instance().getCurLanguage().ordinal();
  		dialog.display( DELETE_GROUPS_CAPTION[lan], DELETE_GROUPS_SUBJECT[lan], currentSelected, new ArrayList<Integer>( Arrays.asList( 0, 1 ) ) );
  	}
  	
  	@Override
	public void showGroupDetails() {
		// TODO Auto-generated method stub
  		if ( this.currentSelected == null || this.currentSelected.size( ) != 1 )
	    	return;
  		
	    showSingleSelectedDetails( this.currentSelected.toArray( new SearchResultRow[0] )[0] );
	}
  	
  	private final static String[] GROUP_ACTIVITY_No_SELECTION = {"Please select at least on group", "请至少选择一个组"};
  	private final static String[] FOOTERVIEW_GROUP_ACTIVITY_UPDATE_USERSTATE_SUCCEED = {"Update user state succeeds", "更新用户状态成功"};
  	private final static String[] FOOTERVIEW_GROUP_ACTIVITY_UPDATE_USERSTATE_FAIL = {"Failed to update user state", "更新用户状态成功"};
 
  	@Override
	public void onResumeGroup() {
		// TODO Auto-generated method stub
		if (!selectionIsValid())
			return;
		
		ConfirmationView confirmView = this.clientFactory.getConfirmationView();
		confirmView.setPresenter(this);
	  
		int lan = LanguageSelection.instance().getCurLanguage().ordinal();
		confirmView.display(RESUME_GROUPS_CAPTION[lan], RESUME_GROUPS_SUBJECT[lan]);
	} 	
	@Override
	public void onPauseGroup() {
		// TODO Auto-generated method stub
		if (!selectionIsValid())
			return;
		
		ConfirmationView confirmView = this.clientFactory.getConfirmationView();
		confirmView.setPresenter(this);
	  
		int lan = LanguageSelection.instance().getCurLanguage().ordinal();
		confirmView.display(PAUSE_GROUPS_CAPTION[lan], PAUSE_GROUPS_SUBJECT[lan]);
	}
	@Override
	public void onBanGroup() {
		// TODO Auto-generated method stub
		if (!selectionIsValid())
			return;
		
		ConfirmationView confirmView = this.clientFactory.getConfirmationView();
		confirmView.setPresenter(this);
	  
		int lan = LanguageSelection.instance().getCurLanguage().ordinal();
		confirmView.display(BAN_GROUPS_CAPTION[lan], BAN_GROUPS_SUBJECT[lan]);
	}
	
 	@Override
  	public void confirm( String subject ) {
 		int lan = LanguageSelection.instance().getCurLanguage().ordinal();
	  if ( DELETE_GROUPS_SUBJECT[lan].equals( subject ) ) {
		  doDeleteGroups( );
	  }
	  else if ( RESUME_GROUPS_SUBJECT[lan].equals( subject ) ) {
		  updateUserStateByGroup(EnumState.NORMAL);
	  }
	  else if ( PAUSE_GROUPS_SUBJECT[lan].equals( subject ) ) {
		  updateUserStateByGroup(EnumState.PAUSE);
	  }
	  else if ( BAN_GROUPS_SUBJECT[lan].equals( subject ) ) {
		  updateUserStateByGroup(EnumState.BAN);
	  }
  	}
  	
 	private boolean selectionIsValid() {
		if ( currentSelected == null || currentSelected.size( ) < 1 ) {
			int lan = LanguageSelection.instance().getCurLanguage().ordinal();
			clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.ERROR, GROUP_ACTIVITY_No_SELECTION[lan], FooterView.DEFAULT_STATUS_CLEAR_DELAY );
			return false;
		}
	  
		return true;
	}
	
	
  	final static String[] FOOTERVIEW_GROUP_DELETING = {"Deleting groups ...", "正在删除用户组 ..."};
  	final static String[] FOOTERVIEW_GROUP_DELET_SUCCEED = {"Groups deleted", "删除用户组成功"};
  	final static String[] FOOTERVIEW_GROUP_DELET_FAIL = {"Failed to delete groups", "删除用户组失败"};

  	private void doDeleteGroups( ) {
  		if ( currentSelected == null || currentSelected.size( ) < 1 ) {
  			return;
  		}
    
  		final ArrayList<String> ids = Lists.newArrayList( ); 
  		
  		for ( SearchResultRow row : currentSelected ) {
  			ids.add( row.getField( 0 ) );
  		}
  		
  		final int lan = LanguageSelection.instance().getCurLanguage().ordinal();
    
  		clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.LOADING, FOOTERVIEW_GROUP_DELETING[lan], 0 );
    
  		clientFactory.getBackendService( ).deleteGroups( clientFactory.getLocalSession( ).getSession( ), ids, new AsyncCallback<Void>( ) {

	      @Override
	      public void onFailure( Throwable caught ) {
	        ActivityUtil.logoutForInvalidSession( clientFactory, caught );
	        clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.ERROR, FOOTERVIEW_GROUP_DELET_FAIL[lan], FooterView.DEFAULT_STATUS_CLEAR_DELAY );
	        clientFactory.getShellView( ).getLogView( ).log( LogType.ERROR, "Failed to delete groups " + ids + ": " + caught.getMessage( ) );
	      }

	      @Override
	      public void onSuccess( Void arg0 ) {
	    	  int lan = LanguageSelection.instance().getCurLanguage().ordinal();
	    	  clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.NONE, FOOTERVIEW_GROUP_DELET_SUCCEED[lan], FooterView.DEFAULT_STATUS_CLEAR_DELAY );
	    	  clientFactory.getShellView( ).getLogView( ).log( LogType.INFO, "Groups " + ids + " deleted" );
	    	  reloadCurrentRange( );
	      }
      
  		} );
	}
  	
  	private void updateUserStateByGroup(EnumState userState) {
  		final ArrayList<String> ids = Lists.newArrayList( );  		
  		for ( SearchResultRow row : currentSelected ) {
  			ids.add( row.getField( 0 ) );
  		}
    
  		clientFactory.getBackendService( ).updateGroupState( clientFactory.getLocalSession( ).getSession( ), ids, userState, new AsyncCallback<Void>( ) {

		      @Override
		      public void onFailure( Throwable caught ) {
		        ActivityUtil.logoutForInvalidSession( clientFactory, caught );
		        int lan = LanguageSelection.instance().getCurLanguage().ordinal();
		        clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.ERROR, FOOTERVIEW_GROUP_ACTIVITY_UPDATE_USERSTATE_FAIL[lan], FooterView.DEFAULT_STATUS_CLEAR_DELAY );
		        clientFactory.getShellView( ).getLogView( ).log( LogType.ERROR, "Failed to update user state in groups " + ids + ": " + caught.getMessage( ) );
		      }
	
		      @Override
		      public void onSuccess( Void arg0 ) {
		    	  int lan = LanguageSelection.instance().getCurLanguage().ordinal();
		    	  clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.NONE, FOOTERVIEW_GROUP_ACTIVITY_UPDATE_USERSTATE_SUCCEED[lan], FooterView.DEFAULT_STATUS_CLEAR_DELAY );
		    	  clientFactory.getShellView( ).getLogView( ).log( LogType.INFO, "Groups " + ids + " user state updated" );
		        
		    	  reloadCurrentRange();
		      }
  			}
  		);
	}
	
  @Override
  public void process( String subject, ArrayList<String> values ) {
	int lan = LanguageSelection.instance().getCurLanguage().ordinal();
    if ( ADD_USERS_SUBJECT[lan].equals( subject ) ) {
      doAddUsers( values.get( 0 ) );
    } else if ( ADD_POLICY_SUBJECT[lan].equals( subject ) ) {
      doAddPolicy( values.get( 0 ), values.get( 1 ) );
    }
  }
  
  private void doAddUsers( final String names ) {
    if ( currentSelected == null || currentSelected.size( ) < 1 ) {
      return;
    }
    
    final ArrayList<String> ids = Lists.newArrayList( ); 
    for ( SearchResultRow row : currentSelected ) {
      ids.add( row.getField( 0 ) );
    }
    
    clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.LOADING, "Adding users to selected groups ...", 0 );
    
    clientFactory.getBackendService( ).addUsersToGroupsByName( clientFactory.getLocalSession( ).getSession( ), names, ids, new AsyncCallback<Void>( ) {

      @Override
      public void onFailure( Throwable caught ) {
        ActivityUtil.logoutForInvalidSession( clientFactory, caught );
        clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.ERROR, "Failed to add users to selected groups", FooterView.DEFAULT_STATUS_CLEAR_DELAY );
        clientFactory.getShellView( ).getLogView( ).log( LogType.ERROR, "Failed to add users " + names + " to groups " + ids + ": " + caught.getMessage( ) );
      }

      @Override
      public void onSuccess( Void arg0 ) {
        clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.NONE, "Users are added to selected groups", FooterView.DEFAULT_STATUS_CLEAR_DELAY );
        clientFactory.getShellView( ).getLogView( ).log( LogType.INFO, "Users " + names + " are added to groups " + ids );
      }
      
    } );
  }
  
	private void doAddPolicy( final String name, final String document ) {
		if ( currentSelected == null || currentSelected.size( ) != 1 ) {
			return;
		}
      
		final String groupId = this.currentSelected.toArray( new SearchResultRow[0] )[0].getField( 0 );
		this.clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.LOADING, "Adding policy " + name + " ...", 0 );
		this.clientFactory.getBackendService( ).addGroupPolicy( this.clientFactory.getLocalSession( ).getSession( ), groupId, name, document, new AsyncCallback<Void>( ) {

        @Override
        public void onFailure( Throwable caught ) {
        	ActivityUtil.logoutForInvalidSession( clientFactory, caught );
        	clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.ERROR, "Failed to add policy", FooterView.DEFAULT_STATUS_CLEAR_DELAY );
        	clientFactory.getShellView( ).getLogView( ).log( LogType.ERROR, "Failed to add policy " + name + " for group " + groupId + ": " + caught.getMessage( ) );
        }

        @Override
        public void onSuccess( Void arg ) {
        	clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.NONE, "Policy added", FooterView.DEFAULT_STATUS_CLEAR_DELAY );
        	clientFactory.getShellView( ).getLogView( ).log( LogType.INFO, "New policy " + name + " is added to group " + groupId );
        	reloadCurrentRange( );
        }
      });
    }
    
	private void doModifyGroup() {
		if ( this.currentSelected == null || this.currentSelected.size( ) != 1 )
	    	return;
	    
		SearchResultRow row = this.currentSelected.toArray(new SearchResultRow[0])[0];
		
		final GroupInfo group = new GroupInfo();
		group.setId(Integer.parseInt(row.getField(0)));
		group.setName(row.getField(3));
		group.setDescription(row.getField(4));
		
		String groupState = row.getField(5);		
		EnumState state = Enum2String.getInstance().getEnumState(groupState);
		group.setState(state);
		
		String accountId = row.getField(6);

		if (!Strings.isNullOrEmpty(accountId))
			group.setAccountId(Integer.parseInt(accountId));
		
		final GroupAddView window = clientFactory.getGroupAddView();
  		window.setPresenter(this);
  		window.display();
  		
		LoginUserProfile curUser = this.clientFactory.getSessionData().getLoginUser();
		if (curUser.isSystemAdmin()) {
			this.clientFactory.getBackendService().listAccounts(this.clientFactory.getLocalSession().getSession(), new AsyncCallback<ArrayList<AccountInfo>> () {
		
				@Override
				public void onFailure(Throwable caught) {
						// TODO Auto-generated method stub
				}
			
				@Override
				public void onSuccess(ArrayList<AccountInfo> result) {
					// TODO Auto-generated method stub
					window.setAccountsInfo(result);
					window.setGroup(group);
				}
			});
		 }
	}
    
  	@Override
  	public void onDoubleClick(DoubleClickEvent event) {
  		// TODO Auto-generated method stub
  		this.doModifyGroup();
  	}
}
