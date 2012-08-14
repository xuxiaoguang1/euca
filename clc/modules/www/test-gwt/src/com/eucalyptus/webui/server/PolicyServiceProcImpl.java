package com.eucalyptus.webui.server;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.eucalyptus.webui.client.service.EucalyptusServiceException;
import com.eucalyptus.webui.client.service.SearchRange;
import com.eucalyptus.webui.client.service.SearchResult;
import com.eucalyptus.webui.client.service.SearchResultFieldDesc;
import com.eucalyptus.webui.client.service.SearchResultRow;
import com.eucalyptus.webui.client.service.SearchResultFieldDesc.TableDisplay;
import com.eucalyptus.webui.client.service.SearchResultFieldDesc.Type;
import com.eucalyptus.webui.client.session.Session;
import com.eucalyptus.webui.server.auth.AccessKeyDBProcWrapper;
import com.eucalyptus.webui.server.auth.AccessKeySyncException;
import com.eucalyptus.webui.server.auth.CertificateDBProcWrapper;
import com.eucalyptus.webui.server.auth.CertificateSyncException;
import com.eucalyptus.webui.server.auth.PolicyDBProcWrapper;
import com.eucalyptus.webui.server.auth.PolicySyncException;
import com.eucalyptus.webui.server.auth.util.B64;
import com.eucalyptus.webui.server.auth.util.X509CertHelper;
import com.eucalyptus.webui.server.db.ResultSetWrapper;
import com.eucalyptus.webui.server.dictionary.DBTableColName;
import com.eucalyptus.webui.server.dictionary.Enum2String;
import com.eucalyptus.webui.server.user.LoginUserProfileStorer;
import com.eucalyptus.webui.server.user.UserSyncException;
import com.eucalyptus.webui.shared.auth.AccessKey;
import com.eucalyptus.webui.shared.auth.Certificate;
import com.eucalyptus.webui.shared.auth.Policy;
import com.eucalyptus.webui.shared.user.LoginUserProfile;
import com.google.common.base.Strings;

public class PolicyServiceProcImpl {
	
	private PolicyDBProcWrapper policyDBProc;

	public PolicyServiceProcImpl() {
		policyDBProc = new PolicyDBProcWrapper();
	}


	private SearchResult getSearchResult(boolean isRootAdmin, ResultSetWrapper rs, SearchRange range) {
		assert (range != null);

		List<SearchResultFieldDesc> FIELDS;

		if (isRootAdmin) {
			FIELDS = FIELDS_ROOT;
		} else {
			FIELDS = FIELDS_NONROOT;
		}

		final int sortField = range.getSortField();

		DATA = resultSet2List(isRootAdmin, rs);

		int resultLength = Math.min(range.getLength(),
				DATA.size() - range.getStart());
		SearchResult result = new SearchResult(DATA.size(), range);
		result.setDescs(FIELDS);
		result.setRows(DATA.subList(range.getStart(), range.getStart()
				+ resultLength));

		for (SearchResultRow row : result.getRows()) {
			System.out.println("Row: " + row);
		}

		return result;
	}

