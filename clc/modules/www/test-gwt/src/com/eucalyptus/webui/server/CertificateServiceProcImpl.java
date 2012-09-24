package com.eucalyptus.webui.server;

import java.security.cert.X509Certificate;
import java.sql.ResultSet;
import java.sql.SQLException;
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
import com.eucalyptus.webui.server.auth.CertificateDBProcWrapper;
import com.eucalyptus.webui.server.auth.CertificateSyncException;
import com.eucalyptus.webui.server.auth.crypto.Crypto;
import com.eucalyptus.webui.server.auth.util.B64;
import com.eucalyptus.webui.server.auth.util.X509CertHelper;
import com.eucalyptus.webui.server.db.ResultSetWrapper;
import com.eucalyptus.webui.shared.auth.Certificate;
import com.eucalyptus.webui.shared.dictionary.DBTableColName;
import com.eucalyptus.webui.shared.dictionary.Enum2String;
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
			Certificate cert = new Certificate(
					userId, 
					Crypto.generateQueryId(), 
					B64.url.encString(pem));
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
					Crypto.generateQueryId(), 
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
				rs = certDBProc.queryCertificatesBy(curUser.getAccountId(), curUser.getUserId(), curUser.getUserType());
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
