package com.eucalyptus.webui.server;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.eucalyptus.webui.client.service.EucalyptusServiceException;
import com.eucalyptus.webui.client.service.SearchRange;
import com.eucalyptus.webui.client.service.SearchResult;
import com.eucalyptus.webui.client.service.SearchResultFieldDesc;
import com.eucalyptus.webui.client.service.SearchResultRow;
import com.eucalyptus.webui.client.service.SearchResultFieldDesc.TableDisplay;
import com.eucalyptus.webui.client.service.SearchResultFieldDesc.Type;
import com.eucalyptus.webui.server.db.ResultSetWrapper;
import com.eucalyptus.webui.server.dictionary.DBTableColName;
import com.eucalyptus.webui.server.user.RandomPwdCreator;
import com.eucalyptus.webui.server.user.UserDBProcWrapper;
import com.eucalyptus.webui.server.user.UserSyncException;
import com.eucalyptus.webui.shared.dictionary.ConfDef;
import com.eucalyptus.webui.shared.dictionary.Enum2String;
import com.eucalyptus.webui.shared.user.EnumState;
import com.eucalyptus.webui.shared.user.LoginUserProfile;
import com.eucalyptus.webui.shared.user.UserInfo;

public class UserServiceProcImpl {
	
	public void createUser(int accountId, UserInfo user) throws EucalyptusServiceException {
		if ( user == null) {
			throw new EucalyptusServiceException( "Empty accountId or invalid user on creating user" );
		}
		
		user.setPwd(RandomPwdCreator.genRandomNum(ConfDef.DEFAULT_PWD_LEN));
		  
		// user account_id == 0, which means that this user group is same as the session's account
		if (user.getAccountId() == 0)
			user.setAccountId(accountId);
		  
		try {
			userDBProc.addUser(user);
		} catch (UserSyncException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new EucalyptusServiceException("Failed to create or update user");
		}
	}
	
	public void modifyUser(UserInfo user) throws EucalyptusServiceException {
		if ( user == null) {
			throw new EucalyptusServiceException( "Empty accountId or invalid user on modifying user" );
		}
		
		try {
			userDBProc.modifyUser(user);
		} catch (UserSyncException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new EucalyptusServiceException("Failed to modify user");
		}
	}

	/**
	   * Search users.
	   * 
	   * @param session
	   * @param search
	   * @param range
	   * @return
	   * @throws EucalyptusServiceException
	 * @throws UserSyncException 
	   */
	  public SearchResult lookupUser( LoginUserProfile curUser, String search, SearchRange range ) throws EucalyptusServiceException {
		  boolean isRootAdmin = curUser.isSystemAdmin();
		  
		  ResultSetWrapper rs;
		   try {
			  if (isRootAdmin) {
					rs = userDBProc.queryTotalUsers();
			  }
			  else {		  
				  rs = userDBProc.queryUsersBy(curUser.getAccountId(), curUser.getUserId(), curUser.getUserType());
			  }
		  } catch (UserSyncException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new EucalyptusServiceException("query users fails");
		  }
		  if (rs == null)
			  return null;
		  
		  return getSearchResult(rs, range);
	  }
	  
	  public SearchResult lookupUserByGroupId(LoginUserProfile curUser, int groupId, SearchRange range ) throws EucalyptusServiceException {
		  ResultSetWrapper rs;
		  
		  try {
			  rs = userDBProc.queryUsersByGroupId(groupId);
		  } catch (UserSyncException e) {
			  // TODO Auto-generated catch block
			  e.printStackTrace();
			  throw new EucalyptusServiceException("query users fails");
		  }
		  
		  if (rs == null)
			  return null;
		  
		  return getSearchResult(rs, range);
	  }
	  
	  public SearchResult lookupUserByAccountId(int accountId, SearchRange range ) throws EucalyptusServiceException {
		  
		  ResultSetWrapper rs;
		  
		  try {
			  rs = userDBProc.queryUsersByAccountId(accountId);
		  } catch (UserSyncException e) {
			  // TODO Auto-generated catch block
			  e.printStackTrace();
			  throw new EucalyptusServiceException("query users fails");
		  }
		  
		  if (rs == null)
			  return null;
		  
		  return getSearchResult(rs, range);
	  }
	  
