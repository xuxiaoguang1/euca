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
import com.eucalyptus.webui.server.dictionary.DBTableColName;
import com.eucalyptus.webui.server.user.GroupDBProcWrapper;
import com.eucalyptus.webui.server.user.LoginUserProfileStorer;
import com.eucalyptus.webui.server.user.UserSyncException;
import com.eucalyptus.webui.shared.config.EnumService;
import com.eucalyptus.webui.shared.config.LanguageSelection;
import com.eucalyptus.webui.shared.config.SearchTableCol;
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
		  
		if (isRootAdmin) {
			rs = groupDBProc.queryTotalGroups(range);
		}
		else {		  
			rs = groupDBProc.queryGroupsBy(curUser.getAccountId(), range);
		}
		  
		if (rs == null)
			return null;
		  
		List<SearchResultRow> DATA = resultSet2List(rs);
		  
		int resultLength = Math.min( range.getLength( ), DATA.size( ) - range.getStart( ) );
		SearchResult result = new SearchResult(DATA.size(), range );
		result.setDescs( ViewSearchTableServerConfig.instance().getConfig(EnumService.GROUP_SRV, LanguageSelection.instance().getCurLanguage()) );
	  
		if (DATA.size() > 0)
			result.setRows( DATA.subList( range.getStart( ), range.getStart( ) + resultLength ) );
		
		return result;
	}
	
	public ArrayList<GroupInfo> listGroups(Session session) throws EucalyptusServiceException {
		LoginUserProfile curUser = LoginUserProfileStorer.instance().get(session.getId());
		boolean isRootAdmin = curUser.isSystemAdmin();
		
		ResultSetWrapper rs;
		ArrayList<GroupInfo> groups = new ArrayList<GroupInfo>();
		
		try {  
			if (isRootAdmin) {
				rs = groupDBProc.listTotalGroups(null);
			}
			else {		  
				rs = groupDBProc.listGroupsBy(curUser.getAccountId(), null);
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
	  
	private List<SearchResultRow> resultSet2List(ResultSetWrapper rsWrapper) {
	
		ResultSet rs = rsWrapper.getResultSet();
		int index = 1;
		List<SearchResultRow> result = null;
		try {
			if (rs != null) {
				result = new ArrayList<SearchResultRow>();
				
				ArrayList<SearchTableCol> tableCols = ViewSearchTableServerConfig.instance().getConfig(EnumService.GROUP_SRV);
				String[] dbFields = new String[tableCols.size()];
				  
				for (int i=0; i<dbFields.length; i++)
					dbFields[i] = tableCols.get(i).getDbField();
				  
				while (rs.next()) {
					ArrayList<String> rowValue = new ArrayList<String>();
					 
					for (int i=0; i<dbFields.length; i++) {
						String value;
						  
						if (!dbFields[i].equalsIgnoreCase("null")) {
							if (dbFields[i].equalsIgnoreCase(DBTableColName.GROUP.STATE))
								value = Enum2String.getInstance().getEnumStateName(rs.getString(DBTableColName.GROUP.STATE));
							else
								value = rs.getString(dbFields[i]);
						}
						else
							value = Integer.toString(index++);
					  			 
						rowValue.add(value);
					}
					
					result.add( new SearchResultRow(rowValue));
				  }
			}

			rsWrapper.close();			
		  } catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		  }
		  
		  return result;
	}
	
	private SorterProxy sorterProxy = new SorterProxy(EnumService.GROUP_SRV);
	private GroupDBProcWrapper groupDBProc = new GroupDBProcWrapper(sorterProxy);
}
