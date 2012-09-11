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
import com.eucalyptus.webui.client.session.Session;
import com.eucalyptus.webui.server.db.ResultSetWrapper;
import com.eucalyptus.webui.server.dictionary.DBTableColName;
import com.eucalyptus.webui.server.user.GroupDBProcWrapper;
import com.eucalyptus.webui.server.user.LoginUserProfileStorer;
import com.eucalyptus.webui.server.user.UserSyncException;
import com.eucalyptus.webui.shared.dictionary.Enum2String;
import com.eucalyptus.webui.shared.user.EnumState;
import com.eucalyptus.webui.shared.user.GroupInfo;
import com.eucalyptus.webui.shared.user.LoginUserProfile;

public class GroupServiceProcImpl {
	
	public void createGroup(int accountId, GroupInfo group)
	  		throws EucalyptusServiceException {
	  	// TODO Auto-generated method stub
	  	if (group == null) {
	  		throw new EucalyptusServiceException( "Invalid group para on creating group" );
	  	}
	  	
	  	// group account_id == 0, which means that this group's account is same as the session's account
		if (group.getAccountId() == 0)
			  group.setAccountId(accountId);
	  	
	  	try {
			groupDBProc.addGroup(group);
		} catch (UserSyncException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new EucalyptusServiceException("Failed to create group");
		}
	}
	
	public void updateGroup(GroupInfo group)
	  		throws EucalyptusServiceException {
	  	// TODO Auto-generated method stub
	  	if (group == null) {
	  		throw new EucalyptusServiceException( "Invalid group para on updating group" );
	  	}
	  	
	  	try {
			groupDBProc.updateGroup(group);
		} catch (UserSyncException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new EucalyptusServiceException("Failed to update group");
		}
	}

