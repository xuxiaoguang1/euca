package com.eucalyptus.webui.server;


public class WebSession {
  
  private String id;
  
  private long creationTime;
  private long lastAccessTime;
  
  public WebSession( String id, long creationTime, long lastAccessTime ) {
    this.setId( id );
    this.setCreationTime( creationTime );
    this.setLastAccessTime( lastAccessTime );
  }

  public void setId( String id ) {
    this.id = id;
  }

  public String getId( ) {
    return id;
  }
  
  public void setCreationTime( long creationTime ) {
    this.creationTime = creationTime;
  }

  public long getCreationTime( ) {
    return creationTime;
  }

  public void setLastAccessTime( long lastAccessTime ) {
    this.lastAccessTime = lastAccessTime;
  }

  public long getLastAccessTime( ) {
    return lastAccessTime;
  }
}
