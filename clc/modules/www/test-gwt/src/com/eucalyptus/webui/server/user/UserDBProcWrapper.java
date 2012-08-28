package com.eucalyptus.webui.server.user;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;

import com.eucalyptus.webui.server.db.DBProcWrapper;
import com.eucalyptus.webui.server.db.ResultSetWrapper;
import com.eucalyptus.webui.server.dictionary.DBTableColName;
import com.eucalyptus.webui.server.dictionary.DBTableName;
import com.eucalyptus.webui.shared.user.EnumState;
import com.eucalyptus.webui.shared.user.EnumUserType;
import com.eucalyptus.webui.shared.user.UserInfo;
import com.google.common.base.Strings;

public class UserDBProcWrapper {
	public void addUser(UserInfo user) throws UserSyncException {
		DBProcWrapper dbProc = DBProcWrapper.Instance();
		String sql = addUserSql(user);
		
		try {
			dbProc.update(sql);
		} catch (SQLException e) {
			throw new UserSyncException ("Database fails");
		}
	}
	
	public void modifyUser(UserInfo user) throws UserSyncException {
		DBProcWrapper dbProc = DBProcWrapper.Instance();
		String sql = updateUserSql(user);

		try {
			dbProc.update(sql);
		} catch (SQLException e) {
			throw new UserSyncException ("Database fails");
		}
	}
	
