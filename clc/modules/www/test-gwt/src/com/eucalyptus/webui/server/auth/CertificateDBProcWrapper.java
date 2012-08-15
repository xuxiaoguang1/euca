package com.eucalyptus.webui.server.auth;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import com.eucalyptus.webui.client.service.EucalyptusServiceException;
import com.eucalyptus.webui.client.service.SearchRange;
import com.eucalyptus.webui.client.service.SearchResult;
import com.eucalyptus.webui.server.db.DBProcWrapper;
import com.eucalyptus.webui.server.db.ResultSetWrapper;
import com.eucalyptus.webui.server.dictionary.DBTableColName;
import com.eucalyptus.webui.server.dictionary.DBTableName;
import com.eucalyptus.webui.server.user.UserSyncException;
import com.eucalyptus.webui.shared.auth.Certificate;
import com.eucalyptus.webui.shared.user.EnumUserType;
import com.eucalyptus.webui.shared.user.LoginUserProfile;

public class CertificateDBProcWrapper {

	public void addCertificate(Certificate certificate)
			throws CertificateSyncException {
		DBProcWrapper dbProc = DBProcWrapper.Instance();
		String sql = addCertificateSQL(certificate);

		try {
			dbProc.update(sql);
		} catch (SQLException e) {
			throw new CertificateSyncException("Database fails");
		}
	}

	public void deleteCertificate(ArrayList<String> ids)
			throws CertificateSyncException {
		DBProcWrapper dbProc = DBProcWrapper.Instance();
		String sql = deleteCertificateSQL(ids);

		try {
			dbProc.update(sql);
		} catch (SQLException e) {
			throw new CertificateSyncException("Database fails");
		}
	}
	
	public void modifiCertificate(ArrayList<String> ids, Boolean active,
			Boolean revoked) throws CertificateSyncException {
		if(active == null && revoked == null)
			throw new CertificateSyncException("active and revoke cannot both be null");
		DBProcWrapper dbProc = DBProcWrapper.Instance();
		String sql = modifyCertificateSQL(ids, active, revoked);

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
										.getString(DBTableColName.USER_CERT.ID)),
								res.getString(DBTableColName.USER_CERT.CERT_ID),
								res.getString(DBTableColName.USER_CERT.PEM),
								Boolean.parseBoolean(res
										.getString(DBTableColName.USER_CERT.ACTIVE)),
								Boolean.parseBoolean(res
										.getString(DBTableColName.USER_CERT.REVOKED)),
								df.parse(res
										.getString(DBTableColName.USER_CERT.CREATED_DATE)),
								res.getString(DBTableColName.USER_CERT.USER_ID));
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
	
	public ResultSetWrapper queryTotalCertificates() throws CertificateSyncException {
		DBProcWrapper dbProc = DBProcWrapper.Instance();
		
		StringBuilder sql = certificateAccountGroupViewSql();
		
		System.out.println(sql.toString());
		
		try {
			ResultSetWrapper result = dbProc.query(sql.toString());
			
			return result;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new CertificateSyncException("Fail to Query certificate");
		}
	}
	
	public ResultSetWrapper queryCertificatesBy(int accountId, int userId, EnumUserType userType)  throws CertificateSyncException {
		DBProcWrapper dbProc = DBProcWrapper.Instance();

		StringBuilder sql = certificateAccountGroupViewSql();
		
		sql.append(" AND ").
		append(DBTableName.USER).append(".").append(DBTableColName.USER.ACCOUNT_ID).
		append(" = '").append(accountId).append("' ");
		
		switch (userType) {
		  case ADMIN:
			  break;
		  case USER:
			  sql.append(" AND ").
			  append(DBTableName.USER).append(".").append(DBTableColName.USER.ID).
			  append(" = '").
			  append(userId).
			  append("'");
			  break;
		  default:
			  return null;
		  }
		
		System.out.println(sql.toString());
		
		try {
			ResultSetWrapper result = dbProc.query(sql.toString());
			return result;
		} catch (SQLException e) {
			e.printStackTrace();
			throw new CertificateSyncException("Fail to Query certificate");
		}
	}
	
