package com.eucalyptus.webui.server.user;

import java.io.Serializable;

public class UserSyncException extends Exception implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public static final String INVALID_SESSION = "Invalid session";
	  
	public UserSyncException( ) {
		super( );
	}
	  
	public UserSyncException( String message ) {
		super( message );
	}
	  
	public UserSyncException( Throwable cause ) {
		super( cause );
	}
	  
	public UserSyncException( String message, Throwable cause ) {
		super( message, cause );
	}
}