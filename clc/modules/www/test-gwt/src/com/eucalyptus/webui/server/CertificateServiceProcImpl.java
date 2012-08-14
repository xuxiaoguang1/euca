package com.eucalyptus.webui.server;

import java.security.cert.X509Certificate;
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

public class CertificateServiceProcImpl {

	private CertificateDBProcWrapper certDBProc;
	
	public CertificateServiceProcImpl() {
		certDBProc = new CertificateDBProcWrapper();
	}

	// public void addAccessKey(Session session, String userId)
	// throws EucalyptusServiceException {
	// checkPermission(session);
	//
	// if (userId == null) {
	// throw new EucalyptusServiceException("UserId cannot be NULL");
	// }
	//
	// AccessKey key = new AccessKey(userId);
	// key.setActive(true);
	//
	// try {
	// keyDBProc.addAccessKey(key);
	// } catch (AccessKeySyncException e) {
	// e.printStackTrace();
	// throw new EucalyptusServiceException("Failed to create access key");
	// }
	// }
	//
	// public void deleteAccessKey(Session session, SearchResultRow row)
	// throws EucalyptusServiceException {
	// checkPermission(session);
	//
	// if (row == null) {
	// throw new EucalyptusServiceException(
	// "SearchResultRow cannot be NULL");
	// }
	//
	// int key_id = Integer
	// .parseInt(row.getField(KeyColumnIndex.AccessKey_ID));
	//
	// try {
	// keyDBProc.deleteAccessKey(key_id);
	// } catch (AccessKeySyncException e) {
	// e.printStackTrace();
	// throw new EucalyptusServiceException("Failed to create access key");
	// }
	// }
	//
	// public SearchResult listAccesssKeyByUser(Session session, String userId)
	// throws EucalyptusServiceException {
	// if (userId == null) {
	// throw new EucalyptusServiceException("userId cannot be NULL");
	// }
	// try {
	// List<AccessKey> list = keyDBProc.getAccessKeys(userId);
	// SearchResult res = new SearchResult();
	// DateFormat df = new SimpleDateFormat(AccessKey.DATE_PATTERN);
	// for (AccessKey key : list) {
	// List<String> row = new ArrayList<String>();
	// row.add(Integer.toString(key.getId()));
	// row.add(key.getAccessKey());
	// row.add(key.getSecretKey());
	// row.add(Boolean.toString(key.isActive()));
	// row.add(df.format(key.getCreatedDate()));
	// row.add(key.getUserId());
	// res.addRow(new SearchResultRow(row));
	// }
	// return res;
	// } catch (AccessKeySyncException e) {
	// e.printStackTrace();
	// throw new EucalyptusServiceException("Failed to get access keys");
	// }
	// }
	//
	// public SearchResult listAccessKeys(Session session)
	// throws EucalyptusServiceException {
	// try {
	// List<AccessKey> list = keyDBProc.listAccessKeys();
	// SearchResult res = new SearchResult();
	// DateFormat df = new SimpleDateFormat(AccessKey.DATE_PATTERN);
	// for (AccessKey key : list) {
	// List<String> row = new ArrayList<String>();
	// row.add(Integer.toString(key.getId()));
	// row.add(key.getAccessKey());
	// row.add(key.getSecretKey());
	// row.add(Boolean.toString(key.isActive()));
	// row.add(df.format(key.getCreatedDate()));
	// row.add(key.getUserId());
	// res.addRow(new SearchResultRow(row));
	// }
	// return res;
	// } catch (AccessKeySyncException e) {
	// e.printStackTrace();
	// throw new EucalyptusServiceException("Failed to list access keys");
	// }
	// }

	public void addCertificate(String userId, String pem)
			throws EucalyptusServiceException {

		if (Strings.isNullOrEmpty(userId)) {
			throw new EucalyptusServiceException("empty userID");
		}

		if (Strings.isNullOrEmpty(pem)) {
			throw new EucalyptusServiceException("Empty Certificate");
		}
		
		//TODO:just test
		{
			Certificate cert = new Certificate(userId, B64.url.encString(pem));
			cert.setActive(true);
			cert.setRevoked(false);

			try {
				certDBProc.addCertificate(cert);
			} catch (CertificateSyncException e) {
				e.printStackTrace();
				throw new EucalyptusServiceException("Failed to create certificate");
			}
			
			if(true) return;
		}

		try {
			String encodedPem = B64.url.encString(pem);
			for (Certificate c : certDBProc.getCertificates(userId)) {
				if (c.getPem().equals(encodedPem)) {
					if (!c.isRevoked()) {
						throw new EucalyptusServiceException("Auth conflict");
					} else {
						ArrayList<String> ids = new ArrayList<String>();
						ids.add(Integer.toString(c.getId()));
						certDBProc.deleteCertificate(ids);
					}
				}
			}
			X509Certificate x509 = X509CertHelper.toCertificate(encodedPem);
			if (x509 == null) {
				throw new EucalyptusServiceException("Invalid cert");
			}

			Certificate cert = new Certificate(userId,
					X509CertHelper.fromCertificate(x509));
			cert.setActive(true);
			cert.setRevoked(false);

			certDBProc.addCertificate(cert);
		} catch (CertificateSyncException e) {
			e.printStackTrace();
			throw new EucalyptusServiceException("Failed to create certificate");
		}
	}

