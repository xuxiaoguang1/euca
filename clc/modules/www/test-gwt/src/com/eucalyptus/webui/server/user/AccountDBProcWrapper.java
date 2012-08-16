package com.eucalyptus.webui.server.user;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.eucalyptus.webui.server.db.DBProcWrapper;
import com.eucalyptus.webui.server.db.ResultSetWrapper;
import com.eucalyptus.webui.server.dictionary.DBTableColName;
import com.eucalyptus.webui.server.dictionary.DBTableName;
import com.eucalyptus.webui.shared.user.AccountInfo;
import com.eucalyptus.webui.shared.user.EnumState;

public class AccountDBProcWrapper {
	public int addAccount(String name, String email, String description, EnumState state) throws AccountSyncException {
		DBProcWrapper dbProc = DBProcWrapper.Instance();
		String sql = addAccountSql(name, email, description, state);
		
		try {
			return dbProc.insertAndGetInsertId(sql);
		} catch (SQLException e) {
			throw new AccountSyncException ("Database fails");
		}
	}
	
	public void modifyAccount(int id, String name, String email)  throws AccountSyncException {
		if (id == 0)
			return;
		
		String sql = modifyAccountSql(id, name, email);
		
		DBProcWrapper dbProc = DBProcWrapper.Instance();
		
		try {
			dbProc.update(sql);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new AccountSyncException("Database fails");
		}
	}
	
	public void delAccounts(ArrayList<String> ids) throws AccountSyncException {
		if (ids == null || ids.size() == 0)
			return;
		
		String sql = delAccountsSql(ids);
		
		DBProcWrapper dbProc = DBProcWrapper.Instance();
		
		try {
			dbProc.update(sql);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new AccountSyncException("Database fails");
		}
	}
	
	public ResultSetWrapper queryTotalAccounts() throws AccountSyncException {
		DBProcWrapper dbProc = DBProcWrapper.Instance();
		
		StringBuilder sql = queryAccountSql();
		
		System.out.println(sql.toString());
		
		try {
			ResultSetWrapper result = dbProc.query(sql.toString());
			
			return result;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new AccountSyncException("Database fails");
		}
	}
	
	public AccountInfo queryAccountBy(int accountId) throws AccountSyncException {
		DBProcWrapper dbProc = DBProcWrapper.Instance();
		
		StringBuilder sql = queryAccountSql();
		
		sql.append(" AND ").
		append(DBTableName.ACCOUNT).append(".").append(DBTableColName.ACCOUNT.ID).
		append(" = ").append(accountId);
		
		System.out.println(sql.toString());
		
		try {
			ResultSetWrapper result = dbProc.query(sql.toString());
			
			ResultSet rs = result.getResultSet();
			
			if (rs != null) {
				rs.last();
				
				if (rs.getRow() <= 0) {
					result.close();
					return null;
				}
				else {
					EnumState state = EnumState.NONE;
					
					try {
						state = EnumState.values()[Integer.valueOf(rs.getString(DBTableColName.ACCOUNT.STATE))];
					} catch (Exception e) {
						result.close();
						e.printStackTrace();
						throw new AccountSyncException ("Database fails");
					}
					
					String name = rs.getString(DBTableColName.ACCOUNT.NAME);
					String email = rs.getString(DBTableColName.ACCOUNT.EMAIL);
					String des = rs.getString(DBTableColName.ACCOUNT.DES);
					
					AccountInfo account = new AccountInfo(accountId, name, email, des, state);
					result.close();
					return account;
				}
			}
			else {
				result.close();
				return null;
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new AccountSyncException("Database fails");
		}
	}
	
	public void updateAccountState(ArrayList<String> ids, EnumState state) throws AccountSyncException {
		DBProcWrapper dbProc = DBProcWrapper.Instance();
		
		String sql = updateAccountStateSql(ids, state);
		
		try {
			dbProc.update(sql);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new AccountSyncException("Database fails");
		}
	}
			
	private StringBuilder queryAccountSql() {
		StringBuilder sql = new StringBuilder("SELECT * FROM ").
				append(DBTableName.ACCOUNT).
				append(" WHERE 1=1 ");
		
		return sql;
	}
	
	
	private String addAccountSql(String name, String email, String description, EnumState state) {
		StringBuilder str = new StringBuilder();
		str.append("INSERT INTO ").
		append(DBTableName.ACCOUNT).append(" ( ").
		append(DBTableColName.ACCOUNT.ID).append(", ").
		append(DBTableColName.ACCOUNT.NAME).append(", ").
		append(DBTableColName.ACCOUNT.EMAIL).append(", ").
		append(DBTableColName.ACCOUNT.DES).append(", ").
		append(DBTableColName.ACCOUNT.STATE).
		append(" ) VALUES (null, ");
		
		str.append("'");
		str.append(name);
		str.append("', '");
		str.append(email);
		str.append("', '");
		str.append(description);
		str.append("', ");
		str.append(state.ordinal());
		str.append(")");
		
		return str.toString();
	}
	
	private String modifyAccountSql(int id, String name, String email) {
		StringBuilder str = new StringBuilder("UPDATE ").append(DBTableName.ACCOUNT).append(" SET ");
		
		str.append(DBTableColName.ACCOUNT.NAME).append(" = '").
		append(name).
		append("', ").
		
		append(DBTableColName.ACCOUNT.EMAIL).append(" = '").
		append(email).
		append("' ").
		
		append("WHERE ").append(DBTableColName.ACCOUNT.ID).append(" = ").
		append(id);
		
		return str.toString();
	}
	
	private String delAccountsSql(ArrayList<String> ids) {
		StringBuilder sql = new StringBuilder();
		sql.append("DELETE FROM ").
		append(DBTableName.ACCOUNT).
		append(" WHERE ");
		
		for (String str : ids) {
			sql.append(" ").append(DBTableColName.ACCOUNT.ID).
			append(" = '").
			append(str).
			append("' OR ");
		}
		
		sql.delete(sql.length() -3 , sql.length());
		
		System.out.println(sql);
		
		return sql.toString();
	}
	
	private String updateAccountStateSql(ArrayList<String> ids, EnumState state) {
		StringBuilder sql = new StringBuilder("UPDATE ").append(DBTableName.ACCOUNT).append(" SET ");
		
		sql.append(DBTableColName.ACCOUNT.STATE).append(" = ").
		append(state.ordinal()).
		append(" WHERE ");
		
		for (String str : ids) {
			sql.append(" ").append(DBTableColName.ACCOUNT.ID).
			append(" = '").
			append(str).
			append("' OR ");
		}
		
		sql.delete(sql.length() -3 , sql.length());
		
		System.out.println(sql);
		
		return sql.toString();
	}
}