//	public List<Certificate> listCertificates()
//			throws CertificateSyncException {
//		DBProcWrapper dbProc = DBProcWrapper.Instance();
//		String sql = listCertificatesSQL();
//
//		ResultSet res = null;
//
//		try {
//			res = dbProc.query(sql).getResultSet();
//		} catch (SQLException e) {
//			throw new CertificateSyncException("Database fails");
//		}
//
//		List<Certificate> list = new ArrayList<Certificate>();
//
//		if (res != null) {
//			list = new ArrayList<Certificate>();
//			DateFormat df = new SimpleDateFormat(Certificate.DATE_PATTERN);
//			try {
//				while (res.next()) {
//					Certificate cert = null;
//					try {
//						cert = new Certificate(
//								Integer.valueOf(res
//										.getString(DBTableColName.USER_CERT.ID)),
//								res.getString(DBTableColName.USER_CERT.CERT_ID),
//								res.getString(DBTableColName.USER_CERT.PEM),
//								Boolean.parseBoolean(res
//										.getString(DBTableColName.USER_CERT.ACTIVE)),
//								Boolean.parseBoolean(res
//										.getString(DBTableColName.USER_CERT.REVOKED)),
//								df.parse(res
//										.getString(DBTableColName.USER_CERT.CREATED_DATE)),
//								res.getString(DBTableColName.USER_CERT.USER_ID));
//					} catch (NumberFormatException e) {
//						e.printStackTrace();
//					} catch (ParseException e) {
//						e.printStackTrace();
//					}
//					if (cert != null) {
//						list.add(cert);
//					}
//				}
//			} catch (SQLException e) {
//				e.printStackTrace();
//			}
//		}
//		return list;
//	}

	private String addCertificateSQL(Certificate certificate) {
		StringBuilder str = new StringBuilder();
		str.append("INSERT INTO ").append(DBTableName.USER_CERT)
				.append(" ( ").append(DBTableColName.USER_CERT.ID)
				.append(", ").append(DBTableColName.USER_CERT.CERT_ID)
				.append(", ").append(DBTableColName.USER_CERT.PEM)
				.append(", ").append(DBTableColName.USER_CERT.ACTIVE)
				.append(", ").append(DBTableColName.USER_CERT.REVOKED)
				.append(", ").append(DBTableColName.USER_CERT.CREATED_DATE)
				.append(", ").append(DBTableColName.USER_CERT.USER_ID)
				.append(") VALUES (null, ");

		str.append("'");
		str.append(certificate.getCertificateId());
		str.append("', '");

		str.append(certificate.getPem());
		str.append("', ");

		str.append(certificate.isActive());
		str.append(", ");

		str.append(certificate.isRevoked());
		str.append(", '");

		DateFormat df = new SimpleDateFormat(Certificate.DATE_PATTERN);
		str.append(df.format(certificate.getCreatedDate()));
		str.append("', '");

		str.append(certificate.getUserId());
		str.append("')");

		return str.toString();
	}

	private String deleteCertificateSQL(ArrayList<String> ids) {
		StringBuilder sb = new StringBuilder();
		sb.append("DELETE ");
		sb.append(" FROM ").append(DBTableName.USER_CERT);
		sb.append(" WHERE ");
		for (String str : ids) {
			sb.append(" ").append(DBTableColName.USER_CERT.ID).
			append(" = '").
			append(str).
			append("' OR ");
		}
		sb.delete(sb.length() - 3 , sb.length());
		return sb.toString();
	}
	
	private String modifyCertificateSQL(ArrayList<String> ids, Boolean active, Boolean revoked){
		StringBuilder sql = new StringBuilder();
		sql.append("UPDATE ").
		append(DBTableName.USER_CERT).
		append(" SET ");
		if(active != null){
			sql.append(DBTableColName.USER_CERT.ACTIVE).append(" = ").append(active);
		}
		if(active != null && revoked != null){
			sql.append(" , ");
		}
		if(revoked != null){
			sql.append(DBTableColName.USER_CERT.REVOKED).append(" = ").append(revoked);
		}
		
		sql.append(" WHERE ");
		
		for (String str : ids) {
			sql.append(DBTableColName.USER_CERT.ID).
			append(" = '").
			append(str).
			append("' or ");
		}
		
		sql.delete(sql.length() -3 , sql.length());
		
		System.out.println(sql);
		
		return sql.toString();
	}

	private String getCertificatesSQL(String userId) {
		StringBuilder sql = new StringBuilder()
				.append("SELECT * FROM ").append(DBTableName.USER_CERT)
				.append(" WHERE ").append(DBTableColName.USER_CERT.USER_ID).append(" = ")
				.append(userId);
		return sql.toString();
	}
	
	private String listCertificatesSQL() {
		StringBuilder sql = new StringBuilder()
				.append("SELECT * FROM ").append(DBTableName.USER_CERT)
				.append(" WHERE 1 = 1 ");
		return sql.toString();
	}
	
	private StringBuilder certificateAccountGroupViewSql() {
		StringBuilder sql = new StringBuilder("SELECT * ").
				append(" FROM ").
				append("( (").
				append(DBTableName.USER_CERT).
				append(" LEFT JOIN ").
				append(DBTableName.USER).
				append(" ON ").
				append(DBTableName.USER_CERT).append(".").append(DBTableColName.USER_KEY.USER_ID).
				append(" = ").
				append(DBTableName.USER).append(".").append(DBTableColName.USER.ID).
				append(") ").
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
				append(" WHERE 1=1 ");
		
		return sql;
	}

}