	  public SearchResult lookupUserExcludeGroupId(LoginUserProfile curUser, int accountId, int groupId, SearchRange range ) throws EucalyptusServiceException {
		  
		  ResultSetWrapper rs;
		  
		  try {
			  rs = userDBProc.queryUsersByAccountIdExcludeGroupId(accountId, groupId);
		  } catch (UserSyncException e) {
			  // TODO Auto-generated catch block
			  e.printStackTrace();
			  throw new EucalyptusServiceException("query users fails");
		  }
		  
		  if (rs == null)
			  return null;
		  
		  return getSearchResult(rs, range);
	  }
	  
	  private SearchResult getSearchResult(ResultSetWrapper rs, SearchRange range) {
		  
		  assert (range != null);
		  
		  List<SearchResultFieldDesc> FIELDS;
		  
		  FIELDS = FIELDS_ROOT;
		  
		  final int sortField = range.getSortField( );
		  
		  List<SearchResultRow> DATA = resultSet2List(rs);
		  
		  int resultLength = Math.min( range.getLength( ), DATA.size( ) - range.getStart( ) );
		  SearchResult result = new SearchResult(DATA.size(), range );
		  result.setDescs( FIELDS );
		  
		  if (DATA.size() > 0)
			  result.setRows( DATA.subList( range.getStart( ), range.getStart( ) + resultLength ) );
		
		  for ( SearchResultRow row : result.getRows( ) ) {
			  System.out.println( "Row: " + row );
		  }
			
		  return result;
	  }
	  
	  public void deleteUsers(ArrayList<String> ids ) throws EucalyptusServiceException {
		  try {
			userDBProc.delUsers(ids);
		} catch (UserSyncException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new EucalyptusServiceException("Failed to delete users");
		}
	  }
	  
	  public void updateUserState(ArrayList<String> ids,
		  		EnumState userState) throws EucalyptusServiceException {
		  try {
				userDBProc.updateUserState(ids, userState);
			} catch (UserSyncException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new EucalyptusServiceException("Failed to update users state");
			}
	  }
	  
	  public void addUsersToGroupsById(ArrayList<String> userIds, int groupId )
	  				throws EucalyptusServiceException {
		  try {
			  userDBProc.updateUsersGroup(userIds, groupId);
			} catch (UserSyncException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new EucalyptusServiceException("Failed to add users to group by group id");
			}
	  }
	  
	  public void updateUserStateByGroups(ArrayList<String> ids, EnumState userState )
				throws EucalyptusServiceException {
		  try {
			  userDBProc.updateUserStateByGroups(ids, userState);
			} catch (UserSyncException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new EucalyptusServiceException("Failed to update users state by groups");
			}
	  }
	  
	  public void updateUserStateByAccounts(ArrayList<String> ids, EnumState userState )
				throws EucalyptusServiceException {
		  try {
			  userDBProc.updateUserStateByAccounts(ids, userState);
			} catch (UserSyncException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new EucalyptusServiceException("Failed to update users state by accounts");
			}
	  }
	  
	  public void modifyIndividual(LoginUserProfile curUser, String title, String mobile, String email) throws EucalyptusServiceException {
		  int userId = curUser.getUserId();
		  
		  try {
			  userDBProc.modifyUser(userId, title, mobile, email);
			} catch (UserSyncException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new EucalyptusServiceException("Failed to modify individual");
			}
	  }
	  
	  public void changePassword(LoginUserProfile curUser, String oldPass, String newPass, String email ) throws EucalyptusServiceException {
		  int userId = curUser.getUserId();
		  
		  try {
			  userDBProc.changePwd(userId, oldPass, newPass);
			} catch (UserSyncException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new EucalyptusServiceException("Failed to change password");
			}
	  }
	  
