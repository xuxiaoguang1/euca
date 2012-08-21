package com.eucalyptus.webui.client.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.eucalyptus.webui.client.session.Session;
import com.eucalyptus.webui.client.view.DeviceCPUDeviceAddView;
import com.eucalyptus.webui.client.view.DeviceMemoryDeviceAddView;
import com.eucalyptus.webui.client.view.DeviceDiskDeviceAddView;
import com.eucalyptus.webui.shared.user.AccountInfo;
import com.eucalyptus.webui.shared.user.EnumState;
import com.eucalyptus.webui.shared.user.EnumUserAppState;
import com.eucalyptus.webui.shared.user.GroupInfo;
import com.eucalyptus.webui.shared.user.LoginUserProfile;
import com.eucalyptus.webui.shared.user.UserApp;
import com.eucalyptus.webui.shared.user.UserAppStateCount;
import com.eucalyptus.webui.shared.user.UserInfo;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("backend")
public interface EucalyptusService extends RemoteService {

	Session login(String accountName, String userName, String password)
			throws EucalyptusServiceException;

	/**
	 * User existed ?
	 * 
	 * @throws EucalyptusServiceException
	 */
	void checkUserExisted(String accountName, String userName)
			throws EucalyptusServiceException;

	/**
	 * Logout current user.
	 * 
	 * @throws EucalyptusServiceException
	 */
	void logout(Session session) throws EucalyptusServiceException;

	/**
	 * Get the login user profile
	 * 
	 * @param session
	 * @return
	 * @throws EucalyptusServiceException
	 */
	LoginUserProfile getLoginUserProfile(Session session)
			throws EucalyptusServiceException;

	/**
	 * Get system properties.
	 * 
	 * @param session
	 * @return
	 * @throws EucalyptusServiceException
	 */
	HashMap<String, String> getSystemProperties(Session session)
			throws EucalyptusServiceException;

	/**
	 * Get quicklinks tree data.
	 * 
	 * @param session
	 * @return
	 * @throws EucalyptusServiceException
	 */
	ArrayList<QuickLinkTag> getQuickLinks(Session session)
			throws EucalyptusServiceException;

	/**
	 * Search system configurations.
	 * 
	 * @param session
	 * @param search
	 * @param range
	 * @return
	 * @throws EucalyptusServiceException
	 */
	SearchResult lookupConfiguration(Session session, String search,
			SearchRange range) throws EucalyptusServiceException;

	/**
	 * Set system configurations.
	 * 
	 * @param session
	 * @param config
	 * @throws EucalyptusServiceException
	 */
	void setConfiguration(Session session, SearchResultRow config)
			throws EucalyptusServiceException;

	/**
	 * Search accounts.
	 * 
	 * @param session
	 * @param search
	 * @param range
	 * @return
	 * @throws EucalyptusServiceException
	 */
	SearchResult lookupAccount(Session session, String search, SearchRange range)
			throws EucalyptusServiceException;

	/**
	 * Search VM types.
	 * 
	 * @param session
	 * @param search
	 * @param range
	 * @return
	 * @throws EucalyptusServiceException
	 */
	SearchResult lookupVmType(Session session, String search, SearchRange range)
			throws EucalyptusServiceException;

	/**
	 * Set VmType values.
	 * 
	 * @param session
	 * @param result
	 * @throws EucalyptusServiceException
	 */
	void setVmType(Session session, SearchResultRow result)
			throws EucalyptusServiceException;

	/**
	 * Search user groups.
	 * 
	 * @param session
	 * @param search
	 * @param range
	 * @return
	 * @throws EucalyptusServiceException
	 */
	SearchResult lookupGroup(Session session, String search, SearchRange range)
			throws EucalyptusServiceException;

	/**
	 * Search users.
	 * 
	 * @param session
	 * @param search
	 * @param range
	 * @return
	 * @throws EucalyptusServiceException
	 */
	SearchResult lookupUser(Session session, String search, SearchRange range)
			throws EucalyptusServiceException;

	/**
	 * Search user applications.
	 * 
	 * @param session
	 * @param search
	 * @param range
	 * @param state
	 * @return
	 * @throws EucalyptusServiceException
	 */
	SearchResult lookupUserApp(Session session, String search,
			SearchRange range, EnumUserAppState state)
			throws EucalyptusServiceException;

