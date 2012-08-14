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
import com.eucalyptus.webui.client.view.CertAddView;
import com.eucalyptus.webui.client.view.ConfirmationView;
import com.eucalyptus.webui.client.view.FooterView;
import com.eucalyptus.webui.client.view.GroupListView;
import com.eucalyptus.webui.client.view.UserAddView;
import com.eucalyptus.webui.client.view.UserView;
import com.eucalyptus.webui.client.view.HasValueWidget;
import com.eucalyptus.webui.client.view.FooterView.StatusType;
import com.eucalyptus.webui.client.view.LogView.LogType;
import com.eucalyptus.webui.shared.user.AccountInfo;
import com.eucalyptus.webui.shared.user.EnumState;
import com.eucalyptus.webui.shared.user.GroupInfo;
import com.eucalyptus.webui.shared.user.LoginUserProfile;
import com.eucalyptus.webui.shared.user.UserInfo;
import com.google.common.collect.Lists;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class UserActivity extends AbstractSearchActivity
    implements UserView.Presenter, ConfirmationView.Presenter, UserAddView.Presenter, GroupListView.Presenter, CertAddView.Presenter {
  
  public static final String[] TITLE = {"USERS", "用户"};

  public static final String[] ADD_USER_CAPTION = {"Add users", "添加用户"};
  public static final String[] ADD_USER_SUBJECT = {"Are you sure to add user?", "确定添加该用户?"};
  
  public static final String[] RESUME_USERS_CAPTION = {"Resume users", "激活用户"};
  public static final String[] RESUME_USERS_SUBJECT = {"Are you sure to resume users?", "确定激活所选用户?"};
  
  public static final String[] PAUSE_USERS_CAPTION = {"Pause users", "暂停用户"};
  public static final String[] PAUSE_USERS_SUBJECT = {"Are you sure to pause users?", "确定暂停所选用户?"};
  
  public static final String[] BAN_USERS_CAPTION = {"Add users", "添加用户"};
  public static final String[] BAN_USERS_SUBJECT = {"Are you sure to add user?", "确定添加该用户"};
  
  public static final String[] DELETE_USERS_CAPTION = {"Delete selected users", "删除选择的用户"};
  public static final String[] DELETE_USERS_SUBJECT = {"Are you sure you want to delete following selected users?", "确定删除选择的用户?"};
  
  public static final String[] ADD_TO_GROUPS_CAPTION = {"Add selected users to groups", "将选择的用户加入组"};
  public static final String[] ADD_TO_GROUPS_SUBJECT = {"Are you sure to add selected users to the group?", "确定要将选定的用户加入组？"};

  public static final String[] REMOVE_FROM_GROUPS_CAPTION = {"Remove users from selected groups", "从选择的组中删除用户"};
  public static final String[] REMOVE_FROM_GROUPS_SUBJECT = {"Are you sure to remove the selected users from the group", "确定要将所选择的用户从组中删除？"};
  
  public static final String[] ADD_POLICY_CAPTION = {"Add new policy", "增加策略"};
  public static final String[] ADD_POLICY_SUBJECT = {"Enter new policy to assign to the selected user:", "为选中的用户输入策略"};
  public static final String[] POLICY_NAME_INPUT_TITLE = {"Policy name", "策略名称"};
  public static final String[] POLICY_CONTENT_INPUT_TITLE = {"Policy content", "策略内容"};
  
  public static final String[] ADD_KEY_CAPTION = {"Add new key", "增加密钥"};
  public static final String[] ADD_KEY_SUBJECT = {"Enter new key to assign to the selected user:", "为选中的用户增加密钥"};
  
  public static final String[] ADD_CERT_CAPTION = {"Add new certificate", "增加证书"};
  public static final String[] ADD_CERT_SUBJECT = {"Enter new certificate to assign to the selected user:", "为选定的用户增加证书"};
  public static final String[] CERT_PEM_INPUT_TITLE = {"Certificate (PEM)", "证书(PEM)"};
  
  private final String[] FOOTERVIEW_USER_NO_SELECTION = {"Must select users", "必须选择至少一个用户"};
  private final String[] FOOTERVIEW_USER_ONE_SELECTION = {"Must select one user", "必须选择一个用户"};

  private static final Logger LOG = Logger.getLogger( UserActivity.class.getName( ) );
  
  private Set<SearchResultRow> currentSelected;
  
  public UserActivity( SearchPlace place, ClientFactory clientFactory ) {
    super( place, clientFactory );
  }

  @Override
  protected void doSearch( String query, SearchRange range ) {
    this.clientFactory.getBackendService( ).lookupUser( this.clientFactory.getLocalSession( ).getSession( ), search, range,
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
    final ArrayList<String> newVals = Lists.newArrayList( );
    for ( HasValueWidget w : values ) {
      newVals.add( w.getValue( ) );
    }
    
    final String userId = emptyForNull( getField( newVals, 0 ) );
    this.clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.LOADING, "Modifying user " + userId + " ...", 0 );
    
    clientFactory.getBackendService( ).modifyUser( clientFactory.getLocalSession( ).getSession( ), keys, newVals, new AsyncCallback<Void>( ) {

      @Override
      public void onFailure( Throwable caught ) {
        ActivityUtil.logoutForInvalidSession( clientFactory, caught );
        clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.ERROR, "Failed to modify user", FooterView.DEFAULT_STATUS_CLEAR_DELAY );
        clientFactory.getShellView( ).getLogView( ).log( LogType.ERROR, "Failed to modify user " + userId  + ": " + caught.getMessage( ) );
      }

      @Override
      public void onSuccess( Void arg0 ) {
        clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.NONE, "Successfully modified user", FooterView.DEFAULT_STATUS_CLEAR_DELAY );
        clientFactory.getShellView( ).getLogView( ).log( LogType.INFO, "Modified user " + userId );
        //clientFactory.getShellView( ).getDetailView( ).disableSave( );
        reloadCurrentRange( );
      }
      
    } );
  }

  @Override
  protected String getTitle( ) {
    return TITLE[1];
  }

  @Override
  protected void showView( SearchResult result ) {
    if ( this.view == null ) {
      this.view = this.clientFactory.getUserView( );
      ( ( UserView ) this.view ).setPresenter( this );
      ( ( UserView ) this.view ).setPresenter(this.clientFactory.getSessionData().getLoginUser());
      container.setWidget( this.view );
      ( ( UserView ) this.view ).clear( );
    }
    ( ( UserView ) this.view ).showSearchResult( result );    
  }

  public static final String USER_NAME_INPUT_TITLE[] = {"User ID", "ID"};
  public static final String USER_PWD_INPUT_TITLE[] = {"User PWD", "密码"};
  public static final String USER_TITLE_INPUT_TITLE[] = {"User name", "姓名"};
  public static final String USER_MOBILE_INPUT_TITLE[] = {"User mobile", "手机"};
  public static final String USER_EMAIL_INPUT_TITLE[] = {"User emial", "邮箱"};
  public static final String USER_TYPE_INPUT_TITLE[] = {"User type", "类型"};
  
  @Override
  public void process(UserInfo user) {
  	// TODO Auto-generated method stub
	if (user == null) {
		return;
	}
	
  	clientFactory.getBackendService().createUser(clientFactory.getLocalSession( ).getSession( ), user, new AsyncCallback<Void>( ) {

	        @Override
	        public void onFailure( Throwable caught ) {
	          ActivityUtil.logoutForInvalidSession( clientFactory, caught );
	          clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.ERROR, "Failed to create user", FooterView.DEFAULT_STATUS_CLEAR_DELAY );
	          clientFactory.getShellView( ).getLogView( ).log( LogType.ERROR, "Failed to create user " + ": " + caught.getMessage( ) );
	        }
	
	        @Override
	        public void onSuccess( Void arg0 ) {
	          clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.NONE, "Successfully create user", FooterView.DEFAULT_STATUS_CLEAR_DELAY );
	          clientFactory.getShellView( ).getLogView( ).log( LogType.INFO, "Create user");
	          reloadCurrentRange( );
	        }
  		}
  	);
  }
  
  @Override
  public void onAddUser() {
	  // TODO Auto-generated method stub
	  final UserAddView window = this.clientFactory.getUserAddView();
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
	public void onDeleteUsers( ) {
	  	if ( currentSelected == null || currentSelected.size( ) < 1 ) {
    		clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.ERROR, "Select users to delete", FooterView.DEFAULT_STATUS_CLEAR_DELAY );
      		return;
		}
		System.out.println("size: " + currentSelected.size());
    
		ConfirmationView dialog = this.clientFactory.getConfirmationView( );
		dialog.setPresenter( this );
		dialog.display( DELETE_USERS_CAPTION[1], DELETE_USERS_SUBJECT[1]);
	}
	
	 @Override
	  public void onAddToGroups( ) {
		 if (!selectionIsValid())
				return;
		 
	    clientFactory.getGroupListView().setPresenter(this);
	    
		clientFactory.getBackendService().listGroups(clientFactory.getLocalSession().getSession(), new AsyncCallback<ArrayList<GroupInfo>> () {

				@Override
				public void onFailure(Throwable caught) {
					// TODO Auto-generated method stub
					
				}
		
				@Override
				public void onSuccess(ArrayList<GroupInfo> groupList) {
					// TODO Auto-generated method stub
					clientFactory.getGroupListView().setGroupList(groupList);
					clientFactory.getGroupListView().display();
				}
			}
		);
	  }
  

	 @Override
	public void onRemoveFromGroups( ) {
		 if (!selectionIsValid())
				return;
			
		ConfirmationView confirmView = this.clientFactory.getConfirmationView();
		confirmView.setPresenter(this);
	  
		confirmView.display(REMOVE_FROM_GROUPS_CAPTION[1], REMOVE_FROM_GROUPS_SUBJECT[1]);
	}
	
	@Override
	public void onAddPolicy() {
		// TODO Auto-generated method stub
		if (!oneSelectionIsValid())
			return;
		
		ConfirmationView confirmView = this.clientFactory.getConfirmationView();
		confirmView.setPresenter(this);
	  
		confirmView.display(ADD_POLICY_CAPTION[1], ADD_POLICY_SUBJECT[1]);
	}

	@Override
	public void onAddKey() {
		// TODO Auto-generated method stub
		if (!oneSelectionIsValid())
			return;
		
		ConfirmationView confirmView = this.clientFactory.getConfirmationView();
		confirmView.setPresenter(this);
	  
		confirmView.display(ADD_KEY_CAPTION[1], ADD_KEY_SUBJECT[1]);
	}
	
	@Override
	public void onAddCert() {
		// TODO Auto-generated method stub
		if (!oneSelectionIsValid())
			return;
		
//		ConfirmationView confirmView = this.clientFactory.getConfirmationView();
//		confirmView.setPresenter(this);
//		confirmView.display(ADD_CERT_CAPTION[1], ADD_CERT_SUBJECT[1]);
		
		CertAddView certAddView = this.clientFactory.getCertAddView();
		certAddView.setPresenter(this);
		certAddView.display(ADD_CERT_CAPTION[1]);
	}
	
	@Override
	public void onResumeUses() {
		// TODO Auto-generated method stub
		if (!selectionIsValid())
			return;
		
		ConfirmationView confirmView = this.clientFactory.getConfirmationView();
		confirmView.setPresenter(this);
	  
		confirmView.display(RESUME_USERS_CAPTION[1], RESUME_USERS_SUBJECT[1]);
	}
  
	@Override
	public void onPauseUsers() {
		if (!selectionIsValid())
			return;
		
		ConfirmationView confirmView = this.clientFactory.getConfirmationView();
		confirmView.setPresenter(this);
	  
		confirmView.display(PAUSE_USERS_CAPTION[1], PAUSE_USERS_SUBJECT[1]);
	}

	@Override
	public void onBanUsers() {
		// TODO Auto-generated method stub
		if (!selectionIsValid())
			return;
		
		ConfirmationView confirmView = this.clientFactory.getConfirmationView();
		confirmView.setPresenter(this);
	  
		confirmView.display(BAN_USERS_CAPTION[1], BAN_USERS_SUBJECT[1]);
	}
	
	
	private boolean selectionIsValid() {
		if ( currentSelected == null || currentSelected.size( ) < 1 ) {
			clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.ERROR, FOOTERVIEW_USER_NO_SELECTION[1], FooterView.DEFAULT_STATUS_CLEAR_DELAY );
			return false;
		}
	  
		return true;
	}
	
	private boolean oneSelectionIsValid() {
		if ( currentSelected == null || currentSelected.size( ) != 1 ) {
			clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.ERROR, FOOTERVIEW_USER_ONE_SELECTION[1], FooterView.DEFAULT_STATUS_CLEAR_DELAY );
			return false;
		}
	  
		return true;
	}
  
  @Override
  public void confirm( String subject ) {
    if ( DELETE_USERS_SUBJECT[1].equals( subject ) ) {
    	doDeleteUsers( );
    } else if ( RESUME_USERS_SUBJECT[1].equals( subject ) ) {
    	doUpdateUserState(EnumState.NORMAL);
    } else if ( PAUSE_USERS_SUBJECT[1].equals( subject ) ) {
    	doUpdateUserState(EnumState.PAUSE);
    } else if ( BAN_USERS_SUBJECT[1].equals( subject ) ) {
    	doUpdateUserState(EnumState.BAN);
    } else if ( REMOVE_FROM_GROUPS_SUBJECT[1].equals( subject ) ) {
    	doRemoveUserFromGroup();
    } else if ( ADD_POLICY_SUBJECT[1].equals( subject ) ) {
    	//doAddPolicy();
    } else if ( ADD_KEY_SUBJECT[1].equals( subject ) ) {
    	doAddKey();
    } else if ( ADD_CERT_SUBJECT[1].equals( subject ) ) {
    	//doAddCert();
    }
  }

  private void doDeleteUsers( ) {
	    if ( currentSelected == null || currentSelected.size( ) < 1 ) {
	      return;
	    }
  
	    final ArrayList<String> ids = Lists.newArrayList( ); 
	    for ( SearchResultRow row : currentSelected ) {
	      ids.add( row.getField( 0 ) );
	    }
	    
	    clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.LOADING, FOOTERVIEW_DELETE_USERS[1], 0 );
	    
	    clientFactory.getBackendService( ).deleteUsers( clientFactory.getLocalSession( ).getSession( ), ids, new AsyncCallback<Void>( ) {

	    	@Override
	    	public void onFailure( Throwable caught ) {
	    		ActivityUtil.logoutForInvalidSession( clientFactory, caught );
	    		clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.ERROR, FOOTERVIEW_FAILED_TO_DELETE_USERS[1], FooterView.DEFAULT_STATUS_CLEAR_DELAY );
	    		clientFactory.getShellView( ).getLogView( ).log( LogType.ERROR, "Failed to delete users " + ids + ": " + caught.getMessage( ) );
	    	}

	    	@Override
	    	public void onSuccess( Void arg0 ) {
	    		clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.NONE, FOOTERVIEW_USERS_DELETED[1], FooterView.DEFAULT_STATUS_CLEAR_DELAY );
	    		clientFactory.getShellView( ).getLogView( ).log( LogType.INFO, "Users " + ids + " deleted" );
	    		reloadCurrentRange( );
	    		currentSelected = null;
	    	}
	    });
	}

  private void doAddKey( ) {
	    if ( currentSelected == null || currentSelected.size( ) != 1 ) {
	    	return;
	    }
	    
	    final String userId = this.currentSelected.toArray( new SearchResultRow[0] )[0].getField( 0 );
	    this.clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.LOADING, "Adding access key ...", 0 );
	    
	    this.clientFactory.getBackendService( ).addAccessKey( this.clientFactory.getLocalSession( ).getSession( ), userId, new AsyncCallback<Void>( ) {
	  
	        @Override
	        public void onFailure( Throwable caught ) {
	        	ActivityUtil.logoutForInvalidSession( clientFactory, caught );
	        	clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.ERROR, "Failed to add access key", FooterView.DEFAULT_STATUS_CLEAR_DELAY );
	        	clientFactory.getShellView( ).getLogView( ).log( LogType.ERROR, "Failed to add access key for user " + userId + ": " + caught.getMessage( ) );
	        }
	  	    @Override
	        public void onSuccess( Void arg ) {
	  	    	clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.NONE, "Access key added", FooterView.DEFAULT_STATUS_CLEAR_DELAY );
	  	    	clientFactory.getShellView( ).getLogView( ).log( LogType.INFO, "New access key is added to user " + userId );
	  	    	reloadCurrentRange( );
	        }
	     });
  }

	  
	private void doAddCert(final String pem) {
		if (currentSelected == null || currentSelected.size() != 1 || pem == null) {
			return;
		}

		final String userId = this.currentSelected.toArray(new SearchResultRow[0])[0].getField(0);
		this.clientFactory.getShellView().getFooterView().showStatus(StatusType.LOADING, "Adding certificate...", 0);
		clientFactory.getBackendService().addCertificate(
				clientFactory.getLocalSession().getSession(), userId, pem,
				new AsyncCallback<Void>() {

					@Override
					public void onFailure(Throwable caught) {
						ActivityUtil.logoutForInvalidSession(clientFactory,
								caught);
						clientFactory
								.getShellView()
								.getFooterView()
								.showStatus(StatusType.ERROR, "创建证书失败", FooterView.DEFAULT_STATUS_CLEAR_DELAY);
						clientFactory
								.getShellView()
								.getLogView()
								.log(LogType.ERROR, "创建证书失败" + ": " + caught.getMessage());
					}

					@Override
					public void onSuccess(Void arg0) {
						clientFactory
								.getShellView()
								.getFooterView()
								.showStatus(StatusType.NONE, "创建证书成功", FooterView.DEFAULT_STATUS_CLEAR_DELAY);
						clientFactory.getShellView().getLogView()
								.log(LogType.INFO, "Create certificate");
						reloadCurrentRange();
					}
				});
	}

  private void doAddPolicy( final String name, final String document ) {
	  if ( currentSelected == null || currentSelected.size( ) != 1 ) {
		  return;
	  }
	  final String userId = this.currentSelected.toArray( new SearchResultRow[0] )[0].getField( 0 );
	  this.clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.LOADING, "Adding policy " + name + " ...", 0 );
	      
	  this.clientFactory.getBackendService( ).addUserPolicy( this.clientFactory.getLocalSession( ).getSession( ), userId, name, document, new AsyncCallback<Void>() {  
		  @Override
		  public void onFailure( Throwable caught ) {
			  ActivityUtil.logoutForInvalidSession( clientFactory, caught );
		      clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.ERROR, "Failed to add policy", FooterView.DEFAULT_STATUS_CLEAR_DELAY );
		      clientFactory.getShellView( ).getLogView( ).log( LogType.ERROR, "Failed to add policy " + name + " for user " + userId + ": " + caught.getMessage( ) );
		  }
		  
		  @Override
		  public void onSuccess( Void arg ) {
			  clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.NONE, "Policy added", FooterView.DEFAULT_STATUS_CLEAR_DELAY );
		      clientFactory.getShellView( ).getLogView( ).log( LogType.INFO, "New policy " + name + " is added to user " + userId );
		      reloadCurrentRange( );
		  }
	  });
  }
	  
  static final String[] FOOTERVIEW_UPDATE_USER_STATE = {"Update user state", "更新用户状态"};
  static final String[] FOOTERVIEW_UPDATE_USER_FAILS = {"Update user state fails", "更新用户状态"};
    
  private void doUpdateUserState(EnumState userState) {
	// TODO Auto-generated method stub
			if ( currentSelected == null || currentSelected.size( ) < 1 ) {
				//clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.ERROR, "Select users to delete", FooterView.DEFAULT_STATUS_CLEAR_DELAY );
				return;
			}
			
			ArrayList<String> ids = new ArrayList<String>();
			
			for (SearchResultRow row : currentSelected) {
				ids.add(row.getField(0));
			}
			
			this.clientFactory.getBackendService().updateUserState(clientFactory.getLocalSession( ).getSession( ), ids, userState, new AsyncCallback<Void>( )
					{

						@Override
						public void onFailure(Throwable caught) {
							// TODO Auto-generated method stub
							clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.ERROR, FOOTERVIEW_UPDATE_USER_FAILS[1], FooterView.DEFAULT_STATUS_CLEAR_DELAY );
						}

						@Override
						public void onSuccess(Void result) {
							// TODO Auto-generated method stub
							clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.NONE, FOOTERVIEW_UPDATE_USER_STATE[1], FooterView.DEFAULT_STATUS_CLEAR_DELAY );
							reloadCurrentRange();
						}
						
					});
	}

  	static final String[] FOOTERVIEW_DELETE_USERS = {"Deleting users ...", ""};
  	static final String[] FOOTERVIEW_FAILED_TO_DELETE_USERS = {"Failed to delete users", ""};
  	static final String[] FOOTERVIEW_USERS_DELETED = {"Users deleted", ""};
  	
	private void doRemoveUserFromGroup() {
		
		ArrayList<String> userIds = new ArrayList<String>();
		
		for (SearchResultRow row : currentSelected) {
			userIds.add(row.getField(0));
		}
		
		clientFactory.getBackendService().addUsersToGroupsById(clientFactory.getLocalSession().getSession(), 
																userIds, 0, 
																new AsyncCallback<Void> () 
																{

																	@Override
																	public void onFailure(Throwable caught) {
																		// TODO Auto-generated method stub
																		final String[] FOOTERVIEW_REMOVE_USER_TO_GROUP_FAIL = {"Remove user from group fails", "用户删除组失败"};
																		clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.ERROR, FOOTERVIEW_REMOVE_USER_TO_GROUP_FAIL[1], FooterView.DEFAULT_STATUS_CLEAR_DELAY );
																	}

																	@Override
																	public void onSuccess(Void result) {
																		// TODO Auto-generated method stub
																		final String[] FOOTERVIEW_REMOVE_USER_FROM_GROUP_SUCCEED = {"Remove user from group succeed", "用户删除组成功"};
																		clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.NONE, FOOTERVIEW_REMOVE_USER_FROM_GROUP_SUCCEED[1], FooterView.DEFAULT_STATUS_CLEAR_DELAY );
																		reloadCurrentRange();
																	}
																}
															);
	}
	
	public void doAddUserToGroup(int groupId) {
		// TODO Auto-generated method stub
		ArrayList<String> userIds = new ArrayList<String>();
		
		for (SearchResultRow row : currentSelected) {
			userIds.add(row.getField(0));
		}
		
		clientFactory.getBackendService().addUsersToGroupsById(clientFactory.getLocalSession().getSession(), 
																userIds, groupId, 
																new AsyncCallback<Void> () 
																{

																	@Override
																	public void onFailure(Throwable caught) {
																		// TODO Auto-generated method stub
																		final String[] FOOTERVIEW_ADD_USER_TO_GROUP_FAIL = {"Add user to group fails", "用户加入组失败"};
																		clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.ERROR, FOOTERVIEW_ADD_USER_TO_GROUP_FAIL[1], FooterView.DEFAULT_STATUS_CLEAR_DELAY );
																	}

																	@Override
																	public void onSuccess(Void result) {
																		// TODO Auto-generated method stub
																		final String[] FOOTERVIEW_ADD_USER_TO_GROUP_SUCCEED = {"Add user to group succeed", "用户加入组成功"};
																		clientFactory.getShellView( ).getFooterView( ).showStatus( StatusType.NONE, FOOTERVIEW_ADD_USER_TO_GROUP_SUCCEED[1], FooterView.DEFAULT_STATUS_CLEAR_DELAY );
																		clientFactory.getUserView().clearSelection();
																		reloadCurrentRange();
																	}
																}
															);
	}

	@Override
	public void processAddCert(String pem) {
		doAddCert(pem);
	}
}
