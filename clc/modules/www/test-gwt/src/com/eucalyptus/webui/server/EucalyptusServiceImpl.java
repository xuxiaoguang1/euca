package com.eucalyptus.webui.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.apache.log4j.Logger;

import com.eucalyptus.webui.client.service.CloudInfo;
import com.eucalyptus.webui.client.service.GuideItem;
import com.eucalyptus.webui.client.service.SearchRange;
import com.eucalyptus.webui.client.service.SearchResultRow;
import com.eucalyptus.webui.client.service.EucalyptusService;
import com.eucalyptus.webui.client.service.EucalyptusServiceException;
import com.eucalyptus.webui.client.service.SearchResult;
import com.eucalyptus.webui.client.session.Session;
import com.eucalyptus.webui.server.user.AuthenticateUserLogin;
import com.eucalyptus.webui.server.user.LoginUserProfileStorer;
import com.eucalyptus.webui.server.user.PwdResetProc;
import com.eucalyptus.webui.shared.user.AccountInfo;
import com.eucalyptus.webui.shared.user.EnumState;
import com.eucalyptus.webui.shared.user.GroupInfo;
import com.eucalyptus.webui.shared.user.LoginUserProfile;
import com.eucalyptus.webui.shared.user.UserInfo;
import com.google.common.base.Strings;
import com.google.gwt.thirdparty.guava.common.collect.Maps;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class EucalyptusServiceImpl extends RemoteServiceServlet implements EucalyptusService {

	private static final Logger LOG = Logger.getLogger( EucalyptusServiceImpl.class );
	private static final long serialVersionUID = 1L;
	private static final Random RANDOM = new Random( );
	private static AuthenticateUserLogin authenticateUserLogin = new AuthenticateUserLogin();
	
	private AccountServiceProcImpl accountServiceProc = new AccountServiceProcImpl();
	private UserServiceProcImpl userServiceProc = new UserServiceProcImpl();
	private GroupServiceProcImpl groupServiceProc = new GroupServiceProcImpl();
	private DeviceServerServiceProcImpl deviceServerServiceProc = new DeviceServerServiceProcImpl();
	private DeviceCPUServiceProcImpl deviceCPUServiceProc = new DeviceCPUServiceProcImpl();
	private DeviceMemoryServiceProcImpl deviceMemoryServiceProc = new DeviceMemoryServiceProcImpl();
	private DeviceDiskServiceProcImpl deviceDiskServiceProc = new DeviceDiskServiceProcImpl();
	private DeviceVMServiceProcImpl deviceVMServiceProc = new DeviceVMServiceProcImpl();
	private DeviceBWServiceProcImpl deviceBWServiceProc = new DeviceBWServiceProcImpl();

  private static void randomDelay( ) {
    try {
      Thread.sleep( 200 + RANDOM.nextInt( 800 ) );
    } catch ( Exception e ) { }
  }
  
  public void verifySession( Session session ) throws EucalyptusServiceException {
    WebSession ws = WebSessionManager.getInstance( ).getSession( session.getId( ) );
    if ( ws == null ) {
      throw new EucalyptusServiceException( EucalyptusServiceException.INVALID_SESSION );
    }
  }
  
  @Override
  public Session login( String accountName, String userName, String password ) throws EucalyptusServiceException {
    // Simple thwart to automatic login attack.
    randomDelay( );
    if ( Strings.isNullOrEmpty( accountName) || 
    		Strings.isNullOrEmpty( userName ) || 
    		Strings.isNullOrEmpty( password ) ) {
      throw new EucalyptusServiceException( "Empty login or password" );
    }
    
    return authenticateUserLogin.checkPwdAndUserState(accountName, userName, password);
  }
  
  @Override
  public void checkUserExisted(String accountName, String userName)
  		throws EucalyptusServiceException {
  	// TODO Auto-generated method stub
	  if ( Strings.isNullOrEmpty( accountName) || 
	    		Strings.isNullOrEmpty( userName ) ) {
	      throw new EucalyptusServiceException( "Empty user name" );
	    }
	    authenticateUserLogin.checkUserExisted( accountName, userName );
  }


  @Override
  public LoginUserProfile getLoginUserProfile( Session session ) throws EucalyptusServiceException {
	  verifySession(session);
	  return LoginUserProfileStorer.instance().get(session.getId());
  }

  @Override
  public HashMap<String, String> getSystemProperties( Session session ) throws EucalyptusServiceException {
	  verifySession(session);
	  
	  HashMap<String, String> props = Maps.newHashMap( );
	  props.put( "version", "Eucalyptus EEE 3.0" );
	  props.put( "search-result-page-size", "5" );
	  return props;
  }
  
  @Override
  public void logout( Session session ) throws EucalyptusServiceException {
	  // TODO Auto-generated method stub
	  verifySession(session);
  }

  @Override
  public SearchResult lookupConfiguration( Session session, String search, SearchRange range ) throws EucalyptusServiceException {
	  //TODO Auto-generated method stub
	  verifySession(session);
	  return null;
  }

  @Override
  public void setConfiguration( Session session, SearchResultRow config ) throws EucalyptusServiceException {
    // TODO Auto-generated method stub
	verifySession(session);
  }

  @Override
  public SearchResult lookupVmType( Session session, String query, SearchRange range ) throws EucalyptusServiceException {
    // TODO Auto-generated method stub
	verifySession(session);
	return null;
  }

  @Override
  public void setVmType( Session session, SearchResultRow result ) throws EucalyptusServiceException {
    // TODO Auto-generated method stub
	verifySession(session);
  }

  @Override
  public SearchResult lookupGroup( Session session, String search, SearchRange range ) throws EucalyptusServiceException {
	  // TODO Auto-generated method stub
	  verifySession(session);
	  return groupServiceProc.lookupGroup(session, search, range);
  }
  

  @Override
  public SearchResult lookupPolicy( Session session, String search, SearchRange range ) throws EucalyptusServiceException {
    // TODO Auto-generated method stub
	  verifySession(session);
	  return null;
  }

  @Override
  public SearchResult lookupKey( Session session, String search, SearchRange range ) throws EucalyptusServiceException {
    // TODO Auto-generated method stub
	  verifySession(session);
	  return null;
  }

  @Override
  public SearchResult lookupCertificate( Session session, String search, SearchRange range ) throws EucalyptusServiceException {
    // TODO Auto-generated method stub
	  verifySession(session);
	  return null;
  }

  @Override
  public SearchResult lookupImage( Session session, String search, SearchRange range ) throws EucalyptusServiceException {
    // TODO Auto-generated method stub
	  verifySession(session);
	  return null;
  }


  /**
   * account service 
   * 
   * 
   */

	public String createAccount(Session session, ArrayList<String> values) throws EucalyptusServiceException {
		// TODO Auto-generated method stub
		verifySession(session);
		
		LoginUserProfile curUser = LoginUserProfileStorer.instance().get(session.getId());
		boolean isRootAdmin = curUser.isSystemAdmin();
  
		if (!isRootAdmin) {
			throw new EucalyptusServiceException("No permission");
		}
		
		String name = values.get(0);
		String email = values.get(1);
		String description = values.get(2);
		
		this.accountServiceProc.createAccount(name, email, description);
		
		return name;
	}
	@Override
	public SearchResult lookupAccount( Session session, String search, SearchRange range ) throws EucalyptusServiceException {
		verifySession(session);
	  
		LoginUserProfile curUser = LoginUserProfileStorer.instance().get(session.getId());
		boolean isRootAdmin = curUser.isSystemAdmin();

		if (!isRootAdmin) {
			throw new EucalyptusServiceException("No permission");
		}
		
		System.out.println( "New search: " + range );
    
		return this.accountServiceProc.lookupAccount(search, range);
  }
  @Override
  public void deleteAccounts( Session session, ArrayList<String> ids ) throws EucalyptusServiceException {
	  // TODO Auto-generated method stub
	  verifySession(session);
	  LoginUserProfile curUser = LoginUserProfileStorer.instance().get(session.getId());
	  boolean isRootAdmin = curUser.isSystemAdmin();
	  if (!isRootAdmin) {
		  throw new EucalyptusServiceException("No permission");
	  }
		
	  this.accountServiceProc.deleteAccounts(ids);
  }
  @Override
  public void modifyAccount( Session session, int accountId, String name, String email ) throws EucalyptusServiceException {
	  // TODO Auto-generated method stub
	  verifySession(session);
	  
	  LoginUserProfile curUser = LoginUserProfileStorer.instance().get(session.getId());
	  boolean isRootAdmin = curUser.isSystemAdmin();
	  if (!isRootAdmin) {
		  throw new EucalyptusServiceException("No permission");
	  }
	  
	  this.accountServiceProc.modifyAccount(accountId, name, email);
  }
  @Override
  public void updateAccountState( Session session, ArrayList<String> ids, EnumState state ) throws EucalyptusServiceException {
	  // TODO Auto-generated method stub
	  verifySession(session);
	  
	  LoginUserProfile curUser = LoginUserProfileStorer.instance().get(session.getId());
	  boolean isRootAdmin = curUser.isSystemAdmin();
	  if (!isRootAdmin) {
		  throw new EucalyptusServiceException("No permission");
	  }
	  
	  accountServiceProc.updateAccountState(ids, state);
  }
  @Override
  public ArrayList<AccountInfo> listAccounts(Session session) throws EucalyptusServiceException {
	  // TODO Auto-generated method stub
	  verifySession(session);
	  
	  LoginUserProfile curUser = LoginUserProfileStorer.instance().get(session.getId());
	  boolean isRootAdmin = curUser.isSystemAdmin();
	  if (!isRootAdmin) {
		  throw new EucalyptusServiceException("No permission");
	  }
	  
	  return accountServiceProc.listAccounts();
  }

  /**
   * user service
   * 
   * 
   */
	@Override
	public ArrayList<String> createUsers( Session session, String accountId, String names, String path ) throws EucalyptusServiceException {
		// TODO Auto-generated method stub
		verifySession(session);
		return null;
	}
  
	@Override
	public void createUser(Session session, UserInfo user) throws EucalyptusServiceException {
		// TODO Auto-generated method stub
		verifySession(session);
		LoginUserProfile curUser = LoginUserProfileStorer.instance().get(session.getId());
		
		if (!curUser.isSystemAdmin() && !curUser.isAccountAdmin()) {
			throw new EucalyptusServiceException("No permission");
		}
		  
		int accountId = curUser.getAccountId();
		userServiceProc.createUser(accountId, user);  
	}
  
	@Override
	public SearchResult lookupUser( Session session, String search, SearchRange range ) throws EucalyptusServiceException {
		// TODO Auto-generated method stub
		verifySession(session);
		LoginUserProfile curUser = LoginUserProfileStorer.instance().get(session.getId());
		return userServiceProc.lookupUser(curUser, search, range);
	}
	
  @Override
  public SearchResult lookupUserByGroupId( Session session, int groupId, SearchRange range ) throws EucalyptusServiceException {
	  verifySession(session);
	  LoginUserProfile curUser = LoginUserProfileStorer.instance().get(session.getId());
	  return userServiceProc.lookupUserByGroupId(curUser, groupId, range);
  }
  @Override
  public SearchResult lookupUserByAccountId( Session session, int accountId, SearchRange range ) throws EucalyptusServiceException {
	  verifySession(session);
	  return userServiceProc.lookupUserByAccountId(accountId, range);
  }
  @Override
  public SearchResult lookupUserExcludeGroupId( Session session, int accountId, int groupId, SearchRange range ) throws EucalyptusServiceException {
	  verifySession(session);
	  return userServiceProc.lookupUserExcludeGroupId(accountId, groupId, range);
  }
  @Override
  public void deleteUsers( Session session, ArrayList<String> ids ) throws EucalyptusServiceException {
    // TODO Auto-generated method stub
	  verifySession(session);
	  userServiceProc.deleteUsers(ids); 
  }
  @Override
  public void updateUserState(Session session, ArrayList<String> ids,
  		EnumState userState) throws EucalyptusServiceException {
  	// TODO Auto-generated method stub
	  verifySession(session);
	  userServiceProc.updateUserState(ids, userState);
  }
  @Override
  public void addUsersToGroupsById( Session session, ArrayList<String> userIds, int groupId ) throws EucalyptusServiceException {
    // TODO Auto-generated method stub
	  verifySession(session);
	  userServiceProc.addUsersToGroupsById(userIds, groupId);
  }
  @Override
  public void removeUsersFromGroup( Session session, ArrayList<String> userIds ) throws EucalyptusServiceException {
    // TODO Auto-generated method stub
	  verifySession(session);
	  userServiceProc.addUsersToGroupsById(userIds, 0);
  }
  
  @Override
  public void modifyUser( Session session, ArrayList<String> keys, ArrayList<String> values ) throws EucalyptusServiceException {
    // TODO Auto-generated method stub
	  verifySession(session);
  }
  
  @Override
  public LoginUserProfile modifyIndividual( Session session, String title, String mobile, String email ) throws EucalyptusServiceException {
	  // TODO Auto-generated method stub
	  verifySession(session);
	  LoginUserProfile curUser = LoginUserProfileStorer.instance().get(session.getId());
	  userServiceProc.modifyIndividual(curUser, title, mobile, email);
	  
	  curUser.setUserTitle(title);
	  curUser.setUserMobile(mobile);
	  curUser.setUserEmail(email);
	  
	  return curUser;
  }
  
  @Override
  public void changePassword( Session session, String oldPass, String newPass, String email ) throws EucalyptusServiceException {
    // TODO Auto-generated method stub
	  verifySession(session);
	  LoginUserProfile curUser = LoginUserProfileStorer.instance().get(session.getId());
	  userServiceProc.changePassword(curUser, oldPass, newPass, email);
  }

  /**
   * Group service
   * 
   * 
   */
  @Override
  public void createGroup(Session session, GroupInfo group)
  		throws EucalyptusServiceException {
	  // TODO Auto-generated method stub
	  verifySession(session);
	  groupServiceProc.createGroup(session, group);
  }
  
  @Override
  public ArrayList<String> createGroups( Session session, String accountId, String names, String path ) throws EucalyptusServiceException {
    // TODO Auto-generated method stub
    return null;
  }

	@Override
	public void deleteGroups( Session session, ArrayList<String> ids ) throws EucalyptusServiceException {
	  // TODO Auto-generated method stub
		verifySession(session);
	  groupServiceProc.deleteGroups(session, ids);
	}

	@Override
	public void modifyGroup( Session session, ArrayList<String> values ) throws EucalyptusServiceException {
		// TODO Auto-generated method stub
		verifySession(session);
	}

	@Override
	public ArrayList<GroupInfo> listGroups(Session session)	throws EucalyptusServiceException {
		// TODO Auto-generated method stub
		verifySession(session);
		return groupServiceProc.listGroups(session);
	}
	@Override
	public void updateGroupState( Session session, ArrayList<String> ids, EnumState state ) throws EucalyptusServiceException {
		// TODO Auto-generated method stub
		verifySession(session);
		groupServiceProc.updateGroupState(session, ids, state);
	}

  @Override
  public void addAccountPolicy( Session session, String accountId, String name, String document ) throws EucalyptusServiceException {
    // TODO Auto-generated method stub
	  verifySession(session);
  }

  @Override
  public void addUserPolicy( Session session, String usertId, String name, String document ) throws EucalyptusServiceException {
    // TODO Auto-generated method stub
	  verifySession(session);
  }

  @Override
  public void addGroupPolicy( Session session, String groupId, String name, String document ) throws EucalyptusServiceException {
    // TODO Auto-generated method stub
	  verifySession(session);
  }

  @Override
  public void deletePolicy( Session session, SearchResultRow policySerialized ) throws EucalyptusServiceException {
    // TODO Auto-generated method stub
	  verifySession(session);
  }

  @Override
  public void deleteAccessKey( Session session, SearchResultRow keySerialized ) throws EucalyptusServiceException {
    // TODO Auto-generated method stub
	  verifySession(session);
  }

  @Override
  public void deleteCertificate( Session session, SearchResultRow certSerialized ) throws EucalyptusServiceException {
    // TODO Auto-generated method stub
	  verifySession(session);
  }

  @Override
  public void addUsersToGroupsByName( Session session, String userNames, ArrayList<String> groupIds ) throws EucalyptusServiceException {
    // TODO Auto-generated method stub
	  verifySession(session);
  }

  @Override
  public void removeUsersFromGroupsById( Session session, ArrayList<String> userIds, String groupNames ) throws EucalyptusServiceException {
    // TODO Auto-generated method stub
	  verifySession(session);
  }
  
  @Override
  public void modifyAccessKey( Session session, ArrayList<String> values ) throws EucalyptusServiceException {
    // TODO Auto-generated method stub
	  verifySession(session);
  }

  @Override
  public void modifyCertificate( Session session, ArrayList<String> values ) throws EucalyptusServiceException {
    // TODO Auto-generated method stub
	  verifySession(session);
  }

  @Override
  public void addAccessKey( Session session, String userId ) throws EucalyptusServiceException {
    // TODO Auto-generated method stub
	  verifySession(session);
  }

  @Override
  public void addCertificate( Session session, String userId, String pem ) throws EucalyptusServiceException {
    // TODO Auto-generated method stub
	  verifySession(session);
  }

  @Override
  public void signupAccount( String accountName, String password, String email ) throws EucalyptusServiceException {
	  try {
      Thread.sleep( 2000 );
    } catch ( InterruptedException e ) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  @Override
  public void signupUser( String userName, String accountName, String password, String email ) throws EucalyptusServiceException {
    // TODO Auto-generated method stub
  }

  @Override
  public ArrayList<String> approveAccounts( Session session, ArrayList<String> accountNames ) throws EucalyptusServiceException {
    // TODO Auto-generated method stub
	  verifySession(session);
    return null;
  }

  @Override
  public ArrayList<String> rejectAccounts( Session session, ArrayList<String> accountNames ) throws EucalyptusServiceException {
    // TODO Auto-generated method stub
	  verifySession(session);
    return null;
  }

  @Override
  public ArrayList<String> approveUsers( Session session, ArrayList<String> userIds ) throws EucalyptusServiceException {
    // TODO Auto-generated method stub
	  verifySession(session);
    return null;
  }

  @Override
  public ArrayList<String> rejectUsers( Session session, ArrayList<String> userIds ) throws EucalyptusServiceException {
    // TODO Auto-generated method stub
	  verifySession(session);
    return null;
  }

  @Override
  public void confirmUser( String confirmationCode ) throws EucalyptusServiceException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void requestPasswordRecovery( String userName, String accountName, String email ) throws EucalyptusServiceException {
    // TODO Auto-generated method stub
	  PwdResetProc pwdResetProc = new PwdResetProc();
	  pwdResetProc.requestPasswordRecovery(userName, accountName, email);
  }

  @Override
  public void resetPassword( String confirmationCode, String password ) throws EucalyptusServiceException {
    // TODO Auto-generated method stub
	  PwdResetProc pwdResetProc = new PwdResetProc();
	  pwdResetProc.resetPassword(confirmationCode, password);
  }

  @Override
  public CloudInfo getCloudInfo( Session session, boolean setExternalHostPort ) throws EucalyptusServiceException {
	  verifySession(session);
	  CloudInfo cloudInfo = new CloudInfo( );
    cloudInfo.setCloudId( "CLOUDID1234567890" );
    cloudInfo.setExternalHostPort( "127.0.0.1:8443" );
    cloudInfo.setInternalHostPort( "127.0.0.1:8443" );
    cloudInfo.setServicePath( "/Eucalyptus/services" );
    return cloudInfo;
  }

public ArrayList getQuickLinks(Session session)
		throws EucalyptusServiceException {
	// TODO Auto-generated method stub
	verifySession(session);
	LoginUserProfile curUser = LoginUserProfileStorer.instance().get(session.getId());
	return QuickLinks.getTags(curUser.isSystemAdmin(), curUser.getUserType());
}


public String getUserToken(Session session) throws EucalyptusServiceException {
	// TODO Auto-generated method stub
	verifySession(session);
	return null;
}

@Override
public ArrayList<GuideItem> getGuide(Session session, String snippet)
		throws EucalyptusServiceException {
	// TODO Auto-generated method stub
	verifySession(session);
	return null;
}

	@Override
	public SearchResult lookupDeviceServer(Session session, String search, SearchRange range, int queryState)
	        throws EucalyptusServiceException {
		return deviceServerServiceProc.lookupServer(session, search, range, queryState);
	}
	
	@Override
	public SearchResult lookupDeviceMemory(Session session, String search, SearchRange range, int queryState)
	        throws EucalyptusServiceException {
		return deviceMemoryServiceProc.lookupMemory(session, search, range, queryState);
	}
	
	@Override
	public SearchResult lookupDeviceDisk(Session session, String search, SearchRange range, int queryState)
	        throws EucalyptusServiceException {
		return deviceDiskServiceProc.lookupDisk(session, search, range, queryState);
	}
	
	@Override
	public SearchResult lookupDeviceVM(Session session, String search, SearchRange range, int queryState)
	        throws EucalyptusServiceException {
		return deviceVMServiceProc.lookupVM(session, search, range, queryState);
	}
	
	@Override
	public SearchResult lookupDeviceBW(Session session, String search, SearchRange range)
	        throws EucalyptusServiceException {
		return deviceBWServiceProc.lookupBW(session, search, range);
	}
	
	@Override
	public SearchResult lookupDeviceCPU(Session session, String search, SearchRange range, int queryState)
	        throws EucalyptusServiceException {
		return deviceCPUServiceProc.lookup(session, search, range, queryState);
	}
	
	@Override
	public Map<Integer, Integer> queryDeviceCPUCounts(Session session) throws EucalyptusServiceException {
		return deviceCPUServiceProc.queryCounts(session);
	}

	@Override
	public SearchResultRow modifyDeviceCPUService(Session session,
			SearchResultRow row, String endtime, int state) {
		// TODO Auto-generated method stub
		return deviceCPUServiceProc.modifyService(session, row, endtime, state);
	}

	@Override
	public boolean deleteDeviceCPUService(Session session, List<Integer> list) {
		// TODO Auto-generated method stub
		return deviceCPUServiceProc.deleteService(session, list);
	}
}
