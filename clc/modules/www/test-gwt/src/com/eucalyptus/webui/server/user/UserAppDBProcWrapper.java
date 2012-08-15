package com.eucalyptus.webui.server.user;

import java.sql.SQLException;
import java.util.ArrayList;
import com.eucalyptus.webui.server.db.DBProcWrapper;
import com.eucalyptus.webui.server.db.ResultSetWrapper;
import com.eucalyptus.webui.server.dictionary.DBTableColName;
import com.eucalyptus.webui.server.dictionary.DBTableName;
import com.eucalyptus.webui.shared.user.EnumUserAppState;

public class UserAppDBProcWrapper {
	public void addUserApp(UserApp userApp) throws UserSyncException {
		DBProcWrapper dbProc = DBProcWrapper.Instance();
		String sql = addUserAppSql(userApp);
		
		System.out.println(sql);
		
		try {
			dbProc.update(sql);
		} catch (SQLException e) {
			throw new UserSyncException ("Database fails");
		}
	}
	
	public void updateUserApp(UserApp userApp) throws UserSyncException {
		DBProcWrapper dbProc = DBProcWrapper.Instance();
		String sql = updateUserAppSql(userApp);
		
		try {
			dbProc.update(sql);
		} catch (SQLException e) {
			throw new UserSyncException ("Database fails");
		}
	}
	
	public ResultSetWrapper queryUserApp(int accountId, int userId, EnumUserAppState state) throws UserSyncException {
		DBProcWrapper dbProc = DBProcWrapper.Instance();
		
		StringBuilder sql = userAppAccountUserViewSql();
				
		//EnumUserAppState.DEFAULT means that query all the user applications
		if (state != EnumUserAppState.DEFAULT) {
			sql.append(" AND ").
			append(DBTableName.USER_APP).
			append(".").
			append(DBTableColName.USER_APP.STATE).
			append(" = ").
			append(state.ordinal());
		}
		
		if (accountId != 0) {
			sql.append(" AND ").
			append(DBTableName.USER).
			append(".").
			append(DBTableColName.USER.ACCOUNT_ID).
			append(" = ").
			append(accountId);
		}
		
		if (userId != 0) {
			sql.append(" AND ").
			append(DBTableName.USER_APP).
			append(".").
			append(DBTableColName.USER_APP.USER_ID).
			append(" = ").
			append(userId);
		}
		
		System.out.println(sql.toString());
		
		try {
			ResultSetWrapper result = dbProc.query(sql.toString());
			
			return result;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new UserSyncException("Fail to query user apps");
		}
	}
	
	public void delUserApps(ArrayList<String> ids) throws UserSyncException {
		if (ids == null || ids.size() == 0)
			return;
		
		String sql = delUserAppSql(ids);
		
		DBProcWrapper dbProc = DBProcWrapper.Instance();
		
		try {
			dbProc.update(sql);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new UserSyncException("Database fails");
		}
	}
	
	public void updateUserState(ArrayList<String> ids, EnumUserAppState state) throws UserSyncException {
		if (ids == null || ids.size() == 0)
			return;
		
		String sql = updateUserAppStateSql(ids, state);
		
		DBProcWrapper dbProc = DBProcWrapper.Instance();
		
		try {
			dbProc.update(sql);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new UserSyncException("Database fails");
		}
	}
	
	private String addUserAppSql(UserApp userApp) {
		StringBuilder str = new StringBuilder();
		str.append("INSERT INTO ").
		append(DBTableName.USER_APP).append(" ( ").
		append(DBTableColName.USER_APP.ID).append(", ").
		append(DBTableColName.USER_APP.TIME).append(", ").
		append(DBTableColName.USER_APP.STATE).append(", ").
		append(DBTableColName.USER_APP.DEL).append(", ").
		append(DBTableColName.USER_APP.CONTENT).append(", ").
		append(DBTableColName.USER_APP.COMMENT).append(", ").
		append(DBTableColName.USER_APP.USER_ID).append(", ").
		append(DBTableColName.USER_APP.TEMPLATE_ID).append(") VALUES (null, ");
		
		str.append("'");
		str.append(userApp.getTime().toString());
		str.append("', '");
		
		str.append(userApp.getState().ordinal());
		str.append("', '");
		
		str.append(userApp.getDelState());
		str.append("', '");
		
		str.append(userApp.getContent());
		str.append("', '");
		
		str.append(userApp.getComments());
		str.append("', '");
		
		str.append(userApp.getUserId());
		str.append("', ");
		
		if (userApp.getTemplateId() != 0)
			str.append("null");
		else
			str.append(userApp.getTemplateId());
		
		str.append(")");
		
		return str.toString();
	}
	
