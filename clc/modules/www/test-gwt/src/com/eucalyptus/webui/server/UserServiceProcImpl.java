package com.eucalyptus.webui.server;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.eucalyptus.webui.client.service.EucalyptusServiceException;
import com.eucalyptus.webui.client.service.SearchRange;
import com.eucalyptus.webui.client.service.SearchResult;
import com.eucalyptus.webui.client.service.SearchResultRow;
import com.eucalyptus.webui.client.session.Session;
import com.eucalyptus.webui.server.config.ViewSearchTableServerConfig;
import com.eucalyptus.webui.server.db.ResultSetWrapper;
import com.eucalyptus.webui.server.user.UserDBProcWrapper;
import com.eucalyptus.webui.server.user.UserSyncException;
import com.eucalyptus.webui.shared.config.EnumService;
import com.eucalyptus.webui.shared.config.LanguageSelection;
import com.eucalyptus.webui.shared.config.SearchTableCol;
import com.eucalyptus.webui.shared.dictionary.DBTableColName;
import com.eucalyptus.webui.shared.dictionary.Enum2String;
import com.eucalyptus.webui.shared.user.EnumState;
import com.eucalyptus.webui.shared.user.LoginUserProfile;
import com.eucalyptus.webui.shared.user.UserInfo;

public class UserServiceProcImpl {
	
	public int createUser(int accountId, UserInfo user) throws EucalyptusServiceException {
		if ( user == null) {
			throw new EucalyptusServiceException( "Empty accountId or invalid user on creating user" );
		}
		
		//user.setPwd(RandomPwdCreator.genRandomNum(ConfDef.DEFAULT_PWD_LEN));
		//use name as default pwd temporarily, because the mail notification function not enabled
		user.setPwd(user.getName());
		  
		// user account_id == 0, which means that this user group is same as the session's account
		if (user.getAccountId() == 0)
			user.setAccountId(accountId);
		  
		try {
			return userDBProc.addUser(user);
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
		  assert (range != null);
		  
		  ResultSetWrapper rs;
		   try {
			  if (isRootAdmin) {
					rs = userDBProc.queryTotalUsers(range);
			  }
			  else {		  
				  rs = userDBProc.queryUsersBy(curUser.getAccountId(), curUser.getUserId(), curUser.getUserType(), range);
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
			  rs = userDBProc.queryUsersByGroupId(groupId, range);
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
			  rs = userDBProc.queryUsersByAccountId(accountId, range);
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
			  rs = userDBProc.queryUsersByAccountIdExcludeGroupId(accountId, groupId, range);
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
		  
		  List<SearchResultRow> DATA = resultSet2List(rs);
		  
		  int resultLength = Math.min( range.getLength( ), DATA.size( ) - range.getStart( ) );
		  SearchResult result = new SearchResult(DATA.size(), range );
		  result.setDescs( ViewSearchTableServerConfig.instance().getConfig(EnumService.USER_SRV, LanguageSelection.instance().getCurLanguage()) );
		  
		  if (DATA.size() > 0)
			  result.setRows( DATA.subList( range.getStart( ), range.getStart( ) + resultLength ) );
			
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
	  
	  public ArrayList<Integer> getUserCounts(Session session) throws EucalyptusServiceException {
		  ResultSetWrapper rs;
		  
		  try {
			  rs = userDBProc.queryTotalUsers(null);
		  } catch (UserSyncException e) {
			  e.printStackTrace();
			  throw new EucalyptusServiceException("query users fails");
		  }
		  
		  ResultSet set = rs.getResultSet();
		  
		  int total = 0;
		  int active = 0;

		  try {
			  while(set.next()){
				++total;
				  
				if(set.getInt(DBTableColName.USER.STATE) == 1) {
					  ++active;
				}
			  }
		  }catch(SQLException e){
			  throw new EucalyptusServiceException(e.getMessage());
		  }
		  
		  ArrayList<Integer> res = new ArrayList<Integer>();
		  
		  res.add(total);
		  res.add(active);
		  
		  return res;
	}
	  
	private List<SearchResultRow> resultSet2List(ResultSetWrapper rsWrapper) {
		ResultSet rs = rsWrapper.getResultSet();
		int index = 1;
		List<SearchResultRow> result = null;
		
		try {
			if (rs != null) {
				result = new ArrayList<SearchResultRow>();
				  
				ArrayList<SearchTableCol> tableCols = ViewSearchTableServerConfig.instance().getConfig(EnumService.USER_SRV);
				String[] dbFields = new String[tableCols.size()];
				
				for (int i=0; i<dbFields.length; i++)
					dbFields[i] = tableCols.get(i).getDbField();
				
				while (rs.next()) {
					ArrayList<String> rowValue = new ArrayList<String>();
					for (int i=0; i<dbFields.length; i++) {
						String value;
						if (!dbFields[i].equalsIgnoreCase("null")) {
							if (dbFields[i].equalsIgnoreCase(DBTableColName.USER.STATE))
								value = Enum2String.getInstance().getEnumStateName(rs.getString(DBTableColName.USER.STATE));
							else if (dbFields[i].equalsIgnoreCase(DBTableColName.USER.TYPE))
								value = Enum2String.getInstance().getEnumUserTypeName(rs.getString(DBTableColName.USER.TYPE));
							else
								value = rs.getString(dbFields[i]);
						}
						else
							value = Integer.toString(index++);
								 
						rowValue.add(value);
					}
						 
					result.add(new SearchResultRow(rowValue));
				}
				rsWrapper.close();
			}
		} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		}
		
		return result;
	}
	
	private SorterProxy sorterProxy = new SorterProxy(EnumService.USER_SRV);
	private UserDBProcWrapper userDBProc = new UserDBProcWrapper(sorterProxy);
}
