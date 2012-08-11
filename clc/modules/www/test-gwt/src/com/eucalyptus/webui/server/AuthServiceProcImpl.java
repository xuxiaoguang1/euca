package com.eucalyptus.webui.server;

import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import com.eucalyptus.webui.client.service.EucalyptusServiceException;
import com.eucalyptus.webui.client.service.SearchResult;
import com.eucalyptus.webui.client.service.SearchResultRow;
import com.eucalyptus.webui.client.session.Session;
import com.eucalyptus.webui.server.auth.AccessKeyDBProcWrapper;
import com.eucalyptus.webui.server.auth.AccessKeySyncException;
import com.eucalyptus.webui.server.auth.CertificateDBProcWrapper;
import com.eucalyptus.webui.server.auth.CertificateSyncException;
import com.eucalyptus.webui.server.auth.PolicyDBProcWrapper;
import com.eucalyptus.webui.server.auth.PolicySyncException;
import com.eucalyptus.webui.server.auth.util.B64;
import com.eucalyptus.webui.server.auth.util.X509CertHelper;
import com.eucalyptus.webui.server.user.LoginUserProfileStorer;
import com.eucalyptus.webui.shared.auth.AccessKey;
import com.eucalyptus.webui.shared.auth.Certificate;
import com.eucalyptus.webui.shared.auth.Policy;
import com.eucalyptus.webui.shared.user.LoginUserProfile;
import com.google.common.base.Strings;

public class AuthServiceProcImpl {

	private AccessKeyDBProcWrapper keyDBProc;

	private CertificateDBProcWrapper certDBProc;

	private PolicyDBProcWrapper policyDBProc;

	public void addAccessKey(Session session, String userId)
			throws EucalyptusServiceException {
		checkPermission(session);

		if (userId == null) {
			throw new EucalyptusServiceException("UserId cannot be NULL");
		}

		AccessKey key = new AccessKey(userId);
		key.setActive(true);

		try {
			keyDBProc.addAccessKey(key);
		} catch (AccessKeySyncException e) {
			e.printStackTrace();
			throw new EucalyptusServiceException("Failed to create access key");
		}
	}

	public void deleteAccessKey(Session session, SearchResultRow row)
			throws EucalyptusServiceException {
		checkPermission(session);

		if (row == null) {
			throw new EucalyptusServiceException(
					"SearchResultRow cannot be NULL");
		}

		int key_id = Integer
				.parseInt(row.getField(KeyColumnIndex.AccessKey_ID));

		try {
			keyDBProc.deleteAccessKey(key_id);
		} catch (AccessKeySyncException e) {
			e.printStackTrace();
			throw new EucalyptusServiceException("Failed to create access key");
		}
	}

	public SearchResult listAccesssKeyByUser(Session session, String userId)
			throws EucalyptusServiceException {
		if (userId == null) {
			throw new EucalyptusServiceException("userId cannot be NULL");
		}
		try {
			List<AccessKey> list = keyDBProc.getAccessKeys(userId);
			SearchResult res = new SearchResult();
			DateFormat df = new SimpleDateFormat(AccessKey.DATE_PATTERN);
			for (AccessKey key : list) {
				List<String> row = new ArrayList<String>();
				row.add(Integer.toString(key.getId()));
				row.add(key.getAccessKey());
				row.add(key.getSecretKey());
				row.add(Boolean.toString(key.isActive()));
				row.add(df.format(key.getCreatedDate()));
				row.add(key.getUserId());
				res.addRow(new SearchResultRow(row));
			}
			return res;
		} catch (AccessKeySyncException e) {
			e.printStackTrace();
			throw new EucalyptusServiceException("Failed to get access keys");
		}
	}

	public SearchResult listAccessKeys(Session session)
			throws EucalyptusServiceException {
		try {
			List<AccessKey> list = keyDBProc.listAccessKeys();
			SearchResult res = new SearchResult();
			DateFormat df = new SimpleDateFormat(AccessKey.DATE_PATTERN);
			for (AccessKey key : list) {
				List<String> row = new ArrayList<String>();
				row.add(Integer.toString(key.getId()));
				row.add(key.getAccessKey());
				row.add(key.getSecretKey());
				row.add(Boolean.toString(key.isActive()));
				row.add(df.format(key.getCreatedDate()));
				row.add(key.getUserId());
				res.addRow(new SearchResultRow(row));
			}
			return res;
		} catch (AccessKeySyncException e) {
			e.printStackTrace();
			throw new EucalyptusServiceException("Failed to list access keys");
		}
	}

