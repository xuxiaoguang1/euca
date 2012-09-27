package com.eucalyptus.webui.server.ws;

import java.io.Serializable;

public class EucaWSException extends Exception implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public static final String INVALID_SESSION = "Invalid session";
	  
	public EucaWSException( ) {
		super( );
	}
	  
	public EucaWSException( String message ) {
		super( message );
	}
	  
	public EucaWSException( Throwable cause ) {
		super( cause );
	}
	  
	public EucaWSException( String message, Throwable cause ) {
		super( message, cause );
	}
}