	/**
	 * Search users.
	 * 
	 * @param session
	 * @param groud
	 *            id
	 * @param range
	 * @return
	 * @throws EucalyptusServiceException
	 */
	SearchResult lookupUserByGroupId(Session session, int groupId,
			SearchRange range) throws EucalyptusServiceException;

	/**
	 * Search users.
	 * 
	 * @param session
	 * @param account
	 *            id
	 * @param range
	 * @return
	 * @throws EucalyptusServiceException
	 */
	SearchResult lookupUserByAccountId(Session session, int accountId,
			SearchRange range) throws EucalyptusServiceException;

	/**
	 * Search users.
	 * 
	 * @param session
	 * @param account
	 *            id
	 * @param range
	 * @return
	 * @throws EucalyptusServiceException
	 */
	SearchResult lookupUserExcludeGroupId(Session session, int accountId,
			int groupId, SearchRange range) throws EucalyptusServiceException;

	/**
	 * Delete a list of users.
	 * 
	 * @param session
	 * @param ids
	 * @throws EucalyptusServiceException
	 */
	void deleteUsers(Session session, ArrayList<String> ids)
			throws EucalyptusServiceException;

	/**
	 * Update users' state.
	 * 
	 * @param session
	 * @param ids
	 * @param user
	 *            state
	 * @throws EucalyptusServiceException
	 */
	void updateUserState(Session session, ArrayList<String> ids,
			EnumState userState) throws EucalyptusServiceException;

	/**
	 * Search policies.
	 * 
	 * @param session
	 * @param search
	 * @param range
	 * @return
	 * @throws EucalyptusServiceException
	 */
	SearchResult lookupPolicy(Session session, String search, SearchRange range)
			throws EucalyptusServiceException;

	/**
	 * Search access keys.
	 * 
	 * @param session
	 * @param search
	 * @param range
	 * @return
	 * @throws EucalyptusServiceException
	 */
	SearchResult lookupKey(Session session, String search, SearchRange range)
			throws EucalyptusServiceException;

	/**
	 * Search X509 certificates.
	 * 
	 * @param session
	 * @param search
	 * @param range
	 * @return
	 * @throws EucalyptusServiceException
	 */
	SearchResult lookupCertificate(Session session, String search,
			SearchRange range) throws EucalyptusServiceException;

	/**
	 * Search VM images.
	 * 
	 * @param session
	 * @param search
	 * @param range
	 * @return
	 * @throws EucalyptusServiceException
	 */
	SearchResult lookupImage(Session session, String search, SearchRange range)
			throws EucalyptusServiceException;

	/**
	 * Create/update a new account.
	 * 
	 * @param session
	 * @param accountName
	 * @param adminPassword
	 * @throws EucalyptusServiceException
	 */
	void createAccount(Session session, AccountInfo account)
			throws EucalyptusServiceException;

	/**
	 * Delete accounts.
	 * 
	 * @param session
	 * @param ids
	 * @throws EucalyptusServiceException
	 */
	void deleteAccounts(Session session, ArrayList<String> ids)
			throws EucalyptusServiceException;

	/**
	 * List all the accounts
	 * 
	 * @param session
	 * @throws EucalyptusServiceException
	 */
	ArrayList<AccountInfo> listAccounts(Session session)
			throws EucalyptusServiceException;

	/**
	 * Create a user in the same account, with same path.
	 * 
	 * @param session
	 * @param user
	 *            info
	 * @throws EucalyptusServiceException
	 */
	void createUser(Session session, UserInfo user)
			throws EucalyptusServiceException;

	/**
	 * Create one user in the account.
	 * 
	 * @param session
	 * @param accountId
	 * @param names
	 *            User names separated by spaces.
	 * @param path
	 * @return the created user names.
	 * @throws EucalyptusServiceException
	 */
	ArrayList<String> createUsers(Session session, String accountId,
			String names, String path) throws EucalyptusServiceException;

	/**
	 * Create a group in the same account.
	 * 
	 * @param session
	 * @param group
	 *            info
	 * @throws EucalyptusServiceException
	 */
	void createGroup(Session session, GroupInfo group)
			throws EucalyptusServiceException;