	  private List<SearchResultRow> resultSet2List(ResultSetWrapper rsWrapper) {
		  ResultSet rs = rsWrapper.getResultSet();
		  int index = 1;
		  List<SearchResultRow> result = null;
		  try {
			  if (rs != null) {
				  result = new ArrayList<SearchResultRow>();
				  
				  while (rs.next()) {
						  result.add( new SearchResultRow(
							  			Arrays.asList(rs.getString(DBTableColName.USER.ID),
							  							Integer.toString(index++),
							  							rs.getString(DBTableColName.ACCOUNT.NAME),
							  							rs.getString(DBTableColName.GROUP.NAME),
							  							rs.getString(DBTableColName.USER.NAME),
							  							rs.getString(DBTableColName.USER.TITLE),
														rs.getString(DBTableColName.USER.EMAIL),
														rs.getString(DBTableColName.USER.MOBILE),
														Enum2String.getInstance().getEnumStateName(rs.getString(DBTableColName.USER.STATE)),
														Enum2String.getInstance().getEnumUserTypeName(rs.getString(DBTableColName.USER.TYPE)),
														rs.getString(DBTableColName.ACCOUNT.ID),
									  					rs.getString(DBTableColName.GROUP.ID),
									  					rs.getString(DBTableColName.USER.PWD)
													)
						  						)
								  	);
				}
				rsWrapper.close();
			}
		  } catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		  }
		  
		  return result;
	  }
	  
	  private UserDBProcWrapper userDBProc = new UserDBProcWrapper();
	  
	  private static final String[] TABLE_COL_TITLE_CHECKALL = {"Check All", "全选"};
	  private static final String[] TABLE_COL_TITLE_NO = {"No.", "序号"};
	  private static final String[] TABLE_COL_ACCOUNT_NAME = {"Account", "账户"};
	  private static final String[] TABLE_COL_GROUP_NAME = {"Group", "组"};
	  private static final String[] TABLE_COL_TITLE_NAME = {"ID", "帐号"};
	  private static final String[] TABLE_COL_TITLE_TITLE = {"Name", "姓名"};
	  private static final String[] TABLE_COL_TITLE_EMAIL = {"Emial", "邮箱"};
	  private static final String[] TABLE_COL_TITLE_MOBILE = {"Mobile", "手机"};
	  private static final String[] TABLE_COL_TITLE_STATE = {"State", "状态"};
	  private static final String[] TABLE_COL_TITLE_TYPE = {"type", "类型"};
	  private static final String[] TABLE_COL_ACCOUNT_ID = {"AccountID", "账户ID"};
	  private static final String[] TABLE_COL_GROUP_ID = {"GroupID", "组ID"};
	  private static final String[] TABLE_COL_USER_PWD = {"PWD", "密码"};
	  	
	  private static final List<SearchResultFieldDesc> FIELDS_ROOT = Arrays.asList(
				new SearchResultFieldDesc( TABLE_COL_TITLE_CHECKALL[1], "5%", false ),
				new SearchResultFieldDesc( TABLE_COL_TITLE_NO[1], false, "5%", TableDisplay.MANDATORY, Type.TEXT, false, false ),
				new SearchResultFieldDesc( TABLE_COL_ACCOUNT_NAME[1], true, "8%", TableDisplay.MANDATORY, Type.TEXT, false, false ),
				new SearchResultFieldDesc( TABLE_COL_GROUP_NAME[1], true, "10%", TableDisplay.MANDATORY, Type.TEXT, false, false ),
				new SearchResultFieldDesc( TABLE_COL_TITLE_NAME[1], true, "10%", TableDisplay.MANDATORY, Type.TEXT, false, false ),
				new SearchResultFieldDesc( TABLE_COL_TITLE_TITLE[1], true, "10%", TableDisplay.MANDATORY, Type.TEXT, false, false ),
				new SearchResultFieldDesc( TABLE_COL_TITLE_EMAIL[1], true, "18%", TableDisplay.MANDATORY, Type.LINK, false, false ),
				new SearchResultFieldDesc( TABLE_COL_TITLE_MOBILE[1], true, "15%", TableDisplay.MANDATORY, Type.TEXT, false, false ),
				new SearchResultFieldDesc( TABLE_COL_TITLE_STATE[1], true, "10%", TableDisplay.MANDATORY, Type.TEXT, false, false ),
				new SearchResultFieldDesc( TABLE_COL_TITLE_TYPE[1], true, "9%", TableDisplay.MANDATORY, Type.TEXT, false, false ),
				new SearchResultFieldDesc( TABLE_COL_ACCOUNT_ID[1], true, "0%", TableDisplay.MANDATORY, Type.TEXT, false, true ),
				new SearchResultFieldDesc( TABLE_COL_GROUP_ID[1], true, "0%", TableDisplay.MANDATORY, Type.TEXT, false, true ),
				new SearchResultFieldDesc( TABLE_COL_USER_PWD[1], true, "0%", TableDisplay.MANDATORY, Type.TEXT, false, true )
			);
}
