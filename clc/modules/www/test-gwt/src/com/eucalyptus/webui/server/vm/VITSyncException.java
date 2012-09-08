package com.eucalyptus.webui.server.vm;

import java.io.Serializable;

public class VITSyncException extends Exception implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public static final String INVALID_SESSION = "Invalid session";
	  
	public VITSyncException( ) {
		super( );
	}
	  
	public VITSyncException( String message ) {
		super( message );
	}
	  
	public VITSyncException( Throwable cause ) {
		super( cause );
	}
	  
	public VITSyncException( String message, Throwable cause ) {
		super( message, cause );
	}
}