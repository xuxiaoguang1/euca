package com.eucalyptus.webui.server.user;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.eucalyptus.webui.client.service.SearchRange;
import com.eucalyptus.webui.server.SorterProxy;
import com.eucalyptus.webui.server.db.DBProcWrapper;
import com.eucalyptus.webui.server.db.ResultSetWrapper;
import com.eucalyptus.webui.shared.dictionary.ConfDef;
import com.eucalyptus.webui.shared.dictionary.DBTableColName;
import com.eucalyptus.webui.shared.dictionary.DBTableName;
import com.eucalyptus.webui.shared.user.EnumState;
import com.eucalyptus.webui.shared.user.GroupInfo;
import com.google.common.base.Strings;

public class GroupDBProcWrapper {
	
	public GroupDBProcWrapper(SorterProxy sorterProxy) {
		this.sorterProxy = sorterProxy;
	}
	
	public int addGroup(GroupInfo group) throws UserSyncException {
		DBProcWrapper dbProc = DBProcWrapper.Instance();
		String sql = addGroupSql(group);
		
		try {
			return dbProc.insertAndGetInsertId(sql);
		} catch (SQLException e) {
			throw new UserSyncException ("Database fails");
		}
	}
	
	public void updateGroup(GroupInfo group) throws UserSyncException {
		DBProcWrapper dbProc = DBProcWrapper.Instance();
		String sql = updateGroupSql(group);
		
		try {
			dbProc.update(sql);
		} catch (SQLException e) {
			throw new UserSyncException ("Database fails");
		}
	}
	/**
	 * query total groups
	 * 
	 * @return result set wrapper
	 */
	public ResultSetWrapper queryTotalGroups(SearchRange range) {
		return doQueryTotalGroups(0, range);
	}
	
	/**
	 * query groups by account id
	 * @param accountId
	 * @return result set wrapper
	 */
	public ResultSetWrapper queryGroupsBy(int accountId, SearchRange range) {
		return doQueryTotalGroups(accountId, range);
	}
	
	/**
	 * delete groups by ids
	 * @param ids
	 */
	public void delGroups(ArrayList<String> ids) throws UserSyncException {
		if (ids == null || ids.size() == 0)
			return;
		
		DBProcWrapper dbProc = DBProcWrapper.Instance();
		String sql = delGroupSql(ids);
		
		try {
			dbProc.update(sql);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new UserSyncException("Database fails");
		}
	}
	
	/**
	 * list groups
	 * @param accountId
	 * @return result set wrapper
	 */
	public ResultSetWrapper listTotalGroups(SearchRange range) throws UserSyncException {
		DBProcWrapper dbProc = DBProcWrapper.Instance();
		
		StringBuilder sql = listGroupsSql(0);
		
		if (this.sorterProxy != null) {
			String orderBy = this.sorterProxy.orderBy(range);
			if (orderBy != null)
				sql.append(orderBy);
		}
		
		ResultSetWrapper result;
		try {
			result = dbProc.query(sql.toString());
			return result;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new UserSyncException ("Database fails");
		}
	}
	
	public ResultSetWrapper listGroupsBy(int accountId, SearchRange range) throws UserSyncException {
		DBProcWrapper dbProc = DBProcWrapper.Instance();
		
		StringBuilder sql = listGroupsSql(accountId);
		
		if (this.sorterProxy != null) {
			String orderBy = this.sorterProxy.orderBy(range);
			if (orderBy != null)
				sql.append(orderBy);
		}
		
		ResultSetWrapper result;
		try {
			result = dbProc.query(sql.toString());
			return result;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new UserSyncException ("Database fails");
		}
	}
	
	public void updateGroupState(ArrayList<String> ids, EnumState state) throws UserSyncException {
		DBProcWrapper dbProc = DBProcWrapper.Instance();
		
		String sql = updateGroupStateSql(ids, state);
		
		try {
			dbProc.update(sql);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new UserSyncException ("Database fails");
		}
	}
	
