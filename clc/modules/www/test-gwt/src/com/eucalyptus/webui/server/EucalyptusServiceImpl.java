package com.eucalyptus.webui.server;

import java.util.ArrayList;
import java.util.Date;
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
import com.eucalyptus.webui.server.device.DeviceAccountService;
import com.eucalyptus.webui.server.device.DeviceAreaService;
import com.eucalyptus.webui.server.device.DeviceBWService;
import com.eucalyptus.webui.server.device.DeviceCPUService;
import com.eucalyptus.webui.server.device.DeviceCabinetService;
import com.eucalyptus.webui.server.device.DeviceDiskService;
import com.eucalyptus.webui.server.device.DeviceIPService;
import com.eucalyptus.webui.server.device.DeviceMemoryService;
import com.eucalyptus.webui.server.device.DeviceDevicePriceService;
import com.eucalyptus.webui.server.device.DeviceRoomService;
import com.eucalyptus.webui.server.device.DeviceServerService;
import com.eucalyptus.webui.server.device.DeviceTemplatePriceService;
import com.eucalyptus.webui.server.device.DeviceTemplateService;
import com.eucalyptus.webui.server.mail.MailSenderInfo;
import com.eucalyptus.webui.server.user.AuthenticateUserLogin;
import com.eucalyptus.webui.server.user.LoginUserProfileStorer;
import com.eucalyptus.webui.server.user.PwdResetProc;
import com.eucalyptus.webui.server.ws.EucaWSAdapter;
import com.eucalyptus.webui.server.ws.EucaWSException;
import com.eucalyptus.webui.shared.config.SysConfig;
import com.eucalyptus.webui.shared.query.QueryType;
import com.eucalyptus.webui.shared.resource.VMImageType;
import com.eucalyptus.webui.shared.resource.device.AreaInfo;
import com.eucalyptus.webui.shared.resource.device.BWServiceInfo;
import com.eucalyptus.webui.shared.resource.device.CPUInfo;
import com.eucalyptus.webui.shared.resource.device.CPUServiceInfo;
import com.eucalyptus.webui.shared.resource.device.CabinetInfo;
import com.eucalyptus.webui.shared.resource.device.DiskInfo;
import com.eucalyptus.webui.shared.resource.device.DiskServiceInfo;
import com.eucalyptus.webui.shared.resource.device.IPServiceInfo;
import com.eucalyptus.webui.shared.resource.device.MemoryInfo;
import com.eucalyptus.webui.shared.resource.device.MemoryServiceInfo;
import com.eucalyptus.webui.shared.resource.device.DevicePriceInfo;
import com.eucalyptus.webui.shared.resource.device.RoomInfo;
import com.eucalyptus.webui.shared.resource.device.ServerInfo;
import com.eucalyptus.webui.shared.resource.device.TemplateInfo;
import com.eucalyptus.webui.shared.resource.device.TemplatePriceInfo;
import com.eucalyptus.webui.shared.resource.device.status.CPUState;
import com.eucalyptus.webui.shared.resource.device.status.DiskState;
import com.eucalyptus.webui.shared.resource.device.status.IPState;
import com.eucalyptus.webui.shared.resource.device.status.IPType;
import com.eucalyptus.webui.shared.resource.device.status.MemoryState;
import com.eucalyptus.webui.shared.resource.device.status.ServerState;
import com.eucalyptus.webui.shared.user.AccountInfo;
import com.eucalyptus.webui.shared.user.EnumState;
import com.eucalyptus.webui.shared.user.EnumUserAppStatus;
import com.eucalyptus.webui.shared.user.GroupInfo;
import com.eucalyptus.webui.shared.user.LoginUserProfile;
import com.eucalyptus.webui.shared.user.UserApp;
import com.eucalyptus.webui.shared.user.UserAppStateCount;
import com.eucalyptus.webui.shared.user.UserInfo;
import com.google.common.base.Strings;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class EucalyptusServiceImpl extends RemoteServiceServlet implements EucalyptusService {

	private static final Logger LOG = Logger.getLogger(EucalyptusServiceImpl.class);
	private static final long serialVersionUID = 1L;
	private static final Random RANDOM = new Random();
	private static AuthenticateUserLogin authenticateUserLogin = new AuthenticateUserLogin();

	private UserKeyServiceProcImpl userKeyServiceProc = new UserKeyServiceProcImpl();
	private CertificateServiceProcImpl certServiceProc = new CertificateServiceProcImpl();
	private PolicyServiceProcImpl policyServiceProc = new PolicyServiceProcImpl();
	
	private HistoryServiceProcImpl historyServiceProc = new HistoryServiceProcImpl();
	
	private AccountServiceProcImpl accountServiceProc = new AccountServiceProcImpl();
	private UserServiceProcImpl userServiceProc = new UserServiceProcImpl();
	private UserAppServiceProcImpl userAppServiceProc = new UserAppServiceProcImpl();
	private GroupServiceProcImpl groupServiceProc = new GroupServiceProcImpl();
	private DeviceVMServiceProcImpl deviceVMServiceProc = new DeviceVMServiceProcImpl();
	private static void randomDelay() {
		try {
			Thread.sleep(200 + RANDOM.nextInt(800));
		}
		catch (Exception e) {
		}
	}

	public void verifySession(Session session) throws EucalyptusServiceException {
		WebSession ws = WebSessionManager.getInstance().getSession(session.getId());
		if (ws == null) {
			throw new EucalyptusServiceException(EucalyptusServiceException.INVALID_SESSION);
		}
	}

	@Override
	public Session login(String accountName, String userName, String password) throws EucalyptusServiceException {
		// Simple thwart to automatic login attack.
		randomDelay();
		if (Strings.isNullOrEmpty(accountName) || Strings.isNullOrEmpty(userName) || Strings.isNullOrEmpty(password)) {
			throw new EucalyptusServiceException("Empty login or password");
		}
		return authenticateUserLogin.checkPwdAndUserState(accountName, userName, password);
	}

	@Override
	public void checkUserExisted(String accountName, String userName) throws EucalyptusServiceException {
		// TODO Auto-generated method stub
		if (Strings.isNullOrEmpty(accountName) || Strings.isNullOrEmpty(userName)) {
			throw new EucalyptusServiceException("Empty user name");
		}
		authenticateUserLogin.checkUserExisted(accountName, userName);
	}

	@Override
	public LoginUserProfile getLoginUserProfile(Session session) throws EucalyptusServiceException {
		verifySession(session);
		return LoginUserProfileStorer.instance().get(session.getId());
	}

	@Override
	public HashMap<String, String> getSystemProperties(Session session) throws EucalyptusServiceException {
		verifySession(session);

		HashMap<String, String> props = new HashMap<String, String>();
		props.put("version", "Eucalyptus EEE 3.0");
		props.put("search-result-page-size", "5");
		return props;
	}

	@Override
	public void logout(Session session) throws EucalyptusServiceException {
		// TODO Auto-generated method stub
		verifySession(session);
	}

	@Override
	public SearchResult lookupConfiguration(Session session, String search, SearchRange range)
	        throws EucalyptusServiceException {
		// TODO Auto-generated method stub
		verifySession(session);
		return null;
	}

	@Override
	public void setConfiguration(Session session, SearchResultRow config) throws EucalyptusServiceException {
		// TODO Auto-generated method stub
		verifySession(session);
	}

	@Override
	public SearchResult lookupVmType(Session session, String query, SearchRange range)
	        throws EucalyptusServiceException {
		// TODO Auto-generated method stub
		verifySession(session);
		return null;
	}

	@Override
	public void setVmType(Session session, SearchResultRow result) throws EucalyptusServiceException {
		// TODO Auto-generated method stub
		verifySession(session);
	}

	@Override
	public SearchResult lookupGroup(Session session, String search, SearchRange range)
	        throws EucalyptusServiceException {
		// TODO Auto-generated method stub
		verifySession(session);
		return groupServiceProc.lookupGroup(session, search, range);
	}

	@Override
	public SearchResult lookupPolicy(Session session, String search, SearchRange range)
	        throws EucalyptusServiceException {
		// TODO Auto-generated method stub
		verifySession(session);
		LoginUserProfile curUser = LoginUserProfileStorer.instance().get(session.getId());
		return policyServiceProc.lookupPolicy(curUser, search, range);
	}

	@Override
	public SearchResult lookupKey(Session session, String search, SearchRange range) throws EucalyptusServiceException {
		// TODO Auto-generated method stub
		verifySession(session);
		LoginUserProfile curUser = LoginUserProfileStorer.instance().get(session.getId());
		return userKeyServiceProc.lookupUserKey(curUser, search, range);
	}

	@Override
	public SearchResult lookupCertificate(Session session, String search, SearchRange range)
	        throws EucalyptusServiceException {
		// TODO Auto-generated method stub
		verifySession(session);
		LoginUserProfile curUser = LoginUserProfileStorer.instance().get(session.getId());
		return certServiceProc.lookupCertificate(curUser, search, range);
	}

	@Override
	public SearchResult lookupImage(Session session, String search, SearchRange range)
	        throws EucalyptusServiceException {
		// TODO Auto-generated method stub
		verifySession(session);
		return null;
	}

	/**
	 * account service
	 * 
	 * 
	 */

	public void createAccount(Session session, AccountInfo account) throws EucalyptusServiceException {
		// TODO Auto-generated method stub
		verifySession(session);

		LoginUserProfile curUser = LoginUserProfileStorer.instance().get(session.getId());
		boolean isRootAdmin = curUser.isSystemAdmin();

		if (!isRootAdmin) {
			throw new EucalyptusServiceException("No permission");
		}
		
		if (account.getId() == 0)
			this.accountServiceProc.createAccount(account, true);
		else
			this.accountServiceProc.modifyAccount(account);
	}

	@Override
	public SearchResult lookupAccount(Session session, String search, SearchRange range)
	        throws EucalyptusServiceException {
		verifySession(session);

		LoginUserProfile curUser = LoginUserProfileStorer.instance().get(session.getId());
		boolean isRootAdmin = curUser.isSystemAdmin();

		if (!isRootAdmin) {
			throw new EucalyptusServiceException("No permission");
		}

		System.out.println("New search: " + range);

		return this.accountServiceProc.lookupAccount(search, range);
	}

	@Override
	public void deleteAccounts(Session session, ArrayList<String> ids) throws EucalyptusServiceException {
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
	public void updateAccountState(Session session, ArrayList<String> ids, EnumState state)
	        throws EucalyptusServiceException {
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
	public ArrayList<String> createUsers(Session session, String accountId, String names, String path)
	        throws EucalyptusServiceException {
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
		if (user.getId() == 0) {
			
			try {
				EucaWSAdapter.instance().createUser(Integer.toString(accountId), user.getName(), "/");
				userServiceProc.createUser(accountId, user);
			} catch (EucaWSException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new EucalyptusServiceException("Failed to create user in Euca");
			}
		}
		else {
			try {
				EucaWSAdapter.instance().updateUser(Integer.toString(accountId), user.getName(), null);
				userServiceProc.modifyUser(user);
			} catch (EucaWSException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new EucalyptusServiceException("Failed to update user in Euca");
			}
		}
	}

	@Override
	public SearchResult lookupUser(Session session, String search, SearchRange range) throws EucalyptusServiceException {
		// TODO Auto-generated method stub
		verifySession(session);
		LoginUserProfile curUser = LoginUserProfileStorer.instance().get(session.getId());
		return userServiceProc.lookupUser(curUser, search, range);
	}
	
	@Override
	public SearchResult lookupUserApp(Session session, String search,
			SearchRange range, EnumUserAppStatus state)
			throws EucalyptusServiceException {
		// TODO Auto-generated method stub
		verifySession(session);
		LoginUserProfile curUser = LoginUserProfileStorer.instance().get(session.getId());
		
		return userAppServiceProc.lookupUserApp(curUser, search, range, state);
	}

	@Override
	public SearchResult lookupUserByGroupId(Session session, int groupId, SearchRange range)
	        throws EucalyptusServiceException {
		verifySession(session);
		LoginUserProfile curUser = LoginUserProfileStorer.instance().get(session.getId());
		return userServiceProc.lookupUserByGroupId(curUser, groupId, range);
	}

	@Override
	public SearchResult lookupUserByAccountId(Session session, int accountId, SearchRange range)
	        throws EucalyptusServiceException {
		verifySession(session);
		return userServiceProc.lookupUserByAccountId(accountId, range);
	}

	@Override
	public SearchResult lookupUserExcludeGroupId(Session session, int accountId, int groupId, SearchRange range)
	        throws EucalyptusServiceException {
		verifySession(session);
		LoginUserProfile curUser = LoginUserProfileStorer.instance().get(session.getId());
		return userServiceProc.lookupUserExcludeGroupId(curUser, accountId, groupId, range);
	}

	@Override
	public void deleteUsers(Session session, ArrayList<String> ids) throws EucalyptusServiceException {
		// TODO Auto-generated method stub
		verifySession(session);
		userServiceProc.deleteUsers(ids);
	}

	@Override
	public void updateUserState(Session session, ArrayList<String> ids, EnumState userState)
	        throws EucalyptusServiceException {
		// TODO Auto-generated method stub
		verifySession(session);
		userServiceProc.updateUserState(ids, userState);
	}

	@Override
	public void addUsersToGroupsById(Session session, ArrayList<String> userIds, int groupId)
	        throws EucalyptusServiceException {
		// TODO Auto-generated method stub
		verifySession(session);
		userServiceProc.addUsersToGroupsById(userIds, groupId);
	}

	@Override
	public void removeUsersFromGroup(Session session, ArrayList<String> userIds) throws EucalyptusServiceException {
		// TODO Auto-generated method stub
		verifySession(session);
		userServiceProc.addUsersToGroupsById(userIds, 0);
	}

	@Override
	public void modifyUser(Session session, ArrayList<String> keys, ArrayList<String> values)
	        throws EucalyptusServiceException {
		// TODO Auto-generated method stub
		verifySession(session);
	}

	@Override
	public LoginUserProfile modifyIndividual(Session session, String title, String mobile, String email)
	        throws EucalyptusServiceException {
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
	public void changePassword(Session session, String oldPass, String newPass, String email)
	        throws EucalyptusServiceException {
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
	public void createGroup(Session session, GroupInfo group) throws EucalyptusServiceException {
		// TODO Auto-generated method stub
		verifySession(session);
		if (group.getId() == 0) {
			LoginUserProfile curUser = LoginUserProfileStorer.instance().get(session.getId());
			groupServiceProc.createGroup(curUser.getAccountId(), group);
		}
		else
			groupServiceProc.updateGroup(group);
	}

	@Override
	public ArrayList<String> createGroups(Session session, String accountId, String names, String path)
	        throws EucalyptusServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deleteGroups(Session session, ArrayList<String> ids) throws EucalyptusServiceException {
		// TODO Auto-generated method stub
		verifySession(session);
		groupServiceProc.deleteGroups(session, ids);
	}

	@Override
	public void modifyGroup(Session session, ArrayList<String> values) throws EucalyptusServiceException {
		// TODO Auto-generated method stub
		verifySession(session);
	}

	@Override
	public ArrayList<GroupInfo> listGroups(Session session) throws EucalyptusServiceException {
		// TODO Auto-generated method stub
		verifySession(session);
		return groupServiceProc.listGroups(session);
	}

	@Override
	public void updateGroupState(Session session, ArrayList<String> ids, EnumState state)
	        throws EucalyptusServiceException {
		// TODO Auto-generated method stub
		verifySession(session);
		groupServiceProc.updateGroupState(session, ids, state);
	}

	@Override
	public void addAccountPolicy(Session session, String accountId, String name, String document)
	        throws EucalyptusServiceException {
		verifySession(session);
		policyServiceProc.addAccountPolicy(accountId, name, document);
	}

	@Override
	public void addUserPolicy(Session session, String userId, String name, String document)
	        throws EucalyptusServiceException {
		verifySession(session);
		policyServiceProc.addUserPolicy(userId, name, document);
	}

	@Override
	public void addGroupPolicy(Session session, String groupId, String name, String document)
	        throws EucalyptusServiceException {
		verifySession(session);
		policyServiceProc.addGroupPolicy(groupId, name, document);
	}

	@Override
	public void deletePolicy(Session session, ArrayList<String> ids) throws EucalyptusServiceException {
		verifySession(session);
		policyServiceProc.deletePolicy(ids);
	}

	@Override
	public void deleteAccessKey(Session session, ArrayList<String> ids) throws EucalyptusServiceException {
		verifySession(session);
		//authServiceProc.deleteAccessKey(session, keySerialized);
		userKeyServiceProc.deleteUserKeys(ids);
	}

	@Override
	public void deleteCertificate(Session session, ArrayList<String> ids) throws EucalyptusServiceException {
		verifySession(session);
		certServiceProc.deleteCertification(ids);
	}

	@Override
	public void addUsersToGroupsByName(Session session, String userNames, ArrayList<String> groupIds)
	        throws EucalyptusServiceException {
		// TODO Auto-generated method stub
		verifySession(session);
	}

	@Override
	public void removeUsersFromGroupsById(Session session, ArrayList<String> userIds, String groupNames)
	        throws EucalyptusServiceException {
		// TODO Auto-generated method stub
		verifySession(session);
	}

	@Override
	public void modifyAccessKey(Session session, ArrayList<String> ids, boolean active) throws EucalyptusServiceException {
		// TODO Auto-generated method stub
		verifySession(session);
		userKeyServiceProc.modifyUserKey(ids, active);
	}

	@Override
	public void modifyCertificate(Session session, ArrayList<String> ids, Boolean active, Boolean revoked) throws EucalyptusServiceException {
		// TODO Auto-generated method stub
		verifySession(session);
		certServiceProc.modifiCertificate(ids, active, revoked);
	}

	@Override
	public void addAccessKey(Session session, String userId) throws EucalyptusServiceException {
		verifySession(session);
		//authServiceProc.addAccessKey(session, userId);
		userKeyServiceProc.addAccessKey(Integer.parseInt(userId));
	}

	@Override
	public void addCertificate(Session session, String userId, String pem) throws EucalyptusServiceException {
		verifySession(session);
		certServiceProc.addCertificate(userId, pem);
	}

	@Override
	public void signupAccount(String accountName, String password, String email) throws EucalyptusServiceException {
		randomDelay( );
		AccountInfo account = new AccountInfo();
		account.setName(accountName);
		account.setEmail(email);
		account.setState(EnumState.NORMAL);
		this.accountServiceProc.createAccount(account, false);
		
		notifyAccountRegisteration( account, ServletUtils.getRequestUrl( getThreadLocalRequest( ) ) );
	}
	
	public static final String ACCOUNT = "account";
	  public static final String USER = "user";
	  public static final String GROUP = "group";
	  public static final String PASSWORD = "password";
	  
	  private void notifyAccountRegisteration(AccountInfo account, String backendUrl) {
		try {  
		  String from = MailSenderInfo.instance().getUser();
		  String to = account.getEmail();
		  
		  String accountName = account.getName();
		  String email = account.getEmail();
		  String userName = "admin";
		  
		  String subject = WebProperties.getProperty( WebProperties.ACCOUNT_SIGNUP_SUBJECT, WebProperties.ACCOUNT_SIGNUP_SUBJECT_DEFAULT );
	      String approveUrl = QueryBuilder.get( ).start( QueryType.approve ).add( ACCOUNT, accountName ).url( backendUrl );
	      String rejectUrl = QueryBuilder.get( ).start( QueryType.reject ).add( ACCOUNT, accountName ).url( backendUrl );
	      String emailMessage =
	    		  userName + " has requested an account on the Eucalyptus system\n" +
	        "\n   Account name:  " + accountName +
	        "\n   Email address: " + email +
	        "\n\n" +
	        "To APPROVE this request, click on the following link:\n\n   " +
	        approveUrl +
	        "\n\n" +
	        "To REJECT this request, click on the following link:\n\n   " +
	        rejectUrl +
	        "\n\n";
	      ServletUtils.sendMail( from, to, subject + " (" + accountName + ", " + email + ")", emailMessage);
	      
	  } catch ( Exception e ) {
	      LOG.error( "Failed to send account signup email", e );
	      LOG.debug( e, e );
	    }
	  }

	@Override
	public void signupUser(String userName, String accountName, String password, String email)
	        throws EucalyptusServiceException {
		// TODO Auto-generated method stub
	}

	@Override
	public ArrayList<String> approveAccounts(Session session, ArrayList<String> accountNames)
	        throws EucalyptusServiceException {
		// TODO Auto-generated method stub
		verifySession(session);
		return null;
	}

	@Override
	public ArrayList<String> rejectAccounts(Session session, ArrayList<String> accountNames)
	        throws EucalyptusServiceException {
		// TODO Auto-generated method stub
		verifySession(session);
		return null;
	}

	@Override
	public ArrayList<String> approveUsers(Session session, ArrayList<String> userIds) throws EucalyptusServiceException {
		// TODO Auto-generated method stub
		verifySession(session);
		return null;
	}

	@Override
	public ArrayList<String> rejectUsers(Session session, ArrayList<String> userIds) throws EucalyptusServiceException {
		// TODO Auto-generated method stub
		verifySession(session);
		return null;
	}

	@Override
	public void confirmUser(String confirmationCode) throws EucalyptusServiceException {
		// TODO Auto-generated method stub

	}

	@Override
	public void requestPasswordRecovery(String userName, String accountName, String email)
	        throws EucalyptusServiceException {
		// TODO Auto-generated method stub
		PwdResetProc pwdResetProc = new PwdResetProc();
		pwdResetProc.requestPasswordRecovery(userName, accountName, email);
	}

	@Override
	public void resetPassword(String confirmationCode, String password) throws EucalyptusServiceException {
		// TODO Auto-generated method stub
		PwdResetProc pwdResetProc = new PwdResetProc();
		pwdResetProc.resetPassword(confirmationCode, password);
	}

	@Override
	public CloudInfo getCloudInfo(Session session, boolean setExternalHostPort) throws EucalyptusServiceException {
		verifySession(session);
		CloudInfo cloudInfo = new CloudInfo();
		cloudInfo.setCloudId("CLOUDID1234567890");
		cloudInfo.setExternalHostPort("127.0.0.1:8443");
		cloudInfo.setInternalHostPort("127.0.0.1:8443");
		cloudInfo.setServicePath("/Eucalyptus/services");
		return cloudInfo;
	}

	public ArrayList getQuickLinks(Session session) throws EucalyptusServiceException {
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
	public ArrayList<GuideItem> getGuide(Session session, String snippet) throws EucalyptusServiceException {
		// TODO Auto-generated method stub
		verifySession(session);
		return null;
	}
	
	@Override
	public Map<String, Integer> lookupDeviceAccountNames(Session session) throws EucalyptusServiceException {
	    return DeviceAccountService.getInstance().lookupAccountNames();
	}
	
	@Override
	public Map<String, Integer> lookupDeviceUserNamesByAccountID(Session session, int account_id) throws EucalyptusServiceException {
	    return DeviceAccountService.getInstance().lookupUserNamesByAccountID(account_id);
	}
	
	@Override
	public SearchResult lookupDeviceAreaByDate(Session session, SearchRange range, Date dateBegin, Date dateEnd) throws EucalyptusServiceException {
		return DeviceAreaService.getInstance().lookupAreaByDate(session, range, dateBegin, dateEnd);
	}
	
	@Override
	public void createDeviceArea(Session session, String area_name, String area_desc) throws EucalyptusServiceException {
	    DeviceAreaService.getInstance().createArea(false, session, area_name, area_desc);
	}
	
	@Override
	public void modifyDeviceArea(Session session, int area_id, String area_desc) throws EucalyptusServiceException {
	    DeviceAreaService.getInstance().modifyArea(false, session, area_id, area_desc);
	}

	@Override
	public void deleteDeviceArea(Session session, List<Integer> area_ids) throws EucalyptusServiceException {
	    DeviceAreaService.getInstance().deleteArea(false, session, area_ids);
	}
	
	@Override
	public Map<String, Integer> lookupDeviceAreaNames(Session session) throws EucalyptusServiceException {
	    return DeviceAreaService.getInstance().lookupAreaNames();
	}
	
	@Override
	public AreaInfo lookupDeviceAreaByID(Session session, int area_id) throws EucalyptusServiceException {
	    return DeviceAreaService.getInstance().lookupAreaByID(area_id);
	}
	
	@Override
	public SearchResult lookupDeviceRoomByDate(Session session, SearchRange range, Date dateBegin, Date dateEnd) throws EucalyptusServiceException {
		return DeviceRoomService.getInstance().lookupRoomByDate(session, range, dateBegin, dateEnd);
	}
	
	@Override
	public void createDeviceRoom(Session session, String room_name, String room_desc, int area_id) throws EucalyptusServiceException {
	    DeviceRoomService.getInstance().createRoom(false, session, room_name, room_desc, area_id);
	}
	
	@Override
	public void modifyDeviceRoom(Session session, int room_id, String room_desc) throws EucalyptusServiceException {
	    DeviceRoomService.getInstance().modifyRoom(false, session, room_id, room_desc);
	}
	
	@Override
	public void deleteDeviceRoom(Session session, List<Integer> room_ids) throws EucalyptusServiceException {
	    DeviceRoomService.getInstance().deleteRoom(false, session, room_ids);
	}
	
	@Override
	public Map<String, Integer> lookupDeviceRoomNamesByAreaID(Session session, int area_id) throws EucalyptusServiceException {
	    return DeviceRoomService.getInstance().lookupRoomNamesByAreaID(area_id);
	}
	
	@Override
	public RoomInfo lookupDeviceRoomByID(Session session, int room_id) throws EucalyptusServiceException {
	    return DeviceRoomService.getInstance().lookupRoomByID(room_id);
	}
	
	@Override
	public SearchResult lookupDeviceCabinetByDate(Session session, SearchRange range, Date dateBegin, Date dateEnd) throws EucalyptusServiceException {
		return DeviceCabinetService.getInstance().lookupCabinetByDate(session, range, dateBegin, dateEnd);
	}
	
	@Override
	public void createDeviceCabinet(Session session, String cabinet_name, String cabinet_desc, int room_id) throws EucalyptusServiceException {
	    DeviceCabinetService.getInstance().createCabinet(false, session, cabinet_name, cabinet_desc, room_id);
	}
	
	@Override
	public void modifyDeviceCabinet(Session session, int cabinet_id, String cabinet_desc) throws EucalyptusServiceException {
	    DeviceCabinetService.getInstance().modifyCabinet(false, session, cabinet_id, cabinet_desc);
	}

	@Override
	public void deleteDeviceCabinet(Session session, List<Integer> cabinet_ids) throws EucalyptusServiceException {
	    DeviceCabinetService.getInstance().deleteCabinet(false, session, cabinet_ids);
	}
	
	@Override
	public Map<String, Integer> lookupDeviceCabinetNamesByRoomID(Session session, int room_id) throws EucalyptusServiceException {
	    return DeviceCabinetService.getInstance().lookupCabinetNamesByRoomID(room_id);
	}
	
	@Override
	public CabinetInfo lookupDeviceCabinetByID(Session session, int cabinet_id) throws EucalyptusServiceException {
	    return DeviceCabinetService.getInstance().lookupCabinetByID(cabinet_id);
	}
	
	@Override
	public  SearchResult lookupDeviceServerByDate(Session session, SearchRange range, ServerState server_state, Date dateBegin, Date dateEnd) throws EucalyptusServiceException {
	    return DeviceServerService.getInstance().lookupServerByDate(session, range, server_state, dateBegin, dateEnd);
	}
	
    @Override
    public Map<Integer, Integer> lookupDeviceServerCounts(Session session) throws EucalyptusServiceException {
        return DeviceServerService.getInstance().lookupServerCountsByState(session);
    }
   
    @Override
    public void createDeviceServer(Session session, String server_name, String server_desc, String server_ip, int server_bw, ServerState server_state, int cabinet_id) throws EucalyptusServiceException {
        DeviceServerService.getInstance().createServer(false, session, server_name, server_desc, server_ip, server_bw, server_state, cabinet_id);
    }
    
    @Override
    public void modifyDeviceServer(Session session, int server_id, String server_desc, String server_ip, int server_bw) throws EucalyptusServiceException {
        DeviceServerService.getInstance().modifyServer(false, session, server_id, server_desc, server_ip, server_bw);
    }
    
    @Override
    public void modifyDeviceServerState(Session session, int server_id, ServerState server_state) throws EucalyptusServiceException {
        DeviceServerService.getInstance().modifyServerState(false, session, server_id, server_state);
    }
    
    @Override
    public void deleteDeviceServer(Session session, List<Integer> server_ids) throws EucalyptusServiceException {
        DeviceServerService.getInstance().deleteServer(false, session, server_ids);
    }
    
    @Override
    public Map<String, Integer> lookupDeviceServerNamesByCabinetID(Session session, int cabinet_id) throws EucalyptusServiceException {
        return DeviceServerService.getInstance().lookupServerNamesByCabinetID(cabinet_id);
    }
    
    @Override
    public ServerInfo lookupDeviceServerByID(Session session, int server_id) throws EucalyptusServiceException {
        return DeviceServerService.getInstance().lookupServerInfoByID(server_id);
    }
    
    @Override
    public SearchResult lookupDeviceCPUByDate(Session session, SearchRange range, CPUState cs_state, Date dateBegin, Date dateEnd) throws EucalyptusServiceException {
        return DeviceCPUService.getInstance().lookupCPUByDate(session, range, cs_state, dateBegin, dateEnd);
    }
    
    @Override
    public Map<Integer, Integer> lookupDeviceCPUCounts(Session session) throws EucalyptusServiceException {
    	return DeviceCPUService.getInstance().lookupCPUCounts(session);
    }
    
    @Override
    public void createDeviceCPU(Session session, String cpu_name, String cpu_desc, int cpu_total, int server_id) throws EucalyptusServiceException {
    	DeviceCPUService.getInstance().createCPU(false, session, cpu_name, cpu_desc, cpu_total, server_id);
    }
    
    @Override
    public void modifyDeviceCPU(Session session, int cpu_id, String cpu_desc, int cpu_total) throws EucalyptusServiceException {
    	DeviceCPUService.getInstance().modifyCPU(false, session, cpu_id, cpu_desc, cpu_total);
    }
    
    @Override
    public void deleteDeviceCPU(Session session, List<Integer> cpu_ids) throws EucalyptusServiceException {
    	DeviceCPUService.getInstance().deleteCPU(false, session, cpu_ids);
    }
    
    @Override
    public CPUInfo lookupDeviceCPUByID(Session session, int cpu_id) throws EucalyptusServiceException {
    	return DeviceCPUService.getInstance().lookupCPUInfoByID(cpu_id);
    }
    
    @Override
    public SearchResult lookupDeviceMemoryByDate(Session session, SearchRange range, MemoryState memory_state, Date dateBegin, Date dateEnd) throws EucalyptusServiceException {
        return DeviceMemoryService.getInstance().lookupMemoryByDate(session, range, memory_state, dateBegin, dateEnd);
    }
    
    @Override
    public Map<Integer, Long> lookupDeviceMemoryCounts(Session session) throws EucalyptusServiceException {
    	return DeviceMemoryService.getInstance().lookupMemoryCounts(session);
    }
    
    @Override
    public void createDeviceMemory(Session session, String mem_name, String mem_desc, long mem_size, int server_id) throws EucalyptusServiceException {
    	DeviceMemoryService.getInstance().createMemory(false, session, mem_name, mem_desc, mem_size, server_id);
    }
    
    @Override
    public void modifyDeviceMemory(Session session, int mem_id, String mem_desc, long mem_size) throws EucalyptusServiceException {
    	DeviceMemoryService.getInstance().modifyMemory(false, session, mem_id, mem_desc, mem_size);
    }
    
    @Override
    public void deleteDeviceMemory(Session session, List<Integer> mem_ids) throws EucalyptusServiceException {
    	DeviceMemoryService.getInstance().deleteMemory(false, session, mem_ids);
    }
    
    @Override
    public MemoryInfo lookupDeviceMemoryByID(Session session, int mem_id) throws EucalyptusServiceException {
    	return DeviceMemoryService.getInstance().lookupMemoryInfoByID(mem_id);
    }
    
    @Override
    public SearchResult lookupDeviceDiskByDate(Session session, SearchRange range, DiskState disk_state, Date dateBegin, Date dateEnd) throws EucalyptusServiceException {
        return DeviceDiskService.getInstance().lookupDiskByDate(session, range, disk_state, dateBegin, dateEnd);
    }
    
    @Override
    public Map<Integer, Long> lookupDeviceDiskCounts(Session session) throws EucalyptusServiceException {
        return DeviceDiskService.getInstance().lookupDiskCounts(session);
    }
    
    @Override
    public void createDeviceDisk(Session session, String disk_name, String disk_desc, long disk_size, int server_id) throws EucalyptusServiceException {
        DeviceDiskService.getInstance().createDisk(false, session, disk_name, disk_desc, disk_size, server_id);
    }
    
    @Override
    public void modifyDeviceDisk(Session session, int disk_id, String disk_desc, long disk_size) throws EucalyptusServiceException {
        DeviceDiskService.getInstance().modifyDisk(false, session, disk_id, disk_desc, disk_size);
    }
    
    @Override
    public void deleteDeviceDisk(Session session, List<Integer> disk_ids) throws EucalyptusServiceException {
        DeviceDiskService.getInstance().deleteDisk(false, session, disk_ids);
    }
    
    @Override
    public DiskInfo lookupDeviceDiskByID(Session session, int disk_id) throws EucalyptusServiceException {
        return DeviceDiskService.getInstance().lookupDiskInfoByID(disk_id);
    }
    
    @Override
    public SearchResult lookupDeviceIPByDate(Session session, SearchRange range, IPType ip_type, IPState is_state, Date dateBegin, Date dateEnd) throws EucalyptusServiceException {
    	return DeviceIPService.getInstance().lookupIPByDate(session, range, ip_type, is_state, dateBegin, dateEnd);
    }
    
    @Override
    public Map<Integer, Integer> lookupDeviceIPCounts(Session session, IPType ip_type) throws EucalyptusServiceException {
    	return DeviceIPService.getInstance().lookupIPCountsByState(session, ip_type);
    }
    
    @Override
    public void createDeviceIP(Session session, String ip_addr, String ip_desc, IPType ip_type) throws EucalyptusServiceException {
    	DeviceIPService.getInstance().createIP(false, session, ip_addr, ip_desc, ip_type);
    }
    
    @Override
    public void createDeviceIPService(Session session, String is_desc, IPState is_state, Date is_starttime, Date is_endtime, int ip_id, int user_id) throws EucalyptusServiceException {
    	DeviceIPService.getInstance().createIPService(false, session, is_desc, is_state, is_starttime, is_endtime, ip_id, user_id);
    }
    
    @Override
    public void modifyDeviceIP(Session session, int ip_id, String ip_desc, IPType ip_type) throws EucalyptusServiceException {
    	DeviceIPService.getInstance().modifyIP(false, session, ip_id, ip_desc, ip_type);
    }
    
    @Override
    public void modifyDeviceIPService(Session session, int ip_id, String is_desc, Date is_starttime, Date is_endtime) throws EucalyptusServiceException {
    	DeviceIPService.getInstance().modifyIPService(false, session, ip_id, is_desc, is_starttime, is_endtime);
    }
    
    @Override
    public void deleteDeviceIP(Session session, List<Integer> ip_ids) throws EucalyptusServiceException {
    	DeviceIPService.getInstance().deleteIP(false, session, ip_ids);
    }
    
    @Override
    public void deleteDeviceIPService(Session session, List<Integer> ip_ids) throws EucalyptusServiceException {
    	DeviceIPService.getInstance().deleteIPService(false, session, ip_ids);
    }
    
    @Override
    public Map<String, Integer> lookupDeviceIPUnusedByIPType(Session session, IPType ip_type) throws EucalyptusServiceException {
    	return DeviceIPService.getInstance().lookupIPReservedByIPType(session, ip_type);
    }
    
    @Override
    public IPServiceInfo lookupDeviceIPServiceByID(Session session, int ip_id) throws EucalyptusServiceException {
    	return DeviceIPService.getInstance().lookupIPServiceInfoByID(ip_id);
    }
    
	@Override
    public SearchResult lookupDeviceBWByDate(Session session, SearchRange range, Date dateBegin, Date dateEnd) throws EucalyptusServiceException {
		return DeviceBWService.getInstance().lookupBWServiceByDate(session, range, dateBegin, dateEnd);
    }
	
	@Override
    public void createDeviceBWService(Session session, String bs_desc, int bs_bw_max, Date bs_starttime, Date bs_endtime, int ip_id) throws EucalyptusServiceException {
		DeviceBWService.getInstance().createBWService(false, session, bs_desc, bs_bw_max, bs_starttime, bs_endtime, ip_id);
    }
	
	@Override
    public void modifyDeviceBWService(Session session, int bs_id, String bs_desc, int bs_bw_max, Date bs_starttime, Date bs_endtime) throws EucalyptusServiceException {
		DeviceBWService.getInstance().modifyBWService(false, session, bs_id, bs_desc, bs_bw_max, bs_starttime, bs_endtime);
    }
	
	@Override
    public void deleteDeviceBWService(Session session, List<Integer> bs_ids) throws EucalyptusServiceException {
		DeviceBWService.getInstance().deleteBWService(false, session, bs_ids);
    }
	
	@Override
    public BWServiceInfo lookupDeviceBWServiceByID(Session session, int bs_id) throws EucalyptusServiceException {
		return DeviceBWService.getInstance().lookupBWServiceInfoByID(bs_id);
    }
	
	@Override
	public Map<String, Integer> lookupDeviceIPsWihtoutBWService(Session session, int account_id, int user_id) throws EucalyptusServiceException {
		return DeviceBWService.getInstance().lookupIPsWithoutBWService(false, session, null, account_id, user_id);
	}
	
	@Override
	public SearchResult lookupDeviceTemplateByDate(Session session, SearchRange range, Date dateBegin, Date dateEnd) throws EucalyptusServiceException {
		return DeviceTemplateService.getInstance().lookupTemplateByDate(session, range, dateBegin, dateEnd);
	}
	
	@Override
    public void createDeviceTemplateService(Session session, String template_name, String template_desc, int template_ncpus, long template_mem, long template_disk, int template_bw) throws EucalyptusServiceException {
		DeviceTemplateService.getInstance().createTemplate(session, template_name, template_desc, template_ncpus, template_mem, template_disk, template_bw);
    }
	
	@Override
	public void modifyDeviceTemplateService(Session session, int template_id, String template_desc, int template_ncpus, long template_mem, long template_disk, int template_bw) throws EucalyptusServiceException {
		DeviceTemplateService.getInstance().modifyTempalte(session, template_id, template_desc, template_ncpus, template_mem, template_disk, template_bw);
	}
	
	@Override
	public void deleteDeviceTemplateService(Session session, List<Integer> template_ids) throws EucalyptusServiceException {
		DeviceTemplateService.getInstance().deleteTemplate(session, template_ids);
	}
	
	@Override
	public Map<String, Integer> lookupDeviceTemplates(Session session) throws EucalyptusServiceException {
	    return DeviceTemplateService.getInstance().lookupTemplates(session);
	}
	
	@Override
	public TemplateInfo lookupDeviceTemplateInfoByID(Session session, int template_id) throws EucalyptusServiceException {
		return DeviceTemplateService.getInstance().lookupTemplateInfoByID(template_id);
	}
	
	@Override
	public DevicePriceInfo lookupDeviceCPUPrice(Session session) throws EucalyptusServiceException {
	    return DeviceDevicePriceService.getInstance().lookupDevicePriceCPU();
	}
		
	@Override
	public DevicePriceInfo lookupDeviceMemoryPrice(Session session) throws EucalyptusServiceException {
	    return DeviceDevicePriceService.getInstance().lookupDevicePriceMemory();
	}
	
	@Override
    public DevicePriceInfo lookupDeviceDiskPrice(Session session) throws EucalyptusServiceException {
        return DeviceDevicePriceService.getInstance().lookupDevicePriceDisk();
    }
	
	@Override
    public DevicePriceInfo lookupDeviceBWPrice(Session session) throws EucalyptusServiceException {
        return DeviceDevicePriceService.getInstance().lookupDevicePriceBandwidth();
    }
	
	@Override
    public void modifyDeviceCPUPrice(Session session, String op_desc, double op_price) throws EucalyptusServiceException {
        DeviceDevicePriceService.getInstance().modifyDevicePriceCPU(session, op_desc, op_price);
    }
	
	@Override
	public void modifyDeviceMemoryPrice(Session session, String op_desc, double op_price) throws EucalyptusServiceException {
	    DeviceDevicePriceService.getInstance().modifyDevicePriceMemory(session, op_desc, op_price);
	}
	
	@Override
    public void modifyDeviceDiskPrice(Session session, String op_desc, double op_price) throws EucalyptusServiceException {
        DeviceDevicePriceService.getInstance().modifyDevicePriceDisk(session, op_desc, op_price);
    }
	
	@Override
    public void modifyDeviceBWPrice(Session session, String op_desc, double op_price) throws EucalyptusServiceException {
        DeviceDevicePriceService.getInstance().modifyDevicePriceBandwidth(session, op_desc, op_price);
    }
	
	@Override
	public SearchResult lookupDeviceTemplatePriceByDate(Session session, SearchRange range, Date dateBegin, Date dateEnd) throws EucalyptusServiceException {
	    return DeviceTemplatePriceService.getInstance().lookupTemplatePriceByDate(session, range, dateBegin, dateEnd);
	}
	
	@Override
	public void createDeviceTemplatePriceByID(Session session, int template_id, String tp_desc, double tp_cpu, double tp_mem, double tp_disk, double tp_bw) throws EucalyptusServiceException {
	    DeviceTemplatePriceService.getInstance().createTemplatePriceByID(session, template_id, tp_desc, tp_cpu, tp_mem, tp_disk, tp_bw);
    }
	
	@Override
	public void modifyDeviceTemplatePrice(Session session, int tp_id, String tp_desc, double tp_cpu, double tp_mem, double tp_disk, double tp_bw) throws EucalyptusServiceException {
	    DeviceTemplatePriceService.getInstance().modifyTemplatePrice(session, tp_id, tp_desc, tp_cpu, tp_mem, tp_disk, tp_bw);
	}
	
	@Override
	public void deleteDeviceTemplatePrice(Session session, List<Integer> tp_ids) throws EucalyptusServiceException {
	    DeviceTemplatePriceService.getInstance().deleteTemplatePrice(session, tp_ids);
	}
	
	@Override
	public TemplatePriceInfo lookupDeviceTemplatePriceByID(Session session, int tp_id) throws EucalyptusServiceException {
	    return DeviceTemplatePriceService.getInstance().lookupTemplatePriceByID(tp_id);
	}
	
	@Override
	public Map<String, Integer> lookupDeviceTemplatesWithoutPrice(Session session) throws EucalyptusServiceException {
	    return DeviceTemplatePriceService.getInstance().lookupTemplatesWithoutPrice(session);
	}
				
	@Override
	public SearchResult lookupDeviceVM(Session session, String search, SearchRange range, int queryState) throws EucalyptusServiceException {
		return deviceVMServiceProc.lookupVM(session, search, range, queryState);
	}

	@Override
	public void modifyPolicy(Session session, String policyId, String name, String content) throws EucalyptusServiceException {
		verifySession(session);
		policyServiceProc.modifyPolicy(policyId, name, content);
	}

//	@Override
//	public SearchResult listAccessKeysByUser(Session session, String userId)
//			throws EucalyptusServiceException {
//		return authServiceProc.listAccesssKeyByUser(session, userId);
//	}

//	@Override
//	public SearchResult listAccessKeys(Session session)
//			throws EucalyptusServiceException {
//		return authServiceProc.listAccessKeys(session);
//	}

//	@Override
//	public SearchResult listCertificatesByUser(Session session, String userId)
//			throws EucalyptusServiceException {
//		return authServiceProc.listCertificatesByUser(session, userId);
//	}

//	@Override
//	public SearchResult listCertificates(Session session)
//			throws EucalyptusServiceException {
//		return authServiceProc.listCertificates(session);
//	}

//	@Override
//	public SearchResult listPolicies(Session session)
//			throws EucalyptusServiceException {
//		return authServiceProc.listPolicies(session);
//	}

	@Override
	public void addUserApp(Session session, UserApp userApp) throws EucalyptusServiceException {
		// TODO Auto-generated method stub
		verifySession(session);
		userAppServiceProc.addUserApp(session, userApp);
	}

	@Override
	public void deleteUserApp(Session session, ArrayList<String> ids)
			throws EucalyptusServiceException {
		// TODO Auto-generated method stub
		verifySession(session);
		userAppServiceProc.deleteUserApps(ids);
	}
	
	@Override
	//public void modifyUserApp(Session session, ArrayList<UserApp> userApps)	throws EucalyptusServiceException {
	public void confirmUserApp(Session session, List<String> userAppIdList, EnumUserAppStatus userAppStatus) throws EucalyptusServiceException {
		// TODO Auto-generated method stub
		verifySession(session);
		
		for (String userAppId : userAppIdList) {
			String euca_vi_key = null;
			//When approving user application, run relative VM instance
			//we must do it firstly, becuase we need obtain vm instance key by runVMInstance function 
			if (userAppStatus == EnumUserAppStatus.APPROVED) {
				euca_vi_key = userAppServiceProc.runVMInstance(session, Integer.parseInt(userAppId));
			}
			
			UserApp userApp = new UserApp();	
			userApp.setUAId(Integer.parseInt(userAppId));
			userApp.setStatus(userAppStatus);
			userApp.setEucaVMInstanceKey(euca_vi_key);
			  
			userAppServiceProc.updateUserApp(userApp);
		}
	}	
	
	@Override
	public ArrayList<UserAppStateCount> countUserApp(Session session) throws EucalyptusServiceException {
		// TODO Auto-generated method stub
		verifySession(session);
		LoginUserProfile curUser = LoginUserProfileStorer.instance().get(session.getId());
		
		return userAppServiceProc.countUserApp(curUser);
	}

	@Override
	public ArrayList<VMImageType> queryVMImageType(Session session)
			throws EucalyptusServiceException {
		// TODO Auto-generated method stub
		verifySession(session);
		return deviceVMServiceProc.queryVMImageType();
	}

	@Override
	public SearchResult lookupHistory(Session session, String search,
			SearchRange range) throws EucalyptusServiceException {
		verifySession(session);
		LoginUserProfile curUser = LoginUserProfileStorer.instance().get(session.getId());
		return historyServiceProc.lookupHistory(curUser, search, range);
	}

	@Override
	public List<String> queryKeyPair(Session session) throws EucalyptusServiceException {
		// TODO Auto-generated method stub
		verifySession(session);
	  // FIXME userID
		return EucaServiceWrapper.getInstance().getKeypairs(session, 0);
	}

	@Override
	public List<String> querySecurityGroup(Session session) throws EucalyptusServiceException {
		// TODO Auto-generated method stub
		verifySession(session);
  	// FIXME userID
		return EucaServiceWrapper.getInstance().getSecurityGroups(session, 0);
	}
	
	@Override
	public SysConfig readSysConfig() throws EucalyptusServiceException {
		// TODO Auto-generated method stub
		return SysConfProcImpl.instance().getSysConfig();
	}

	@Override
	public List<String> listDeviceVMsByUser(Session session, String account,
			String user) {
		// TODO Auto-generated method stub
		return null;
	}

//	@Override
//	public SearchResult listAccessKeysByUser(Session session, String userId)
//			throws EucalyptusServiceException {
//		return authServiceProc.listAccesssKeyByUser(session, userId);
//	}

//	@Override
//	public SearchResult listAccessKeys(Session session)
//			throws EucalyptusServiceException {
//		return authServiceProc.listAccessKeys(session);
//	}

//	@Override
//	public SearchResult listCertificatesByUser(Session session, String userId)
//			throws EucalyptusServiceException {
//		return authServiceProc.listCertificatesByUser(session, userId);
//	}

//	@Override
//	public SearchResult listCertificates(Session session)
//			throws EucalyptusServiceException {
//		return authServiceProc.listCertificates(session);
//	}

//	@Override
//	public SearchResult listPolicies(Session session)
//			throws EucalyptusServiceException {
//		return authServiceProc.listPolicies(session);
//	}
}