	public void addCertificate(Session session, String userId, String pem)
			throws EucalyptusServiceException {
		checkPermission(session);

		if (userId == null || pem == null) {
			throw new EucalyptusServiceException(
					"userID and pem cannot be NULL");
		}

		if (Strings.isNullOrEmpty(pem)) {
			throw new EucalyptusServiceException("Empty Cert");
		}

		try {
			String encodedPem = B64.url.encString(pem);
			for (Certificate c : certDBProc.getCertificates(userId)) {
				if (c.getPem().equals(encodedPem)) {
					if (!c.isRevoked()) {
						throw new EucalyptusServiceException("Auth conflict");
					} else {
						certDBProc
								.deleteCertificate(Integer.toString(c.getId()));
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

	public void deleteCertification(Session session, SearchResultRow row)
			throws EucalyptusServiceException {
		checkPermission(session);

		if (row == null) {
			throw new EucalyptusServiceException(
					"SearchResultRow cannot be NULL");
		}

		String id = row.getField(CertColumnIndex.Certificate_ID);

		try {
			certDBProc.deleteCertificate(id);
		} catch (CertificateSyncException e) {
			e.printStackTrace();
			throw new EucalyptusServiceException("Failed to create access key");
		}
	}

	public SearchResult listCertificatesByUser(Session session, String userId)
			throws EucalyptusServiceException {
		if (userId == null) {
			throw new EucalyptusServiceException("userId cannot be NULL");
		}
		try {
			List<Certificate> list = certDBProc.getCertificates(userId);
			SearchResult res = new SearchResult();
			DateFormat df = new SimpleDateFormat(Certificate.DATE_PATTERN);
			for (Certificate cert : list) {
				List<String> row = new ArrayList<String>();
				row.add(Integer.toString(cert.getId()));
				row.add(cert.getCertificateId());
				row.add(cert.getPem());
				row.add(Boolean.toString(cert.isActive()));
				row.add(Boolean.toString(cert.isRevoked()));
				row.add(df.format(cert.getCreatedDate()));
				row.add(cert.getUserId());
				res.addRow(new SearchResultRow(row));
			}
			return res;
		} catch (CertificateSyncException e) {
			e.printStackTrace();
			throw new EucalyptusServiceException("Failed to get certificate");
		}
	}

	public SearchResult listCertificates(Session session)
			throws EucalyptusServiceException {
		try {
			List<Certificate> list = certDBProc.listCertificates();
			SearchResult res = new SearchResult();
			DateFormat df = new SimpleDateFormat(Certificate.DATE_PATTERN);
			for (Certificate cert : list) {
				List<String> row = new ArrayList<String>();
				row.add(Integer.toString(cert.getId()));
				row.add(cert.getCertificateId());
				row.add(cert.getPem());
				row.add(Boolean.toString(cert.isActive()));
				row.add(Boolean.toString(cert.isRevoked()));
				row.add(df.format(cert.getCreatedDate()));
				row.add(cert.getUserId());
				res.addRow(new SearchResultRow(row));
			}
			return res;
		} catch (CertificateSyncException e) {
			e.printStackTrace();
			throw new EucalyptusServiceException("Failed to list certificate");
		}
	}

	public void addAccountPolicy(Session session, String accountId,
			String name, String text) throws EucalyptusServiceException {
		checkPermission(session);

		if (accountId == null) {
			throw new EucalyptusServiceException("Account id cannot be NULL");
		}

		Policy pol = new Policy();
		pol.setAccountId(accountId);

		addPolicy(pol, name, text);
	}

	public void addGroupPolicy(Session session, String groupId, String name,
			String text) throws EucalyptusServiceException {
		checkPermission(session);

		if (groupId == null) {
			throw new EucalyptusServiceException("Group id cannot be NULL");
		}

		Policy pol = new Policy();
		pol.setGroupId(groupId);

		addPolicy(pol, name, text);
	}

	public void addUserPolicy(Session session, String userId, String name,
			String text) throws EucalyptusServiceException {
		checkPermission(session);

		if (userId == null) {
			throw new EucalyptusServiceException("User id cannot be NULL");
		}

		Policy pol = new Policy();
		pol.setUserId(userId);

		addPolicy(pol, name, text);
	}

	private void checkPermission(Session session)
			throws EucalyptusServiceException {
		LoginUserProfile curUser = LoginUserProfileStorer.instance().get(
				session.getId());

		if (!curUser.isSystemAdmin() && !curUser.isAccountAdmin()) {
			throw new EucalyptusServiceException("No permission");
		}
	}

	private void addPolicy(Policy pol, String name, String text)
			throws EucalyptusServiceException {
		if (name == null) {
			throw new EucalyptusServiceException("Policy name cannot be NULL");
		}

		if (text == null) {
			throw new EucalyptusServiceException("Policy text cannot be NULL");
		}

		pol.setName(name);
		pol.setText(text);

		try {
			policyDBProc.addPolicy(pol);
		} catch (PolicySyncException e) {
			e.printStackTrace();
			throw new EucalyptusServiceException("Failed to create policy");
		}
	}

	public void deletePolicy(Session session, SearchResultRow row)
			throws EucalyptusServiceException {
		checkPermission(session);

		if (row == null) {
			throw new EucalyptusServiceException(
					"SearchResultRow cannot be NULL");
		}

		int policy_id = Integer.parseInt(row
				.getField(PolicyColumnIndex.Policy_ID));

		try {
			policyDBProc.deletePolicy(policy_id);
		} catch (PolicySyncException e) {
			e.printStackTrace();
			throw new EucalyptusServiceException("Failed to delete policy");
		}
	}
	
	public SearchResult listPolicies(Session session)
			throws EucalyptusServiceException {
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
	

	public static class KeyColumnIndex {
		public static final int AccessKey_ID = 0;
		public static final int AccessKey_AKEY = 1;
		public static final int AccessKey_SKEY = 2;
		public static final int AccessKey_ACTIVE = 3;
		public static final int AccessKey_DATE = 4;
		public static final int AccessKey_USER_ID = 5;
	}

	public static class CertColumnIndex {
		public static final int Certificate_ID = 0;
		public static final int Certificate_CERT_ID = 1;
		public static final int Certificate_PEM = 2;
		public static final int Certificate_ACTIVE = 3;
		public static final int Certificate_REVOKED = 4;
		public static final int Certificate_DATE = 5;
		public static final int Certificate_USER_ID = 6;
	}

	public static class PolicyColumnIndex {
		public static final int Policy_ID = 0;
		public static final int Policy_NAME = 1;
		public static final int Policy_VERSION = 2;
		public static final int Policye_TEXT = 3;
		public static final int Policy_ACCOUNT_ID = 4;
		public static final int Policy_GROUP_ID = 5;
		public static final int Policy_USER_ID = 6;
	}

}