	public SearchResult lookupCertificate(LoginUserProfile curUser, String search, SearchRange range) throws EucalyptusServiceException {
		boolean isRootAdmin = curUser.isSystemAdmin();

		ResultSetWrapper rs;
		try {
			if (isRootAdmin) {
				rs = certDBProc.queryTotalCertificates();
			} else {
				rs = certDBProc.queryCertificatesBy(curUser.getAccountId(),
						curUser.getUserId(), curUser.getUserType());
			}
		} catch (CertificateSyncException e) {
			e.printStackTrace();
			throw new EucalyptusServiceException("Fail to query certificates");
		}
		if (rs == null)
			return null;

		return getSearchResult(isRootAdmin, rs, range);
	}
	
	public void deleteCertification(ArrayList<String> ids)
			throws EucalyptusServiceException {

		if (ids == null || ids.size() == 0) {
			throw new EucalyptusServiceException("cert id cannot be NULL");
		}

		try {
			certDBProc.deleteCertificate(ids);
		} catch (CertificateSyncException e) {
			e.printStackTrace();
			throw new EucalyptusServiceException("Failed to create access key");
		}
	}
	
	public void modifiCertificate(ArrayList<String> ids, Boolean active, Boolean revoked) throws EucalyptusServiceException{
		if (ids == null || ids.size() == 0) {
			throw new EucalyptusServiceException("cert id cannot be NULL");
		}

		try {
			certDBProc.modifiCertificate(ids, active, revoked);
		} catch (CertificateSyncException e) {
			e.printStackTrace();
			throw new EucalyptusServiceException("Failed to create access key");
		}
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
					boolean userKeyActive = Integer.parseInt(rs.getString(DBTableColName.USER_CERT.ACTIVE)) == 1;
					boolean userKeyRevoked = Integer.parseInt(rs.getString(DBTableColName.USER_CERT.REVOKED)) == 1;

					if (isRootView) {
						result.add(new SearchResultRow(
								Arrays.asList(
										rs.getString(DBTableColName.USER_CERT.ID),
										Integer.toString(index++),
										Enum2String.getInstance().getActiveState(userKeyActive),
										Enum2String.getInstance().getRevokedState(userKeyRevoked),
										rs.getString(DBTableColName.USER_CERT.CREATED_DATE),
										rs.getString(DBTableColName.ACCOUNT.NAME),
										rs.getString(DBTableColName.GROUP.NAME),
										rs.getString(DBTableColName.USER.NAME))));
					} else {
						result.add(new SearchResultRow(
								Arrays.asList(
										rs.getString(DBTableColName.USER_CERT.ID),
										Integer.toString(index++),
										Enum2String.getInstance().getActiveState(userKeyActive),
										Enum2String.getInstance().getRevokedState(userKeyRevoked),
										rs.getString(DBTableColName.USER_CERT.CREATED_DATE),
										rs.getString(DBTableColName.GROUP.NAME),
										rs.getString(DBTableColName.USER.NAME))));
					}
				}
			}
			rsWrapper.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return result;
	}

//	public SearchResult listCertificatesByUser(Session session, String userId)
//			throws EucalyptusServiceException {
//		if (userId == null) {
//			throw new EucalyptusServiceException("userId cannot be NULL");
//		}
//		try {
//			List<Certificate> list = certDBProc.getCertificates(userId);
//			SearchResult res = new SearchResult();
//			DateFormat df = new SimpleDateFormat(Certificate.DATE_PATTERN);
//			for (Certificate cert : list) {
//				List<String> row = new ArrayList<String>();
//				row.add(Integer.toString(cert.getId()));
//				row.add(cert.getCertificateId());
//				row.add(cert.getPem());
//				row.add(Boolean.toString(cert.isActive()));
//				row.add(Boolean.toString(cert.isRevoked()));
//				row.add(df.format(cert.getCreatedDate()));
//				row.add(cert.getUserId());
//				res.addRow(new SearchResultRow(row));
//			}
//			return res;
//		} catch (CertificateSyncException e) {
//			e.printStackTrace();
//			throw new EucalyptusServiceException("Failed to get certificate");
//		}
//	}

