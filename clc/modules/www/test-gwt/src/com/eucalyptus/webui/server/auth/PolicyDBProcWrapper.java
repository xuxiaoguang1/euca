package com.eucalyptus.webui.server.auth;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.eucalyptus.webui.server.db.DBProcWrapper;
import com.eucalyptus.webui.server.db.ResultSetWrapper;
import com.eucalyptus.webui.shared.auth.Policy;
import com.eucalyptus.webui.shared.dictionary.DBTableColName;
import com.eucalyptus.webui.shared.dictionary.DBTableName;
import com.eucalyptus.webui.shared.user.EnumUserType;

public class PolicyDBProcWrapper {

	public void addPolicy(Policy pol) throws PolicySyncException {
		DBProcWrapper dbProc = DBProcWrapper.Instance();
		String sql = addPolicySQL(pol);
		
		//for debug
		System.out.println(sql);

		try {
			dbProc.update(sql);
		} catch (SQLException e) {
			throw new PolicySyncException("Database fails: " + e.getMessage());
		}
	}

	public void deletePolicy(ArrayList<String> ids) throws PolicySyncException {
		DBProcWrapper dbProc = DBProcWrapper.Instance();
		String sql = deletePolicySQL(ids);

		try {
			dbProc.update(sql);
		} catch (SQLException e) {
			throw new PolicySyncException("Database fails");
		}
	}
	
	public void modifyPolicy(String id, String name, String content) throws PolicySyncException {
		DBProcWrapper dbProc = DBProcWrapper.Instance();
		String sql = modifyPolicySQL(id, name, content);

		try {
			dbProc.update(sql);
		} catch (SQLException e) {
			throw new PolicySyncException("Database fails");
		}
	}
	
	private String modifyPolicySQL(String id, String name, String content){
		StringBuilder sql = new StringBuilder();
		sql.append("UPDATE ").append(DBTableName.USER_POLICY).
			append(" SET ").
			append(DBTableColName.USER_POLICY.NAME).append(" = '").append(name).append("' , ").
			append(DBTableColName.USER_POLICY.TEXT).append(" = '").append(content).append("' ").
			append(" WHERE ").
			append(DBTableColName.USER_POLICY.ID).append(" = '").append(id).append("'");
		
		return sql.toString();
	}

