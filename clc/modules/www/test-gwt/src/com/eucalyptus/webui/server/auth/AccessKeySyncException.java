package com.eucalyptus.webui.server.auth;

import java.io.Serializable;

public class AccessKeySyncException extends Exception implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public AccessKeySyncException() {
		super();
	}

	public AccessKeySyncException(String message) {
		super(message);
	}

	public AccessKeySyncException(Throwable cause) {
		super(cause);
	}

	public AccessKeySyncException(String message, Throwable cause) {
		super(message, cause);
	}
}
