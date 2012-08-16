package com.eucalyptus.webui.server;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.eucalyptus.webui.client.service.EucalyptusServiceException;
import com.eucalyptus.webui.client.service.SearchRange;
import com.eucalyptus.webui.client.service.SearchResult;
import com.eucalyptus.webui.client.service.SearchResultFieldDesc;
import com.eucalyptus.webui.client.service.SearchResultRow;
import com.eucalyptus.webui.client.service.SearchResultFieldDesc.TableDisplay;
import com.eucalyptus.webui.client.service.SearchResultFieldDesc.Type;
import com.eucalyptus.webui.server.db.ResultSetWrapper;
import com.eucalyptus.webui.server.device.DeviceSyncException;
import com.eucalyptus.webui.server.dictionary.DBTableColName;
import com.eucalyptus.webui.server.dictionary.Enum2String;
import com.eucalyptus.webui.server.user.UserAppDBProcWrapper;
import com.eucalyptus.webui.server.user.UserSyncException;
import com.eucalyptus.webui.shared.user.EnumUserAppResult;
import com.eucalyptus.webui.shared.user.EnumUserAppState;
import com.eucalyptus.webui.shared.user.LoginUserProfile;
import com.eucalyptus.webui.shared.user.UserApp;
import com.eucalyptus.webui.shared.user.UserAppStateCount;

public class UserAppServiceProcImpl {
	  
	  public void addUserApp(int userId, int templateId) throws EucalyptusServiceException {
		  UserApp userApp = new UserApp();
		  
		  userApp.setState(EnumUserAppState.TOSOLVE);
		  userApp.setResult(EnumUserAppResult.NONE);
		  userApp.setDelState(0);
		  userApp.setUserId(userId);
		  userApp.setTemplateId(templateId);
		  
		  Calendar cal = Calendar.getInstance();
		  Date date = cal.getTime();
		  userApp.setTime(date);
		  
		  try {
			  //update device state by template id
			  deviceTemDBProc.updateDeviceState(userId, templateId);
			  
			  userAppDBProc.addUserApp(userApp);
			}
		  catch (DeviceSyncException e) {
			  e.printStackTrace();
				throw new EucalyptusServiceException("Failed to sync device state");
		  }
		  catch (UserSyncException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new EucalyptusServiceException("Failed to add user app");
			}
	  }
	  
	  /**
	   * Search keys.
	   * 
	   * @param session
	   * @param search
	   * @param range
	   * @return
	   * @throws EucalyptusServiceException
	 * @throws UserSyncException 
	   */
	  public SearchResult lookupUserApp( LoginUserProfile curUser, String search, SearchRange range, EnumUserAppState state ) throws EucalyptusServiceException {
		  boolean isRootAdmin = curUser.isSystemAdmin();
		  
		  ResultSetWrapper rs;
		  try {
			  if (isRootAdmin) {
				  rs = userAppDBProc.queryUserApp(0, 0, state);
			  }
			  else {		  
				  rs = userAppDBProc.queryUserApp(curUser.getAccountId(), curUser.getUserId(), state);
			  }
		  } catch (UserSyncException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new EucalyptusServiceException("Fail to query user apps");
			}
		  if (rs == null)
			  return null;
		  
		  return getSearchResult(isRootAdmin, rs, range);
	  }
	  
	  public void deleteUserApps(ArrayList<String> ids ) throws EucalyptusServiceException {
		  try {
			  userAppDBProc.delUserApps(ids);
		} catch (UserSyncException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new EucalyptusServiceException("Failed to delete user apps");
		}
	  }
	  
	  public void updateUserApp(UserApp userApp) throws EucalyptusServiceException {
		  try {
			  userAppDBProc.updateUserApp(userApp);
		} catch (UserSyncException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new EucalyptusServiceException("Failed to update user apps");
		}
	  }
	  
	  public ArrayList<UserAppStateCount> countUserApp(LoginUserProfile curUser) throws EucalyptusServiceException {
		  try {
			  if (curUser.isSystemAdmin())
				  return userAppDBProc.countUserAppByState(0,0);
			  else if (curUser.isAccountAdmin())
				  return userAppDBProc.countUserAppByState(curUser.getAccountId(), 0);
			  else
				  return userAppDBProc.countUserAppByState(curUser.getAccountId(), curUser.getUserId());
			  
		} catch (UserSyncException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new EucalyptusServiceException("Failed to update user apps");
		}
	  }
	  
