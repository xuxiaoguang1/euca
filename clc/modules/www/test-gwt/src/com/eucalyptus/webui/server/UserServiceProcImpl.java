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
import com.eucalyptus.webui.server.dictionary.ConfDef;
import com.eucalyptus.webui.server.dictionary.DBTableColName;
import com.eucalyptus.webui.server.dictionary.Enum2String;
import com.eucalyptus.webui.server.user.RandomPwdCreator;
import com.eucalyptus.webui.server.user.UserDBProcWrapper;
import com.eucalyptus.webui.server.user.UserSyncException;
import com.eucalyptus.webui.shared.user.EnumState;
import com.eucalyptus.webui.shared.user.LoginUserProfile;
import com.eucalyptus.webui.shared.user.UserInfo;

public class UserServiceProcImpl {
	
	public void createUser(int accountId, UserInfo user)
	  		throws EucalyptusServiceException {
		
		if ( user == null) {
		      throw new EucalyptusServiceException( "Empty accountId or invalid user on create user" );
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
			throw new EucalyptusServiceException("Create user fails");
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
		  
		  if (isRootAdmin) {
			  try {
				rs = userDBProc.queryTotalUsers();
			} catch (UserSyncException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new EucalyptusServiceException("query users fails");
			}
		  }
		  else {		  
			  rs = userDBProc.queryUsersBy(curUser.getAccountId(), curUser.getUserId(), curUser.getUserType());
		  }
		  
		  if (rs == null)
			  return null;
		  
		  return getSearchResult(isRootAdmin, rs, range);
	  }
	  
	  public SearchResult lookupUserByGroupId(LoginUserProfile curUser, int groupId, SearchRange range ) throws EucalyptusServiceException {
		  boolean isRootAdmin = curUser.isSystemAdmin();
		  
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
		  
		  return getSearchResult(isRootAdmin, rs, range);
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
		  
		  return getSearchResult(false, rs, range);
	  }
	  
	  public SearchResult lookupUserExcludeGroupId(int accountId, int groupId, SearchRange range ) throws EucalyptusServiceException {
		  
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
		  
		  return getSearchResult(false, rs, range);
	  }
	  
	  private SearchResult getSearchResult(boolean isRootAdmin, ResultSetWrapper rs, SearchRange range) {
		  
		  assert (range != null);
		  
		  List<SearchResultFieldDesc> FIELDS;
		  
		  if (isRootAdmin) {
			 
			  FIELDS = UserServiceProcImpl.FIELDS_ROOT;
		  }
		  else {
			  FIELDS = UserServiceProcImpl.FIELDS_NONROOT;
		  }
		  
		  final int sortField = range.getSortField( );
		  
		  DATA = resultSet2List(isRootAdmin, rs);
		  
		  int resultLength = Math.min( range.getLength( ), DATA.size( ) - range.getStart( ) );
		  SearchResult result = new SearchResult( DATA.size( ), range );
		  result.setDescs( FIELDS );
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
	  
	  private List<SearchResultRow> resultSet2List(boolean isRootView, ResultSetWrapper rsWrapper) {
		  ResultSet rs = rsWrapper.getResultSet();
		  int index = 1;
		  List<SearchResultRow> result = null;
		  try {
			  if (rs != null) {
				  result = new ArrayList<SearchResultRow>();
				  
				  while (rs.next()) {
					  
					  if (isRootView)
						  result.add( new SearchResultRow(
									  			Arrays.asList(rs.getString(DBTableColName.USER.ID),
									  							Integer.toString(index++),
									  							rs.getString(DBTableColName.ACCOUNT.NAME),
									  							rs.getString(DBTableColName.GROUP.NAME),
									  							rs.getString(DBTableColName.USER.NAME),
									  							rs.getString(DBTableColName.USER.TITLE),
																rs.getString(DBTableColName.USER.EMAIL),
																rs.getString(DBTableColName.USER.MOBILE),
																Enum2String.getInstance().getEnumStateName(rs.getString(DBTableColName.USER.STATE))
															)
								  						)
								  	);
					  else
						  result.add( new SearchResultRow(
										  Arrays.asList(rs.getString(DBTableColName.USER.ID),
							  							Integer.toString(index++),
							  							rs.getString(DBTableColName.GROUP.NAME),
							  							rs.getString(DBTableColName.USER.NAME),
							  							rs.getString(DBTableColName.USER.TITLE),
														rs.getString(DBTableColName.USER.EMAIL),
														rs.getString(DBTableColName.USER.MOBILE),
														Enum2String.getInstance().getEnumStateName(rs.getString(DBTableColName.USER.STATE))
												  		)
		  											)
								  	);
				  }
			  }
			rsWrapper.close();			
		  } catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		  }
		  
		  return result;
	  }
	  
	  private UserDBProcWrapper userDBProc = new UserDBProcWrapper();
	  
	  private static List<SearchResultRow> DATA = null;
	
	  private static final String[] TABLE_COL_TITLE_CHECKALL = {"Check All", "全选"};
	  private static final String[] TABLE_COL_TITLE_NO = {"No.", "序号"};
	  private static final String[] TABLE_COL_ACCOUNT_NAME = {"Account", "账户"};
	  private static final String[] TABLE_COL_GROUP_NAME = {"Group", "组"};
	  private static final String[] TABLE_COL_TITLE_NAME = {"ID", "帐号"};
	  private static final String[] TABLE_COL_TITLE_TITLE = {"Name", "姓名"};
	  private static final String[] TABLE_COL_TITLE_EMAIL = {"Emial", "邮箱"};
	  private static final String[] TABLE_COL_TITLE_MOBILE = {"Mobile", "手机"};
	  private static final String[] TABLE_COL_TITLE_STATE = {"State", "状态"};
	
	  private static final List<SearchResultFieldDesc> FIELDS_ROOT = Arrays.asList(
				new SearchResultFieldDesc( TABLE_COL_TITLE_CHECKALL[1], "9%", false ),
				new SearchResultFieldDesc( TABLE_COL_TITLE_NO[1], false, "9%", TableDisplay.MANDATORY, Type.TEXT, false, false ),
				new SearchResultFieldDesc( TABLE_COL_ACCOUNT_NAME[1], true, "9%", TableDisplay.MANDATORY, Type.TEXT, false, false ),
				new SearchResultFieldDesc( TABLE_COL_GROUP_NAME[1], true, "10%", TableDisplay.MANDATORY, Type.TEXT, false, false ),
				new SearchResultFieldDesc( TABLE_COL_TITLE_NAME[1], true, "10%", TableDisplay.MANDATORY, Type.TEXT, false, false ),
				new SearchResultFieldDesc( TABLE_COL_TITLE_TITLE[1], true, "10%", TableDisplay.MANDATORY, Type.TEXT, false, false ),
				new SearchResultFieldDesc( TABLE_COL_TITLE_EMAIL[1], true, "18%", TableDisplay.MANDATORY, Type.LINK, false, false ),
				new SearchResultFieldDesc( TABLE_COL_TITLE_MOBILE[1], true, "15%", TableDisplay.MANDATORY, Type.TEXT, false, false ),
				new SearchResultFieldDesc( TABLE_COL_TITLE_STATE[1], true, "10%", TableDisplay.MANDATORY, Type.TEXT, false, false )
			);
	  
	  private static final List<SearchResultFieldDesc> FIELDS_NONROOT = Arrays.asList(
				new SearchResultFieldDesc( TABLE_COL_TITLE_CHECKALL[1], "10%", false ),
				new SearchResultFieldDesc( TABLE_COL_TITLE_NO[1], false, "10%", TableDisplay.MANDATORY, Type.TEXT, false, false ),
				new SearchResultFieldDesc( TABLE_COL_GROUP_NAME[1], true, "10%", TableDisplay.MANDATORY, Type.TEXT, false, false ),
				new SearchResultFieldDesc( TABLE_COL_TITLE_NAME[1], true, "10%", TableDisplay.MANDATORY, Type.TEXT, false, false ),
				new SearchResultFieldDesc( TABLE_COL_TITLE_TITLE[1], true, "10%", TableDisplay.MANDATORY, Type.TEXT, false, false ),
				new SearchResultFieldDesc( TABLE_COL_TITLE_EMAIL[1], true, "25%", TableDisplay.MANDATORY, Type.LINK, false, false ),
				new SearchResultFieldDesc( TABLE_COL_TITLE_MOBILE[1], true, "15%", TableDisplay.MANDATORY, Type.TEXT, false, false ),
				new SearchResultFieldDesc( TABLE_COL_TITLE_STATE[1], true, "10%", TableDisplay.MANDATORY, Type.TEXT, false, false )
			);
}
