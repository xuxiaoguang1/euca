package com.eucalyptus.webui.server.stat;

import java.io.Serializable;

public class HistorySyncException extends Exception implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public HistorySyncException() {
		super();
	}

	public HistorySyncException(String message) {
		super(message);
	}

	public HistorySyncException(Throwable cause) {
		super(cause);
	}

	public HistorySyncException(String message, Throwable cause) {
		super(message, cause);
	}
}
