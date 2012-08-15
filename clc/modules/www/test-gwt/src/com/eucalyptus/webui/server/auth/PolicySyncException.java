package com.eucalyptus.webui.server.auth;

import java.io.Serializable;

public class PolicySyncException extends Exception implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public PolicySyncException() {
		super();
	}

	public PolicySyncException(String message) {
		super(message);
	}

	public PolicySyncException(Throwable cause) {
		super(cause);
	}

	public PolicySyncException(String message, Throwable cause) {
		super(message, cause);
	}
}