	private List<SearchResultRow> resultSet2List(boolean isRootView,
			ResultSetWrapper rsWrapper) {
		ResultSet rs = rsWrapper.getResultSet();
		int index = 1;
		List<SearchResultRow> result = null;
		try {
			if (rs != null) {
				result = new ArrayList<SearchResultRow>();

				while (rs.next()) {
					if (isRootView) {
						result.add(new SearchResultRow(
								Arrays.asList(
										rs.getString(DBTableColName.USER_POLICY.ID),
										Integer.toString(index++),
										rs.getString(DBTableColName.USER_POLICY.NAME),
										rs.getString(DBTableColName.USER_POLICY.VERSION),
										rs.getString(DBTableColName.USER_POLICY.ACCOUNT_ID),
										rs.getString(DBTableColName.USER_POLICY.GROUP_ID),
										rs.getString(DBTableColName.USER_POLICY.USER_ID),
										rs.getString(DBTableColName.USER_POLICY.TEXT))));
					} else {
						result.add(new SearchResultRow(
								Arrays.asList(
										rs.getString(DBTableColName.USER_POLICY.ID),
										Integer.toString(index++),
										rs.getString(DBTableColName.USER_POLICY.NAME),
										rs.getString(DBTableColName.USER_POLICY.VERSION),
										rs.getString(DBTableColName.USER_POLICY.ACCOUNT_ID),
										rs.getString(DBTableColName.USER_POLICY.GROUP_ID),
										rs.getString(DBTableColName.USER_POLICY.USER_ID),
										rs.getString(DBTableColName.USER_POLICY.TEXT))));
					}
				}
			}
			rsWrapper.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return result;
	}


	public void addAccountPolicy(String accountId, String name, String text) throws EucalyptusServiceException {
		if (accountId == null) {
			throw new EucalyptusServiceException("Account id cannot be NULL");
		}

		Policy pol = new Policy();
		pol.setAccountId(accountId);

		if (name == null) {
			throw new EucalyptusServiceException("Policy name cannot be NULL");
		}else{
			pol.setName(name);
		}

		if (text == null) {
			throw new EucalyptusServiceException("Policy text cannot be NULL");
		}else{
			pol.setText(text);
		}
		addPolicy(pol);
	}

	public void addGroupPolicy(String groupId, String name, String text) throws EucalyptusServiceException {
		if (groupId == null) {
			throw new EucalyptusServiceException("Group id cannot be NULL");
		}

		Policy pol = new Policy();
		pol.setGroupId(groupId);
		
		if (name == null) {
			throw new EucalyptusServiceException("Policy name cannot be NULL");
		}else{
			pol.setName(name);
		}

		if (text == null) {
			throw new EucalyptusServiceException("Policy text cannot be NULL");
		}else{
			pol.setText(text);
		}
		addPolicy(pol);
	}

	public void addUserPolicy(String userId, String name,String text) throws EucalyptusServiceException {
		if (userId == null) {
			throw new EucalyptusServiceException("User id cannot be NULL");
		}

		Policy pol = new Policy();
		pol.setUserId(userId);
		if (name == null) {
			throw new EucalyptusServiceException("Policy name cannot be NULL");
		}else{
			pol.setName(name);
		}

		if (text == null) {
			throw new EucalyptusServiceException("Policy text cannot be NULL");
		}else{
			pol.setText(text);
		}
		addPolicy(pol);
	}

	private void addPolicy(Policy pol) throws EucalyptusServiceException {
		try {
			policyDBProc.addPolicy(pol);
		} catch (PolicySyncException e) {
			e.printStackTrace();
			throw new EucalyptusServiceException("Failed to create policy");
		}
	}
	
	public void modifyPolicy(String id, String name, String content) throws EucalyptusServiceException {
		try {
			policyDBProc.modifyPolicy(id, name, content);
		} catch (PolicySyncException e) {
			e.printStackTrace();
			throw new EucalyptusServiceException("Failed to modify policy");
		}
	}

	public void deletePolicy(ArrayList<String> ids) throws EucalyptusServiceException {
		if (ids == null || ids.size() == 0) {
			throw new EucalyptusServiceException(
					"SearchResultRow cannot be NULL");
		}

		try {
			policyDBProc.deletePolicy(ids);
		} catch (PolicySyncException e) {
			e.printStackTrace();
			throw new EucalyptusServiceException("Failed to delete policy");
		}
	}

	@Deprecated
	public SearchResult listPolicies() throws EucalyptusServiceException {
		try {
			List<Policy> list = policyDBProc.listPolicies();
			SearchResult res = new SearchResult();
			for (Policy pol : list) {
				List<String> row = new ArrayList<String>();
				row.add(Integer.toString(pol.getId()));
				row.add(pol.getName());
				row.add(pol.getVersion());
				row.add(pol.getText());
				row.add(pol.getAccountId());
				row.add(pol.getGroupId());
				row.add(pol.getUserId());
				res.addRow(new SearchResultRow(row));
			}
			return res;
		} catch (PolicySyncException e) {
			e.printStackTrace();
			throw new EucalyptusServiceException("Failed to list policy");
		}
	}

	private static List<SearchResultRow> DATA = null;

	private static final String[] TABLE_COL_TITLE_CHECKALL = { "Check All", "全选" };
	private static final String[] TABLE_COL_TITLE_NO = { "No.", "序号" };
	private static final String[] TABLE_COL_TITLE_POLICY_NAME = { "Name", "名称" };
	private static final String[] TABLE_COL_TITLE_POLICY_VERSION = { "Version", "版本"};
	private static final String[] TABLE_COL_TITLE_ACCOUNT_ID = {"Account", "账户" };
	private static final String[] TABLE_COL_TITLE_GROUP_ID = { "Group", "组" };
	private static final String[] TABLE_COL_TITLE_USER_ID = {"User", "用户"};

	private static final List<SearchResultFieldDesc> FIELDS_ROOT = Arrays
			.asList(new SearchResultFieldDesc(TABLE_COL_TITLE_CHECKALL[1], "10%", false), 
					new SearchResultFieldDesc(TABLE_COL_TITLE_NO[1], false, "10%",TableDisplay.MANDATORY, Type.TEXT, false, false),
					new SearchResultFieldDesc(TABLE_COL_TITLE_POLICY_NAME[1], true, "15%", TableDisplay.MANDATORY, Type.TEXT, false, false), 
					new SearchResultFieldDesc(TABLE_COL_TITLE_POLICY_VERSION[1], true, "20%", TableDisplay.MANDATORY, Type.TEXT, false, false), 
					new SearchResultFieldDesc(TABLE_COL_TITLE_ACCOUNT_ID[1], true, "15%", TableDisplay.MANDATORY, Type.TEXT, false, false),
					new SearchResultFieldDesc(TABLE_COL_TITLE_GROUP_ID[1], true, "15%", TableDisplay.MANDATORY, Type.TEXT, false, false), 
					new SearchResultFieldDesc(TABLE_COL_TITLE_USER_ID[1], true, "15%", TableDisplay.MANDATORY, Type.TEXT, false, false));

	private static final List<SearchResultFieldDesc> FIELDS_NONROOT = Arrays
			.asList(new SearchResultFieldDesc(TABLE_COL_TITLE_CHECKALL[1], "10%", false), 
					new SearchResultFieldDesc(TABLE_COL_TITLE_NO[1], false, "10%",TableDisplay.MANDATORY, Type.TEXT, false, false),
					new SearchResultFieldDesc(TABLE_COL_TITLE_POLICY_NAME[1], true, "15%", TableDisplay.MANDATORY, Type.TEXT, false, false), 
					new SearchResultFieldDesc(TABLE_COL_TITLE_POLICY_VERSION[1], true, "20%", TableDisplay.MANDATORY, Type.TEXT, false, false), 
					new SearchResultFieldDesc(TABLE_COL_TITLE_ACCOUNT_ID[1], true, "15%", TableDisplay.MANDATORY, Type.TEXT, false, false),
					new SearchResultFieldDesc(TABLE_COL_TITLE_GROUP_ID[1], true, "15%", TableDisplay.MANDATORY, Type.TEXT, false, false), 
					new SearchResultFieldDesc(TABLE_COL_TITLE_USER_ID[1], true, "15%", TableDisplay.MANDATORY, Type.TEXT, false, false));

	
	public SearchResult lookupPolicy(LoginUserProfile curUser, String search, SearchRange range) throws EucalyptusServiceException {
		boolean isRootAdmin = curUser.isSystemAdmin();

		ResultSetWrapper rs;
		try {
			if (isRootAdmin) {
				rs = policyDBProc.queryTotalPolicies();
			} else {
				rs = policyDBProc.queryPoliciesBy(curUser.getAccountId(), curUser.getUserId(), curUser.getUserType());
			}
		} catch (CertificateSyncException e) {
			e.printStackTrace();
			throw new EucalyptusServiceException("Fail to query certificates");
		}
		if (rs == null)
			return null;

		return getSearchResult(isRootAdmin, rs, range);
	}

}
