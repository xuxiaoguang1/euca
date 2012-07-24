package com.eucalyptus.webui.client.session;


public interface LocalSession {
  
  // Load session from disk
  Session getSession( );
  
  // Save session to disk
  void saveSession( Session session, boolean persistent );
  
  // Clear session
  void clearSession( );
  
}