	private String updateUserAppSql(UserApp userApp) {
		StringBuilder str = new StringBuilder("UPDATE ").append(DBTableName.USER_APP).append(" SET ");
		
		if (userApp.getState() != EnumUserAppState.DEFAULT) {
			str.append(DBTableColName.USER_APP.STATE).append(" = '").
			append(userApp.getState()).
			append("', ");
		}
		
		if (userApp.getTime() != null) {
			str.append(DBTableColName.USER_APP.TIME).append(" = ").
			append(userApp.getTime()).
			append(", ");
		}
		
		if (userApp.getContent() != null) {
			str.append(DBTableColName.USER_APP.CONTENT).append(" = '").
			append(userApp.getContent()).
			append("', ");
		}
		
		if (userApp.getComments() != null) {
			str.append(DBTableColName.USER_APP.COMMENT).append(" = '").
			append(userApp.getComments()).
			append("', ");
		}
		
		if (userApp.getUserId() != 0) {
			str.append(DBTableColName.USER_APP.USER_ID).append(" = ").
			append(userApp.getUserId()).
			append(", ");
		}
		
		if (userApp.getTemplateId() != 0) {
			str.append(DBTableColName.USER_APP.TEMPLATE_ID).append(" = ").
			append(userApp.getTemplateId());
		}
		
		str.append(" WHERE ").append(DBTableColName.USER_APP.ID).append(" = ").
		append(userApp.getUAId());
		
		return str.toString();
	}
	
	private StringBuilder userAppAccountUserViewSql() {
		StringBuilder sql = new StringBuilder("SELECT ").
				append(DBTableName.USER_APP).append(".*").
				append(", ").
				append(DBTableName.ACCOUNT).append(".").append(DBTableColName.ACCOUNT.NAME).
				append(", ").
				append(DBTableName.USER).append(".*").
				append(" FROM ").
				append("( ").
				append(DBTableName.USER_APP).
				append(" LEFT JOIN ").
				append(DBTableName.USER).
				append(" ON ").
				append(DBTableName.USER).append(".").append(DBTableColName.USER.ID).
				append(" = ").
				append(DBTableName.USER_APP).append(".").append(DBTableColName.USER_APP.USER_ID).
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
	
	private String delUserAppSql(ArrayList<String> ids) {

		StringBuilder sql = new StringBuilder("UPDATE ").append(DBTableName.USER_APP).append(" SET ");
		
		sql.append(DBTableColName.USER_APP.DEL).append(" = 1 ");
		sql.append(" WHERE ");
		
		for (String str : ids) {
			sql.append(DBTableColName.USER_APP.ID).append(" = ").
			append(str).
			append(" OR ");
		}
		
		sql.delete(sql.length() -3 , sql.length());
		
		System.out.println(sql);
		
		return sql.toString();
	}
	
	private String updateUserAppStateSql(ArrayList<String> ids, EnumUserAppState state) {
		StringBuilder sql = new StringBuilder();
		sql.append("UPDATE ").
		append(DBTableName.USER_APP).
		append(" SET ").
		append(DBTableColName.USER_APP.STATE).
		append(" = ").
		append(state.ordinal()).
		append(" WHERE ");
		
		for (String str : ids) {
			sql.append(DBTableColName.USER_APP.ID).
			append(" = '").
			append(str).
			append("' or ");
		}
		
		sql.delete(sql.length() -3 , sql.length());
		
		System.out.println(sql);
		
		return sql.toString();
	}
}
