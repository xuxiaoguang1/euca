package com.eucalyptus.webui.server.auth;

import java.io.Serializable;

public class CertificateSyncException extends Exception implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public CertificateSyncException() {
		super();
	}

	public CertificateSyncException(String message) {
		super(message);
	}

	public CertificateSyncException(Throwable cause) {
		super(cause);
	}

	public CertificateSyncException(String message, Throwable cause) {
		super(message, cause);
	}
}
