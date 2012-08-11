package com.eucalyptus.webui.shared.auth;

import java.io.Serializable;
import java.util.Date;

import com.eucalyptus.webui.server.auth.crypto.Crypto;

public class AccessKey implements Serializable {
	
	public static final String DATE_PATTERN = "yyyy-MM-dd-HH-mm-ss";

	private static final long serialVersionUID = 1L;

	private int id;

	private String accessKey;
	private String secretKey;

	private boolean isActive;

	private Date createdDate;
	private String userId;

	public AccessKey(String userId) {
		this.id = -1;
		this.accessKey = Crypto.generateQueryId();
		this.secretKey = Crypto.generateSecretKey();
		this.createdDate = new Date();
		this.userId = userId;
	}

	public AccessKey(int id, String ak, String sk, boolean active, Date date,
			String userId) {
		this.id = id;
		this.accessKey = ak;
		this.secretKey = sk;
		this.isActive = active;
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

	public String getAccessKey() {
		return accessKey;
	}

	// public void setAccessKey(String accessKey) {
	// this.accessKey = accessKey;
	// }

	public String getSecretKey() {
		return secretKey;
	}

	// public void setSecretKey(String secretKey) {
	// this.secretKey = secretKey;
	// }

	public Date getCreatedDate() {
		return createdDate;
	}

	// public void setCreatedDate(Date createdDate) {
	// this.createdDate = createdDate;
	// }

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

}
