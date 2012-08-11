package com.eucalyptus.webui.shared.auth;

import java.io.Serializable;
import java.security.cert.X509Certificate;
import java.util.Date;

import com.eucalyptus.webui.server.auth.crypto.Crypto;
import com.eucalyptus.webui.server.auth.util.X509CertHelper;

public class Certificate implements Serializable {

	public static final String DATE_PATTERN = "yyyy-MM-dd-HH-mm-ss";
	
	private static final long serialVersionUID = 1L;

	private int id;

	private String certificateId;
	
	/**
	 * encoded..
	 */
	private String pem;

	private boolean isActive;
	private boolean isRevoked;

	private Date createdDate;

	private String userId;

	public Certificate(String userId, String pem) {
		this.isActive = false;
		this.isRevoked = false;
		this.certificateId = Crypto.generateQueryId();
		this.pem = pem;
		this.createdDate = new Date();
		this.userId = userId;
	}

	public Certificate(int id, String cid, String pem, boolean active,
			boolean revoked, Date date, String userId) {
		this.id = id;
		this.certificateId = cid;
		this.pem = pem;
		this.isActive = active;
		this.isRevoked = revoked;
		this.createdDate = date;
		this.userId = userId;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	public boolean isRevoked() {
		return isRevoked;
	}

	public void setRevoked(boolean isRevoked) {
		this.isRevoked = isRevoked;
	}

	public String getCertificateId() {
		return certificateId;
	}

	// public void setCertificateId(String certificateId) {
	// this.certificateId = certificateId;
	// }

	public String getPem() {
		return pem;
	}

	// public void setPem(String pem) {
	// this.pem = pem;
	// }

	public Date getCreatedDate() {
		return createdDate;
	}

	// public void setCreatedDate(Date createDate) {
	// this.createDate = createDate;
	// }

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public X509Certificate getX509Certificate() {
		return X509CertHelper.toCertificate(pem);
	}

}