	@Deprecated
	public List<Policy> getPolicies(String userId) throws PolicySyncException {
		DBProcWrapper dbProc = DBProcWrapper.Instance();
		String sql = getPoliciesSQL(userId);

		ResultSet res = null;

		try {
			res = dbProc.query(sql).getResultSet();
		} catch (SQLException e) {
			throw new PolicySyncException("Database fails");
		}

		List<Policy> list = new ArrayList<Policy>();

		if (res != null) {
			list = new ArrayList<Policy>();
			try {
				while (res.next()) {
					Policy pol = null;
					try {
						pol = new Policy(
								Integer.valueOf(res
										.getString(DBTableColName.USER_POLICY.ID)),
								res.getString(DBTableColName.USER_POLICY.NAME),
								res.getString(DBTableColName.USER_POLICY.VERSION),
								res.getString(DBTableColName.USER_POLICY.TEXT),
								res.getString(DBTableColName.USER_POLICY.ACCOUNT_ID),
								res.getString(DBTableColName.USER_POLICY.GROUP_ID),
								res.getString(DBTableColName.USER_POLICY.USER_ID));
					} catch (NumberFormatException e) {
						e.printStackTrace();
					}
					if (pol != null) {
						list.add(pol);
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return list;
	}

	@Deprecated
	public List<Policy> listPolicies() throws PolicySyncException {
		DBProcWrapper dbProc = DBProcWrapper.Instance();
		String sql = listPoliciesSQL();

		ResultSet res = null;

		try {
			res = dbProc.query(sql).getResultSet();
		} catch (SQLException e) {
			throw new PolicySyncException("Database fails");
		}

		List<Policy> list = new ArrayList<Policy>();

		if (res != null) {
			list = new ArrayList<Policy>();
			try {
				while (res.next()) {
					Policy pol = null;
					try {
						pol = new Policy(
								Integer.valueOf(res
										.getString(DBTableColName.USER_POLICY.ID)),
								res.getString(DBTableColName.USER_POLICY.NAME),
								res.getString(DBTableColName.USER_POLICY.VERSION),
								res.getString(DBTableColName.USER_POLICY.TEXT),
								res.getString(DBTableColName.USER_POLICY.ACCOUNT_ID),
								res.getString(DBTableColName.USER_POLICY.GROUP_ID),
								res.getString(DBTableColName.USER_POLICY.USER_ID));
					} catch (NumberFormatException e) {
						e.printStackTrace();
					}
					if (pol != null) {
						list.add(pol);
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return list;
	}

	private String addPolicySQL(Policy pol) {
		StringBuilder str = new StringBuilder();
		str.append("INSERT INTO ").append(DBTableName.USER_POLICY).append(" ( ")
				.append(DBTableColName.USER_POLICY.ID).append(", ")
				.append(DBTableColName.USER_POLICY.NAME).append(", ")
				.append(DBTableColName.USER_POLICY.VERSION).append(", ")
				.append(DBTableColName.USER_POLICY.TEXT).append(", ")
				.append(DBTableColName.USER_POLICY.ACCOUNT_ID).append(", ")
				.append(DBTableColName.USER_POLICY.GROUP_ID).append(", ")
				.append(DBTableColName.USER_POLICY.USER_ID)
				.append(") VALUES (null, ");

		str.append("'");
		str.append(pol.getName());
		str.append("', '");

		str.append(pol.getVersion());
		str.append("', '");

		str.append(pol.getText());
		str.append("', ");

		if(pol.getAccountId() == null){
			str.append(" null , ");
		}else{
			str.append(" '" + pol.getAccountId() + "' , ");
		}
		
		if(pol.getGroupId() == null){
			str.append(" null , ");
		}else{
			str.append(" '" + pol.getGroupId() + "' , ");
		}
		
		if(pol.getUserId() == null){
			str.append(" null ) ");
		}else{
			str.append(" '" + pol.getUserId() + "' ) ");
		}

		return str.toString();
	}

	private String deletePolicySQL(ArrayList<String> ids) {
		StringBuilder sb = new StringBuilder();
		sb.append("DELETE ");
		sb.append(" FROM ").append(DBTableName.USER_POLICY);
		sb.append(" WHERE ");
		for(String id : ids){
			sb.append(DBTableColName.USER_POLICY.ID).append(" = ").append(id).append(" OR ");
		}
		
		sb.delete(sb.length()-3, sb.length());
		
		return sb.toString();
	}

	private String getPoliciesSQL(String userId) {
		StringBuilder sql = new StringBuilder().append("SELECT * FROM ")
				.append(DBTableName.USER_POLICY).append(" WHERE ")
				.append(DBTableColName.USER_POLICY.USER_ID).append(" = ")
				.append(userId);
		return sql.toString();
	}

	private String listPoliciesSQL() {
		StringBuilder sql = new StringBuilder().append("SELECT * FROM ")
				.append(DBTableName.USER_POLICY).append(" WHERE 1 = 1 ");
		return sql.toString();
	}
	
	
	public ResultSetWrapper queryTotalPolicies() throws CertificateSyncException {
		DBProcWrapper dbProc = DBProcWrapper.Instance();
		
		StringBuilder sql = plicyAccountGroupViewSql();
		
		System.out.println(sql.toString());
		
		try {
			ResultSetWrapper result = dbProc.query(sql.toString());
			
			return result;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new CertificateSyncException("Fail to Query policy");
		}
	}
	
	private StringBuilder plicyAccountGroupViewSql() {
		StringBuilder sql = new StringBuilder("SELECT * ").
				append(" FROM ").
				append("( (").
				append(DBTableName.USER_POLICY).
				append(" LEFT JOIN ").append(DBTableName.USER).
				append(" ON ").
				append(DBTableName.USER_POLICY).append(".").append(DBTableColName.USER_POLICY.USER_ID).
				append(" = ").
				append(DBTableName.USER).append(".").append(DBTableColName.USER.ID).
				append(") ").
				append(" LEFT JOIN ").append(DBTableName.GROUP).
				append(" ON ").
				append(DBTableName.USER_POLICY).append(".").append(DBTableColName.USER_POLICY.GROUP_ID).
				append(" = ").
				append(DBTableName.GROUP).append(".").append(DBTableColName.GROUP.ID).
				append(" ) ").
				append(" LEFT JOIN ").append(DBTableName.ACCOUNT).
				append(" ON ").
				append(DBTableName.USER_POLICY).append(".").append(DBTableColName.USER_POLICY.ACCOUNT_ID).
				append(" = ").
				append(DBTableName.ACCOUNT).append(".").append(DBTableColName.ACCOUNT.ID).
				append(" WHERE 1=1 ");
		
		return sql;
	}
	
	public ResultSetWrapper queryPoliciesBy(int accountId, int userId, EnumUserType userType)  throws CertificateSyncException {
		DBProcWrapper dbProc = DBProcWrapper.Instance();
		
		StringBuilder sql = plicyAccountGroupViewSql();
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
			System.out.println(sql.toString());
			ResultSetWrapper result = dbProc.query(sql.toString());
			
			return result;
		} catch (SQLException e) {
			e.printStackTrace();
			throw new CertificateSyncException("Fail to Query policy");
		}
	}
	
	
}