	/**
	 * Create multiple groups in the same account, with same path.
	 * 
	 * @param session
	 * @param accountId
	 * @param names
	 *            Group names separated by spaces.
	 * @param path
	 * @return the created group names.
	 * @throws EucalyptusServiceException
	 */
	ArrayList<String> createGroups(Session session, String accountId,
			String names, String path) throws EucalyptusServiceException;

	/**
	 * Delete a list of groups.
	 * 
	 * @param session
	 * @param ids
	 * @throws EucalyptusServiceException
	 */
	void deleteGroups(Session session, ArrayList<String> ids)
			throws EucalyptusServiceException;

	/**
	 * List current groups.
	 * 
	 * @param session
	 * @throws EucalyptusServiceException
	 */
	ArrayList<GroupInfo> listGroups(Session session)
			throws EucalyptusServiceException;

	/**
	 * Update group state.
	 * 
	 * @param session
	 * @param group
	 *            ids
	 * @throws EucalyptusServiceException
	 */
	void updateGroupState(Session session, ArrayList<String> ids,
			EnumState userState) throws EucalyptusServiceException;

	/**
	 * Update user state by accounts.
	 * 
	 * @param session
	 * @param account
	 *            ids
	 * @throws EucalyptusServiceException
	 */
	void updateAccountState(Session session, ArrayList<String> ids,
			EnumState userState) throws EucalyptusServiceException;

	/**
	 * Add policy to account.
	 * 
	 * @param session
	 * @param accountId
	 * @param name
	 * @param document
	 * @throws EucalyptusServiceException
	 */
	void addAccountPolicy(Session session, String accountId, String name,
			String document) throws EucalyptusServiceException;

	/**
	 * Add policy to user.
	 * 
	 * @param session
	 * @param usertId
	 * @param name
	 * @param document
	 * @throws EucalyptusServiceException
	 */
	void addUserPolicy(Session session, String usertId, String name,
			String document) throws EucalyptusServiceException;

	/**
	 * Add policy to group.
	 * 
	 * @param session
	 * @param groupId
	 * @param name
	 * @param document
	 * @throws EucalyptusServiceException
	 */
	void addGroupPolicy(Session session, String groupId, String name,
			String document) throws EucalyptusServiceException;

	/**
	 * Delete a policy.
	 * 
	 * @param session
	 * @param policySerialized
	 * @throws EucalyptusServiceException
	 */
	void deletePolicy(Session session, ArrayList<String> ids)
			throws EucalyptusServiceException;

	/**
	 * Delete an access key.
	 * 
	 * @param session
	 * @param keySerialized
	 * @throws EucalyptusServiceException
	 */
	void deleteAccessKey(Session session, ArrayList<String> ids)
			throws EucalyptusServiceException;

	/**
	 * Delete certificate.
	 * 
	 * @param session
	 * @param certSerialized
	 * @throws EucalyptusServiceException
	 */
	void deleteCertificate(Session session, ArrayList<String> ids)
			throws EucalyptusServiceException;

	/**
	 * Add users to groups using user names input
	 * 
	 * @param session
	 * @param userNames
	 * @param groupIds
	 * @throws EucalyptusServiceException
	 */
	void addUsersToGroupsByName(Session session, String userNames,
			ArrayList<String> groupIds) throws EucalyptusServiceException;

	/**
	 * Add users to groups using group id.
	 * 
	 * @param session
	 * @param userIds
	 * @param groupId
	 * @throws EucalyptusServiceException
	 */
	void addUsersToGroupsById(Session session, ArrayList<String> userIds,
			int groupId) throws EucalyptusServiceException;

	/**
	 * Remove users from groups using user names input.
	 * 
	 * @param session
	 * @param user
	 *            ids
	 * @throws EucalyptusServiceException
	 */
	void removeUsersFromGroup(Session session, ArrayList<String> userIds)
			throws EucalyptusServiceException;

	/**
	 * Remove users from groups using group names input.
	 * 
	 * @param session
	 * @param userIds
	 * @param groupNames
	 * @throws EucalyptusServiceException
	 */
	void removeUsersFromGroupsById(Session session, ArrayList<String> userIds,
			String groupNames) throws EucalyptusServiceException;

	/**
	 * Modify user info.
	 * 
	 * @param session
	 * @param keys
	 * @param values
	 * @throws EucalyptusServiceException
	 */
	void modifyUser(Session session, ArrayList<String> keys,
			ArrayList<String> values) throws EucalyptusServiceException;

