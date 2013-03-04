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
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface EucalyptusServiceAsync {

	void login(String accountName, String userName, String password, AsyncCallback<Session> callback);

	void checkUserExisted(String accountName, String userName, AsyncCallback<Void> callback);

	void logout(Session session, AsyncCallback<Void> callback);

	void getLoginUserProfile(Session session, AsyncCallback<LoginUserProfile> callback);

	void getSystemProperties(Session session, AsyncCallback<HashMap<String, String>> callback);

	void getQuickLinks(Session session, AsyncCallback<ArrayList<QuickLinkTag>> callback);

	void lookupAccount(Session session, String search, SearchRange range, AsyncCallback<SearchResult> callback);

	void lookupConfiguration(Session session, String search, SearchRange range, AsyncCallback<SearchResult> callback);

	void setConfiguration(Session session, SearchResultRow config, AsyncCallback<Void> callback);

	void lookupVmType(Session session, String query, SearchRange range, AsyncCallback<SearchResult> asyncCallback);

	void setVmType(Session session, SearchResultRow result, AsyncCallback<Void> asyncCallback);

	void updateUserState(Session session, ArrayList<String> ids, EnumState userState, AsyncCallback<Void> callback);

	void lookupPolicy(Session session, String search, SearchRange range, AsyncCallback<SearchResult> callback);

	void lookupKey(Session session, String search, SearchRange range, AsyncCallback<SearchResult> callback);

	void lookupCertificate(Session session, String search, SearchRange range, AsyncCallback<SearchResult> callback);

	void lookupImage(Session session, String search, SearchRange range, AsyncCallback<SearchResult> callback);

	void createAccount(Session session, AccountInfo account, AsyncCallback<Void> callback);

	void deleteAccounts(Session session, ArrayList<String> ids, AsyncCallback<Void> callback);

	void updateAccountState(Session session, ArrayList<String> ids, EnumState userState,
	        AsyncCallback<Void> asyncCallback);

	void listAccounts(Session session, AsyncCallback<ArrayList<AccountInfo>> asyncCallback);

	void createGroups(Session session, String accountId, String names, String path,
	        AsyncCallback<ArrayList<String>> callback);

	void createGroup(Session session, GroupInfo group, AsyncCallback<Void> callback);

	void deleteGroups(Session session, ArrayList<String> ids, AsyncCallback<Void> callback);

	void lookupGroup(Session session, String search, SearchRange range, AsyncCallback<SearchResult> callback);

	void listGroups(Session session, AsyncCallback<ArrayList<GroupInfo>> asyncCallback);

	void updateGroupState(Session session, ArrayList<String> ids, EnumState userState, AsyncCallback<Void> asyncCallback);

	void createUsers(Session session, String accountId, String names, String path,
	        AsyncCallback<ArrayList<String>> callback);

	void createUser(Session session, UserInfo user, AsyncCallback<Void> callback);

	void deleteUsers(Session session, ArrayList<String> ids, AsyncCallback<Void> callback);

	void lookupUser(Session session, String search, SearchRange range, AsyncCallback<SearchResult> callback);

	void lookupUserByGroupId(Session session, int groupId, SearchRange range, AsyncCallback<SearchResult> callback);

	void lookupUserByAccountId(Session session, int accountId, SearchRange range, AsyncCallback<SearchResult> callback);

	void lookupUserExcludeGroupId(Session session, int accountId, int groupId, SearchRange range, AsyncCallback<SearchResult> callback);

	void lookupUserApp( Session session, String search, SearchRange range, EnumUserAppStatus state, AsyncCallback<SearchResult> callback);
	
	void removeUsersFromGroup(Session session, ArrayList<String> userIds, AsyncCallback<Void> callback);

	void deletePolicy(Session session, ArrayList<String> ids, AsyncCallback<Void> callback);

	void deleteAccessKey(Session session, ArrayList<String> ids, AsyncCallback<Void> callback);

	void deleteCertificate(Session session, ArrayList<String> ids, AsyncCallback<Void> callback);

	void addAccountPolicy(Session session, String accountId, String name, String document, AsyncCallback<Void> callback);

	void addUserPolicy(Session session, String usertId, String name, String document, AsyncCallback<Void> callback);

	void addGroupPolicy(Session session, String groupId, String name, String document, AsyncCallback<Void> callback);

	void addUsersToGroupsByName(Session session, String userNames, ArrayList<String> groupIds,
	        AsyncCallback<Void> callback);

	void addUsersToGroupsById(Session session, ArrayList<String> userIds, int groupId, AsyncCallback<Void> callback);

	void removeUsersFromGroupsById(Session session, ArrayList<String> userIds, String groupNames,
	        AsyncCallback<Void> callback);

	void modifyUser(Session session, ArrayList<String> keys, ArrayList<String> values, AsyncCallback<Void> callback);

	void modifyIndividual(Session session, String title, String mobile, String email,
	        AsyncCallback<LoginUserProfile> callback);

	void modifyGroup(Session session, ArrayList<String> values, AsyncCallback<Void> callback);

	void modifyAccessKey(Session session, ArrayList<String> values, boolean active, AsyncCallback<Void> callback);

	void modifyCertificate(Session session, ArrayList<String> ids, Boolean active, Boolean revoked, AsyncCallback<Void> callback);

	void addAccessKey(Session session, String userId, AsyncCallback<Void> callback);

	void addCertificate(Session session, String userId, String pem, AsyncCallback<Void> callback);

	void changePassword(Session session, String oldPass, String newPass, String email, AsyncCallback<Void> callback);

	void signupAccount(String accountName, String password, String email, AsyncCallback<Void> callback);

	void approveAccounts(Session session, ArrayList<String> accountNames, AsyncCallback<ArrayList<String>> callback);

	void rejectAccounts(Session session, ArrayList<String> accountNames, AsyncCallback<ArrayList<String>> callback);

	void approveUsers(Session session, ArrayList<String> userIds, AsyncCallback<ArrayList<String>> callback);

	void rejectUsers(Session session, ArrayList<String> userIds, AsyncCallback<ArrayList<String>> callback);

	void signupUser(String userName, String accountName, String password, String email, AsyncCallback<Void> callback);

	void confirmUser(String confirmationCode, AsyncCallback<Void> callback);

	void requestPasswordRecovery(String userName, String accountName, String email, AsyncCallback<Void> callback);

	void resetPassword(String confirmationCode, String password, AsyncCallback<Void> callback);

	void getCloudInfo(Session session, boolean setExternalHostPort, AsyncCallback<CloudInfo> callback);

	void getGuide(Session session, String snippet, AsyncCallback<ArrayList<GuideItem>> callback);

	void getUserToken(Session session, AsyncCallback<String> callback);
	

    void lookupDeviceAccountNames(Session session, AsyncCallback<Map<String, Integer>> callback);
    void lookupDeviceUserNamesByAccountID(Session session, int account_id, AsyncCallback<Map<String, Integer>> callback);
	
	void lookupDeviceAreaByDate(Session session, SearchRange range, Date dateBegin, Date dateEnd, AsyncCallback<SearchResult> callback);
	void createDeviceArea(Session session, String area_name, String area_desc, AsyncCallback<Void> callback);
	void modifyDeviceArea(Session session, int area_id, String area_desc, AsyncCallback<Void> callback);
	void deleteDeviceArea(Session session, List<Integer> area_ids, AsyncCallback<Void> callback);
	void lookupDeviceAreaNames(Session session, AsyncCallback<Map<String, Integer>> callback);
	void lookupDeviceAreaByID(Session session, int area_id, AsyncCallback<AreaInfo> callback);

	void lookupDeviceRoomByDate(Session session, SearchRange range, Date dateBegin, Date dateEnd, AsyncCallback<SearchResult> callback);
	void createDeviceRoom(Session session, String room_name, String room_desc, int area_id, AsyncCallback<Void> callback);
	void modifyDeviceRoom(Session session, int room_id, String room_desc, AsyncCallback<Void> callback);
	void deleteDeviceRoom(Session session, List<Integer> room_ids, AsyncCallback<Void> callback);
	void lookupDeviceRoomNamesByAreaID(Session session, int area_id, AsyncCallback<Map<String, Integer>> callback);
	void lookupDeviceRoomByID(Session session, int room_id, AsyncCallback<RoomInfo> callback);
	
	void lookupDeviceCabinetByDate(Session session, SearchRange range, Date dateBegin, Date dateEnd, AsyncCallback<SearchResult> callback);
	void createDeviceCabinet(Session session, String cabinet_name, String cabinet_desc, int room_id, AsyncCallback<Void> callback);
	void modifyDeviceCabinet(Session session, int cabinet_id, String cabinet_desc, AsyncCallback<Void> callback);
	void deleteDeviceCabinet(Session session, List<Integer> cabinet_ids, AsyncCallback<Void> callback);
    void lookupDeviceCabinetNamesByRoomID(Session session, int room_id, AsyncCallback<Map<String, Integer>> callback);
    void lookupDeviceCabinetByID(Session session, int cabinet_id, AsyncCallback<CabinetInfo> callback);
    
    void lookupDeviceServerByDate(Session session, SearchRange range, ServerState server_state, Date dateBegin, Date dateEnd, AsyncCallback<SearchResult> callback);
    void lookupDeviceServerCounts(Session session, AsyncCallback<Map<Integer, Integer>> callback);
    void createDeviceServer(Session session, String server_name, String server_desc, String server_ip, int server_bw, ServerState server_state, int cabinet_id, AsyncCallback<Void> callback);
    void modifyDeviceServer(Session session, int server_id, String server_desc, String server_ip, int server_bw, AsyncCallback<Void> callback);
	void modifyDeviceServerState(Session session, int server_id, ServerState server_state, AsyncCallback<Void> callback);
    void deleteDeviceServer(Session session, List<Integer> server_ids, AsyncCallback<Void> callback);
    void lookupDeviceServerNamesByCabinetID(Session session, int cabinet_id, AsyncCallback<Map<String, Integer>> callback);
    void lookupDeviceServerByID(Session session, int server_id, AsyncCallback<ServerInfo> callback);
    
    void lookupDeviceCPUByDate(Session session, SearchRange range, CPUState cs_state, Date dateBegin, Date dateEnd, AsyncCallback<SearchResult> callback);
    void lookupDeviceCPUCounts(Session session, AsyncCallback<Map<Integer, Integer>> callback);
    void createDeviceCPU(Session session, String cpu_name, String cpu_desc, int cpu_total, int server_id, AsyncCallback<Void> callback);
    void modifyDeviceCPU(Session session, int cpu_id, String cpu_desc, int cpu_total, AsyncCallback<Void> callback);
    void deleteDeviceCPU(Session session, List<Integer> cpu_ids, AsyncCallback<Void> callback);
    void lookupDeviceCPUByID(Session session, int cpu_id, AsyncCallback<CPUInfo> callback);
    
    void lookupDeviceMemoryByDate(Session session, SearchRange range, MemoryState ms_state, Date dateBegin, Date dateEnd, AsyncCallback<SearchResult> callback);
    void lookupDeviceMemoryCounts(Session session, AsyncCallback<Map<Integer, Long>> callback);
    void createDeviceMemory(Session session, String mem_name, String mem_desc, long mem_size, int server_id, AsyncCallback<Void> callback);
    void modifyDeviceMemory(Session session, int mem_id, String mem_desc, long mem_size, AsyncCallback<Void> callback);
    void deleteDeviceMemory(Session session, List<Integer> mem_ids, AsyncCallback<Void> callback);
    void lookupDeviceMemoryByID(Session session, int mem_id, AsyncCallback<MemoryInfo> callback);
    
    void lookupDeviceDiskByDate(Session session, SearchRange range, DiskState disk_state, Date dateBegin, Date dateEnd, AsyncCallback<SearchResult> callback);
    void lookupDeviceDiskCounts(Session session, AsyncCallback<Map<Integer, Long>> callback);
    void createDeviceDisk(Session session, String disk_name, String disk_desc, long disk_size, int server_id, AsyncCallback<Void> callback);
    void modifyDeviceDisk(Session session, int disk_id, String disk_desc, long disk_size, AsyncCallback<Void> callback);
    void deleteDeviceDisk(Session session, List<Integer> disk_ids, AsyncCallback<Void> callback);
    void lookupDeviceDiskByID(Session session, int disk_id, AsyncCallback<DiskInfo> callback);

    void lookupDeviceIPByDate(Session session, SearchRange range, IPType ip_type, IPState ip_state, Date dateBegin, Date dateEnd, AsyncCallback<SearchResult> callback);
	void lookupDeviceIPCounts(Session session, IPType ip_type, AsyncCallback<Map<Integer, Integer>> callback);
	void createDeviceIP(Session session, String ip_addr, String ip_desc, IPType ip_type, AsyncCallback<Void> callback);
	void createDeviceIPService(Session session, String is_desc, IPState is_state, Date is_starttime, Date is_endtime, int ip_id, int user_id, AsyncCallback<Void> callback);
	void modifyDeviceIPService(Session session, int ip_id, String is_desc, Date is_starttime, Date is_endtime, AsyncCallback<Void> callback);
	void modifyDeviceIP(Session session, int ip_id, String ip_desc, IPType ip_type, AsyncCallback<Void> callback);
	void deleteDeviceIP(Session session, List<Integer> ip_ids, AsyncCallback<Void> callback);
	void deleteDeviceIPService(Session session, List<Integer> ip_ids, AsyncCallback<Void> callback);
	void lookupDeviceIPUnusedByIPType(Session session, IPType ip_type, AsyncCallback<Map<String, Integer>> callback);
    void lookupDeviceIPServiceByID(Session session, int ip_id, AsyncCallback<IPServiceInfo> callback);
	
	void lookupDeviceBWByDate(Session session, SearchRange range, Date dateBegin, Date dateEnd, AsyncCallback<SearchResult> callback);
	void createDeviceBWService(Session session, String bs_desc, int bs_bw_max, Date bs_starttime, Date bs_endtime, int ip_id, AsyncCallback<Void> callback);
	void modifyDeviceBWService(Session session, int bs_id, String bs_desc, int bs_bw_max, Date bs_starttime, Date bs_endtime, AsyncCallback<Void> callback);
	void deleteDeviceBWService(Session session, List<Integer> bs_ids, AsyncCallback<Void> callback);
	void lookupDeviceBWServiceByID(Session session, int bs_id, AsyncCallback<BWServiceInfo> callback);
	void lookupDeviceIPsWihtoutBWService(Session session, int account_id, int user_id, AsyncCallback<Map<String, Integer>> callback);
	
	void lookupDeviceTemplateByDate(Session session, SearchRange range, Date dateBegin, Date dateEnd, AsyncCallback<SearchResult> callback);
	void createDeviceTemplateService(Session session, String template_name, String template_desc, int template_ncpus, long template_mem, long template_disk, int template_bw, AsyncCallback<Void> callback);
	void modifyDeviceTemplateService(Session session, int template_id, String template_desc, int template_ncpus, long template_mem, long template_disk, int template_bw, AsyncCallback<Void> callback);
	void deleteDeviceTemplateService(Session session, List<Integer> template_ids, AsyncCallback<Void> callback);
	void lookupDeviceTemplateInfoByID(Session session, int template_id, AsyncCallback<TemplateInfo> callback);
    void lookupTemplates(Session session, AsyncCallback<Map<String, Integer>> callback);
	
	void lookupDeviceCPUPrice(Session session, AsyncCallback<DevicePriceInfo> callback);
	void lookupDeviceMemoryPrice(Session session, AsyncCallback<DevicePriceInfo> callback);
    void lookupDeviceDiskPrice(Session session, AsyncCallback<DevicePriceInfo> callback);
    void lookupDeviceBWPrice(Session session, AsyncCallback<DevicePriceInfo> callback);
    void modifyDeviceCPUPrice(Session session, String op_desc, double op_price, AsyncCallback<Void> callback);
    void modifyDeviceMemoryPrice(Session session, String op_desc, double op_price, AsyncCallback<Void> callback);
    void modifyDeviceDiskPrice(Session session, String op_desc, double op_price, AsyncCallback<Void> callback);
    void modifyDeviceBWPrice(Session session, String op_desc, double op_price, AsyncCallback<Void> callback);
    
    void lookupDeviceTemplatePriceByDate(Session session, SearchRange range, Date dateBegin, Date dateEnd, AsyncCallback<SearchResult> callback);
    void createDeviceTemplatePriceByID(Session session, int template_id, String tp_desc, double tp_cpu, double tp_mem, double tp_disk, double tp_bw, AsyncCallback<Void> callback);
    void modifyDeviceTemplatePrice(Session session, int tp_id, String tp_desc, double tp_cpu, double tp_mem, double tp_disk, double tp_bw, AsyncCallback<Void> callback);
    void deleteDeviceTemplatePrice(Session session, List<Integer> tp_ids, AsyncCallback<Void> callback);
    void lookupDeviceTemplatePriceByID(Session session, int tp_id, AsyncCallback<TemplatePriceInfo> callback);
    void lookupDeviceTemplatesWithoutPrice(Session session, AsyncCallback<Map<String, Integer>> callback);
	
	void listDeviceVMsByUser(Session session, String account, String user, AsyncCallback<List<String>> callback);

	void lookupDeviceVM(Session session, String search, SearchRange range, int queryState, AsyncCallback<SearchResult> callback);
	
	void addUserApp(Session session, UserApp userApp, AsyncCallback<Void> callback);
	void deleteUserApp(Session session, ArrayList<String> ids, AsyncCallback<Void> callback);
	void confirmUserApp(Session session, List<String> userAppId, EnumUserAppStatus userAppState, AsyncCallback<Void> callback);
	void countUserApp(Session session, AsyncCallback<ArrayList<UserAppStateCount>> callback);
	void queryVMImageType(Session session, AsyncCallback<ArrayList<VMImageType>> callback);
	
	void queryKeyPair(Session session, AsyncCallback<List<String>> callback);
	void querySecurityGroup(Session session, AsyncCallback<List<String>> callback);
	
	void readSysConfig(AsyncCallback<SysConfig> callback);
	
//	void listAccessKeysByUser(Session session, String userId, AsyncCallback<SearchResult> callback);
//	void listAccessKeys(Session session, AsyncCallback<SearchResult> callback);
//	void listCertificatesByUser(Session session, String userId, AsyncCallback<SearchResult> callback);
//	void listCertificates(Session session, AsyncCallback<SearchResult> callback);
//	void listPolicies(Session session, AsyncCallback<SearchResult> callback);
	
	void modifyPolicy(Session session, String policyId, String name, String content, AsyncCallback<Void> callback);
	
	void lookupHistory(Session session, String search, SearchRange range, AsyncCallback<SearchResult> callback);

}
