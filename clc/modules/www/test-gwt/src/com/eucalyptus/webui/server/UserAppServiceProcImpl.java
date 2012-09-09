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
import com.eucalyptus.webui.client.session.Session;
import com.eucalyptus.webui.server.db.ResultSetWrapper;
import com.eucalyptus.webui.server.dictionary.DBTableColName;
import com.eucalyptus.webui.server.user.UserAppDBProcWrapper;
import com.eucalyptus.webui.server.user.UserAppSyncException;
import com.eucalyptus.webui.server.vm.VITDBProcWrapper;
import com.eucalyptus.webui.server.vm.VITSyncException;
import com.eucalyptus.webui.server.vm.VmImageType;
import com.eucalyptus.webui.shared.dictionary.Enum2String;
import com.eucalyptus.webui.shared.resource.Template;
import com.eucalyptus.webui.shared.user.EnumUserAppStatus;
import com.eucalyptus.webui.shared.user.LoginUserProfile;
import com.eucalyptus.webui.shared.user.UserApp;
import com.eucalyptus.webui.shared.user.UserAppStateCount;

public class UserAppServiceProcImpl {
	  
	  public void addUserApp(Session session, UserApp userApp) throws EucalyptusServiceException {
		  
		  userApp.setStatus(EnumUserAppStatus.APPLYING);
		  userApp.setDelState(0);
		  
		  Calendar cal = Calendar.getInstance();
		  Date date = cal.getTime();
		  userApp.setAppTime(date);
		  
		  long srvDuration = userApp.getSrvEndingTime().getTime() - userApp.getSrvStartingTime().getTime();
		  try {
			  //update device state by user application
			  deviceTemDBProc.actionTemplate(session, userApp.getUserId(), userApp.getTemplateId(), (int)srvDuration);
			  
			  userAppDBProc.addUserApp(userApp);
		  }
		  catch (UserAppSyncException e) {
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
	 * @throws UserAppSyncException 
	   */
	  public SearchResult lookupUserApp( LoginUserProfile curUser, String search, SearchRange range, EnumUserAppStatus state ) throws EucalyptusServiceException {
		  boolean isRootAdmin = curUser.isSystemAdmin();
		  
		  ResultSetWrapper rs;
		  try {
			  if (isRootAdmin) {
				  rs = userAppDBProc.queryUserApp(0, 0, state);
			  }
			  else {		  
				  rs = userAppDBProc.queryUserApp(curUser.getAccountId(), curUser.getUserId(), state);
			  }
		  } catch (UserAppSyncException e) {
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
		} catch (UserAppSyncException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new EucalyptusServiceException("Failed to delete user apps");
		}
	  }
	  
	  public void updateUserApp(UserApp userApp) throws EucalyptusServiceException {
		  try {
			  userAppDBProc.updateUserApp(userApp);
		  } catch (UserAppSyncException e) {
			  // TODO Auto-generated catch block
			  e.printStackTrace();
			  throw new EucalyptusServiceException("Failed to update user apps");
		  }
	  }
	  /**
	   * @param session
	   * @param userAppId
	   * @return eucalyptus vm instance key
	   * @throws EucalyptusServiceException
	   */
	  public String runVMInstance(Session session, int userAppId) throws EucalyptusServiceException {
		  try {
			  UserApp userApp = this.userAppDBProc.lookupUserApp(userAppId);
			  
			  int templateId = userApp.getTemplateId();
			  
			  String keyPair = userApp.getKeyPair();
			  String securityGroup = userApp.getSecurityGroup();
			  
			  String euca_vit_id = null;	  
			  VmImageType vit = this.vitDBProc.lookupVIT(userApp.getVmIdImageTypeId());
			  if (vit != null)
				  euca_vit_id = vit.getEucaVITId();
			  
			  Template template = deviceTemDBProc.lookupTemplateByID(session, templateId);
			  
			  if (keyPair != null && securityGroup != null && euca_vit_id != null) {
				  String euca_vi_key = EucaServiceWrapper.getInstance().runVM(session, template, keyPair, securityGroup, euca_vit_id);
				  
				  if (euca_vi_key != null) {
					  return euca_vi_key;
				  }
				  else
					  throw new EucalyptusServiceException("Failed to get eucalyptus vm instance key");
			  }
			  else
				  throw new EucalyptusServiceException("User's key_pair or security group para error");
			  
		  } catch (UserAppSyncException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new EucalyptusServiceException("Failed to query user app");
		  } catch (VITSyncException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new EucalyptusServiceException("Failed to query eucalyptus vm image type id");
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
			  
		} catch (UserAppSyncException e) {
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
		  
		  DATA = resultSet2List(rs);
		  
		  int resultLength = Math.min( range.getLength( ), DATA.size( ) - range.getStart( ) );
		  SearchResult result = new SearchResult( DATA.size( ), range );
		  result.setDescs( FIELDS );
		  result.setRows( DATA.subList( range.getStart( ), range.getStart( ) + resultLength ) );
			
		  return result;
	  }
	  
	  private List<SearchResultRow> resultSet2List(ResultSetWrapper rsWrapper) {
		  ResultSet rs = rsWrapper.getResultSet();
		  int index = 1;
		  List<SearchResultRow> result = null;
		  
		  try {
			  if (rs != null) {
				  result = new ArrayList<SearchResultRow>();
				  
				  while (rs.next()) {
					  String id = rs.getString(DBTableColName.USER_APP.ID);
					  String accountName = rs.getNString(DBTableColName.ACCOUNT.NAME);
					  String userName = rs.getString(DBTableColName.USER.NAME);
					  String appTime = rs.getString(DBTableColName.USER_APP.APP_TIME);
					  String srvStatingTime = rs.getString(DBTableColName.USER_APP.SRV_STARTINGTIME);
					  String srvEndingTime = rs.getString(DBTableColName.USER_APP.SRV_ENDINGTIME);
					  String state = Enum2String.getInstance().getUserAppStateName(rs.getString(DBTableColName.USER_APP.STATUS));
					  String comment = rs.getString(DBTableColName.USER_APP.COMMENT);
					  
					  String cpu = rs.getString(DBTableColName.TEMPLATE.CPU);
					  String cpuCount = rs.getString(DBTableColName.TEMPLATE.NCPUS);
					  String mem = rs.getString(DBTableColName.TEMPLATE.MEM);
					  String disk = rs.getString(DBTableColName.TEMPLATE.DISK);

					  String template = formatTemplateInfo(cpu, cpuCount, mem, disk);
				
					  String os = rs.getString(DBTableColName.VM_IMAGE_TYPE.OS);
					  String ver = rs.getString(DBTableColName.VM_IMAGE_TYPE.VER);
					  String vmImageInfo = formatVMImageTypeInfo(os, ver);
					  
					  List<String> fields = Arrays.asList(id, Integer.toString(index++), 
							  										accountName, userName,
							  										SearchResultFieldDesc.LINK_VALUE[1], SearchResultFieldDesc.LINK_VALUE[1],
							  										appTime, srvStatingTime, srvEndingTime, 
							  										state, comment != null ? comment : "");
					  
					  List<String> links = Arrays.asList(null, null, null, null, template, vmImageInfo,	null, null, null, null, null, null);
					  
					  SearchResultRow row = new SearchResultRow(fields, links);
					  
					  result.add(row);
				  }
			  }
			rsWrapper.close();			
		  } catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		  }
		  
		  return result;
	  }
	  
	  private String formatTemplateInfo(String cpu, String cpuCount, String mem, String disk) {
		  StringBuilder str = new StringBuilder();
		  
		  str.append("模板详细信息：").append("\n");
		  str.append("    CPU： ").append(cpu).append("\n");
		  str.append("    CPU数量： ").append(cpuCount).append("\n");
		  str.append("    内存： ").append(mem).append("GB").append("\n");
		  str.append("    磁盘： ").append(disk).append("GB");
		  
		  return str.toString();
	  }
	  
	  private String formatVMImageTypeInfo(String os, String version) {
		  StringBuilder str = new StringBuilder();
		  
		  str.append("镜像详细信息：").append("\n");
		  str.append("    操作系统： ").append(os).append("\n");
		  str.append("    版本： ").append(version);
		  
		  return str.toString();
	  }
	  
	  
	  private UserAppDBProcWrapper userAppDBProc = new UserAppDBProcWrapper();
	  private DeviceTemplateServiceProcImpl deviceTemDBProc = new DeviceTemplateServiceProcImpl();
	  private VITDBProcWrapper vitDBProc = new VITDBProcWrapper();
	  
	  private static List<SearchResultRow> DATA = null;
	
	  private static final String[] TABLE_COL_TITLE_CHECKALL = {"Check All", "全选"};
	  private static final String[] TABLE_COL_TITLE_NO = {"No.", "序号"};
	  private static final String[] TABLE_COL_TITLE_ACCOUNT_NAME = {"Account", "账户"};
	  private static final String[] TABLE_COL_TITLE_NAME = {"ID", "用户"};
	  private static final String[] TABLE_COL_TEMPLATE = {"Template", "模板"};
	  private static final String[] TABLE_COL_VM_IMAGE_TYPE = {"VM Image", "虚拟机镜像"};
	  private static final String[] TABLE_COL_TITLE_APPTIME = {"Applying Time", "申请时间"};
	  private static final String[] TABLE_COL_TITLE_SRV_STARTINGTIME = {"Staring Time", "起始时间"};
	  private static final String[] TABLE_COL_TITLE_SRV_ENDINGTIME = {"Ending Time", "结束时间"};
	  private static final String[] TABLE_COL_TITLE_STATE = {"Application State", "申请状态"};
	  private static final String[] TABLE_COL_TITLE_COMMENT = {"Comment", "备注"};
	
	  private static final List<SearchResultFieldDesc> FIELDS_ROOT = Arrays.asList(
				new SearchResultFieldDesc( TABLE_COL_TITLE_CHECKALL[1], "5%", false ),
				new SearchResultFieldDesc( TABLE_COL_TITLE_NO[1], false, "5%", TableDisplay.MANDATORY, Type.TEXT, false, false ),
				new SearchResultFieldDesc( TABLE_COL_TITLE_ACCOUNT_NAME[1], true, "10%", TableDisplay.MANDATORY, Type.TEXT, false, false ),
				new SearchResultFieldDesc( TABLE_COL_TITLE_NAME[1], true, "10%", TableDisplay.MANDATORY, Type.TEXT, false, false ),
				new SearchResultFieldDesc( TABLE_COL_TEMPLATE[1], true, "10%", TableDisplay.MANDATORY, Type.LINK, false, false ),
				new SearchResultFieldDesc( TABLE_COL_VM_IMAGE_TYPE[1], true, "10%", TableDisplay.MANDATORY, Type.LINK, false, false ),
				new SearchResultFieldDesc( TABLE_COL_TITLE_APPTIME[1], true, "10%", TableDisplay.MANDATORY, Type.TEXT, false, false ),
				new SearchResultFieldDesc( TABLE_COL_TITLE_SRV_STARTINGTIME[1], true, "10%", TableDisplay.MANDATORY, Type.TEXT, false, false ),
				new SearchResultFieldDesc( TABLE_COL_TITLE_SRV_ENDINGTIME[1], true, "10%", TableDisplay.MANDATORY, Type.TEXT, false, false ),
				new SearchResultFieldDesc( TABLE_COL_TITLE_STATE[1], true, "10%", TableDisplay.MANDATORY, Type.TEXT, false, false ),
				new SearchResultFieldDesc( TABLE_COL_TITLE_COMMENT[1], true, "20%", TableDisplay.MANDATORY, Type.TEXT, false, false )
				
			);
	  
	  private static final List<SearchResultFieldDesc> FIELDS_NONROOT = Arrays.asList(
			  new SearchResultFieldDesc( TABLE_COL_TITLE_CHECKALL[1], "5%", false ),
				new SearchResultFieldDesc( TABLE_COL_TITLE_NO[1], false, "5%", TableDisplay.MANDATORY, Type.TEXT, false, false ),
				new SearchResultFieldDesc( TABLE_COL_TITLE_ACCOUNT_NAME[1], true, "10%", TableDisplay.MANDATORY, Type.TEXT, false, true ),
				new SearchResultFieldDesc( TABLE_COL_TITLE_NAME[1], true, "10%", TableDisplay.MANDATORY, Type.TEXT, false, false ),
				new SearchResultFieldDesc( TABLE_COL_TEMPLATE[1], true, "10%", TableDisplay.MANDATORY, Type.LINK, false, false ),
				new SearchResultFieldDesc( TABLE_COL_VM_IMAGE_TYPE[1], true, "10%", TableDisplay.MANDATORY, Type.LINK, false, false ),
				new SearchResultFieldDesc( TABLE_COL_TITLE_APPTIME[1], true, "10%", TableDisplay.MANDATORY, Type.TEXT, false, false ),
				new SearchResultFieldDesc( TABLE_COL_TITLE_SRV_STARTINGTIME[1], true, "10%", TableDisplay.MANDATORY, Type.TEXT, false, false ),
				new SearchResultFieldDesc( TABLE_COL_TITLE_SRV_ENDINGTIME[1], true, "10%", TableDisplay.MANDATORY, Type.TEXT, false, false ),
				new SearchResultFieldDesc( TABLE_COL_TITLE_STATE[1], true, "10%", TableDisplay.MANDATORY, Type.TEXT, false, false ),
				new SearchResultFieldDesc( TABLE_COL_TITLE_COMMENT[1], true, "20%", TableDisplay.MANDATORY, Type.TEXT, false, false )
			);
}
