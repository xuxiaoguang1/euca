package com.eucalyptus.webui.server.user;

import java.io.Serializable;

public class AccountSyncException extends Exception implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public static final String INVALID_SESSION = "Invalid session";
	  
	public AccountSyncException( ) {
		super( );
	}
	  
	public AccountSyncException( String message ) {
		super( message );
	}
	  
	public AccountSyncException( Throwable cause ) {
		super( cause );
	}
	  
	public AccountSyncException( String message, Throwable cause ) {
		super( message, cause );
	}
}