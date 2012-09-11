package com.eucalyptus.webui.client.service;

import java.io.Serializable;

import com.eucalyptus.webui.client.activity.device.ClientMessage;

public class EucalyptusServiceException extends Exception implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public static final String INVALID_SESSION = "Invalid session";
	
	private ClientMessage msg;
	
	public EucalyptusServiceException() {
		super();
	}
	
	public EucalyptusServiceException(ClientMessage msg) {
		super();
		this.msg = msg;
	}
	
	public EucalyptusServiceException(ClientMessage msg, Throwable cause) {
		super(cause);
		this.msg = msg;
	}
	
	public EucalyptusServiceException(String message) {
		super(message);
	}
	
	public EucalyptusServiceException(Throwable cause) {
		super(cause);
	}
	
	public EucalyptusServiceException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public ClientMessage getFrontendMessage() {
		return msg;
	}
	
}
