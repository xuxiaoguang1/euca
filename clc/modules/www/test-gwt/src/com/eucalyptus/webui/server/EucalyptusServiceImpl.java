package com.eucalyptus.webui.server;

import java.util.ArrayList;
import java.util.Collection;
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
import com.eucalyptus.webui.client.view.DeviceCPUDeviceAddView;
import com.eucalyptus.webui.client.view.DeviceDiskDeviceAddView;
import com.eucalyptus.webui.client.view.DeviceMemoryDeviceAddView;
import com.eucalyptus.webui.server.device.DeviceAreaServiceProcImpl;
import com.eucalyptus.webui.server.device.DeviceBWServiceProcImpl;
import com.eucalyptus.webui.server.device.DeviceCPUPriceServiceProcImpl;
import com.eucalyptus.webui.server.device.DeviceCPUServiceProcImpl;
import com.eucalyptus.webui.server.device.DeviceCabinetServiceProcImpl;
import com.eucalyptus.webui.server.device.DeviceDiskServiceProcImpl;
import com.eucalyptus.webui.server.device.DeviceIPServiceProcImpl;
import com.eucalyptus.webui.server.device.DeviceMemoryServiceProcImpl;
import com.eucalyptus.webui.server.device.DeviceOthersPriceServiceProcImpl;
import com.eucalyptus.webui.server.device.DeviceRoomServiceProcImpl;
import com.eucalyptus.webui.server.device.DeviceServerServiceProcImpl;
import com.eucalyptus.webui.server.device.DeviceTemplatePriceService;
import com.eucalyptus.webui.server.device.DeviceTemplateServiceProcImpl;
import com.eucalyptus.webui.server.mail.MailSenderInfo;
import com.eucalyptus.webui.server.user.AuthenticateUserLogin;
import com.eucalyptus.webui.server.user.LoginUserProfileStorer;
import com.eucalyptus.webui.server.user.PwdResetProc;
import com.eucalyptus.webui.shared.resource.Template;
import com.eucalyptus.webui.shared.query.QueryType;
import com.eucalyptus.webui.shared.resource.VMImageType;
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
	private DeviceAreaServiceProcImpl deviceAreaServiceProc = new DeviceAreaServiceProcImpl();
	private DeviceRoomServiceProcImpl deviceRoomServiceProc = new DeviceRoomServiceProcImpl();
	private DeviceCabinetServiceProcImpl deviceCabinetServiceProc = new DeviceCabinetServiceProcImpl();
	private DeviceCPUPriceServiceProcImpl deviceCPUPriceServiceProc = new DeviceCPUPriceServiceProcImpl();
	private DeviceOthersPriceServiceProcImpl deviceOthersPriceServiceProc = new DeviceOthersPriceServiceProcImpl();
	private DeviceServerServiceProcImpl deviceServerServiceProc = new DeviceServerServiceProcImpl();
	private DeviceCPUServiceProcImpl deviceCPUServiceProc = new DeviceCPUServiceProcImpl();
	private DeviceMemoryServiceProcImpl deviceMemoryServiceProc = new DeviceMemoryServiceProcImpl();
	private DeviceDiskServiceProcImpl deviceDiskServiceProc = new DeviceDiskServiceProcImpl();
	private DeviceIPServiceProcImpl deviceIPServiceProc = new DeviceIPServiceProcImpl();
	private DeviceBWServiceProcImpl deviceBWServiceProc = new DeviceBWServiceProcImpl();
	private DeviceTemplateServiceProcImpl deviceTemplateServiceProc = new DeviceTemplateServiceProcImpl();
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
	public SearchResult lookupDeviceAreaByDate(Session session, SearchRange range,
			Date creationtimeBegin, Date creationtimeEnd, Date modifiedtimeBegin, Date modifiedtimeEnd) throws EucalyptusServiceException {
		return deviceAreaServiceProc.lookupAreaByDate(session, range, creationtimeBegin, creationtimeEnd, modifiedtimeBegin, modifiedtimeEnd);
	}
	
	@Override
	public void addDeviceArea(Session session, String area_name, String area_desc) throws EucalyptusServiceException {
		deviceAreaServiceProc.addArea(session, area_name, area_desc);
	}
	
	@Override
	public void modifyDeviceArea(Session session, int area_id, String area_desc) throws EucalyptusServiceException {
		deviceAreaServiceProc.modifyArea(session, area_id, area_desc);
	}

	@Override
	public void deleteDeviceArea(Session session, Collection<Integer> area_ids) throws EucalyptusServiceException {
		deviceAreaServiceProc.deleteArea(session, area_ids);
	}
	
	@Override
	public List<String> lookupDeviceAreaNames(Session session) throws EucalyptusServiceException {
		return deviceAreaServiceProc.lookupAreaNames(session);
	}
	
	@Override
	public SearchResult lookupDeviceRoomByDate(Session session, SearchRange range,
			Date creationtimeBegin, Date creationtimeEnd, Date modifiedtimeBegin, Date modifiedtimeEnd) throws EucalyptusServiceException {
		return deviceRoomServiceProc.lookupRoomByDate(session, range, creationtimeBegin, creationtimeEnd, modifiedtimeBegin, modifiedtimeEnd);
	}
	
	@Override
	public void addDeviceRoom(Session session, String room_name, String room_desc, String area_name) throws EucalyptusServiceException {
		deviceRoomServiceProc.addRoom(session, room_name, room_desc, area_name);
	}
	
	@Override
	public void modifyDeviceRoom(Session session, int room_id, String room_desc) throws EucalyptusServiceException {
		deviceRoomServiceProc.modifyRoom(session, room_id, room_desc);
	}

	@Override
	public void deleteDeviceRoom(Session session, Collection<Integer> room_ids) throws EucalyptusServiceException {
		deviceRoomServiceProc.deleteRoom(session, room_ids);
	}
	
	@Override
	public List<String> lookupDeviceRoomNamesByAreaName(Session session, String area_name) throws EucalyptusServiceException {
		return deviceRoomServiceProc.lookupRoomNamesByAreaName(session, area_name);
	}
	
	@Override
	public SearchResult lookupDeviceCabinetByDate(Session session, SearchRange range,
			Date creationtimeBegin, Date creationtimeEnd, Date modifiedtimeBegin, Date modifiedtimeEnd) throws EucalyptusServiceException {
		return deviceCabinetServiceProc.lookupCabinetByDate(session, range, creationtimeBegin, creationtimeEnd, modifiedtimeBegin, modifiedtimeEnd);
	}
	
	@Override
	public void addDeviceCabinet(Session session, String cabinet_name, String cabinet_desc, String room_name) throws EucalyptusServiceException {
		deviceCabinetServiceProc.addCabinet(session, cabinet_name, cabinet_desc, room_name);
	}
	
	@Override
	public void modifyDeviceCabinet(Session session, int cabinet_id, String cabinet_desc) throws EucalyptusServiceException {
		deviceCabinetServiceProc.modifyCabinet(session, cabinet_id, cabinet_desc);
	}

	@Override
	public void deleteDeviceCabinet(Session session, Collection<Integer> cabinet_ids) throws EucalyptusServiceException {
		deviceCabinetServiceProc.deleteCabinet(session, cabinet_ids);
	}
	
	@Override
	public List<String> lookupCabinetNamesByRoomName(Session session, String room_name) throws EucalyptusServiceException {
	    return deviceCabinetServiceProc.lookupCabinetNamesByRoomName(session, room_name);
	}
	
	@Override
	public SearchResult lookupDeviceCPUPriceByDate(Session session, SearchRange range, Date creationtimeBegin, Date creationtimeEnd, Date modifiedtimeBegin, Date modifiedtimeEnd)
			throws EucalyptusServiceException {
		return deviceCPUPriceServiceProc.lookupCPUPriceByDate(session, range, creationtimeBegin, creationtimeEnd, modifiedtimeBegin, modifiedtimeEnd);
	}

	@Override
	public void addDeviceCPUPrice(Session session, String cpu_name, String cpu_price_desc, double cpu_price) throws EucalyptusServiceException {
		deviceCPUPriceServiceProc.addCPUPrice(session, cpu_name, cpu_price_desc, cpu_price);
	}

	@Override
	public void modifyDeviceCPUPrice(Session session, int cpu_price_id, String cpu_price_desc, double cpu_price) throws EucalyptusServiceException {
		deviceCPUPriceServiceProc.modifyCPUPrice(session, cpu_price_id, cpu_price_desc, cpu_price);
	}
	
	@Override
	public SearchResult lookupDeviceTemplatePriceByDate(Session session, SearchRange range, Date creationtimeBegin, Date creationtimeEnd, Date modifiedtimeBegin, Date modifiedtimeEnd)
	        throws EucalyptusServiceException {
	    return DeviceTemplatePriceService.getInstance().lookupTemplatePriceByDate(session, range, creationtimeBegin, creationtimeEnd, modifiedtimeBegin, modifiedtimeEnd);
	}
	
	@Override
	public double lookupDeviceTemplatePriceByPriceID(int template_price_id) throws EucalyptusServiceException {
	    return DeviceTemplatePriceService.getInstance().lookupTemplatePriceByPriceID(template_price_id);
	}
	
	@Override
	public void deleteDeviceTemplatePrice(Session session, Collection<Integer> template_price_ids) throws EucalyptusServiceException {
	    DeviceTemplatePriceService.getInstance().deleteTemplatePrice(session, template_price_ids);
	}
	
	@Override
	public List<String> lookupDeviceTemplateUnpriced(Session session) throws EucalyptusServiceException {
	    return DeviceTemplatePriceService.getInstance().lookupTemplateUnpriced(session);
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
	public Template lookupDeviceTemplateByName(Session session, String template_name) throws EucalyptusServiceException {
		return deviceTemplateServiceProc.lookupTemplateByName(session, template_name);
	}

	@Override
	public void deleteDeviceCPUPrice(Session session, Collection<Integer> cpu_price_ids) throws EucalyptusServiceException {
		deviceCPUPriceServiceProc.deleteCPUPrice(session, cpu_price_ids);
	}
	
	@Override
	public List<String> lookupDeviceCPUNamesUnpriced(Session session) throws EucalyptusServiceException {
	    return deviceCPUPriceServiceProc.lookupCPUNamesUnpriced(session);
	}
	
	@Override
	public SearchResultRow lookupDeviceMemoryPrice(Session session) throws EucalyptusServiceException {
	    return deviceOthersPriceServiceProc.lookupOthersPriceMemory(session);
	}
	
	@Override
    public SearchResultRow lookupDeviceDiskPrice(Session session) throws EucalyptusServiceException {
        return deviceOthersPriceServiceProc.lookupOthersPriceDisk(session);
    }

	@Override
    public SearchResultRow lookupDeviceBandwidthPrice(Session session) throws EucalyptusServiceException {
        return deviceOthersPriceServiceProc.lookupOthersPriceBandwidth(session);
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
    public void modifyDeviceBandwidthPrice(Session session, String others_price_desc, double others_price) throws EucalyptusServiceException {
        deviceOthersPriceServiceProc.modifyOthersPriceBandwidth(session, others_price_desc, others_price);
    }
	
	@Override
	public SearchResult lookupDeviceServer(Session session, String search, SearchRange range, int queryState) {
		return deviceServerServiceProc.lookupServer(session, search, range, queryState);
	}

	@Override
	public Map<Integer, Integer> getDeviceServerCounts(Session session) {
		return deviceServerServiceProc.getServerCounts(session);
	}
	
	@Override
    public List<SearchResultRow> deleteDeviceServer(Session session, List<SearchResultRow> list) {
		return deviceServerServiceProc.deleteDevice(session, list);
    }

	@Override
	public SearchResultRow modifyDeviceServerState(Session session, SearchResultRow row, int state) {
		return deviceServerServiceProc.modifyServerState(session, row, state);
	}
	
	@Override
	public boolean addDeviceServer(Session session, String mark, String name, String conf, String ip,
			int bw, int state, String room) {
		return deviceServerServiceProc.addDevice(session, mark, name, conf, ip, bw, state, room);
	}

	@Override
	public SearchResult lookupDeviceCPU(Session session, String search, SearchRange range, int queryState) {
		return deviceCPUServiceProc.lookupCPU(session, search, range, queryState);
	}

	@Override
	public Map<Integer, Integer> getDeviceCPUCounts(Session session) {
		return deviceCPUServiceProc.getCPUCounts(session);
	}

	@Override
	public SearchResultRow modifyDeviceCPUService(Session session, SearchResultRow row, String endtime, int state) {
		return deviceCPUServiceProc.modifyService(session, row, endtime, state);
	}

	@Override
	public List<SearchResultRow> deleteDeviceCPUService(Session session, List<SearchResultRow> list) {
		return deviceCPUServiceProc.deleteService(session, list);
	}

	@Override
	public List<SearchResultRow> deleteDeviceCPUDevice(Session session, List<SearchResultRow> list) {
		return deviceCPUServiceProc.deleteDevice(session, list);
	}

	@Override
	public SearchResultRow addDeviceCPUService(Session session, SearchResultRow row, String account, String user,
	        String starttime, int life, int state) {
		return deviceCPUServiceProc.addService(session, row, account, user, starttime, life, state);
	}

	@Override
	public boolean addDeviceCPUDevice(Session session, String serverMark, String name, String vendor, String model,
	        double ghz, double cache, int num) {
		return deviceCPUServiceProc.addDevice(session, serverMark, name, vendor, model, ghz, cache, num);
	}

	@Override
	public DeviceCPUDeviceAddView.DataCache lookupDeviceCPUInfo(Session session) {
		return deviceCPUServiceProc.lookupDeviceInfo(session);
	}
	
	@Override
	public List<String> listDeviceCPUAccounts(Session session) {
		return deviceCPUServiceProc.listAccounts(session);
	}

	@Override
	public List<String> listDeviceCPUUsersByAccount(Session session, String account) {
		return deviceCPUServiceProc.listUsersByAccount(session, account);
	}

	@Override
	public SearchResult lookupDeviceMemory(Session session, String search, SearchRange range, int queryState) {
		return deviceMemoryServiceProc.lookupMemory(session, search, range, queryState);
	}

	@Override
	public Map<Integer, Long> getDeviceMemoryCounts(Session session) {
		return deviceMemoryServiceProc.getMemoryCounts(session);
	}

	@Override
	public SearchResultRow modifyDeviceMemoryService(Session session, SearchResultRow row, String endtime, int state) {
		return deviceMemoryServiceProc.modifyService(session, row, endtime, state);
	}

	@Override
	public List<SearchResultRow> deleteDeviceMemoryService(Session session, List<SearchResultRow> list) {
		return deviceMemoryServiceProc.deleteService(session, list);
	}

	@Override
	public List<SearchResultRow> deleteDeviceMemoryDevice(Session session, List<SearchResultRow> list) {
		return deviceMemoryServiceProc.deleteDevice(session, list);
	}

	@Override
	public SearchResultRow addDeviceMemoryService(Session session, SearchResultRow row, String account, String user,
	        long used, String starttime, int life, int state) {
		return deviceMemoryServiceProc.addService(session, row, account, user, used, starttime, life, state);
	}

	@Override
	public boolean addDeviceMemoryDevice(Session session, String serverMark, String name, long total, int num) {
		return deviceMemoryServiceProc.addDevice(session, serverMark, name, total, num);
	}

	@Override
	public DeviceMemoryDeviceAddView.DataCache lookupDeviceMemoryInfo(Session session) {
		return deviceMemoryServiceProc.lookupDeviceInfo(session);
	}

	@Override
	public List<String> listDeviceMemoryAccounts(Session session) {
		return deviceMemoryServiceProc.listAccounts(session);
	}

	@Override
	public List<String> listDeviceMemoryUsersByAccount(Session session, String account) {
		return deviceMemoryServiceProc.listUsersByAccount(session, account);
	}

	@Override
	public SearchResult lookupDeviceDisk(Session session, String search, SearchRange range, int queryState) {
		return deviceDiskServiceProc.lookupDisk(session, search, range, queryState);
	}

	@Override
	public Map<Integer, Long> getDeviceDiskCounts(Session session) {
		return deviceDiskServiceProc.getDiskCounts(session);
	}

	@Override
	public SearchResultRow modifyDeviceDiskService(Session session, SearchResultRow row, String endtime, int state) {
		return deviceDiskServiceProc.modifyService(session, row, endtime, state);
	}

	@Override
	public List<SearchResultRow> deleteDeviceDiskService(Session session, List<SearchResultRow> list) {
		return deviceDiskServiceProc.deleteService(session, list);
	}

	@Override
	public List<SearchResultRow> deleteDeviceDiskDevice(Session session, List<SearchResultRow> list) {
		return deviceDiskServiceProc.deleteDevice(session, list);
	}

	@Override
	public SearchResultRow addDeviceDiskService(Session session, SearchResultRow row, String account, String user,
	        long used, String starttime, int life, int state) {
		return deviceDiskServiceProc.addService(session, row, account, user, used, starttime, life, state);
	}

	@Override
	public boolean addDeviceDiskDevice(Session session, String serverMark, String name, long total, int num) {
		return deviceDiskServiceProc.addDevice(session, serverMark, name, total, num);
	}

	@Override
	public DeviceDiskDeviceAddView.DataCache lookupDeviceDiskInfo(Session session) {
		return deviceDiskServiceProc.lookupDeviceInfo(session);
	}

	@Override
	public List<String> listDeviceDiskAccounts(Session session) {
		return deviceDiskServiceProc.listAccounts(session);
	}

	@Override
	public List<String> listDeviceDiskUsersByAccount(Session session, String account) {
		return deviceDiskServiceProc.listUsersByAccount(session, account);
	}

	@Override
	public SearchResult lookupDeviceIP(Session session, String search, SearchRange range, int queryState, int queryType) {
		return deviceIPServiceProc.lookupIP(session, search, range, queryState, queryType);
	}

	@Override
	public Map<Integer, Integer> getDeviceIPCounts(Session session, int queryType) {
		return deviceIPServiceProc.getIPCounts(session, queryType);
	}

	@Override
	public SearchResultRow modifyDeviceIPService(Session session, SearchResultRow row, String endtime, int state) {
		return deviceIPServiceProc.modifyService(session, row, endtime, state);
	}

	@Override
	public List<SearchResultRow> deleteDeviceIPService(Session session, List<SearchResultRow> list) {
		return deviceIPServiceProc.deleteService(session, list);
	}

	@Override
	public List<SearchResultRow> deleteDeviceIPDevice(Session session, List<SearchResultRow> list) {
		return deviceIPServiceProc.deleteDevice(session, list);
	}

	@Override
	public SearchResultRow addDeviceIPService(Session session, SearchResultRow row, String account, String user,
	        String vmMark, String starttime, int life, int state) {
		return deviceIPServiceProc.addService(session, row, account, user, vmMark, starttime, life, state);
	}

	@Override
	public boolean addDeviceIPDevice(Session session, List<String> publicList, List<String> privateList) {
		return deviceIPServiceProc.addDevice(session, publicList, privateList);
	}

	@Override
	public List<String> listDeviceIPAccounts(Session session) {
		return deviceIPServiceProc.listAccounts(session);
	}

	@Override
	public List<String> listDeviceIPUsersByAccount(Session session, String account) {
		return deviceIPServiceProc.listUsersByAccount(session, account);
	}

	@Override
	public List<String> listDeviceVMsByUser(Session session, String account, String user) {
		return deviceIPServiceProc.listVMsByUser(session, account, user);
	}

	@Override
	public SearchResult lookupDeviceBW(Session session, String search, SearchRange range) {
		return deviceBWServiceProc.lookupBW(session, search, range);
	}

	@Override
	public SearchResultRow modifyDeviceBWService(Session session, SearchResultRow row, String endtime) {
		return deviceBWServiceProc.modifyService(session, row, endtime);
	}

	@Override
	public List<SearchResultRow> deleteDeviceBWService(Session session, List<SearchResultRow> list) {
		return deviceBWServiceProc.deleteService(session, list);
	}

	@Override
	public boolean addDeviceBWService(Session session, String account, String user, String starttime, int life,
	        String ip, long bandwidth) {
		return deviceBWServiceProc.addService(session, account, user, starttime, life, ip, bandwidth);
	}

	@Override
	public List<String> listDeviceBWAccounts(Session session) {
		return deviceBWServiceProc.listAccounts(session);
	}

	@Override
	public List<String> listDeviceBWUsersByAccount(Session session, String account) {
		return deviceBWServiceProc.listUsersByAccount(session, account);
	}

	@Override
	public List<String> listDeviceIPsByUser(Session session, String account, String user) {
		return deviceBWServiceProc.listIPsByUser(session, account, user);
	}

	@Override
	public SearchResult lookupDeviceTemplate(Session session, String search, SearchRange range, Date starttime,
	        Date endtime) {
		return deviceTemplateServiceProc.lookupTemplate(session, search, range, starttime, endtime);
	}

	@Override
	public List<SearchResultRow> deleteDeviceTemplate(Session session, List<SearchResultRow> list) {
		return deviceTemplateServiceProc.deleteTemplate(session, list);
	}

	@Override
	public boolean addDeviceTemplate(Session session, String mark, String cpu, int ncpus, String mem, String disk, String bw,
	        String image) {
		return deviceTemplateServiceProc.addTemplate(session, mark, cpu, ncpus, mem, disk, bw, image);
	}

	@Override
	public SearchResultRow modifyDeviceTempate(Session session, SearchResultRow row, String cpu, int ncpus, String mem,
	        String disk, String bw, String image) {
		return deviceTemplateServiceProc.modifyTemplate(session, row, cpu, ncpus, mem, disk, bw, image);
	}
	
	@Override
	public List<String> listDeviceTemplateCPUNames(Session session) {
		return deviceTemplateServiceProc.listDeviceCPUNames(session);
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
