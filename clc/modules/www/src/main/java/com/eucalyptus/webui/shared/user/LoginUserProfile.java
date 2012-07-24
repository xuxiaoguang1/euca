package com.eucalyptus.webui.shared.user;

import java.io.Serializable;

import com.eucalyptus.webui.server.dictionary.RootAccount;

//import com.eucalyptus.auth.principal.Account;

public class LoginUserProfile implements Serializable {

  private static final long serialVersionUID = 1L;
  
  public static enum LoginAction {
    FIRSTTIME, // Needs first time information filling
    EXPIRATION // Needs password updating
  }
  
  //user id of user db table
  private int userId;
  private String userName;
  private String userTitle;
  private String userMobile;
  private String userEmail;
  private int accountId;
  private String accountName;
  private String userProfileSearch;
  private String userKeySearch;
  private LoginAction loginAction;
  
  private EnumUserType userType;
  
  
  public LoginUserProfile( ) {
  }
  
  public LoginUserProfile( String userName, String userTitle, String mobile, String email, String accountName, String userProfileSearch, String userKeySearch, LoginAction action ) {
    this.setUserName( userName );
    this.setUserTitle(userTitle);
    this.setUserMobile(mobile);
    this.setUserEmail(email);
    this.setAccountName( accountName );
    this.setUserProfileSearch( userProfileSearch );
    this.setUserKeySearch( userKeySearch );
    this.setLoginAction( action );
  }
  
  public boolean isSystemAdmin( ) {
    return RootAccount.NAME.equals( accountName );
  }
  
  public boolean isAccountAdmin( ) {
	    return EnumUserType.ADMIN == this.userType;
  }

  public void setUserId(int userId ) {
	    this.userId = userId;
  }

  public int getUserId( ) {
	  return userId;
  }

  public void setUserName( String userName ) {
	    this.userName = userName;
	  }

  public String getUserName( ) {
    return userName;
  }
	  
  public void setUserTitle( String userTitle ) {
    this.userTitle = userTitle;
  }

  public String getUserTitle( ) {
    return userTitle;
  }
  
  public void setUserMobile( String mobile ) {
	  this.userMobile = mobile;
  }

  public String getUserMobile( ) {
	  return userMobile;
  }
	  
  public void setUserEmail( String email ) {
	  this.userEmail = email;
  }

  public String getUserEmail( ) {
	  return userEmail;
  }
  
  public void setUserType( EnumUserType userType ) {
	  this.userType = userType;
  }

  public EnumUserType getUserType( ) {
	  return userType;
  }  

  public void setAccountId(int accountId ) {
	  this.accountId = accountId;
  }

  public int getAccountId( ) {
	  return accountId;
  }

  public void setAccountName( String accountName ) {
    this.accountName = accountName;
  }

  public String getAccountName( ) {
    return accountName;
  }
  
  public String toString( ) {
    return userTitle + "@" + accountName;
  }

  public void setUserProfileSearch( String userProfileSearch ) {
    this.userProfileSearch = userProfileSearch;
  }

  public String getUserProfileSearch( ) {
    return userProfileSearch;
  }

  public void setLoginAction( LoginAction loginAction ) {
    this.loginAction = loginAction;
  }

  public LoginAction getLoginAction( ) {
    return loginAction;
  }

  public String getUserKeySearch( ) {
    return this.userKeySearch;
  }

  public void setUserKeySearch( String userKeySearch ) {
    this.userKeySearch = userKeySearch;
  }
  
}
