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
import com.eucalyptus.webui.server.device.DeviceCPUPriceService;
import com.eucalyptus.webui.server.device.DeviceCPUService;
import com.eucalyptus.webui.server.device.DeviceCabinetService;
import com.eucalyptus.webui.server.device.DeviceDiskService;
import com.eucalyptus.webui.server.device.DeviceIPService;
import com.eucalyptus.webui.server.device.DeviceMemoryService;
import com.eucalyptus.webui.server.device.DeviceOthersPriceService;
import com.eucalyptus.webui.server.device.DeviceRoomService;
import com.eucalyptus.webui.server.device.DeviceServerService;
import com.eucalyptus.webui.server.device.DeviceTemplatePriceService;
import com.eucalyptus.webui.server.device.DeviceTemplateService;
import com.eucalyptus.webui.server.mail.MailSenderInfo;
import com.eucalyptus.webui.server.user.AuthenticateUserLogin;
import com.eucalyptus.webui.server.user.LoginUserProfileStorer;
import com.eucalyptus.webui.server.user.PwdResetProc;
import com.eucalyptus.webui.shared.resource.Template;
import com.eucalyptus.webui.shared.query.QueryType;
import com.eucalyptus.webui.shared.resource.VMImageType;
import com.eucalyptus.webui.shared.resource.device.AreaInfo;
import com.eucalyptus.webui.shared.resource.device.CabinetInfo;
import com.eucalyptus.webui.shared.resource.device.RoomInfo;
import com.eucalyptus.webui.shared.resource.device.ServerInfo;
import com.eucalyptus.webui.shared.resource.device.TemplateInfo;
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
	private DeviceCPUPriceService deviceCPUPriceServiceProc = DeviceCPUPriceService.getInstance();
	private DeviceOthersPriceService deviceOthersPriceServiceProc = DeviceOthersPriceService.getInstance();
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

		if (user.getId() == 0) {
			int accountId = curUser.getAccountId();
			userServiceProc.createUser(accountId, user);
		}
		else {
			userServiceProc.modifyUser(user);
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
	public Map<Integer, String> lookupDeviceAccountNames(Session session) throws EucalyptusServiceException {
	    return DeviceAccountService.getInstance().lookupAccountNames(session);
	}
	
	@Override
	public Map<Integer, String> lookupDeviceUserNamesByAccountName(Session session, String account_name) throws EucalyptusServiceException {
	    throw new RuntimeException("not finish yet");
	    //return DeviceAccountService.getInstance().lookupUserNamesByAccountName(session, account_name);
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
	    return DeviceAreaService.getInstance().lookupAreaNames(session);
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
	    return DeviceRoomService.getInstance().lookupRoomNamesByAreaID(session, area_id);
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
	    return DeviceCabinetService.getInstance().lookupCabinetNamesByRoomID(session, room_id);
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
        return DeviceServerService.getInstance().lookupServerNamesByCabinetID(session, cabinet_id);
    }
    
    @Override
    public ServerInfo lookupDeviceServerByID(Session session, int server_id) throws EucalyptusServiceException {
        return DeviceServerService.getInstance().lookupServerInfoByID(server_id);
    }
    
    @Override
    public SearchResult lookupDeviceCPUByDate(Session session, SearchRange range, CPUState cpu_state, Date dateBegin, Date dateEnd) throws EucalyptusServiceException {
        return DeviceCPUService.getInstance().lookupCPUByDate(session, range, cpu_state, dateBegin, dateEnd);
    }
    
    @Override
    public Map<Integer, Integer> lookupDeviceCPUCounts(Session session) throws EucalyptusServiceException {
        // return DeviceCPUService.getInstance().lookupCPUCountsGroupByState(session);
        return null;
    }
    
    @Override
    public void addDeviceCPU(Session session, String cpu_name, String cpu_desc, int cpu_total, String cpu_vendor, String cpu_model, double cpu_ghz, double cpu_cache, String server_name) throws EucalyptusServiceException {
        // DeviceCPUService.getInstance().addCPU(session, cpu_name, cpu_desc, cpu_total, cpu_vendor, cpu_model, cpu_ghz, cpu_cache, server_name);
    }
    
    @Override
    public void addDeviceCPUService(Session session, String cs_desc, int cs_size, Date cs_starttime, Date cs_endtime, int cpu_id, String account_name, String user_name) throws EucalyptusServiceException {
        // DeviceCPUService.getInstance().addCPUService(session, cs_desc, cs_size, cs_starttime, cs_endtime, cpu_id, account_name, user_name);
    }
    
    @Override
    public void modifyDeviceCPU(Session session, int cpu_id, String cpu_desc, int cpu_total, String cpu_vendor, String cpu_model, double cpu_ghz, double cpu_cache) throws EucalyptusServiceException {
        // DeviceCPUService.getInstance().modifyCPU(session, cpu_id, cpu_desc, cpu_total, cpu_vendor, cpu_model, cpu_ghz, cpu_cache);
    }
    
    @Override
    public void modifyDeviceCPUService(Session session, int cs_id, String cs_desc, Date cs_starttime, Date cs_endtime) throws EucalyptusServiceException {
        // DeviceCPUService.getInstance().modifyCPUService(session, cs_id, cs_desc, cs_starttime, cs_endtime);
    }
    
    @Override
    public void deleteDeviceCPU(Session session, List<Integer> cpu_ids) throws EucalyptusServiceException {
        // DeviceCPUService.getInstance().deleteCPU(session, cpu_ids);
    }
    
    @Override
    public void deleteDeviceCPUService(Session session, List<Integer> cs_ids) throws EucalyptusServiceException {
        // DeviceCPUService.getInstance().deleteCPUService(session, cs_ids);
    }
    
    @Override
    public List<String> lookupDeviceCPUNamesByServerName(Session session, String server_name) throws EucalyptusServiceException {
        // return DeviceCPUService.getInstance().lookupCPUNamesByServerName(session, server_name);
        return null;
    }
    
    @Override
    public SearchResult lookupDeviceMemoryByDate(Session session, SearchRange range, MemoryState memory_state, Date dateBegin, Date dateEnd) throws EucalyptusServiceException {
        return DeviceMemoryService.getInstance().lookupMemoryByDate(session, range, memory_state, dateBegin, dateEnd);
    }
    
    @Override
    public Map<Integer, Long> lookupDeviceMemoryCounts(Session session) throws EucalyptusServiceException {
        // return DeviceMemoryService.getInstance().lookupMemoryCountsGroupByState(session);
        return null;
    }
    
    @Override
    public void addDeviceMemory(Session session, String memory_name, String memory_desc, long memory_size, String server_name) throws EucalyptusServiceException {
        // DeviceMemoryService.getInstance().addMemory(session, memory_name, memory_desc, memory_size, server_name);
    }
    
    @Override
    public void addDeviceMemoryService(Session session, String ms_desc, long ms_size, Date ms_starttime, Date ms_endtime, int memory_id, String account_name, String user_name) throws EucalyptusServiceException {
        // DeviceMemoryService.getInstance().addMemoryService(session, ms_desc, ms_size, ms_starttime, ms_endtime, memory_id, account_name, user_name);
    }
    
    @Override
    public void modifyDeviceMemory(Session session, int memory_id, String memory_desc, long memory_size) throws EucalyptusServiceException {
        // DeviceMemoryService.getInstance().modifyMemory(session, memory_id, memory_desc, memory_size);
    }
    
    @Override
    public void modifyDeviceMemoryService(Session session, int ms_id, String ms_desc, Date ms_starttime, Date ms_endtime) throws EucalyptusServiceException {
        // DeviceMemoryService.getInstance().modifyMemoryService(session, ms_id, ms_desc, ms_starttime, ms_endtime);
    }
    
    @Override
    public void deleteDeviceMemory(Session session, List<Integer> memory_ids) throws EucalyptusServiceException {
        // DeviceMemoryService.getInstance().deleteMemory(session, memory_ids);
    }
    
    @Override
    public void deleteDeviceMemoryService(Session session, List<Integer> ms_ids) throws EucalyptusServiceException {
        // DeviceMemoryService.getInstance().deleteMemoryService(session, ms_ids);
    }
    
    @Override
    public List<String> lookupDeviceMemoryNamesByServerName(Session session, String server_name) throws EucalyptusServiceException {
        // return DeviceMemoryService.getInstance().lookupMemoryNamesByServerName(session, server_name);
        return null;
    }
    
    @Override
    public SearchResult lookupDeviceDiskByDate(Session session, SearchRange range, DiskState disk_state, Date dateBegin, Date dateEnd) throws EucalyptusServiceException {
        return DeviceDiskService.getInstance().lookupDiskByDate(session, range, disk_state, dateBegin, dateEnd);
    }
    
    @Override
    public Map<Integer, Long> lookupDeviceDiskCounts(Session session) throws EucalyptusServiceException {
        // return DeviceDiskService.getInstance().lookupDiskCountsGroupByState(session);
        return null;
    }
    
    @Override
    public void addDeviceDisk(Session session, String disk_name, String disk_desc, long disk_size, String server_name) throws EucalyptusServiceException {
        // DeviceDiskService.getInstance().addDisk(session, disk_name, disk_desc, disk_size, server_name);
    }
    
    @Override
    public void addDeviceDiskService(Session session, String ds_desc, long ds_size, Date ds_starttime, Date ds_endtime, int disk_id, String account_name, String user_name) throws EucalyptusServiceException {
        // DeviceDiskService.getInstance().addDiskService(session, ds_desc, ds_size, ds_starttime, ds_endtime, disk_id, account_name, user_name);
    }
    
    @Override
    public void modifyDeviceDisk(Session session, int disk_id, String disk_desc, long disk_size) throws EucalyptusServiceException {
//        DeviceDiskService.getInstance().modifyDisk(session, disk_id, disk_desc, disk_size);
    }
    
    @Override
    public void modifyDeviceDiskService(Session session, int ds_id, String ds_desc, Date ds_starttime, Date ds_endtime) throws EucalyptusServiceException {
//        DeviceDiskService.getInstance().modifyDiskService(session, ds_id, ds_desc, ds_starttime, ds_endtime);
    }
    
    @Override
    public void deleteDeviceDisk(Session session, List<Integer> disk_ids) throws EucalyptusServiceException {
//        DeviceDiskServiceervice.getInstance().deleteDisk(session, disk_ids);
    }
    
    @Override
    public void deleteDeviceDiskService(Session session, List<Integer> ds_ids) throws EucalyptusServiceException {
//        DeviceDiskService.getInstance().deleteDiskService(session, ds_ids);
    }
    
    @Override
    public List<String> lookupDeviceDiskNamesByServerName(Session session, String server_name) throws EucalyptusServiceException {
//        return DeviceDiskService.getInstance().lookupDiskNamesByServerName(session, server_name);
        return null;
    }
    
    @Override
    public SearchResult lookupDeviceIPByDate(Session session, SearchRange range, IPType ip_type, IPState ip_state, Date dateBegin, Date dateEnd) throws EucalyptusServiceException {
    	return DeviceIPService.getInstance().lookupIPByDate(session, range, ip_type, ip_state, dateBegin, dateEnd);
    }
    
    @Override
    public Map<Integer, Integer> lookupDeviceIPCounts(Session session, IPType ip_type) throws EucalyptusServiceException {
//    	return DeviceIPService.getInstance().lookupIPCountsGroupByState(session, ip_type);
        return null;
    }
    
    @Override
    public void addDeviceIP(Session session, String ip_addr, String ip_desc, IPType ip_type) throws EucalyptusServiceException {
//    	DeviceIPService.getInstance().addIP(session, ip_addr, ip_desc, ip_type);
    }
    
    @Override
    public void addDeviceIPService(Session session, String is_desc, Date is_starttime, Date is_endtime, int ip_id, String account_name, String user_name) throws EucalyptusServiceException {
//    	DeviceIPService.getInstance().addIPService(session, is_desc, is_starttime, is_endtime, ip_id, account_name, user_name);
    }
    
    @Override
    public void modifyDeviceIP(Session session, int ip_id, String ip_desc, IPType ip_type) throws EucalyptusServiceException {
//    	DeviceIPService.getInstance().modifyIP(session, ip_id, ip_desc, ip_type);
    }
    
    @Override
    public void modifyDeviceIPService(Session session, int is_id, String is_desc, Date is_starttime, Date is_endtime) throws EucalyptusServiceException {
//    	DeviceIPService.getInstance().modifyIPService(session, is_id, is_desc, is_starttime, is_endtime);
    }
    
    @Override
    public void deleteDeviceIP(Session session, List<Integer> ip_ids) throws EucalyptusServiceException {
//    	DeviceIPService.getInstance().deleteIP(session, ip_ids);
    }
    
    @Override
    public void deleteDeviceIPService(Session session, List<Integer> is_ids) throws EucalyptusServiceException {
//    	DeviceIPService.getInstance().deleteIPService(session, is_ids);
    }
    
    @Override
    public List<String> lookupDeviceUnusedIPAddrByIPType(Session session, IPType ip_type) throws EucalyptusServiceException {
//    	return DeviceIPService.getInstance().lookupUnusedIPAddrByIPType(session, ip_type);
        return null;
    }
    
	@Override
    public SearchResult lookupDeviceBWByDate(Session session, SearchRange range, Date dateBegin, Date dateEnd) throws EucalyptusServiceException {
		return DeviceBWService.getInstance().lookupBWServiceByDate(session, range, dateBegin, dateEnd);
    }
	
	@Override
    public void addDeviceBWService(Session session, String bs_desc, int bs_bw_max, Date bs_starttime, Date bs_endtime, String ip_addr) throws EucalyptusServiceException {
//		DeviceBWService.getInstance().addBWService(session, bs_desc, bs_bw_max, bs_starttime, bs_endtime, ip_addr);
    }
	
	@Override
    public void modifyDeviceBWService(Session session, int bs_id, String bs_desc, int bs_bw_max, Date bs_starttime, Date bs_endtime) throws EucalyptusServiceException {
//		DeviceBWService.getInstance().modifyBWService(session, bs_id, bs_desc, bs_bw_max, bs_starttime, bs_endtime);
    }
	
	@Override
    public void deleteDeviceBWService(Session session, List<Integer> bs_ids) throws EucalyptusServiceException {
//		DeviceBWService.getInstance().deleteBWService(session, bs_ids);
    }
	
	@Override
    public List<String> lookupDeviceUnusedIPAddrForBWService(Session session, String account_name, String user_name) throws EucalyptusServiceException {
//		return DeviceBWService.getInstance().lookupUnusedIPAddr(session, account_name, user_name);
	    return null;
    }
	
	@Override
	public SearchResult lookupDeviceTemplateByDate(Session session, SearchRange range, Date dateBegin, Date dateEnd) throws EucalyptusServiceException {
		return DeviceTemplateService.getInstance().lookupTemplateByDate(session, range, dateBegin, dateEnd);
	}
	
	@Override
    public void addDeviceTemplateService(Session session, String template_name, String template_desc, String template_cpu, int template_ncpus, long template_mem, long template_disk, int template_bw, String template_image) throws EucalyptusServiceException {
//		DeviceTemplateService.getInstance().addTemplate(session, template_name, template_desc, template_cpu, template_ncpus, template_mem, template_disk, template_bw, template_image);
    }
	
	@Override
	public void modifyDeviceTemplateService(Session session, int template_id, String template_desc, String template_cpu, int template_ncpus, long template_mem, long template_disk, int template_bw, String template_image) throws EucalyptusServiceException {
		DeviceTemplateService.getInstance().modifyTempalte(session, template_id, template_desc, template_cpu, template_ncpus, template_mem, template_disk, template_bw, template_image);
	}
	
	@Override
	public void deleteDeviceTemplateService(Session session, List<Integer> template_ids) throws EucalyptusServiceException {
		DeviceTemplateService.getInstance().deleteTemplate(session, template_ids);
	}
	
	@Override
	public TemplateInfo lookupDeviceTemplateInfoByName(Session session, String template_name) throws EucalyptusServiceException {
//		return DeviceTemplateService.getInstance().lookupTemplateInfoByName(session, template_name);
	    return null;
	}
	
	@Override
    public void modifyDeviceBandwidthPrice(Session session, String others_price_desc, double others_price) throws EucalyptusServiceException {
        deviceOthersPriceServiceProc.modifyOthersPriceBandwidth(session, others_price_desc, others_price);
    }
	
	@Override
	public SearchResult lookupDeviceCPUPriceByDate(Session session, SearchRange range, Date creationtimeBegin, Date creationtimeEnd, Date modifiedtimeBegin, Date modifiedtimeEnd)
			throws EucalyptusServiceException {
//		return deviceCPUPriceServiceProc.lookupCPUPriceByDate(session, range, creationtimeBegin, creationtimeEnd, modifiedtimeBegin, modifiedtimeEnd);
	    return null;
	}

	@Override
	public void addDeviceCPUPrice(Session session, String cpu_name, String cpu_price_desc, double cpu_price) throws EucalyptusServiceException {
//		deviceCPUPriceServiceProc.addCPUPrice(session, cpu_name, cpu_price_desc, cpu_price);
	}

	@Override
	public void modifyDeviceCPUPrice(Session session, int cpu_price_id, String cpu_price_desc, double cpu_price) throws EucalyptusServiceException {
		deviceCPUPriceServiceProc.modifyCPUPrice(session, cpu_price_id, cpu_price_desc, cpu_price);
	}
	
	@Override
	public SearchResult lookupDeviceTemplatePriceByDate(Session session, SearchRange range, Date creationtimeBegin, Date creationtimeEnd, Date modifiedtimeBegin, Date modifiedtimeEnd)
	        throws EucalyptusServiceException {
//	    return DeviceTemplatePriceService.getInstance().lookupTemplatePriceByDate(session, range, creationtimeBegin, creationtimeEnd, modifiedtimeBegin, modifiedtimeEnd);
	    return null;
	}
	
	@Override
	public double lookupDeviceTemplatePriceByPriceID(int template_price_id) throws EucalyptusServiceException {
//	    return DeviceTemplatePriceService.getInstance().lookupTemplatePriceByPriceID(template_price_id);
	    return 0;
	}
	
	@Override
	public void deleteDeviceTemplatePrice(Session session, List<Integer> template_price_ids) throws EucalyptusServiceException {
//	    DeviceTemplatePriceServicetePriceService.getInstance().deleteTemplatePrice(session, template_price_ids);
	}
	
	@Override
	public List<String> lookupDeviceTemplateUnpriced(Session session) throws EucalyptusServiceException {
//	    return DeviceTemplatePriceService.getInstance().lookupTemplateUnpriced(session);
	    return null;
	}
	
	@Override
	public void modifyDeviceTemplatePrice(Session session, int template_price_id, String template_price_desc,
            double template_price_cpu, double template_price_mem, double template_price_disk, double template_price_bw) throws EucalyptusServiceException {
	    DeviceTemplatePriceService.getInstance().modifyTemplatePrice(session, template_price_id, template_price_desc, template_price_cpu, template_price_mem, template_price_disk, template_price_bw);
	}
	
	@Override
	public void createDeviceTemplatePriceByID(Session session, int template_id, String template_price_desc,
            double template_price_cpu, double template_price_mem, double template_price_disk, double template_price_bw) throws EucalyptusServiceException {
	    DeviceTemplatePriceService.getInstance().createTemplatePriceByID(session, template_id, template_price_desc, template_price_cpu, template_price_mem, template_price_disk, template_price_bw);
	}
	
	@Override
	public void deleteDeviceCPUPrice(Session session, List<Integer> cpu_price_ids) throws EucalyptusServiceException {
		deviceCPUPriceServiceProc.deleteCPUPrice(session, cpu_price_ids);
	}
	
	@Override
	public List<String> lookupDeviceCPUNamesUnpriced(Session session) throws EucalyptusServiceException {
//	    return deviceCPUPriceServiceProc.lookupCPUNamesUnpriced(session);
	    return null;
	}
	
	@Override
	public SearchResultRow lookupDeviceMemoryPrice(Session session) throws EucalyptusServiceException {
//	    return deviceOthersPriceServiceProc.lookupOthersPriceMemory(session);
	    return null;
	}
	
	@Override
    public SearchResultRow lookupDeviceDiskPrice(Session session) throws EucalyptusServiceException {
//        return deviceOthersPriceServiceProc.lookupOthersPriceDisk(session);
	    return null;
    }

	@Override
    public SearchResultRow lookupDeviceBandwidthPrice(Session session) throws EucalyptusServiceException {
        // return deviceOthersPriceServiceProc.lookupOthersPriceBandwidth(session);
	    return null;
    }
	
	@Override
	public void modifyDeviceMemoryPrice(Session session, String others_price_desc, double others_price) throws EucalyptusServiceException {
	    deviceOthersPriceServiceProc.modifyOthersPriceMemory(session, others_price_desc, others_price);
	}
	
	@Override
    public void modifyDeviceDiskPrice(Session session, String others_price_desc, double others_price) throws EucalyptusServiceException {
        deviceOthersPriceServiceProc.modifyOthersPriceDisk(session, others_price_desc, others_price);
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
		return EucaServiceWrapper.getInstance().getKeypairs(session);
	}

	@Override
	public List<String> querySecurityGroup(Session session) throws EucalyptusServiceException {
		// TODO Auto-generated method stub
		return EucaServiceWrapper.getInstance().getSecurityGroups(session);
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
