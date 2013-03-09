package com.eucalyptus.webui.server.device;

import java.io.Serializable;

public class DeviceSyncException extends Exception implements Serializable {

    private static final long serialVersionUID = 1L;
    
    public static final String INVALID_SESSION = "Invalid session";
      
    public DeviceSyncException( ) {
        super( );
    }
      
    public DeviceSyncException( String message ) {
        super( message );
    }
      
    public DeviceSyncException( Throwable cause ) {
        super( cause );
    }
      
    public DeviceSyncException( String message, Throwable cause ) {
        super( message, cause );
    }
}