	/**
	 * Modify individual info.
	 * 
	 * @param session
	 * @param profile
	 * @throws EucalyptusServiceException
	 */

	LoginUserProfile modifyIndividual(Session session, String title,
			String mobile, String email) throws EucalyptusServiceException;

	/**
	 * Modify group info.
	 * 
	 * @param session
	 * @param values
	 * @throws EucalyptusServiceException
	 */
	void modifyGroup(Session session, ArrayList<String> values)
			throws EucalyptusServiceException;

	/**
	 * Modify access key info.
	 * 
	 * @param session
	 * @param values
	 * @throws EucalyptusServiceException
	 */
	void modifyAccessKey(Session session, ArrayList<String> values,
			boolean active) throws EucalyptusServiceException;

	/**
	 * Modify certificate info.
	 * 
	 * @param session
	 * @param values
	 * @throws EucalyptusServiceException
	 */
	void modifyCertificate(Session session, ArrayList<String> values, Boolean active, Boolean revoked)
			throws EucalyptusServiceException;

	/**
	 * Add an access key to a user.
	 * 
	 * @param session
	 * @param userId
	 * @throws EucalyptusServiceException
	 */
	void addAccessKey(Session session, String userId)
			throws EucalyptusServiceException;

	/**
	 * Add a certificate to a user.
	 * 
	 * @param session
	 * @param userId
	 * @throws EucalyptusServiceException
	 */
	void addCertificate(Session session, String userId, String pem)
			throws EucalyptusServiceException;

	/**
	 * Change user password and/or email.
	 * 
	 * @param session
	 * @param oldPass
	 * @param newPass
	 * @param email
	 */
	void changePassword(Session session, String oldPass, String newPass,
			String eamil) throws EucalyptusServiceException;

	/**
	 * Sign up a new account by user.
	 * 
	 * @param accountName
	 * @param password
	 * @param email
	 * @throws EucalyptusServiceException
	 */
	void signupAccount(String accountName, String password, String email)
			throws EucalyptusServiceException;

	/**
	 * Sign up a new user in an account.
	 * 
	 * @param userName
	 * @param accountName
	 * @param password
	 * @param email
	 * @throws EucalyptusServiceException
	 */
	void signupUser(String userName, String accountName, String password,
			String email) throws EucalyptusServiceException;

	/**
	 * Approve account signups.
	 * 
	 * @param session
	 * @param accountNames
	 * @return
	 * @throws EucalyptusServiceException
	 */
	ArrayList<String> approveAccounts(Session session,
			ArrayList<String> accountNames) throws EucalyptusServiceException;

	/**
	 * Reject account signups.
	 * 
	 * @param session
	 * @param accountNames
	 * @return
	 * @throws EucalyptusServiceException
	 */
	ArrayList<String> rejectAccounts(Session session,
			ArrayList<String> accountNames) throws EucalyptusServiceException;

	/**
	 * Approve user signups.
	 * 
	 * @param session
	 * @param userIds
	 * @return
	 * @throws EucalyptusServiceException
	 */
	ArrayList<String> approveUsers(Session session, ArrayList<String> userIds)
			throws EucalyptusServiceException;

	/**
	 * Reject user signups.
	 * 
	 * @param session
	 * @param userIds
	 * @return
	 * @throws EucalyptusServiceException
	 */
	ArrayList<String> rejectUsers(Session session, ArrayList<String> userIds)
			throws EucalyptusServiceException;

	/**
	 * Confirm a user for both account signup (confirm the admin) and user
	 * signup.
	 * 
	 * @param confirmationCode
	 * @throws EucalyptusServiceException
	 */
	void confirmUser(String confirmationCode) throws EucalyptusServiceException;

	/**
	 * Request a reset of password.
	 * 
	 * @param userName
	 * @param accountName
	 * @param email
	 * @throws EucalyptusServiceException
	 */
	void requestPasswordRecovery(String userName, String accountName,
			String email) throws EucalyptusServiceException;

	/**
	 * Reset the password based on the confirmation code.
	 * 
	 * @param confirmationCode
	 * @param password
	 * @throws EucalyptusServiceException
	 */
	void resetPassword(String confirmationCode, String password)
			throws EucalyptusServiceException;