	  private SearchResult getSearchResult(boolean isRootAdmin, ResultSetWrapper rs, SearchRange range) {
		  
		  assert (range != null);
		  
		  List<SearchResultFieldDesc> FIELDS;
		  
		  if (isRootAdmin) {
			 
			  FIELDS = FIELDS_ROOT;
		  }
		  else {
			  FIELDS = FIELDS_NONROOT;
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
	  private List<SearchResultRow> resultSet2List(boolean isRootView, ResultSetWrapper rsWrapper) {
		  ResultSet rs = rsWrapper.getResultSet();
		  int index = 1;
		  List<SearchResultRow> result = null;
		  
		  Date date = new Date();
		  
		  try {
			  if (rs != null) {
				  result = new ArrayList<SearchResultRow>();
				  
				  while (rs.next()) {
					  String id = rs.getString(DBTableColName.USER_APP.ID);
					  String time = rs.getString(DBTableColName.USER_APP.TIME);
					  String state = Enum2String.getInstance().getUserAppStateName(rs.getString(DBTableColName.USER_APP.STATE));
					  String appResult = Enum2String.getInstance().getUserAppResultName(rs.getString(DBTableColName.USER_APP.RESULT));
					  String content = rs.getString(DBTableColName.USER_APP.CONTENT);
					  String comment = rs.getString(DBTableColName.USER_APP.COMMENT);
					  String accountName = rs.getNString(DBTableColName.ACCOUNT.NAME);
					  String userName = rs.getString(DBTableColName.USER.NAME);
					  
					  if (isRootView)
						  result.add( new SearchResultRow(Arrays.asList(id, Integer.toString(index++), time, state, appResult, 
								  										content != null ? content : "", comment != null ? comment : "", accountName, userName)));
					  else
						  result.add( new SearchResultRow(Arrays.asList(id, Integer.toString(index++), time, state, appResult, 
								  										content != null ? content : "", comment != null ? comment : "", userName)));
				  }
			  }
			rsWrapper.close();			
		  } catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		  }
		  
		  return result;
	  }
	  
	  private UserAppDBProcWrapper userAppDBProc = new UserAppDBProcWrapper();
	  private DeviceTemplateServiceProcImpl deviceTemDBProc = new DeviceTemplateServiceProcImpl();
	  
	  private static List<SearchResultRow> DATA = null;
	
	  private static final String[] TABLE_COL_TITLE_CHECKALL = {"Check All", "全选"};
	  private static final String[] TABLE_COL_TITLE_NO = {"No.", "序号"};
	  private static final String[] TABLE_COL_TITLE_TIME = {"Applying Time", "申请时间"};
	  private static final String[] TABLE_COL_TITLE_STATE = {"Application State", "申请状态"};
	  private static final String[] TABLE_COL_TITLE_RESULT = {"Examination Result", "审批结果"};
	  private static final String[] TABLE_COL_TITLE_CONTENT = {"Active", "内容"};
	  private static final String[] TABLE_COL_TITLE_COMMENT = {"Created Date", "备注"};
	  private static final String[] TABLE_COL_TITLE_ACCOUNT_NAME = {"Account", "账户"};
	  private static final String[] TABLE_COL_TITLE_NAME = {"ID", "用户"};
	
	  private static final List<SearchResultFieldDesc> FIELDS_ROOT = Arrays.asList(
				new SearchResultFieldDesc( TABLE_COL_TITLE_CHECKALL[1], "6%", false ),
				new SearchResultFieldDesc( TABLE_COL_TITLE_NO[1], false, "6%", TableDisplay.MANDATORY, Type.TEXT, false, false ),
				new SearchResultFieldDesc( TABLE_COL_TITLE_TIME[1], true, "20%", TableDisplay.MANDATORY, Type.TEXT, false, false ),
				new SearchResultFieldDesc( TABLE_COL_TITLE_STATE[1], true, "6%", TableDisplay.MANDATORY, Type.TEXT, false, false ),
				new SearchResultFieldDesc( TABLE_COL_TITLE_RESULT[1], true, "6%", TableDisplay.MANDATORY, Type.TEXT, false, false ),
				new SearchResultFieldDesc( TABLE_COL_TITLE_CONTENT[1], true, "20%", TableDisplay.MANDATORY, Type.TEXT, false, false ),
				new SearchResultFieldDesc( TABLE_COL_TITLE_COMMENT[1], true, "18%", TableDisplay.MANDATORY, Type.TEXT, false, false ),
				new SearchResultFieldDesc( TABLE_COL_TITLE_ACCOUNT_NAME[1], true, "8%", TableDisplay.MANDATORY, Type.TEXT, false, false ),
				new SearchResultFieldDesc( TABLE_COL_TITLE_NAME[1], true, "8%", TableDisplay.MANDATORY, Type.TEXT, false, false )
			);
	  
	  private static final List<SearchResultFieldDesc> FIELDS_NONROOT = Arrays.asList(
			  new SearchResultFieldDesc( TABLE_COL_TITLE_CHECKALL[1], "6%", false ),
				new SearchResultFieldDesc( TABLE_COL_TITLE_NO[1], false, "6%", TableDisplay.MANDATORY, Type.TEXT, false, false ),
				new SearchResultFieldDesc( TABLE_COL_TITLE_TIME[1], true, "30%", TableDisplay.MANDATORY, Type.TEXT, false, false ),
				new SearchResultFieldDesc( TABLE_COL_TITLE_STATE[1], true, "11%", TableDisplay.MANDATORY, Type.TEXT, false, false ),
				new SearchResultFieldDesc( TABLE_COL_TITLE_RESULT[1], true, "8%", TableDisplay.MANDATORY, Type.TEXT, false, false ),
				new SearchResultFieldDesc( TABLE_COL_TITLE_CONTENT[1], true, "18%", TableDisplay.MANDATORY, Type.TEXT, false, false ),
				new SearchResultFieldDesc( TABLE_COL_TITLE_COMMENT[1], true, "15%", TableDisplay.MANDATORY, Type.TEXT, false, false ),
				new SearchResultFieldDesc( TABLE_COL_TITLE_NAME[1], true, "6%", TableDisplay.MANDATORY, Type.TEXT, false, false )
			);
}
