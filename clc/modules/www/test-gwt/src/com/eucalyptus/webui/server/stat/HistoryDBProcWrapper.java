package com.eucalyptus.webui.server.stat;

import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import com.eucalyptus.webui.server.db.DBProcWrapper;
import com.eucalyptus.webui.server.db.ResultSetWrapper;
import com.eucalyptus.webui.shared.dictionary.DBTableColName;
import com.eucalyptus.webui.shared.dictionary.DBTableName;
import com.eucalyptus.webui.shared.user.EnumUserType;

public class HistoryDBProcWrapper {
	
	public void addHistoryActon(HistoryAction action) throws HistorySyncException {
		DBProcWrapper dbProc = DBProcWrapper.Instance();
		StringBuilder sql = addHistoryAction(action);

		try {
			dbProc.update(sql.toString());
		} catch (SQLException e) {
			throw new HistorySyncException("Database fails");
		}
	}
	
	public ResultSetWrapper queryTotalHistory() throws HistorySyncException{
		DBProcWrapper dbProc = DBProcWrapper.Instance();
		
		StringBuilder sql = historyAccountGroupViewSql();
		
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
	
	private StringBuilder addHistoryAction(HistoryAction action){
		StringBuilder sql = new StringBuilder();
		sql.append("INSERT INTO ").append(DBTableName.HISTORY).append(" ( ")
				.append(DBTableColName.HISTORY.ID).append(", ")
				.append(DBTableColName.HISTORY.ACTION).append(", ")
				.append(DBTableColName.HISTORY.REASON).append(", ")
				.append(DBTableColName.HISTORY.DATE).append(", ")
				.append(DBTableColName.HISTORY.USER_ID).append(", ")
				.append(DBTableColName.HISTORY.VM_ID).append(") VALUES (null, ");

		sql.append("'");
		sql.append(action.getAction());
		sql.append("', '");

		sql.append(action.getReason());
		sql.append("', '");

		DateFormat df = new SimpleDateFormat(HistoryAction.DATE_PATTERN);
		sql.append(df.format(action.getDate()));
		sql.append("', '");
		
		sql.append(action.getUserID());
		sql.append("', '");


		sql.append(action.getVmID());
		sql.append("')");

		return sql;
	}
	
	private StringBuilder historyAccountGroupViewSql() {
		StringBuilder sql = new StringBuilder("SELECT * ").
				append(" FROM ").
				append("( ( (").
				append(DBTableName.HISTORY).
				append(" LEFT JOIN ").
				append(DBTableName.USER).
				append(" ON ").
				append(DBTableName.HISTORY).append(".").append(DBTableColName.HISTORY.USER_ID).
				append(" = ").
				append(DBTableName.USER).append(".").append(DBTableColName.USER.ID).
				append(" ) ").
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
				append(" ) ").
				append(" LEFT JOIN ").
				append(DBTableName.VM).
				append(" ON ").
				append(DBTableName.HISTORY).append(".").append(DBTableColName.HISTORY.VM_ID).
				append(" = ").
				append(DBTableName.VM).append(".").append(DBTableColName.VM.ID).
				append(" WHERE 1=1 ");
		
		return sql;
	}
	
}