	/**
	 * Get cloud info for RightScale registration.
	 * 
	 * @param session
	 * @param setExternalHostport
	 * @return
	 * @throws EucalyptusServiceException
	 */
	public CloudInfo getCloudInfo(Session session, boolean setExternalHostPort)
			throws EucalyptusServiceException;

	/**
	 * Get Start Guide snippet.
	 * 
	 * @param session
	 * @param snippet
	 * @return
	 * @throws EucalyptusServiceException
	 */
	public ArrayList<GuideItem> getGuide(Session session, String snippet)
			throws EucalyptusServiceException;

	/**
	 * Get user's security code.
	 * 
	 * @param session
	 * @return
	 * @throws EucalyptusServiceException
	 */
	public String getUserToken(Session session)
			throws EucalyptusServiceException;

	SearchResult lookupDeviceServer(Session session, String search,
			SearchRange range, int queryState);

	Map<Integer, Integer> getDeviceServerCounts(Session session);

	SearchResultRow modifyDeviceServerState(Session session,
			SearchResultRow row, int state);

	boolean addDeviceServer(Session session, String mark, String name,
			String conf, String ip, int bw, int state, String room);

	List<SearchResultRow> deleteDeviceServer(Session session,
			List<SearchResultRow> list);

	SearchResult lookupDeviceCPU(Session session, String search,
			SearchRange range, int queryState);

	Map<Integer, Integer> getDeviceCPUCounts(Session session);

	SearchResultRow modifyDeviceCPUService(Session session,
			SearchResultRow row, String endtime, int state);

	List<SearchResultRow> deleteDeviceCPUService(Session session,
			List<SearchResultRow> list);

	List<SearchResultRow> deleteDeviceCPUDevice(Session session,
			List<SearchResultRow> list);

	SearchResultRow addDeviceCPUService(Session session, SearchResultRow row,
			String account, String user, String starttime, int life, int state);

	boolean addDeviceCPUDevice(Session session, String serverMark, String name,
			String vendor, String model, double ghz, double cache, int num);

	DeviceCPUDeviceAddView.DataCache lookupDeviceCPUInfo(Session session);

	List<String> listDeviceCPUAccounts(Session session);

	List<String> listDeviceCPUUsersByAccount(Session session, String account);

	SearchResult lookupDeviceMemory(Session session, String search,
			SearchRange range, int queryState);

	Map<Integer, Long> getDeviceMemoryCounts(Session session);

	SearchResultRow modifyDeviceMemoryService(Session session,
			SearchResultRow row, String endtime, int state);

	List<SearchResultRow> deleteDeviceMemoryService(Session session,
			List<SearchResultRow> list);

	List<SearchResultRow> deleteDeviceMemoryDevice(Session session,
			List<SearchResultRow> list);

	SearchResultRow addDeviceMemoryService(Session session,
			SearchResultRow row, String account, String user, long used,
			String starttime, int life, int state);

	boolean addDeviceMemoryDevice(Session session, String serverMark,
			String name, long total, int num);

	DeviceMemoryDeviceAddView.DataCache lookupDeviceMemoryInfo(Session session);

	List<String> listDeviceMemoryAccounts(Session session);

	List<String> listDeviceMemoryUsersByAccount(Session session, String account);

	SearchResult lookupDeviceDisk(Session session, String search,
			SearchRange range, int queryState);

	Map<Integer, Long> getDeviceDiskCounts(Session session);

	SearchResultRow modifyDeviceDiskService(Session session,
			SearchResultRow row, String endtime, int state);

	List<SearchResultRow> deleteDeviceDiskService(Session session,
			List<SearchResultRow> list);

	List<SearchResultRow> deleteDeviceDiskDevice(Session session,
			List<SearchResultRow> list);

	SearchResultRow addDeviceDiskService(Session session, SearchResultRow row,
			String account, String user, long used, String starttime, int life,
			int state);

	boolean addDeviceDiskDevice(Session session, String serverMark,
			String name, long total, int num);

	DeviceDiskDeviceAddView.DataCache lookupDeviceDiskInfo(Session session);

	List<String> listDeviceDiskAccounts(Session session);

	List<String> listDeviceDiskUsersByAccount(Session session, String account);

	SearchResult lookupDeviceIP(Session session, String search,
			SearchRange range, int queryState, int queryType);

