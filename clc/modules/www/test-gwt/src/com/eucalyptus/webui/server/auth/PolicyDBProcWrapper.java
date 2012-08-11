package com.eucalyptus.webui.server.auth;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.eucalyptus.webui.server.db.DBProcWrapper;
import com.eucalyptus.webui.server.dictionary.DBTableColName;
import com.eucalyptus.webui.server.dictionary.DBTableName;
import com.eucalyptus.webui.shared.auth.Policy;

public class PolicyDBProcWrapper {

	public void addPolicy(Policy pol) throws PolicySyncException {
		DBProcWrapper dbProc = DBProcWrapper.Instance();
		String sql = addPolicySQL(pol);

		try {
			dbProc.update(sql);
		} catch (SQLException e) {
			throw new PolicySyncException("Database fails");
		}
	}

	public void deletePolicy(int policy_id) throws PolicySyncException {
		DBProcWrapper dbProc = DBProcWrapper.Instance();
		String sql = deletePolicySQL(policy_id);

		try {
			dbProc.update(sql);
		} catch (SQLException e) {
			throw new PolicySyncException("Database fails");
		}
	}

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
										.getString(DBTableColName.POLICY.ID)),
								res.getString(DBTableColName.POLICY.NAME),
								res.getString(DBTableColName.POLICY.VERSION),
								res.getString(DBTableColName.POLICY.TEXT),
								res.getString(DBTableColName.POLICY.ACCOUNT_ID),
								res.getString(DBTableColName.POLICY.GROUP_ID),
								res.getString(DBTableColName.POLICY.USER_ID));
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
										.getString(DBTableColName.POLICY.ID)),
								res.getString(DBTableColName.POLICY.NAME),
								res.getString(DBTableColName.POLICY.VERSION),
								res.getString(DBTableColName.POLICY.TEXT),
								res.getString(DBTableColName.POLICY.ACCOUNT_ID),
								res.getString(DBTableColName.POLICY.GROUP_ID),
								res.getString(DBTableColName.POLICY.USER_ID));
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
		str.append("INSERT INTO ").append(DBTableName.POLICY).append(" ( ")
				.append(DBTableColName.POLICY.ID).append(", ")
				.append(DBTableColName.POLICY.NAME).append(", ")
				.append(DBTableColName.POLICY.VERSION).append(", ")
				.append(DBTableColName.POLICY.TEXT).append(", ")
				.append(DBTableColName.POLICY.ACCOUNT_ID).append(", ")
				.append(DBTableColName.POLICY.GROUP_ID).append(", ")
				.append(DBTableColName.POLICY.USER_ID)
				.append(") VALUES (null, ");

		str.append("'");
		str.append(pol.getName());
		str.append("', '");

		str.append(pol.getVersion());
		str.append("', '");

		str.append(pol.getText());
		str.append("', '");

		str.append(pol.getAccountId());
		str.append("', '");

		str.append(pol.getGroupId());
		str.append("', '");

		str.append(pol.getUserId());
		str.append("')");

		return str.toString();
	}

	private String deletePolicySQL(int policy_id) {
		StringBuilder sb = new StringBuilder();
		sb.append("DELETE ");
		sb.append(" FROM ").append(DBTableName.POLICY);
		sb.append(" WHERE ");
		sb.append(DBTableColName.POLICY.ID).append(" = ").append(policy_id);
		return sb.toString();
	}

	private String getPoliciesSQL(String userId) {
		StringBuilder sql = new StringBuilder().append("SELECT * FROM ")
				.append(DBTableName.POLICY).append(" WHERE ")
				.append(DBTableColName.POLICY.USER_ID).append(" = ")
				.append(userId);
		return sql.toString();
	}

	private String listPoliciesSQL() {
		StringBuilder sql = new StringBuilder().append("SELECT * FROM ")
				.append(DBTableName.POLICY).append(" WHERE 1 = 1 ");
		return sql.toString();
	}

}
