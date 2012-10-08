package com.eucalyptus.webui.client.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.eucalyptus.webui.client.session.Session;
import com.eucalyptus.webui.shared.resource.Template;
import com.eucalyptus.webui.shared.resource.VMImageType;
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
	

    void lookupDeviceAccountNames(Session session, AsyncCallback<List<String>> callback);
    void lookupDeviceUserNamesByAccountName(Session session, String account_name, AsyncCallback<List<String>> callback);
	
	void lookupDeviceAreaByDate(Session session, SearchRange range, Date dateBegin, Date dateEnd, AsyncCallback<SearchResult> callback);
	void addDeviceArea(Session session, String area_name, String area_desc, AsyncCallback<Void> callback);
	void modifyDeviceArea(Session session, int area_id, String area_desc, AsyncCallback<Void> callback);
	void deleteDeviceArea(Session session, List<Integer> area_ids, AsyncCallback<Void> callback);
	void lookupDeviceAreaNames(Session session, AsyncCallback<List<String>> callback);

	void lookupDeviceRoomByDate(Session session, SearchRange range, Date dateBegin, Date dateEnd, AsyncCallback<SearchResult> callback);
	void addDeviceRoom(Session session, String room_name, String room_desc, String area_name, AsyncCallback<Void> callback);
	void modifyDeviceRoom(Session session, int room_id, String room_desc, AsyncCallback<Void> callback);
	void deleteDeviceRoom(Session session, List<Integer> room_ids, AsyncCallback<Void> callback);
	void lookupDeviceRoomNamesByAreaName(Session session, String area_name, AsyncCallback<List<String>> callback);
	
	void lookupDeviceCabinetByDate(Session session, SearchRange range, Date dateBegin, Date dateEnd, AsyncCallback<SearchResult> callback);
	void addDeviceCabinet(Session session, String cabinet_name, String cabinet_desc, String room_name, AsyncCallback<Void> callback);
	void modifyDeviceCabinet(Session session, int cabinet_id, String cabinet_desc, AsyncCallback<Void> callback);
	void deleteDeviceCabinet(Session session, List<Integer> cabinet_ids, AsyncCallback<Void> callback);
    void lookupCabinetNamesByRoomName(Session session, String room_name, AsyncCallback<List<String>> callback);
    
    void lookupDeviceServerByDate(Session session, SearchRange range, ServerState server_state, Date dateBegin, Date dateEnd, AsyncCallback<SearchResult> callback);
    void lookupDeviceServerCounts(Session session, AsyncCallback<Map<Integer, Integer>> callback);
    void addDeviceServer(Session session, String server_name, String server_desc, String server_ip, int server_bw, ServerState server_state, String cabinet_name, AsyncCallback<Void> callback);
    void modifyDeviceServer(Session session, int server_id, String server_desc, String server_ip, int server_bw, ServerState server_state, AsyncCallback<Void> callback);
	void modifyDeviceServerState(Session session, int server_id, ServerState server_state, AsyncCallback<Void> callback);
    void deleteDeviceServer(Session session, List<Integer> server_ids, AsyncCallback<Void> callback);
    void lookupDeviceServerNamesByCabinetName(Session session, String cabinet_name, AsyncCallback<List<String>> callback);
    
    void lookupDeviceCPUByDate(Session session, SearchRange range, CPUState cpu_state, Date dateBegin, Date dateEnd, AsyncCallback<SearchResult> callback);
    void lookupDeviceCPUCounts(Session session, AsyncCallback<Map<Integer, Integer>> callback);
    void addDeviceCPU(Session session, String cpu_name, String cpu_desc, int cpu_total, String cpu_vendor, String cpu_model, double cpu_ghz, double cpu_cache, String server_name, AsyncCallback<Void> callback);
    void addDeviceCPUService(Session session, String cs_desc, int cs_size, Date cs_starttime, Date cs_endtime, int cpu_id, String account_name, String user_name, AsyncCallback<Void> callback);
    void modifyDeviceCPU(Session session, int cpu_id, String cpu_desc, int cpu_total, String cpu_vendor, String cpu_model, double cpu_ghz, double cpu_cache, AsyncCallback<Void> callback);
    void modifyDeviceCPUService(Session session, int cs_id, String cs_desc, Date cs_starttime, Date cs_endtime, AsyncCallback<Void> callback);
    void deleteDeviceCPU(Session session, List<Integer> cpu_ids, AsyncCallback<Void> callback);
    void deleteDeviceCPUService(Session session, List<Integer> cs_ids, AsyncCallback<Void> callback);
    void lookupDeviceCPUNamesByServerName(Session session, String server_name, AsyncCallback<List<String>> callback);
    
    void lookupDeviceMemoryByDate(Session session, SearchRange range, MemoryState memory_state, Date dateBegin, Date dateEnd, AsyncCallback<SearchResult> callback);
    void lookupDeviceMemoryCounts(Session session, AsyncCallback<Map<Integer, Long>> callback);
    void addDeviceMemory(Session session, String memory_name, String memory_desc, long memory_size, String server_name, AsyncCallback<Void> callback);
    void addDeviceMemoryService(Session session, String ms_desc, long ms_size, Date ms_starttime, Date ms_endtime, int memory_id, String account_name, String user_name, AsyncCallback<Void> callback);
    void modifyDeviceMemory(Session session, int memory_id, String memory_desc, long memory_size, AsyncCallback<Void> callback);
    void modifyDeviceMemoryService(Session session, int ms_id, String ms_desc, Date ms_starttime, Date ms_endtime, AsyncCallback<Void> callback);
    void deleteDeviceMemory(Session session, List<Integer> memory_ids, AsyncCallback<Void> callback);
    void deleteDeviceMemoryService(Session session, List<Integer> ms_ids, AsyncCallback<Void> callback);
    void lookupDeviceMemoryNamesByServerName(Session session, String server_name, AsyncCallback<List<String>> callback);
    
    void lookupDeviceDiskByDate(Session session, SearchRange range, DiskState disk_state, Date dateBegin, Date dateEnd, AsyncCallback<SearchResult> callback);
    void lookupDeviceDiskCounts(Session session, AsyncCallback<Map<Integer, Long>> callback);
    void addDeviceDisk(Session session, String disk_name, String disk_desc, long disk_size, String server_name, AsyncCallback<Void> callback);
    void addDeviceDiskService(Session session, String ds_desc, long ds_size, Date ds_starttime, Date ds_endtime, int disk_id, String account_name, String user_name, AsyncCallback<Void> callback);
    void modifyDeviceDisk(Session session, int disk_id, String disk_desc, long disk_size, AsyncCallback<Void> callback);
    void modifyDeviceDiskService(Session session, int ds_id, String ds_desc, Date ds_starttime, Date ds_endtime, AsyncCallback<Void> callback);
    void deleteDeviceDisk(Session session, List<Integer> disk_ids, AsyncCallback<Void> callback);
    void deleteDeviceDiskService(Session session, List<Integer> ds_ids, AsyncCallback<Void> callback);
    void lookupDeviceDiskNamesByServerName(Session session, String server_name, AsyncCallback<List<String>> callback);

    void lookupDeviceIPByDate(Session session, SearchRange range, IPType ip_type, IPState ip_state, Date dateBegin, Date dateEnd, AsyncCallback<SearchResult> callback);
	void lookupDeviceIPCounts(Session session, IPType ip_type, AsyncCallback<Map<Integer, Integer>> callback);
	void addDeviceIP(Session session, String ip_addr, String ip_desc, IPType ip_type, AsyncCallback<Void> callback);
	void addDeviceIPService(Session session, String is_desc, Date is_starttime, Date is_endtime, int ip_id, String account_name, String user_name, AsyncCallback<Void> callback);
	void modifyDeviceIPService(Session session, int is_id, String is_desc, Date is_starttime, Date is_endtime, AsyncCallback<Void> callback);
	void modifyDeviceIP(Session session, int ip_id, String ip_desc, IPType ip_type, AsyncCallback<Void> callback);
	void deleteDeviceIP(Session session, List<Integer> ip_ids, AsyncCallback<Void> callback);
	void deleteDeviceIPService(Session session, List<Integer> is_ids, AsyncCallback<Void> callback);
	void lookupDeviceUnusedIPAddrByIPType(Session session, IPType ip_type, AsyncCallback<List<String>> callback);
	
	void lookupDeviceBWByDate(Session session, SearchRange range, Date dateBegin, Date dateEnd, AsyncCallback<SearchResult> callback);
	void addDeviceBWService(Session session, String bs_desc, int bs_bw_max, Date bs_starttime, Date bs_endtime, String ip_addr, AsyncCallback<Void> callback);
	void modifyDeviceBWService(Session session, int bs_id, String bs_desc, int bs_bw_max, Date bs_starttime, Date bs_endtime, AsyncCallback<Void> callback);
	void deleteDeviceBWService(Session session, List<Integer> bs_ids, AsyncCallback<Void> callback);
	void lookupDeviceUnusedIPAddrForBWService(Session session, String account_name, String user_name, AsyncCallback<List<String>> callback);
	
	void lookupDeviceTemplateByDate(Session session, SearchRange range, Date dateBegin, Date dateEnd, AsyncCallback<SearchResult> callback);
	void addDeviceTemplateService(Session session, String template_name, String template_desc, String template_cpu, int template_ncpus, long template_mem, long template_disk, int template_bw, String template_image, AsyncCallback<Void> callback);
	void modifyDeviceTemplateService(Session session, int template_id, String template_desc, String template_cpu, int template_ncpus, long template_mem, long template_disk, int template_bw, String template_image, AsyncCallback<Void> callback);
	void deleteDeviceTemplateService(Session session, List<Integer> template_ids, AsyncCallback<Void> callback);
	
	void lookupDeviceCPUPriceByDate(Session session, SearchRange range, Date creationtimeBegin, Date creationtimeEnd, Date modifiedtimeBegin, Date modifiedtimeEnd, AsyncCallback<SearchResult> callback);
	void addDeviceCPUPrice(Session session, String cpu_name, String cpu_price_desc, double cpu_price, AsyncCallback<Void> callback);
	void modifyDeviceCPUPrice(Session session, int cpu_price_id, String cpu_price_desc, double cpu_price, AsyncCallback<Void> callback);
	void deleteDeviceCPUPrice(Session session, List<Integer> cpu_price_ids,
			AsyncCallback<Void> callback);
	void lookupDeviceCPUNamesUnpriced(Session session, AsyncCallback<List<String>> callback);
	
    void lookupDeviceMemoryPrice(Session session, AsyncCallback<SearchResultRow> callback);
    void lookupDeviceDiskPrice(Session session, AsyncCallback<SearchResultRow> callback);
    void lookupDeviceBandwidthPrice(Session session, AsyncCallback<SearchResultRow> callback);
    void modifyDeviceMemoryPrice(Session session, String others_price_desc, double others_price, AsyncCallback<Void> callback);
    void modifyDeviceDiskPrice(Session session, String others_price_desc, double others_price, AsyncCallback<Void> callback);
    void modifyDeviceBandwidthPrice(Session session, String others_price_desc, double others_price, AsyncCallback<Void> callback);
    
    void lookupDeviceTemplateUnpriced(Session session, AsyncCallback<List<String>> callback);
    void lookupDeviceTemplatePriceByDate(Session session, SearchRange range, Date creationtimeBegin,
            Date creationtimeEnd, Date modifiedtimeBegin, Date modifiedtimeEnd, AsyncCallback<SearchResult> callback);
    void lookupDeviceTemplatePriceByPriceID(int template_price_id, AsyncCallback<Double> callback);
    void deleteDeviceTemplatePrice(Session session,
			List<Integer> template_price_ids, AsyncCallback<Void> callback);
    void modifyDeviceTemplatePrice(Session session, int template_price_id, String template_prcie_desc,
            double template_price_cpu, double template_price_mem, double template_price_disk, double template_price_bw,
            AsyncCallback<Void> callback);
    void createDeviceTemplatePriceByID(Session session, int template_id, String template_price_desc,
            double template_price_cpu, double template_price_mem, double template_price_disk, double template_price_bw,
            AsyncCallback<Void> callback);

	void listDeviceVMsByUser(Session session, String account, String user, AsyncCallback<List<String>> callback);

	void lookupDeviceVM(Session session, String search, SearchRange range, int queryState, AsyncCallback<SearchResult> callback);
	
	void addUserApp(Session session, UserApp userApp, AsyncCallback<Void> callback);
	void deleteUserApp(Session session, ArrayList<String> ids, AsyncCallback<Void> callback);
	void confirmUserApp(Session session, List<String> userAppId, EnumUserAppStatus userAppState, AsyncCallback<Void> callback);
	void countUserApp(Session session, AsyncCallback<ArrayList<UserAppStateCount>> callback);
	void queryVMImageType(Session session, AsyncCallback<ArrayList<VMImageType>> callback);
	
	void queryKeyPair(Session session, AsyncCallback<List<String>> callback);
	void querySecurityGroup(Session session, AsyncCallback<List<String>> callback);
	
//	void listAccessKeysByUser(Session session, String userId, AsyncCallback<SearchResult> callback);
//	void listAccessKeys(Session session, AsyncCallback<SearchResult> callback);
//	void listCertificatesByUser(Session session, String userId, AsyncCallback<SearchResult> callback);
//	void listCertificates(Session session, AsyncCallback<SearchResult> callback);
//	void listPolicies(Session session, AsyncCallback<SearchResult> callback);
	
	void modifyPolicy(Session session, String policyId, String name, String content, AsyncCallback<Void> callback);
	
	void lookupHistory(Session session, String search, SearchRange range, AsyncCallback<SearchResult> callback);

	void lookupDeviceTemplateInfoByName(Session session,
			String template_name, AsyncCallback<TemplateInfo> callback);

}