//	public SearchResult listCertificates(Session session)
//			throws EucalyptusServiceException {
//		try {
//			List<Certificate> list = certDBProc.listCertificates();
//			SearchResult res = new SearchResult();
//			DateFormat df = new SimpleDateFormat(Certificate.DATE_PATTERN);
//			for (Certificate cert : list) {
//				List<String> row = new ArrayList<String>();
//				row.add(Integer.toString(cert.getId()));
//				row.add(cert.getCertificateId());
//				row.add(cert.getPem());
//				row.add(Boolean.toString(cert.isActive()));
//				row.add(Boolean.toString(cert.isRevoked()));
//				row.add(df.format(cert.getCreatedDate()));
//				row.add(cert.getUserId());
//				res.addRow(new SearchResultRow(row));
//			}
//			return res;
//		} catch (CertificateSyncException e) {
//			e.printStackTrace();
//			throw new EucalyptusServiceException("Failed to list certificate");
//		}
//	}

//	public void addAccountPolicy(Session session, String accountId,
//			String name, String text) throws EucalyptusServiceException {
//		checkPermission(session);
//
//		if (accountId == null) {
//			throw new EucalyptusServiceException("Account id cannot be NULL");
//		}
//
//		Policy pol = new Policy();
//		pol.setAccountId(accountId);
//
//		addPolicy(pol, name, text);
//	}
//
//	public void addGroupPolicy(Session session, String groupId, String name,
//			String text) throws EucalyptusServiceException {
//		checkPermission(session);
//
//		if (groupId == null) {
//			throw new EucalyptusServiceException("Group id cannot be NULL");
//		}
//
//		Policy pol = new Policy();
//		pol.setGroupId(groupId);
//
//		addPolicy(pol, name, text);
//	}
//
//	public void addUserPolicy(Session session, String userId, String name,
//			String text) throws EucalyptusServiceException {
//		checkPermission(session);
//
//		if (userId == null) {
//			throw new EucalyptusServiceException("User id cannot be NULL");
//		}
//
//		Policy pol = new Policy();
//		pol.setUserId(userId);
//
//		addPolicy(pol, name, text);
//	}
//
//	private void checkPermission(Session session)
//			throws EucalyptusServiceException {
//		LoginUserProfile curUser = LoginUserProfileStorer.instance().get(
//				session.getId());
//
//		if (!curUser.isSystemAdmin() && !curUser.isAccountAdmin()) {
//			throw new EucalyptusServiceException("No permission");
//		}
//	}
//
//	private void addPolicy(Policy pol, String name, String text)
//			throws EucalyptusServiceException {
//		if (name == null) {
//			throw new EucalyptusServiceException("Policy name cannot be NULL");
//		}
//
//		if (text == null) {
//			throw new EucalyptusServiceException("Policy text cannot be NULL");
//		}
//
//		pol.setName(name);
//		pol.setText(text);
//
//		try {
//			policyDBProc.addPolicy(pol);
//		} catch (PolicySyncException e) {
//			e.printStackTrace();
//			throw new EucalyptusServiceException("Failed to create policy");
//		}
//	}
//
//	public void deletePolicy(Session session, SearchResultRow row)
//			throws EucalyptusServiceException {
//		checkPermission(session);
//
//		if (row == null) {
//			throw new EucalyptusServiceException(
//					"SearchResultRow cannot be NULL");
//		}
//
//		int policy_id = Integer.parseInt(row
//				.getField(PolicyColumnIndex.Policy_ID));
//
//		try {
//			policyDBProc.deletePolicy(policy_id);
//		} catch (PolicySyncException e) {
//			e.printStackTrace();
//			throw new EucalyptusServiceException("Failed to delete policy");
//		}
//	}
//
//	public SearchResult listPolicies(Session session)
//			throws EucalyptusServiceException {
//		try {
//			List<Policy> list = policyDBProc.listPolicies();
//			SearchResult res = new SearchResult();
//			for (Policy pol : list) {
//				List<String> row = new ArrayList<String>();
//				row.add(Integer.toString(pol.getId()));
//				row.add(pol.getName());
//				row.add(pol.getVersion());
//				row.add(pol.getText());
//				row.add(pol.getAccountId());
//				row.add(pol.getGroupId());
//				row.add(pol.getUserId());
//				res.addRow(new SearchResultRow(row));
//			}
//			return res;
//		} catch (PolicySyncException e) {
//			e.printStackTrace();
//			throw new EucalyptusServiceException("Failed to list policy");
//		}
//	}