	public void deleteGroups( Session session, ArrayList<String> ids ) throws EucalyptusServiceException {
	    // TODO Auto-generated method stub
		if (ids == null || ids.size() == 0) {
	  		throw new EucalyptusServiceException( "deleteGroups: invalid ids" );
	  	}
		
		try {
			groupDBProc.delGroups(ids);
		} catch (UserSyncException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new EucalyptusServiceException("Create group fails");
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
	   */
	public SearchResult lookupGroup( Session session, String search, SearchRange range ) throws EucalyptusServiceException {
		assert(range != null);
		
		LoginUserProfile curUser = LoginUserProfileStorer.instance().get(session.getId());
		boolean isRootAdmin = curUser.isSystemAdmin();
		
		ResultSetWrapper rs;
		List<SearchResultFieldDesc> FIELDS;
		  
		if (isRootAdmin) {
			rs = groupDBProc.queryTotalGroups();
			FIELDS = GroupServiceProcImpl.FIELDS_ROOT;
		}
		else {		  
			rs = groupDBProc.queryGroupsBy(curUser.getAccountId());
			FIELDS = GroupServiceProcImpl.FIELDS_NONROOT;
		}
		  
		if (rs == null)
			return null;
		
		final int sortField = range.getSortField( );
		  
		List<SearchResultRow> DATA = resultSet2List(isRootAdmin, rs);
		  
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
	
	public ArrayList<GroupInfo> listGroups(Session session) throws EucalyptusServiceException {
		LoginUserProfile curUser = LoginUserProfileStorer.instance().get(session.getId());
		boolean isRootAdmin = curUser.isSystemAdmin();
		
		ResultSetWrapper rs;
		ArrayList<GroupInfo> groups = new ArrayList<GroupInfo>();
		
		try {  
			if (isRootAdmin) {
				rs = groupDBProc.listTotalGroups();
			}
			else {		  
				rs = groupDBProc.listGroupsBy(curUser.getAccountId());
			}
			
			ResultSet resultSet = rs.getResultSet();
			
			while (resultSet.next()) {
				int groupId = Integer.valueOf(resultSet.getString(DBTableColName.GROUP.ID));
				String name = resultSet.getString(DBTableColName.GROUP.NAME);
				
				GroupInfo group = new GroupInfo();
				group.setId(groupId);
				group.setName(name);
				
				groups.add(group);
			}
			
			rs.close();
			
			return groups;
		} catch (UserSyncException e) {
				// TODO Auto-generated catch block
			e.printStackTrace();
			throw new EucalyptusServiceException("List groups info fails");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new EucalyptusServiceException("List groups info fails");
		}
	}
	
	public void updateGroupState(Session session, ArrayList<String> ids, EnumState state) throws EucalyptusServiceException {
		if (ids == null) {
	  		throw new EucalyptusServiceException( "Invalid group ids para on update group state" );
	  	}
		
		try {
			groupDBProc.updateGroupState(ids, state);
		} catch (UserSyncException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new EucalyptusServiceException("Failed to update group state ");
		}
	}
	  
	private GroupDBProcWrapper groupDBProc = new GroupDBProcWrapper();
	  
	private List<SearchResultRow> resultSet2List(boolean isRootView, ResultSetWrapper rsWrapper) {
	
		ResultSet rs = rsWrapper.getResultSet();
		int index = 1;
		List<SearchResultRow> result = null;
		try {
			if (rs != null) {
				result = new ArrayList<SearchResultRow>();
				  
				while (rs.next()) {
					  if (isRootView) {
						  result.add( new SearchResultRow(Arrays.asList(rs.getString(DBTableColName.GROUP.ID),
								  							Integer.toString(index++),
								  							rs.getString(DBTableColName.ACCOUNT.NAME),
								  							rs.getString(DBTableColName.GROUP.NAME),
															rs.getString(DBTableColName.GROUP.DESCRIPTION),
															Enum2String.getInstance().getEnumStateName(rs.getString(DBTableColName.GROUP.STATE)),
															rs.getString(DBTableColName.GROUP.ACCOUNT_ID))
								  							));
					  }
				  }
			}

			rsWrapper.close();			
		  } catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		  }
		  
		  return result;
	  }
	
	  private static final String[] TABLE_COL_TITLE_CHECKALL = {"Check All", "全选"};
	  private static final String[] TABLE_COL_TITLE_NO = {"No.", "序号"};
	  private static final String[] TABLE_COL_TITLE_ACCOUNT_NAME = {"Account", "账户"};
	  private static final String[] TABLE_COL_TITLE_NAME = {"Group name", "组名"};
	  private static final String[] TABLE_COL_TITLE_DESCRIPTION = {"Description", "备注"};
	  private static final String[] TABLE_COL_TITLE_STATE = {"State", "状态"};
	  private static final String[] TABLE_COL_TITLE_ACCOUNT_ID = {"AccountId", "账户ID"};
	
	  private static final List<SearchResultFieldDesc> FIELDS_ROOT = Arrays.asList(
				new SearchResultFieldDesc( TABLE_COL_TITLE_CHECKALL[1], "10%", false ),
				new SearchResultFieldDesc( TABLE_COL_TITLE_NO[1], false, "10%", TableDisplay.MANDATORY, Type.TEXT, false, false ),
				new SearchResultFieldDesc( TABLE_COL_TITLE_ACCOUNT_NAME[1], true, "20%", TableDisplay.MANDATORY, Type.TEXT, false, false ),
				new SearchResultFieldDesc( TABLE_COL_TITLE_NAME[1], true, "20%", TableDisplay.MANDATORY, Type.TEXT, false, false ),
				new SearchResultFieldDesc( TABLE_COL_TITLE_DESCRIPTION[1], false, "30%", TableDisplay.MANDATORY, Type.TEXT, false, false ),
				new SearchResultFieldDesc( TABLE_COL_TITLE_STATE[1], false, "10%", TableDisplay.MANDATORY, Type.TEXT, false, false ),
				new SearchResultFieldDesc( TABLE_COL_TITLE_ACCOUNT_ID[1], true, "0%", TableDisplay.MANDATORY, Type.TEXT, false, true )					
			);
	  
	  private static final List<SearchResultFieldDesc> FIELDS_NONROOT = Arrays.asList(
			  	new SearchResultFieldDesc( TABLE_COL_TITLE_CHECKALL[1], "10%", false ),
			  	new SearchResultFieldDesc( TABLE_COL_TITLE_NO[1], false, "10%", TableDisplay.MANDATORY, Type.TEXT, false, false ),
			  	new SearchResultFieldDesc( TABLE_COL_TITLE_ACCOUNT_NAME[1], true, "20%", TableDisplay.MANDATORY, Type.TEXT, false, true ),
			  	new SearchResultFieldDesc( TABLE_COL_TITLE_NAME[1], true, "25%", TableDisplay.MANDATORY, Type.TEXT, false, false ),
			  	new SearchResultFieldDesc( TABLE_COL_TITLE_DESCRIPTION[1], false, "35%", TableDisplay.MANDATORY, Type.TEXT, false, false ),
			  	new SearchResultFieldDesc( TABLE_COL_TITLE_STATE[1], false, "10%", TableDisplay.MANDATORY, Type.TEXT, false, false ),
			  	new SearchResultFieldDesc( TABLE_COL_TITLE_ACCOUNT_ID[1], true, "0%", TableDisplay.MANDATORY, Type.TEXT, false, true )
			);
}