	Map<Integer, Integer> getDeviceIPCounts(Session session, int queryType);

	SearchResultRow modifyDeviceIPService(Session session, SearchResultRow row,
			String endtime, int state);

	List<SearchResultRow> deleteDeviceIPService(Session session,
			List<SearchResultRow> list);

	List<SearchResultRow> deleteDeviceIPDevice(Session session,
			List<SearchResultRow> list);

	SearchResultRow addDeviceIPService(Session session, SearchResultRow row,
			String account, String user, String vmMark, String starttime,
			int life, int state);

	boolean addDeviceIPDevice(Session session, List<String> publicList,
			List<String> privateList);

	List<String> listDeviceVMsByUser(Session session, String account,
			String user);

	List<String> listDeviceIPAccounts(Session session);

	List<String> listDeviceIPUsersByAccount(Session session, String account);

	SearchResult lookupDeviceBW(Session session, String search,
			SearchRange range);

	SearchResultRow modifyDeviceBWService(Session session, SearchResultRow row,
			String endtime);

	List<SearchResultRow> deleteDeviceBWService(Session session,
			List<SearchResultRow> list);

	boolean addDeviceBWService(Session session, String account, String user, String starttime, int life, String ip, long bandwidth);

	List<String> listDeviceBWAccounts(Session session);

	List<String> listDeviceBWUsersByAccount(Session session, String account);

	List<String> listDeviceIPsByUser(Session session, String account,
			String user);

	SearchResult lookupDeviceTemplate(Session session, String search,
			SearchRange range, Date starttime, Date endtime);

	List<SearchResultRow> deleteDeviceTemplate(Session session,
			List<SearchResultRow> list);

	boolean addDeviceTemplate(Session session, String mark, String cpu, int ncpus, String mem, String disk, String bw, String image);
	SearchResultRow modifyDeviceTempate(Session session, SearchResultRow row, String cpu, int ncpus, String mem, String disk, String bw, String image);
	List<String> listDeviceTemplateCPUNames(Session session);

	SearchResult lookupDeviceVM(Session session, String search,
			SearchRange range, int queryState)
			throws EucalyptusServiceException;
	
	

	/**
	 * Add a user app.
	 * 
	 * @param session
	 * @param userId
	 * @throws EucalyptusServiceException
	 */
	void addUserApp(Session session, String userId, String templateId) throws EucalyptusServiceException;
	
	/**
	 * Delete user applications.
	 * 
	 * @param session
	 * @param ids
	 * @throws EucalyptusServiceException
	 */
	void deleteUserApp(Session session, ArrayList<String> ids) throws EucalyptusServiceException;
	
	/**
	 * Modify user applications.
	 * 
	 * @param session
	 * @param ids
	 * @throws EucalyptusServiceException
	 */
	void modifyUserApp(Session session, ArrayList<UserApp> userApps) throws EucalyptusServiceException;
	
	/**
	 * Count user applications.
	 * 
	 * @param session
	 * @throws EucalyptusServiceException
	 */
	ArrayList<UserAppStateCount> countUserApp(Session session) throws EucalyptusServiceException;
	
  
  /**
   * Acquire access keys by user id
   * @param session
   * @param userId
   * @return
   * @throws EucalyptusServiceException
   */
//  SearchResult listAccessKeysByUser(Session session, String userId) throws EucalyptusServiceException;
  
  /**
   * Acquire all access keys
   * @param session
   * @return
   * @throws EucalyptusServiceException
   */
//  SearchResult listAccessKeys(Session session) throws EucalyptusServiceException;
  
  /**
   * Acquire certificates by user id
   * @param session
   * @param userId
   * @return
   * @throws EucalyptusServiceException
   */
//  SearchResult listCertificatesByUser(Session session, String userId) throws EucalyptusServiceException;
  
  /**
   * Acquire all certificates
   * @param session
   * @return
   * @throws EucalyptusServiceException
   */
//  SearchResult listCertificates(Session session) throws EucalyptusServiceException;
  
  /**
   * 
   * @param session
   * @return
   * @throws EucalyptusServiceException
   */
//  SearchResult listPolicies(Session session) throws EucalyptusServiceException;
  
  void modifyPolicy(Session session, String policyId, String name, String content) throws EucalyptusServiceException;

}
