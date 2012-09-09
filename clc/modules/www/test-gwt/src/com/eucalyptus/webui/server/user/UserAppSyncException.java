package com.eucalyptus.webui.server.user;

import java.io.Serializable;

public class UserAppSyncException extends Exception implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public static final String INVALID_SESSION = "Invalid session";
	  
	public UserAppSyncException( ) {
		super( );
	}
	  
	public UserAppSyncException( String message ) {
		super( message );
	}
	  
	public UserAppSyncException( Throwable cause ) {
		super( cause );
	}
	  
	public UserAppSyncException( String message, Throwable cause ) {
		super( message, cause );
	}
}