package com.eucalyptus.webui.server.auth.crypto;


public interface HmacProvider extends BaseSecurityProvider  {

  /**
   * TODO: DOCUMENT CryptoProvider.java
   * @return
   */
  public abstract String generateSystemSignature( );
  public abstract String generateSystemToken( byte[] data );

}