//	public static class KeyColumnIndex {
//		public static final int AccessKey_ID = 0;
//		public static final int AccessKey_AKEY = 1;
//		public static final int AccessKey_SKEY = 2;
//		public static final int AccessKey_ACTIVE = 3;
//		public static final int AccessKey_DATE = 4;
//		public static final int AccessKey_USER_ID = 5;
//	}

//	public static class CertColumnIndex {
//		public static final int Certificate_ID = 0;
//		public static final int Certificate_CERT_ID = 1;
//		public static final int Certificate_PEM = 2;
//		public static final int Certificate_ACTIVE = 3;
//		public static final int Certificate_REVOKED = 4;
//		public static final int Certificate_DATE = 5;
//		public static final int Certificate_USER_ID = 6;
//	}

	// public static class PolicyColumnIndex {
	// public static final int Policy_ID = 0;
	// public static final int Policy_NAME = 1;
	// public static final int Policy_VERSION = 2;
	// public static final int Policye_TEXT = 3;
	// public static final int Policy_ACCOUNT_ID = 4;
	// public static final int Policy_GROUP_ID = 5;
	// public static final int Policy_USER_ID = 6;
	// }

	private static List<SearchResultRow> DATA = null;

	private static final String[] TABLE_COL_TITLE_CHECKALL = { "Check All", "全选" };
	private static final String[] TABLE_COL_TITLE_NO = { "No.", "序号" };
	private static final String[] TABLE_COL_TITLE_CERT_ACTIVE = { "Active", "密钥状态" };
	private static final String[] TABLE_COL_TITLE_CERT_REVOKED = { "Revoked", "是否撤销"};
	private static final String[] TABLE_COL_TITLE_CERT_CREATED_DATE = {"Created Date", "创建时间" };
	private static final String[] TABLE_COL_TITLE_ACCOUNT_NAME = {"Account", "账户" };
	private static final String[] TABLE_COL_TITLE_GROUP_NAME = { "Group", "组" };
	private static final String[] TABLE_COL_TITLE_NAME = {"ID", "用户"};

	private static final List<SearchResultFieldDesc> FIELDS_ROOT = Arrays
			.asList(new SearchResultFieldDesc(TABLE_COL_TITLE_CHECKALL[1], "10%", false), 
					new SearchResultFieldDesc(TABLE_COL_TITLE_NO[1], false, "10%",TableDisplay.MANDATORY, Type.TEXT, false, false),
					new SearchResultFieldDesc(TABLE_COL_TITLE_CERT_ACTIVE[1], true, "10%", TableDisplay.MANDATORY, Type.TEXT, false, false), 
					new SearchResultFieldDesc(TABLE_COL_TITLE_CERT_REVOKED[1], true, "10%", TableDisplay.MANDATORY, Type.TEXT, false, false), 
					new SearchResultFieldDesc(TABLE_COL_TITLE_CERT_CREATED_DATE[1], true, "25%", TableDisplay.MANDATORY, Type.TEXT, false, false),
					new SearchResultFieldDesc(TABLE_COL_TITLE_ACCOUNT_NAME[1], true, "15%", TableDisplay.MANDATORY, Type.TEXT, false, false), 
					new SearchResultFieldDesc(TABLE_COL_TITLE_GROUP_NAME[1], true, "15%", TableDisplay.MANDATORY, Type.TEXT, false, false),
					new SearchResultFieldDesc(TABLE_COL_TITLE_NAME[1], true, "15%", TableDisplay.MANDATORY, Type.TEXT, false, false));

	private static final List<SearchResultFieldDesc> FIELDS_NONROOT = Arrays
			.asList(new SearchResultFieldDesc(TABLE_COL_TITLE_CHECKALL[1], "10%", false), 
					new SearchResultFieldDesc(TABLE_COL_TITLE_NO[1], false, "10%", TableDisplay.MANDATORY, Type.TEXT, false, false),
					new SearchResultFieldDesc(TABLE_COL_TITLE_CERT_ACTIVE[1], true, "10%", TableDisplay.MANDATORY, Type.TEXT, false, false), 
					new SearchResultFieldDesc(TABLE_COL_TITLE_CERT_REVOKED[1], true, "10%", TableDisplay.MANDATORY, Type.TEXT, false, false), 
					new SearchResultFieldDesc(TABLE_COL_TITLE_CERT_CREATED_DATE[1], true, "30%", TableDisplay.MANDATORY, Type.TEXT, false, false),
					new SearchResultFieldDesc(TABLE_COL_TITLE_GROUP_NAME[1], true, "20%", TableDisplay.MANDATORY, Type.TEXT, false, false), 
					new SearchResultFieldDesc(TABLE_COL_TITLE_NAME[1], true, "20%", TableDisplay.MANDATORY, Type.TEXT, false, false));

}