	public UserInfoAndState lookupUserAndState(String accountName, String userName) throws UserSyncException {
		DBProcWrapper dbProc = DBProcWrapper.Instance();
		String sql = queryUserAndStateSql(accountName, userName);
		System.out.print(sql);
		
		try {
			ResultSetWrapper rsWrapper = dbProc.query(sql);
			ResultSet rs = rsWrapper.getResultSet();
			
			if (!rs.wasNull()) {
				rs.last();
				
				assert(rs.getRow() <= 1);
				
				if (rs.getRow() ==0) {
					rsWrapper.close();
					throw new UserSyncException("User not existed");
				}
				else
				{	
					int userId = Integer.valueOf(rs.getString(DBTableColName.USER.ID));
					String userPwd = rs.getString(DBTableColName.USER.PWD);
					String title = rs.getString(DBTableColName.USER.TITLE);
					String mobile = rs.getString(DBTableColName.USER.MOBILE);
					String email = rs.getString(DBTableColName.USER.EMAIL);
					
					EnumState userState = EnumState.NONE;
					EnumState groupState = EnumState.NONE;
					EnumState accountState = EnumState.NONE;
					
					EnumUserType userType = EnumUserType.NONE;
					int groupId = 0;
					int accountId = 0;

					String userStateStr = rs.getString(DBTableColName.USER.STATE);
					if (!Strings.isNullOrEmpty(userStateStr))
						userState = EnumState.values()[Integer.valueOf(userStateStr)];
					
					String groupStateStr = rs.getString(DBTableColName.GROUP.STATE);
					if (!Strings.isNullOrEmpty(groupStateStr))
						groupState = EnumState.values()[Integer.valueOf(groupStateStr)];
					
					String accountStateStr = rs.getString(DBTableColName.ACCOUNT.STATE);
					if (!Strings.isNullOrEmpty(accountStateStr))
						accountState = EnumState.values()[Integer.valueOf(accountStateStr)];
					
					String userTypeStr = rs.getString(DBTableColName.USER.TYPE);
					if (!Strings.isNullOrEmpty(userTypeStr))
						userType = EnumUserType.values()[Integer.valueOf(userTypeStr)];
					
					String groupIdStr = rs.getString(DBTableColName.USER.GROUP_ID);
					if (!Strings.isNullOrEmpty(groupIdStr))
						groupId = Integer.valueOf(groupIdStr);
					
					String accountIdStr = rs.getString(DBTableColName.USER.ACCOUNT_ID);
					if (!Strings.isNullOrEmpty(accountIdStr))
						accountId = Integer.valueOf(accountIdStr);
						
					UserInfo userInfo = new UserInfo(userId, userName, userPwd, title, mobile, email, userState, userType, groupId, accountId);
					
					UserInfoAndState userAndState = new UserInfoAndState();
					userAndState.setUserInfo(userInfo);
					userAndState.setGroupState(groupState);
					userAndState.setAccountState(accountState);
					
					rsWrapper.close();
					
					return userAndState; 
				}
			}
			else { 
				rsWrapper.close();
				throw new UserSyncException("Database fails");
			}
		} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new UserSyncException("Database fails");
		}
	}
	
	public ResultSetWrapper queryTotalUsers() throws UserSyncException {
		DBProcWrapper dbProc = DBProcWrapper.Instance();
		
		StringBuilder sql = userAccountGroupViewSql();
		
		System.out.println(sql.toString());
		
		try {
			ResultSetWrapper result = dbProc.query(sql.toString());
			
			return result;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new UserSyncException("Fail to query users");
		}
	}
	
	public ResultSetWrapper queryUsersByGroupId(int groupId) throws UserSyncException {
		DBProcWrapper dbProc = DBProcWrapper.Instance();
		
		StringBuilder sql = userAccountGroupViewSql();
		
		sql.append(" AND ").
		append(DBTableName.USER).append(".").append(DBTableColName.USER.GROUP_ID).
		append(" = '").append(groupId).append("' ");
		
		System.out.println(sql.toString());
		
		try {
			ResultSetWrapper result = dbProc.query(sql.toString());
			
			return result;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new UserSyncException("Fail to query users");
		}
	}
	
	public ResultSetWrapper queryUsersByAccountId(int accountId) throws UserSyncException {
		DBProcWrapper dbProc = DBProcWrapper.Instance();
		
		StringBuilder sql = userAccountGroupViewSql();
		
		sql.append(" AND ").
		append(DBTableName.USER).append(".").append(DBTableColName.USER.ACCOUNT_ID).
		append(" = '").append(accountId).append("' ");
		
		System.out.println(sql.toString());
		
		try {
			ResultSetWrapper result = dbProc.query(sql.toString());
			
			return result;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new UserSyncException("Fail to query users");
		}
	}
	
	public ResultSetWrapper queryUsersByAccountIdExcludeGroupId(int accountId, int groupId) throws UserSyncException {
		DBProcWrapper dbProc = DBProcWrapper.Instance();
		
		StringBuilder sql = userAccountGroupViewSql();
		
		sql.append(" AND ").
		append(DBTableName.USER).append(".").append(DBTableColName.USER.ACCOUNT_ID).
		append(" = ").append(accountId).
		append(" AND ").
		append("( ").append(DBTableName.USER).append(".").append(DBTableColName.USER.GROUP_ID).
		append(" != ").
		append(groupId).
		append(" OR ").
		append(DBTableName.USER).append(".").append(DBTableColName.USER.GROUP_ID).
		append(" is NULL )");
		
		System.out.println(sql.toString());
		
		try {
			ResultSetWrapper result = dbProc.query(sql.toString());
			
			return result;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new UserSyncException("Fail to query users");
		}
	}
	
	public ResultSetWrapper queryUsersBy(int accountId, int userId, EnumUserType userType) throws UserSyncException {
		DBProcWrapper dbProc = DBProcWrapper.Instance();
		
		StringBuilder sql = userAccountGroupViewSql();
		sql.append(" AND ").
		append(DBTableName.ACCOUNT).append(".").append(DBTableColName.USER.ACCOUNT_ID).
		append(" = '").append(accountId).append("' ");
		
		switch (userType) {
		  case ADMIN:
			  break;
			  
		  case USER:
			  sql.append(" AND ").
			  append(DBTableColName.USER.ID).
			  append(" = '").
			  append(userId).
			  append("'");
			  break;
			  
		  default:
			  return null;
		  }
		
		System.out.println(sql.toString());
		
		try {
			ResultSetWrapper result = dbProc.query(sql.toString());
			
			return result;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new UserSyncException("Fail to query users");
		}
	}
			
	private StringBuilder userAccountGroupViewSql() {
		StringBuilder sql = new StringBuilder("SELECT ").
				append(DBTableName.USER).append(".*").
				append(", ").
				append(DBTableName.ACCOUNT).append(".").append(DBTableColName.ACCOUNT.NAME).
				append(", ").
				append(DBTableName.GROUP).append(".").append(DBTableColName.GROUP.NAME).
				append(" FROM ").
				append("( ").
				append(DBTableName.USER).
				append(" LEFT JOIN ").
				append(DBTableName.ACCOUNT).
				append(" ON ").
				append(DBTableName.ACCOUNT).append(".").append(DBTableColName.ACCOUNT.ID).
				append(" = ").
				append(DBTableName.USER).append(".").append(DBTableColName.USER.ACCOUNT_ID).
				append(" ) ").
				append(" LEFT JOIN ").
				append(DBTableName.GROUP).
				append(" ON ").
				append(DBTableName.USER).append(".").append(DBTableColName.USER.GROUP_ID).
				append(" = ").
				append(DBTableName.GROUP).append(".").append(DBTableColName.GROUP.ID).
				append(" WHERE 1=1 ");
		
		return sql;
	}
	
	public void delUsers(ArrayList<String> ids) throws UserSyncException {
		if (ids == null || ids.size() == 0)
			return;
		
		String sql = delUserSql(ids);
		
		DBProcWrapper dbProc = DBProcWrapper.Instance();
		
		try {
			dbProc.update(sql);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new UserSyncException("Database fails");
		}
	}
	
	public void updateUserState(ArrayList<String> ids, EnumState userState) throws UserSyncException {
		if (ids == null || ids.size() == 0)
			return;
		
		String sql = updateUserStateSql(ids, userState);
		
		DBProcWrapper dbProc = DBProcWrapper.Instance();
		
		try {
			dbProc.update(sql);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new UserSyncException("Database fails");
		}
	}
	
	public void updateUsersGroup(ArrayList<String> ids, int groupId) throws UserSyncException {
		if (ids == null || ids.size() == 0)
			return;
		
		String sql = updateUserGroupSql(ids, groupId).toString();
		
		DBProcWrapper dbProc = DBProcWrapper.Instance();
		
		try {
			dbProc.update(sql);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new UserSyncException("Database fails");
		}
	}
	
	public void updateUserStateByGroups(ArrayList<String> ids, EnumState userState)  throws UserSyncException {
		if (ids == null || ids.size() == 0)
			return;
		
		String sql = updateUserStateGroupSql(ids, userState).toString();
		
		DBProcWrapper dbProc = DBProcWrapper.Instance();
		
		try {
			dbProc.update(sql);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new UserSyncException("Database fails");
		}
	}
	
	public void updateUserStateByAccounts(ArrayList<String> ids, EnumState userState)  throws UserSyncException {
		if (ids == null || ids.size() == 0)
			return;
		
		String sql = updateUserStateAccountSql(ids, userState).toString();
		
		DBProcWrapper dbProc = DBProcWrapper.Instance();
		
		try {
			dbProc.update(sql);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new UserSyncException("Database fails");
		}
	}
	
	public void modifyUser(int id, String title, String mobile, String email)  throws UserSyncException {
		if (id == 0)
			return;
		
		String sql = modifyUserSql(id, title, mobile, email).toString();
		
		DBProcWrapper dbProc = DBProcWrapper.Instance();
		
		try {
			dbProc.update(sql);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new UserSyncException("Database fails");
		}
	}
	
	public void changePwd(int id, String oldPwd, String newPwd) throws UserSyncException {
		if (id == 0 || Strings.isNullOrEmpty(oldPwd) || Strings.isNullOrEmpty(newPwd))
			return;
		
		String sql = OldPwdIsValidSql(id, oldPwd);
		
		DBProcWrapper dbProc = DBProcWrapper.Instance();
		
		try {
			ResultSetWrapper rsWrapper = dbProc.query(sql);
			ResultSet rs = rsWrapper.getResultSet();
			
			if (!rs.wasNull()) {
				rs.last();
				
				if (rs.getRow() == 1) {
					rsWrapper.close();
					
					sql = changePwdSql(id, newPwd).toString();
					dbProc.update(sql);
				}
				else {
					rsWrapper.close();
					throw new UserSyncException("User old pwd not match");
				}
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new UserSyncException("Database fails");
		}
	}
	
	public void changePwd(int id, String newPwd) throws UserSyncException {
		if (id == 0 || Strings.isNullOrEmpty(newPwd))
			return;
		
		DBProcWrapper dbProc = DBProcWrapper.Instance();
		
		String sql = changePwdSql(id, newPwd).toString();
		try {
			dbProc.update(sql);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new UserSyncException("Change Pwd Error");
		}
	}
	
	public void addAccessKey(int userId, String akey, String skey, boolean active, Date created_date) throws UserSyncException {
		if (userId == 0 || Strings.isNullOrEmpty(akey) || Strings.isNullOrEmpty(skey))
			return;
		
		DBProcWrapper dbProc = DBProcWrapper.Instance();
		
		String sql = addAccessKeySql(userId, akey, skey, active, created_date).toString();
		
		try {
			dbProc.update(sql);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new UserSyncException("Add user access key error");
		}
	}
	
	public ResultSetWrapper queryTotalUserKeys() throws UserSyncException {
		DBProcWrapper dbProc = DBProcWrapper.Instance();
		
		StringBuilder sql = userKeyAccountGroupViewSql();
		
		System.out.println(sql.toString());
		
		try {
			ResultSetWrapper result = dbProc.query(sql.toString());
			
			return result;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new UserSyncException("Fail to Query user access key");
		}
	}
	
	public ResultSetWrapper queryUserKeysBy(int accountId, int userId, EnumUserType userType)  throws UserSyncException {
		DBProcWrapper dbProc = DBProcWrapper.Instance();
		
		StringBuilder sql = userKeyAccountGroupViewSql();
		sql.append(" AND ").
		append(DBTableName.USER).append(".").append(DBTableColName.USER.ACCOUNT_ID).
		append(" = '").append(accountId).append("' ");
		
		switch (userType) {
		  case ADMIN:
			  break;
			  
		  case USER:
			  sql.append(" AND ").
			  append(DBTableName.USER).append(".").append(DBTableColName.USER.ID).
			  append(" = '").
			  append(userId).
			  append("'");
			  break;
			  
		  default:
			  return null;
		  }
		
		System.out.println(sql.toString());
		
		try {
			System.out.println(sql.toString());
			ResultSetWrapper result = dbProc.query(sql.toString());
			
			return result;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new UserSyncException("Fail to Query user access key");
		}
	}
	
	public void delUserKeys(ArrayList<String> ids) throws UserSyncException {
		if (ids == null || ids.size() == 0)
			return;
		
		String sql = delUserKeySql(ids);
		
		DBProcWrapper dbProc = DBProcWrapper.Instance();
		
		try {
			dbProc.update(sql);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new UserSyncException("Failed to delete user key");
		}
	}
	
	public void modifyUserKeyState(ArrayList<String> ids, boolean active) throws UserSyncException {
		if (ids == null || ids.size() == 0)
			return;
		
		String sql = modifyUserKeyStateSql(ids, active);
		
		DBProcWrapper dbProc = DBProcWrapper.Instance();
		
		try {
			dbProc.update(sql);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new UserSyncException("Failed to udpate user keys");
		}
	}
	
	private String addUserSql(UserInfo user) {
		StringBuilder str = new StringBuilder();
		str.append("INSERT INTO ").
		append(DBTableName.USER).append(" ( ").
		append(DBTableColName.USER.ID).append(", ").
		append(DBTableColName.USER.NAME).append(", ").
		append(DBTableColName.USER.PWD).append(", ").
		append(DBTableColName.USER.TITLE).append(", ").
		append(DBTableColName.USER.MOBILE).append(", ").
		append(DBTableColName.USER.EMAIL).append(", ").
		append(DBTableColName.USER.TYPE).append(", ").
		append(DBTableColName.USER.STATE).append(", ").
		append(DBTableColName.USER.GROUP_ID).append(", ").
		append(DBTableColName.USER.ACCOUNT_ID).append(") VALUES (null, ");
		
		str.append("'");
		str.append(user.getName());
		str.append("', '");
		
		str.append(user.getPwd());
		str.append("', '");
		
		str.append(user.getTitle());
		str.append("', '");
		
		str.append(user.getMobile());
		str.append("', '");
		
		str.append(user.getEmail());
		str.append("', '");
		
		str.append(user.getType().ordinal());
		str.append("', '");
		
		str.append(user.getState().ordinal());
		str.append("', ");
		
		int groupId = user.getGroupId();
		
		if (groupId != 0)
			str.append("'").append(user.getGroupId()).append("', ");
		else
			str.append("null, ");
		
		int accountId = user.getAccountId();
		
		if (accountId != 0)
			str.append("'").append(user.getAccountId()).append("')");
		else
			str.append("null)");
		
		return str.toString();
	}
	
	private String updateUserSql(UserInfo user) {
		StringBuilder str = new StringBuilder("UPDATE ").append(DBTableName.USER).append(" SET ");
		
		str.append(DBTableColName.USER.NAME).append(" = '").
		append(user.getName()).
		append("', ").
		
		append(DBTableColName.USER.TITLE).append(" = '").
		append(user.getTitle()).
		append("', ").
		
		append(DBTableColName.USER.MOBILE).append(" = '").
		append(user.getMobile()).
		append("', ").
		
		append(DBTableColName.USER.EMAIL).append(" = '").
		append(user.getEmail()).
		append("', ").
		
		append(DBTableColName.USER.TYPE).append(" = '").
		append(user.getType().ordinal()).
		append("', ").
		
		append(DBTableColName.USER.ACCOUNT_ID).append(" = '").
		append(user.getAccountId()).
		append("', ");
		
		if (user.getGroupId() != 0) {
			str.append(DBTableColName.USER.GROUP_ID).append(" = '").
			append(user.getGroupId()).
			append("', ");
		}
		else {
			str.append(DBTableColName.USER.GROUP_ID).append(" = null, ");
		}
		
		str.append(DBTableColName.USER.STATE).append(" = '").
		append(user.getState().ordinal()).
		append("' ").
		
		append("WHERE ").append(DBTableColName.USER.ID).append(" = '").
		append(user.getId()).
		append("'");
		
		return str.toString();
	}
	
	private String queryUserAndStateSql(String accountName, String userName) {
		StringBuilder sql = new StringBuilder("SELECT ").
							append(DBTableName.USER).append(".*").
							append(", ").
							append(DBTableName.ACCOUNT).append(".").append(DBTableColName.ACCOUNT.STATE).
							append(", ").
							append(DBTableName.GROUP).append(".").append(DBTableColName.GROUP.STATE).
							append(" FROM ").
							append("( ").
							append(DBTableName.USER).
							append(" LEFT JOIN ").
							append(DBTableName.ACCOUNT).
							append(" ON ").
							append(DBTableName.ACCOUNT).append(".").append(DBTableColName.ACCOUNT.ID).
							append(" = ").
							append(DBTableName.USER).append(".").append(DBTableColName.USER.ACCOUNT_ID).
							append(" ) ").
							append(" LEFT JOIN ").
							append(DBTableName.GROUP).
							append(" ON ").
							append(DBTableName.USER).append(".").append(DBTableColName.USER.GROUP_ID).
							append(" = ").
							append(DBTableName.GROUP).append(".").append(DBTableColName.GROUP.ID).
							append(" WHERE 1=1 ");
		
		sql.append(" AND ").
			append(DBTableName.ACCOUNT).
			append(".").
			append(DBTableColName.ACCOUNT.NAME).
			append(" = '").
			append(accountName).
			append("' AND ").
			append(DBTableName.USER).
			append(".").
			append(DBTableColName.USER.NAME).
			append(" = '").
			append(userName).
			append("'");
				
		return sql.toString();
	}
	
	private String delUserSql(ArrayList<String> ids) {
		StringBuilder sql = new StringBuilder();
		sql.append("DELETE FROM ").
		append(DBTableName.USER).
		append(" WHERE ");
		
		for (String str : ids) {
			sql.append(" ").append(DBTableColName.USER.ID).
			append(" = '").
			append(str).
			append("' OR ");
		}
		
		sql.delete(sql.length() -3 , sql.length());
		
		System.out.println(sql);
		
		return sql.toString();
	}
	
	private String updateUserStateSql(ArrayList<String> ids, EnumState userState) {
		StringBuilder sql = new StringBuilder();
		sql.append("UPDATE ").
		append(DBTableName.USER).
		append(" SET ").
		append(DBTableColName.USER.STATE).
		append(" = ").
		append(userState.ordinal()).
		append(" WHERE ");
		
		for (String str : ids) {
			sql.append(DBTableColName.USER.ID).
			append(" = '").
			append(str).
			append("' or ");
		}
		
		sql.delete(sql.length() -3 , sql.length());
		
		System.out.println(sql);
		
		return sql.toString();
	}
	
	private StringBuilder updateUserGroupSql(ArrayList<String> ids, int groupId) {
		StringBuilder sql = new StringBuilder();
		sql.append("UPDATE ").
		append(DBTableName.USER).
		append(" SET ").
		append(DBTableColName.USER.GROUP_ID).
		append(" = ");
		
		if (groupId == 0)
			sql.append("null");
		else
			sql.append(groupId);
		
		sql.append(" WHERE ");
		
		for (String str : ids) {
			sql.append(DBTableColName.USER.ID).
			append(" = '").
			append(str).
			append("' or ");
		}
		
		sql.delete(sql.length() -3 , sql.length());
		
		System.out.println(sql);
		
		return sql;
	}
	
	private StringBuilder updateUserStateGroupSql(ArrayList<String> ids, EnumState userState) {
		StringBuilder sql = new StringBuilder();
		sql.append("UPDATE ").
		append(DBTableName.USER).
		append(" SET ").
		append(DBTableColName.USER.STATE).
		append(" = ").
		append(userState.ordinal());
		
		sql.append(" WHERE ");
		
		for (String str : ids) {
			sql.append(DBTableColName.USER.GROUP_ID).
			append(" = '").
			append(str).
			append("' or ");
		}
		
		sql.delete(sql.length() -3 , sql.length());
		
		System.out.println(sql);
		
		return sql;
	}
	
	private StringBuilder updateUserStateAccountSql(ArrayList<String> ids, EnumState userState) {
		StringBuilder sql = new StringBuilder();
		sql.append("UPDATE ").
		append(DBTableName.USER).
		append(" SET ").
		append(DBTableColName.USER.STATE).
		append(" = ").
		append(userState.ordinal());
		
		sql.append(" WHERE ");
		
		for (String str : ids) {
			sql.append(DBTableColName.USER.ACCOUNT_ID).
			append(" = '").
			append(str).
			append("' or ");
		}
		
		sql.delete(sql.length() -3 , sql.length());
		
		System.out.println(sql);
		
		return sql;
	}
	
	private StringBuilder modifyUserSql(int id, String title, String mobile, String email) {
		StringBuilder sql = new StringBuilder();
		sql.append("UPDATE ").
		append(DBTableName.USER).
		append(" SET ");
		
		if (title != null) {
			sql.append(DBTableColName.USER.TITLE).
			append(" = '").
			append(title).
			append("', ");
		}
		
		if (mobile != null) {
			sql.append(DBTableColName.USER.MOBILE).
			append(" = '").
			append(mobile).
			append("', ");
		}
		
		if (email != null) {
			sql.append(DBTableColName.USER.EMAIL).
			append(" = '").
			append(email).
			append("' , ");
		}
		
		sql.delete(sql.length() - 2, sql.length());
		
		sql.append(" WHERE ").
		append(DBTableColName.USER.ID).
		append(" = ").
		append(id);
		
		System.out.println(sql);
		
		return sql;
	}
	
	private String OldPwdIsValidSql(int id, String oldPwd) {
		StringBuilder sql = new StringBuilder("SELECT ").
				append(DBTableName.USER).
				append(".* FROM ").
				append(DBTableName.USER).
				append(" WHERE ").
				append(DBTableColName.USER.ID).
				append(" = '").
				append(id).
				append("' AND ").
				append(DBTableColName.USER.PWD).
				append(" = '").
				append(oldPwd).
				append("'");
		
		System.out.println(sql);
		
		return sql.toString();
	}
	
	private StringBuilder changePwdSql(int id, String newPwd) {
		StringBuilder sql = new StringBuilder();
		sql.append("UPDATE ").
		append(DBTableName.USER).
		append(" SET ").
		append(DBTableColName.USER.PWD).
		append(" = '").
		append(newPwd).
		append("' ");
		
		sql.append(" WHERE ").
		append(DBTableColName.USER.ID).
		append(" = ").
		append(id);
		
		System.out.println(sql);
		
		return sql;
	}
	
	private StringBuilder addAccessKeySql(int userId, String akey, String skey, boolean active, Date created_date) {
		StringBuilder sql = new StringBuilder();
		sql.append("INSERT INTO ").
		append(DBTableName.USER_KEY).append(" ( ").
		append(DBTableColName.USER_KEY.ID).append(", ").
		append(DBTableColName.USER_KEY.AKEY).append(", ").
		append(DBTableColName.USER_KEY.SKEY).append(", ").
		append(DBTableColName.USER_KEY.ACTIVE).append(", ").
		append(DBTableColName.USER_KEY.CREATED_DATE).append(", ").
		append(DBTableColName.USER_KEY.USER_ID).append(") VALUES (null, ");
		
		sql.append("'");
		sql.append(akey);
		sql.append("', '");
		
		sql.append(skey);
		sql.append("', ");
		
		sql.append(active);
		sql.append(", ");
		
		sql.append("CURDATE()");
		sql.append(", '");
		
		sql.append(userId);
		sql.append("')");
		
		return sql;
	}
	
	private StringBuilder userKeyAccountGroupViewSql() {
		StringBuilder sql = new StringBuilder("SELECT * ").
				append(" FROM ").
				append("( (").
				append(DBTableName.USER_KEY).
				append(" LEFT JOIN ").
				append(DBTableName.USER).
				append(" ON ").
				append(DBTableName.USER_KEY).append(".").append(DBTableColName.USER_KEY.USER_ID).
				append(" = ").
				append(DBTableName.USER).append(".").append(DBTableColName.USER.ID).
				append(") ").
				append(" LEFT JOIN ").
				append(DBTableName.GROUP).
				append(" ON ").
				append(DBTableName.USER).append(".").append(DBTableColName.USER.GROUP_ID).
				append(" = ").
				append(DBTableName.GROUP).append(".").append(DBTableColName.GROUP.ID).
				append(" ) ").
				append(" LEFT JOIN ").
				append(DBTableName.ACCOUNT).
				append(" ON ").
				append(DBTableName.USER).append(".").append(DBTableColName.USER.ACCOUNT_ID).
				append(" = ").
				append(DBTableName.ACCOUNT).append(".").append(DBTableColName.ACCOUNT.ID).
				append(" WHERE 1=1 ");
		
		return sql;
	}
	
	private String delUserKeySql(ArrayList<String> ids) {
		StringBuilder sql = new StringBuilder();
		sql.append("DELETE FROM ").
		append(DBTableName.USER_KEY).
		append(" WHERE ");
		
		for (String str : ids) {
			sql.append(" ").append(DBTableColName.USER_KEY.ID).
			append(" = '").
			append(str).
			append("' OR ");
		}
		
		sql.delete(sql.length() -3 , sql.length());
		
		System.out.println(sql);
		
		return sql.toString();
	}
	
	private String modifyUserKeyStateSql(ArrayList<String> ids, boolean active) {
		StringBuilder sql = new StringBuilder();
		sql.append("UPDATE ").
		append(DBTableName.USER_KEY).
		append(" SET ").
		append(DBTableColName.USER_KEY.ACTIVE).
		append(" = ").
		append(active).
		append(" WHERE ");
		
		for (String str : ids) {
			sql.append(DBTableColName.USER.ID).
			append(" = '").
			append(str).
			append("' or ");
		}
		
		sql.delete(sql.length() -3 , sql.length());
		
		System.out.println(sql);
		
		return sql.toString();
	}
}
