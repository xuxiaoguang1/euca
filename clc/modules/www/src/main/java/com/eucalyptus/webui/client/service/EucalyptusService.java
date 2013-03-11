package com.eucalyptus.webui.client.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.eucalyptus.webui.client.session.Session;
import com.eucalyptus.webui.shared.config.SysConfig;
import com.eucalyptus.webui.shared.resource.VMImageType;
import com.eucalyptus.webui.shared.resource.device.AreaInfo;
import com.eucalyptus.webui.shared.resource.device.BWServiceInfo;
import com.eucalyptus.webui.shared.resource.device.CPUInfo;
import com.eucalyptus.webui.shared.resource.device.CabinetInfo;
import com.eucalyptus.webui.shared.resource.device.DiskInfo;
import com.eucalyptus.webui.shared.resource.device.IPServiceInfo;
import com.eucalyptus.webui.shared.resource.device.MemoryInfo;
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
			SearchRange range, EnumUserAppStatus state)
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
	
	Map<String, Integer> lookupDeviceAccountNames(Session session) throws EucalyptusServiceException;
	Map<String, Integer> lookupDeviceUserNamesByAccountID(Session session, int account_id) throws EucalyptusServiceException;
	
	SearchResult lookupDeviceArea(Session session, SearchRange range) throws EucalyptusServiceException; 
	void modifyDeviceArea(Session session, int area_id, String area_desc) throws EucalyptusServiceException;
	void deleteDeviceArea(Session session, List<Integer> area_ids) throws EucalyptusServiceException;
	void createDeviceArea(Session session, String area_name, String area_desc) throws EucalyptusServiceException;
	Map<String, Integer> lookupDeviceAreaNames(Session session) throws EucalyptusServiceException;
	AreaInfo lookupDeviceAreaByID(Session session, int area_id) throws EucalyptusServiceException;
	
	SearchResult lookupDeviceRoom(Session session, SearchRange range) throws EucalyptusServiceException;
	void createDeviceRoom(Session session, String room_name, String room_desc, int area_id) throws EucalyptusServiceException;
	void modifyDeviceRoom(Session session, int room_id, String room_desc) throws EucalyptusServiceException;
	void deleteDeviceRoom(Session session, List<Integer> room_ids) throws EucalyptusServiceException;
	Map<String, Integer> lookupDeviceRoomNamesByAreaID(Session session, int area_id) throws EucalyptusServiceException;
	RoomInfo lookupDeviceRoomByID(Session session, int room_id) throws EucalyptusServiceException;
	
	SearchResult lookupDeviceCabinet(Session session, SearchRange range) throws EucalyptusServiceException;
	void createDeviceCabinet(Session session, String cabinet_name, String cabinet_desc, int room_id) throws EucalyptusServiceException;
	void modifyDeviceCabinet(Session session, int cabinet_id, String cabinet_desc) throws EucalyptusServiceException;
	void deleteDeviceCabinet(Session session, List<Integer> cabinet_ids) throws EucalyptusServiceException;
	Map<String, Integer> lookupDeviceCabinetNamesByRoomID(Session session, int room_id) throws EucalyptusServiceException;
	CabinetInfo lookupDeviceCabinetByID(Session session, int cabinet_id) throws EucalyptusServiceException;
	
	SearchResult lookupDeviceServer(Session session, SearchRange range, ServerState server_state) throws EucalyptusServiceException;
	Map<Integer, Integer> lookupDeviceServerCounts(Session session) throws EucalyptusServiceException;
	void createDeviceServer(Session session, String server_name, String server_desc, String server_euca, String server_ip, int server_bw, ServerState server_state, int cabinet_id) throws EucalyptusServiceException;
	void modifyDeviceServer(Session session, int server_id, String server_desc, String server_ip, int server_bw) throws EucalyptusServiceException;
	void modifyDeviceServerState(Session session, int server_id, ServerState server_state) throws EucalyptusServiceException;
    void deleteDeviceServer(Session session, List<Integer> server_ids) throws EucalyptusServiceException;
    Map<String, Integer> lookupDeviceServerNamesByCabinetID(Session session, int cabinet_id) throws EucalyptusServiceException;
    ServerInfo lookupDeviceServerByID(Session session, int server_id) throws EucalyptusServiceException;
    
    SearchResult lookupDeviceCPU(Session session, SearchRange range, CPUState cs_state) throws EucalyptusServiceException;
    Map<Integer, Integer> lookupDeviceCPUCounts(Session session) throws EucalyptusServiceException;
    void createDeviceCPU(Session session, String cpu_desc, int cpu_total, int server_id) throws EucalyptusServiceException ;
    void modifyDeviceCPU(Session session, int cpu_id, String cpu_desc, int cpu_total) throws EucalyptusServiceException;
    void deleteDeviceCPU(Session session, List<Integer> cpu_ids) throws EucalyptusServiceException;
    public CPUInfo lookupDeviceCPUByID(Session session, int cpu_id) throws EucalyptusServiceException;
    
    SearchResult lookupDeviceMemory(Session session, SearchRange range, MemoryState ms_state) throws EucalyptusServiceException;
    Map<Integer, Long> lookupDeviceMemoryCounts(Session session) throws EucalyptusServiceException;
    void createDeviceMemory(Session session, String mem_desc, long mem_total, int server_id) throws EucalyptusServiceException;
    void modifyDeviceMemory(Session session, int mem_id, String mem_desc, long mem_total) throws EucalyptusServiceException;
    void deleteDeviceMemory(Session session, List<Integer> mem_ids) throws EucalyptusServiceException;
    MemoryInfo lookupDeviceMemoryByID(Session session, int mem_id) throws EucalyptusServiceException;
    
    SearchResult lookupDeviceDisk(Session session, SearchRange range, DiskState ds_state) throws EucalyptusServiceException;
    Map<Integer, Long> lookupDeviceDiskCounts(Session session) throws EucalyptusServiceException;
    void createDeviceDisk(Session session, String disk_desc, long disk_total, int server_id) throws EucalyptusServiceException;
    void modifyDeviceDisk(Session session, int disk_id, String disk_desc, long disk_total) throws EucalyptusServiceException;
    void deleteDeviceDisk(Session session, List<Integer> disk_ids) throws EucalyptusServiceException;
    DiskInfo lookupDeviceDiskByID(Session session, int disk_id) throws EucalyptusServiceException;
    
    SearchResult lookupDeviceIP(Session session, SearchRange range, IPType ip_type, IPState ip_state) throws EucalyptusServiceException;
    Map<Integer, Integer> lookupDeviceIPCounts(Session session, IPType ip_type) throws EucalyptusServiceException;
    void createDeviceIPService(Session session, IPType ip_type, String is_desc, int count, int user_id) throws EucalyptusServiceException;
    void deleteDeviceIPService(Session session, List<Integer> ip_ids) throws EucalyptusServiceException;
    IPServiceInfo lookupDeviceIPServiceByID(Session session, int ip_id) throws EucalyptusServiceException;
    
    SearchResult lookupDeviceBW(Session session, SearchRange range) throws EucalyptusServiceException;
    void createDeviceBWService(Session session, String bs_desc, int bs_bw_max, int ip_id) throws EucalyptusServiceException;
    void modifyDeviceBWService(Session session, int bs_id, String bs_desc, int bs_bw_max) throws EucalyptusServiceException;
    void deleteDeviceBWService(Session session, List<Integer> bs_ids) throws EucalyptusServiceException;
    BWServiceInfo lookupDeviceBWServiceByID(Session session, int bs_id) throws EucalyptusServiceException;
    public Map<String, Integer> lookupDeviceIPsWihtoutBWService(Session session, int account_id, int user_id) throws EucalyptusServiceException;
    
    SearchResult lookupDeviceTemplate(Session session, SearchRange range) throws EucalyptusServiceException;
    void createDeviceTemplateService(Session session, String template_name, String template_desc, int template_ncpus, long template_mem, long template_disk, int template_bw) throws EucalyptusServiceException;
    void modifyDeviceTemplateService(Session session, int template_id, String template_desc, int template_ncpus, long template_mem, long template_disk, int template_bw) throws EucalyptusServiceException;
    void deleteDeviceTemplateService(Session session, List<Integer> template_ids) throws EucalyptusServiceException;
    TemplateInfo lookupDeviceTemplateInfoByID(Session session, int template_id) throws EucalyptusServiceException;
    Map<String, Integer> lookupDeviceTemplates(Session session) throws EucalyptusServiceException;
    
	DevicePriceInfo lookupDeviceCPUPrice(Session session) throws EucalyptusServiceException;
	DevicePriceInfo lookupDeviceMemoryPrice(Session session) throws EucalyptusServiceException;
	DevicePriceInfo lookupDeviceDiskPrice(Session session) throws EucalyptusServiceException;
	DevicePriceInfo lookupDeviceBWPrice(Session session) throws EucalyptusServiceException;
	void modifyDeviceCPUPrice(Session session, String op_desc, double op_price) throws EucalyptusServiceException;
	void modifyDeviceMemoryPrice(Session session, String op_desc, double op_price) throws EucalyptusServiceException;
	void modifyDeviceDiskPrice(Session session, String op_desc, double op_price) throws EucalyptusServiceException;
	void modifyDeviceBWPrice(Session session, String op_desc, double op_price) throws EucalyptusServiceException;
	
	SearchResult lookupDeviceTemplatePriceByDate(Session session, SearchRange range, Date dateBegin, Date dateEnd) throws EucalyptusServiceException;
	void createDeviceTemplatePriceByID(Session session, int template_id, String tp_desc, double tp_cpu, double tp_mem, double tp_disk, double tp_bw) throws EucalyptusServiceException;
    void modifyDeviceTemplatePrice(Session session, int tp_id, String tp_desc, double tp_cpu, double tp_mem, double tp_disk, double tp_bw) throws EucalyptusServiceException;
    void deleteDeviceTemplatePrice(Session session, List<Integer> tp_ids) throws EucalyptusServiceException;
    TemplatePriceInfo lookupDeviceTemplatePriceByID(Session session, int tp_id) throws EucalyptusServiceException;
    Map<String, Integer> lookupDeviceTemplatesWithoutPrice(Session session) throws EucalyptusServiceException;
    
	List<String> listDeviceVMsByUser(Session session, String account,
			String user);

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
	void addUserApp(Session session, UserApp userApp) throws EucalyptusServiceException;
	
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
	void confirmUserApp(Session session, List<String> userAppId, EnumUserAppStatus userAppState) throws EucalyptusServiceException;
	
	/**
	 * Count user applications.
	 * 
	 * @param session
	 * @throws EucalyptusServiceException
	 */
	ArrayList<UserAppStateCount> countUserApp(Session session) throws EucalyptusServiceException;
	
	/**
	 * query vm image type list.
	 * 
	 * @param session
	 * @throws EucalyptusServiceException
	 */
	ArrayList<VMImageType> queryVMImageType(Session session) throws EucalyptusServiceException;
	
	/**
	 * query key pair list.
	 * 
	 * @param session
	 * @throws EucalyptusServiceException
	 */
	List<String> queryKeyPair(Session session) throws EucalyptusServiceException;
	
	/**
	 * read system configuration
	 * 
	 * @param session
	 * @throws EucalyptusServiceException
	 */
	SysConfig readSysConfig() throws EucalyptusServiceException;
	
	/**
	 * query search table size conf.
	 * 
	 * @param session
	 * @throws EucalyptusServiceException
	 */
	List<String> querySecurityGroup(Session session) throws EucalyptusServiceException;
  
  
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

  SearchResult lookupHistory(Session session, String search, SearchRange range) throws EucalyptusServiceException;

}
