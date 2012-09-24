package com.eucalyptus.webui.server.stat;

import java.sql.SQLException;

import com.eucalyptus.webui.server.db.DBProcWrapper;
import com.eucalyptus.webui.server.db.ResultSetWrapper;
import com.eucalyptus.webui.shared.dictionary.DBTableColName;
import com.eucalyptus.webui.shared.dictionary.DBTableName;
import com.eucalyptus.webui.shared.user.EnumUserType;

public class HistoryDBProcWrapper {
	
	public ResultSetWrapper queryTotalHistory() throws HistorySyncException{
		DBProcWrapper dbProc = DBProcWrapper.Instance();
		
		StringBuilder sql = historyAccountGroupViewSql();
		
		System.out.println(sql.toString());
		
		try {
			ResultSetWrapper result = dbProc.query(sql.toString());
			return result;
		} catch (SQLException e) {
			e.printStackTrace();
			throw new HistorySyncException("Fail to Query History");
		}
	}
	
	public ResultSetWrapper queryHistoryBy(int accountId, int userId, EnumUserType userType) throws HistorySyncException{
		DBProcWrapper dbProc = DBProcWrapper.Instance();

		StringBuilder sql = historyAccountGroupViewSql();
		
		sql.append(" AND ").
		append(DBTableName.USER).append(".").append(DBTableColName.USER.ACCOUNT_ID).
		append(" = '").append(accountId).append("' ");
		
		switch (userType) {
		  case ADMIN:
			  break;
		  case USER:
		  default:
			  sql.append(" AND ").
			  append(DBTableName.USER).append(".").append(DBTableColName.USER.ID).
			  append(" = '").
			  append(userId).
			  append("'");
			  break;
		  }
		
		System.out.println(sql.toString());
		
		try {
			ResultSetWrapper result = dbProc.query(sql.toString());
			return result;
		} catch (SQLException e) {
			e.printStackTrace();
			throw new HistorySyncException("Fail to Query History");
		}
	}
	
	private StringBuilder historyAccountGroupViewSql() {
		StringBuilder sql = new StringBuilder("SELECT * ").
				append(" FROM ").
				append("( (").
				append(DBTableName.HISTORY).
				append(" LEFT JOIN ").
				append(DBTableName.USER).
				append(" ON ").
				append(DBTableName.HISTORY).append(".").append(DBTableColName.HISTORY.USER_ID).
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
	
}
