package com.eucalyptus.webui.server.auth;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import com.eucalyptus.webui.server.db.DBProcWrapper;
import com.eucalyptus.webui.server.dictionary.DBTableColName;
import com.eucalyptus.webui.server.dictionary.DBTableName;
import com.eucalyptus.webui.shared.auth.Certificate;

public class CertificateDBProcWrapper {

	public void addCertificate(Certificate cert)
			throws CertificateSyncException {
		DBProcWrapper dbProc = DBProcWrapper.Instance();
		String sql = addCertificateSQL(cert);

		try {
			dbProc.update(sql);
		} catch (SQLException e) {
			throw new CertificateSyncException("Database fails");
		}
	}

	public void deleteCertificate(String certificateId)
			throws CertificateSyncException {
		DBProcWrapper dbProc = DBProcWrapper.Instance();
		String sql = deleteCertificateSQL(certificateId);

		try {
			dbProc.update(sql);
		} catch (SQLException e) {
			throw new CertificateSyncException("Database fails");
		}
	}

	public List<Certificate> getCertificates(String userId)
			throws CertificateSyncException {
		DBProcWrapper dbProc = DBProcWrapper.Instance();
		String sql = getCertificatesSQL(userId);

		ResultSet res = null;

		try {
			res = dbProc.query(sql).getResultSet();
		} catch (SQLException e) {
			throw new CertificateSyncException("Database fails");
		}

		List<Certificate> list = new ArrayList<Certificate>();

		if (res != null) {
			list = new ArrayList<Certificate>();
			DateFormat df = new SimpleDateFormat(Certificate.DATE_PATTERN);
			try {
				while (res.next()) {
					Certificate cert = null;
					try {
						cert = new Certificate(
								Integer.valueOf(res
										.getString(DBTableColName.CERTIFICATE.ID)),
								res.getString(DBTableColName.CERTIFICATE.CERT_ID),
								res.getString(DBTableColName.CERTIFICATE.PEM),
								Boolean.parseBoolean(res
										.getString(DBTableColName.CERTIFICATE.ACTIVE)),
								Boolean.parseBoolean(res
										.getString(DBTableColName.CERTIFICATE.REVOKED)),
								df.parse(res
										.getString(DBTableColName.CERTIFICATE.CREATED_DATE)),
								res.getString(DBTableColName.CERTIFICATE.USER_ID));
					} catch (NumberFormatException e) {
						e.printStackTrace();
					} catch (ParseException e) {
						e.printStackTrace();
					}
					if (cert != null) {
						list.add(cert);
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return list;
	}
	
	public List<Certificate> listCertificates()
			throws CertificateSyncException {
		DBProcWrapper dbProc = DBProcWrapper.Instance();
		String sql = listCertificatesSQL();

		ResultSet res = null;

		try {
			res = dbProc.query(sql).getResultSet();
		} catch (SQLException e) {
			throw new CertificateSyncException("Database fails");
		}

		List<Certificate> list = new ArrayList<Certificate>();

		if (res != null) {
			list = new ArrayList<Certificate>();
			DateFormat df = new SimpleDateFormat(Certificate.DATE_PATTERN);
			try {
				while (res.next()) {
					Certificate cert = null;
					try {
						cert = new Certificate(
								Integer.valueOf(res
										.getString(DBTableColName.CERTIFICATE.ID)),
								res.getString(DBTableColName.CERTIFICATE.CERT_ID),
								res.getString(DBTableColName.CERTIFICATE.PEM),
								Boolean.parseBoolean(res
										.getString(DBTableColName.CERTIFICATE.ACTIVE)),
								Boolean.parseBoolean(res
										.getString(DBTableColName.CERTIFICATE.REVOKED)),
								df.parse(res
										.getString(DBTableColName.CERTIFICATE.CREATED_DATE)),
								res.getString(DBTableColName.CERTIFICATE.USER_ID));
					} catch (NumberFormatException e) {
						e.printStackTrace();
					} catch (ParseException e) {
						e.printStackTrace();
					}
					if (cert != null) {
						list.add(cert);
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return list;
	}

	private String addCertificateSQL(Certificate cert) {
		StringBuilder str = new StringBuilder();
		str.append("INSERT INTO ").append(DBTableName.CERTIFICATE)
				.append(" ( ").append(DBTableColName.CERTIFICATE.ID)
				.append(", ").append(DBTableColName.CERTIFICATE.CERT_ID)
				.append(", ").append(DBTableColName.CERTIFICATE.PEM)
				.append(", ").append(DBTableColName.CERTIFICATE.ACTIVE)
				.append(", ").append(DBTableColName.CERTIFICATE.REVOKED)
				.append(", ").append(DBTableColName.CERTIFICATE.CREATED_DATE)
				.append(", ").append(DBTableColName.CERTIFICATE.USER_ID)
				.append(") VALUES (null, ");

		str.append("'");
		str.append(cert.getCertificateId());
		str.append("', '");

		str.append(cert.getPem());
		str.append("', '");

		str.append(cert.isActive());
		str.append("', '");

		str.append(cert.isRevoked());
		str.append("', '");

		DateFormat df = new SimpleDateFormat(Certificate.DATE_PATTERN);
		str.append(df.format(cert.getCreatedDate()));
		str.append("', '");

		str.append(cert.getUserId());
		str.append("')");

		return str.toString();
	}

	private String deleteCertificateSQL(String certificateId) {
		StringBuilder sb = new StringBuilder();
		sb.append("DELETE ");
		sb.append(" FROM ").append(DBTableName.CERTIFICATE);
		sb.append(" WHERE ");
		sb.append(DBTableColName.CERTIFICATE.CERT_ID).append(" = ")
				.append(certificateId);
		return sb.toString();
	}

	private String getCertificatesSQL(String userId) {
		StringBuilder sql = new StringBuilder()
				.append("SELECT * FROM ").append(DBTableName.CERTIFICATE)
				.append(" WHERE ").append(DBTableColName.CERTIFICATE.USER_ID).append(" = ")
				.append(userId);
		return sql.toString();
	}
	
	private String listCertificatesSQL() {
		StringBuilder sql = new StringBuilder()
				.append("SELECT * FROM ").append(DBTableName.CERTIFICATE)
				.append(" WHERE 1 = 1 ");
		return sql.toString();
	}

}