	public GroupInfo queryGroupBy(int groupId) throws UserSyncException {
		if (groupId <= 0)
			return null;
		
		DBProcWrapper dbProc = DBProcWrapper.Instance();
		String sql = queryGroupByIdSql(groupId);
		
		try {
			ResultSetWrapper result = dbProc.query(sql);
			ResultSet rs = result.getResultSet();
			
			if (rs != null) {
				rs.last();
				
				if (rs.getRow() == 0) {
					result.close();
					return null;
				}
				else {
					EnumState state = EnumState.NONE;
					int accountId = 0;
					
					try {
						state = EnumState.values()[Integer.valueOf(rs.getString(DBTableColName.GROUP.STATE))];
						
						String accountIdStr = rs.getString(DBTableColName.GROUP.ACCOUNT_ID);
						if (!Strings.isNullOrEmpty(accountIdStr))
							accountId = Integer.valueOf(accountIdStr);
						
					} catch (Exception e) {
						result.close();
						e.printStackTrace();
						throw new UserSyncException ("Database fails");
					}
					
					GroupInfo group = new GroupInfo();
					group.setId(groupId);
					group.setName(rs.getString(DBTableColName.GROUP.NAME));
					group.setDescription(rs.getString(DBTableColName.GROUP.DESCRIPTION));
					
					group.setState(state);
					group.setAccountId(accountId);
					
					result.close();
					
					return group; 
				}
			}
			else {
				result.close();
				return null;
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new UserSyncException ("Database fails");
		}
	}
	
	private ResultSetWrapper doQueryTotalGroups(int accountId, SearchRange range) {
		DBProcWrapper dbProc = DBProcWrapper.Instance();
		
		StringBuilder sql = new StringBuilder("SELECT ").
									append(DBTableName.GROUP).
									append(".*, ").
									append(DBTableName.ACCOUNT).
									append(".").
									append(DBTableColName.ACCOUNT.NAME).
									append(" FROM ").
									append(DBTableName.GROUP).
									append(" LEFT JOIN ").
									append(DBTableName.ACCOUNT).
									append(" ON ").
									append(DBTableName.GROUP).
									append(".").
									append(DBTableColName.GROUP.ACCOUNT_ID).
									append(" = ").
									append(DBTableName.ACCOUNT).
									append(".").
									append(DBTableColName.ACCOUNT.ID).
									append(" WHERE ").
									append(DBTableName.GROUP).append(".").append(DBTableColName.GROUP.DEL).
									append(" = ").
									append(ConfDef.DB_DEL_FIELD_VALID_STATE);
		
		if (accountId > 0) {
			sql.append(" AND ").
			append(DBTableName.GROUP).
			append(".").
			append(DBTableColName.GROUP.ACCOUNT_ID).
			append(" = '").
			append(accountId).
			append("'");
		}
		
		if (this.sorterProxy != null) {
			String orderBy = this.sorterProxy.orderBy(range);
			if (orderBy != null)
				sql.append(orderBy);
		}
		System.out.println(sql.toString());
		
		try {
			ResultSetWrapper result = dbProc.query(sql.toString());
			return result;
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	public ResultSetWrapper queryUsersByName(int accountId, String groupName) {
		DBProcWrapper dbProc = DBProcWrapper.Instance();
		
		StringBuilder sql = new StringBuilder("SELECT * FROM ").
									append(DBTableName.GROUP).
									append(" WHERE ").
									append(DBTableName.GROUP).append(".").append(DBTableColName.GROUP.DEL).
									append(" = ").
									append(ConfDef.DB_DEL_FIELD_VALID_STATE).
									append(" AND ").
									append(DBTableName.GROUP).append(".").append(DBTableColName.GROUP.ACCOUNT_ID).
									append(" = '").append(accountId).append("'").
									append(" AND ").
									append(DBTableColName.GROUP.NAME).append(" = '").append(groupName).append("'");
		
		try {	
			ResultSetWrapper result = dbProc.query(sql.toString());
			return result;
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	private String addGroupSql(GroupInfo group) {
		StringBuilder str = new StringBuilder();
		str.append("INSERT INTO ").
		append(DBTableName.GROUP).append(" ( ").
		append(DBTableColName.GROUP.ID).append(", ").
		append(DBTableColName.GROUP.NAME).append(", ").
		append(DBTableColName.GROUP.DESCRIPTION).append(", ").
		append(DBTableColName.GROUP.STATE).append(", ").
		append(DBTableColName.GROUP.DEL).append(", ").
		append(DBTableColName.GROUP.ACCOUNT_ID).
		append(") VALUES (null, ");
		
		str.append("'");
		str.append(group.getName());
		str.append("', '");
		
		str.append(group.getDescription());
		str.append("', '");
		
		str.append(group.getState().ordinal());
		str.append("', ");
		
		str.append(ConfDef.DB_DEL_FIELD_VALID_STATE);
		str.append(", ");
		
		int accountId = group.getAccountId();
		
		if (accountId != 0)
			str.append("'").append(group.getAccountId()).append("')");
		else
			str.append("null)");
		
		return str.toString();
	}
	
	private String updateGroupSql(GroupInfo group) {
		StringBuilder str = new StringBuilder("UPDATE ").append(DBTableName.GROUP).append(" SET ");
		
		str.append(DBTableColName.GROUP.NAME).append(" = '").
		append(group.getName()).
		append("', ").
		
		append(" ").append(DBTableColName.GROUP.DESCRIPTION).append(" = '").
		append(group.getDescription()).
		append("', ").
		
		append(" ").append(DBTableColName.GROUP.STATE).append(" = ").
		append(group.getState().ordinal()).
		append(", ");
		
		if (group.getAccountId() == 0) {
			str.append(" ").append(DBTableColName.GROUP.ACCOUNT_ID).append(" = null ");
		}
		else {
			str.append(" ").append(DBTableColName.GROUP.ACCOUNT_ID).append(" = '").
			append(group.getAccountId()).
			append("' ");
		}
		
		str.append("WHERE ").append(DBTableColName.GROUP.ID).append(" = ").
		append(group.getId());
		
		return str.toString();
	}
	
	private String delGroupSql(ArrayList<String> ids) {
		StringBuilder sql = new StringBuilder();
		sql.append("UPDATE ").
		append(DBTableName.GROUP).
		append(" LEFT JOIN ").
		append(DBTableName.USER).
		append(" ON ").
		append(DBTableName.GROUP).append(".").append(DBTableColName.GROUP.ACCOUNT_ID).
		append(" = ").
		append(DBTableName.USER).append(".").append(DBTableColName.USER.ACCOUNT_ID).
		append(" SET ").
		append(DBTableName.GROUP).append(".").append(DBTableColName.GROUP.DEL).append(" = ").append(ConfDef.DB_DEL_FIELD_INVALID_STATE).
		append(", ").
		append(DBTableName.USER).append(".").append(DBTableColName.USER.GROUP_ID).append(" = null ").
		append(" WHERE ");
		
		for (String str : ids) {
			sql.append(" ").append(DBTableName.GROUP).append(".").append(DBTableColName.GROUP.ID).
			append(" = '").
			append(str).
			append("' OR ");
		}
		
		sql.delete(sql.length() -3 , sql.length());
		
		return sql.toString();
	}

	private StringBuilder listGroupsSql(int accountId) {
		StringBuilder sql = new StringBuilder("SELECT * FROM ").
		append(DBTableName.GROUP).
		append(" WHERE ").
		append(DBTableName.GROUP).append(".").append(DBTableColName.GROUP.DEL).append(" = ").append(ConfDef.DB_DEL_FIELD_VALID_STATE);
		
		// account id is valid
		if (accountId > 0) {
			sql.append(" AND ").
			append(DBTableColName.GROUP.ACCOUNT_ID).
			append(" = '").
			append(accountId).
			append("'");
		}
		
		return sql;
	}
	
	private String updateGroupStateSql(ArrayList<String> ids, EnumState state) {
		StringBuilder sql = new StringBuilder("UPDATE ").append(DBTableName.GROUP).append(" SET ");
		
		sql.append(DBTableColName.GROUP.STATE).append(" = ").
		append(state.ordinal()).
		append(" WHERE ");
		
		for (String str : ids) {
			sql.append(" ").append(DBTableColName.GROUP.ID).
			append(" = '").
			append(str).
			append("' OR ");
		}
		
		sql.delete(sql.length() -3 , sql.length());
		
		return sql.toString();
	}
	
	private String queryGroupByIdSql(int groupId) {
		StringBuilder sql = new StringBuilder("SELECT * FROM ").
							append(DBTableName.GROUP).
							append(" WHERE ").
							append(DBTableName.GROUP).append(".").append(DBTableColName.GROUP.DEL).append(" = ").append(ConfDef.DB_DEL_FIELD_VALID_STATE).
							append(" AND ").
							append(DBTableName.GROUP).
							append(".").
							append(DBTableColName.GROUP.ID).
							append(" = ").
							append(groupId);
	
		return sql.toString();
	}
	
	SorterProxy sorterProxy;
}
