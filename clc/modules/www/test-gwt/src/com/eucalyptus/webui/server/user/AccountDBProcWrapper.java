package com.eucalyptus.webui.server.user;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.eucalyptus.webui.client.service.SearchRange;
import com.eucalyptus.webui.server.SorterProxy;
import com.eucalyptus.webui.server.db.DBProcWrapper;
import com.eucalyptus.webui.server.db.ResultSetWrapper;
import com.eucalyptus.webui.server.dictionary.DBTableColName;
import com.eucalyptus.webui.server.dictionary.DBTableName;
import com.eucalyptus.webui.shared.dictionary.ConfDef;
import com.eucalyptus.webui.shared.user.AccountInfo;
import com.eucalyptus.webui.shared.user.EnumState;

public class AccountDBProcWrapper {
	
	public AccountDBProcWrapper(SorterProxy sorterProxy) {
		
	}
	public int addAccount(AccountInfo account) throws AccountSyncException {
		if (account == null)
			throw new AccountSyncException("Invalid account para for creating");
		
		DBProcWrapper dbProc = DBProcWrapper.Instance();
		String sql = addAccountSql(account);
		System.out.println(sql);
		
		try {
			return dbProc.insertAndGetInsertId(sql);
		} catch (SQLException e) {
			throw new AccountSyncException ("Database fails");
		}
	}
	
	public void modifyAccount(AccountInfo account)  throws AccountSyncException {
		if (account == null || account.getId() == 0)
			throw new AccountSyncException("Invalid account para for updating");
		
		String sql = modifyAccountSql(account);
		
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
	
	public ResultSetWrapper queryTotalAccounts(SearchRange range) throws AccountSyncException {
		DBProcWrapper dbProc = DBProcWrapper.Instance();
		
		StringBuilder sql = queryAccountSql();
		
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
				append(" WHERE ").
				append(DBTableColName.ACCOUNT.DEL).append(" = ").append(ConfDef.DB_DEL_FIELD_VALID_STATE).append(" ");
		
		return sql;
	}
	
	
	private String addAccountSql(AccountInfo account) {
		StringBuilder str = new StringBuilder();
		str.append("INSERT INTO ").
		append(DBTableName.ACCOUNT).append(" ( ").
		append(DBTableColName.ACCOUNT.ID).append(", ").
		append(DBTableColName.ACCOUNT.NAME).append(", ").
		append(DBTableColName.ACCOUNT.EMAIL).append(", ").
		append(DBTableColName.ACCOUNT.DES).append(", ").
		append(DBTableColName.ACCOUNT.STATE).append(", ").
		append(DBTableColName.ACCOUNT.DEL).
		append(" ) VALUES (null, ");
		
		str.append("'");
		str.append(account.getName());
		str.append("', '");
		str.append(account.getEmail());
		str.append("', '");
		str.append(account.getDescription());
		str.append("', ");
		str.append(account.getState().ordinal());
		str.append(", ");
		str.append(ConfDef.DB_DEL_FIELD_VALID_STATE);
		str.append(")");
		
		return str.toString();
	}
	
	private String modifyAccountSql(AccountInfo account) {
		StringBuilder str = new StringBuilder("UPDATE ").append(DBTableName.ACCOUNT).append(" SET ");
		
		str.append(DBTableColName.ACCOUNT.NAME).append(" = '").
		append(account.getName()).
		append("', ").
		
		append(DBTableColName.ACCOUNT.EMAIL).append(" = '").
		append(account.getEmail()).
		append("', ").
		
		append(DBTableColName.ACCOUNT.DES).append(" = '").
		append(account.getDescription()).
		append("', ").
		
		append(DBTableColName.ACCOUNT.STATE).append(" = ").
		append(account.getState().ordinal()).
		
		append(" WHERE ").append(DBTableColName.ACCOUNT.ID).append(" = ").
		append(account.getId());
		
		return str.toString();
	}
	
	private String delAccountsSql(ArrayList<String> ids) {
		StringBuilder sql = new StringBuilder();
		sql.append("UPDATE ").
		append("( (").
		append(DBTableName.ACCOUNT).
		append(" LEFT JOIN ").
		append(DBTableName.GROUP).
		append(" ON ").
		append(DBTableName.ACCOUNT).append(".").append(DBTableColName.ACCOUNT.ID).
		append(" = ").
		append(DBTableName.GROUP).append(".").append(DBTableColName.GROUP.ACCOUNT_ID).
		append(")").
		append(" LEFT JOIN ").
		append(DBTableName.USER).
		append(" ON ").
		append(DBTableName.ACCOUNT).append(".").append(DBTableColName.ACCOUNT.ID).
		append(" = ").
		append(DBTableName.USER).append(".").append(DBTableColName.USER.ACCOUNT_ID).
		append(" ) ").
		append(" SET ").
		append(DBTableName.ACCOUNT).append(".").append(DBTableColName.ACCOUNT.DEL).append(" = ").append(ConfDef.DB_DEL_FIELD_INVALID_STATE).
		append(", ").
		append(DBTableName.GROUP).append(".").append(DBTableColName.GROUP.DEL).append(" = ").append(ConfDef.DB_DEL_FIELD_INVALID_STATE).
		append(", ").
		append(DBTableName.USER).append(".").append(DBTableColName.USER.DEL).append(" = ").append(ConfDef.DB_DEL_FIELD_INVALID_STATE).
		append(" WHERE ");
		
		for (String str : ids) {
			sql.append(" ").append(DBTableName.ACCOUNT).append(".").append(DBTableColName.ACCOUNT.ID).
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
	
	SorterProxy sorterProxy;
}
