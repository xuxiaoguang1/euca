package com.eucalyptus.webui.client.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.eucalyptus.webui.client.session.Session;
import com.eucalyptus.webui.client.view.DeviceCPUDeviceAddView;
import com.eucalyptus.webui.client.view.DeviceDiskDeviceAddView;
import com.eucalyptus.webui.client.view.DeviceMemoryDeviceAddView;
import com.eucalyptus.webui.shared.resource.Template;
import com.eucalyptus.webui.shared.resource.VMImageType;
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
	
	void lookupDeviceAreaByDate(Session session, SearchRange range, Date creationtimeBegin, Date creationtimeEnd, Date modifiedtimeBegin, Date modifiedtimeEnd, AsyncCallback<SearchResult> callback);
	void addDeviceArea(Session session, String area_name, String area_desc, AsyncCallback<Void> callback);
	void modifyDeviceArea(Session session, int area_id, String area_desc, AsyncCallback<Void> callback);
	void deleteDeviceArea(Session session, Collection<Integer> area_ids, AsyncCallback<Void> callback);
	void lookupDeviceAreaNames(Session session, AsyncCallback<List<String>> callback);

	void lookupDeviceRoomByDate(Session session, SearchRange range, Date creationtimeBegin, Date creationtimeEnd, Date modifiedtimeBegin, Date modifiedtimeEnd, AsyncCallback<SearchResult> callback);
	void addDeviceRoom(Session session, String room_name, String room_desc, String area_name, AsyncCallback<Void> callback);
	void modifyDeviceRoom(Session session, int room_id, String room_desc, AsyncCallback<Void> callback);
	void deleteDeviceRoom(Session session, Collection<Integer> room_ids, AsyncCallback<Void> callback);
	void lookupDeviceRoomNamesByAreaName(Session session, String area_name, AsyncCallback<List<String>> callback);
	
	void lookupDeviceCabinetByDate(Session session, SearchRange range, Date creationtimeBegin, Date creationtimeEnd, Date modifiedtimeBegin, Date modifiedtimeEnd, AsyncCallback<SearchResult> callback);
	void addDeviceCabinet(Session session, String cabinet_name, String cabinet_desc, String room_name, AsyncCallback<Void> callback);
	void modifyDeviceCabinet(Session session, int cabinet_id, String cabinet_desc, AsyncCallback<Void> callback);
	void deleteDeviceCabinet(Session session, Collection<Integer> cabinet_ids, AsyncCallback<Void> callback);
    void lookupCabinetNamesByRoomName(Session session, String room_name, AsyncCallback<List<String>> callback);
	
	void lookupDeviceCPUPriceByDate(Session session, SearchRange range, Date creationtimeBegin, Date creationtimeEnd, Date modifiedtimeBegin, Date modifiedtimeEnd, AsyncCallback<SearchResult> callback);
	void addDeviceCPUPrice(Session session, String cpu_name, String cpu_price_desc, double cpu_price, AsyncCallback<Void> callback);
	void modifyDeviceCPUPrice(Session session, int cpu_price_id, String cpu_price_desc, double cpu_price, AsyncCallback<Void> callback);
	void deleteDeviceCPUPrice(Session session, Collection<Integer> cpu_price_ids, AsyncCallback<Void> callback);
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
    void deleteDeviceTemplatePrice(Session session, Collection<Integer> template_price_ids, AsyncCallback<Void> callback);
    void modifyDeviceTemplatePrice(Session session, int template_price_id, String template_prcie_desc,
            double template_price_cpu, double template_price_mem, double template_price_disk, double template_price_bw,
            AsyncCallback<Void> callback);
    void createDeviceTemplatePriceByID(Session session, int template_id, String template_price_desc,
            double template_price_cpu, double template_price_mem, double template_price_disk, double template_price_bw,
            AsyncCallback<Void> callback);
	void lookupDeviceTemplateByName(Session session, String template_name, AsyncCallback<Template> callback);

	void lookupDeviceServer(Session session, String search, SearchRange range, int queryState,
	        AsyncCallback<SearchResult> callback);
	void getDeviceServerCounts(Session session, AsyncCallback<Map<Integer, Integer>> callback);
	void modifyDeviceServerState(Session session, SearchResultRow row, int state, AsyncCallback<SearchResultRow> callback);
	void addDeviceServer(Session session, String mark, String name, String conf, String ip, int bw, int state,
            String room, AsyncCallback<Boolean> callback);
	void deleteDeviceServer(Session session, List<SearchResultRow> list, AsyncCallback<List<SearchResultRow>> callback);
	
	void lookupDeviceCPU(Session session, String search, SearchRange range, int queryState,
	        AsyncCallback<SearchResult> callback);
	void getDeviceCPUCounts(Session session, AsyncCallback<Map<Integer, Integer>> callback);
	void modifyDeviceCPUService(Session session, SearchResultRow row, String endtime, int state,
	        AsyncCallback<SearchResultRow> callback);
	void deleteDeviceCPUService(Session session, List<SearchResultRow> list,
	        AsyncCallback<List<SearchResultRow>> callback);
	void deleteDeviceCPUDevice(Session session, List<SearchResultRow> list,
	        AsyncCallback<List<SearchResultRow>> callback);
	void addDeviceCPUService(Session session, SearchResultRow row, String account, String user, String starttime,
	        int life, int state, AsyncCallback<SearchResultRow> callback);
	void addDeviceCPUDevice(Session session, String serverMark, String name, String vendor, String model, double ghz,
	        double cache, int num, AsyncCallback<Boolean> callback);
	void lookupDeviceCPUInfo(Session session, AsyncCallback<DeviceCPUDeviceAddView.DataCache> callback);
	
	void listDeviceCPUAccounts(Session session, AsyncCallback<List<String>> callback);
	void listDeviceCPUUsersByAccount(Session session, String account, AsyncCallback<List<String>> callback);

	void lookupDeviceMemory(Session session, String search, SearchRange range, int queryState,
	        AsyncCallback<SearchResult> callback);
	void getDeviceMemoryCounts(Session session,
			AsyncCallback<Map<Integer, Long>> callback);
	void modifyDeviceMemoryService(Session session, SearchResultRow row, String endtime, int state,
	        AsyncCallback<SearchResultRow> callback);
	void deleteDeviceMemoryService(Session session, List<SearchResultRow> list,
	        AsyncCallback<List<SearchResultRow>> callback);
	void deleteDeviceMemoryDevice(Session session, List<SearchResultRow> list,
	        AsyncCallback<List<SearchResultRow>> callback);
	void addDeviceMemoryService(Session session, SearchResultRow row, String account, String user,
			long used, String starttime, int life, int state, AsyncCallback<SearchResultRow> callback);
	void addDeviceMemoryDevice(Session session, String serverMark, String name, long total, int num, AsyncCallback<Boolean> callback);
	void lookupDeviceMemoryInfo(Session session, AsyncCallback<DeviceMemoryDeviceAddView.DataCache> callback);
	void listDeviceMemoryAccounts(Session session, AsyncCallback<List<String>> callback);
	void listDeviceMemoryUsersByAccount(Session session, String account, AsyncCallback<List<String>> callback);
	
	void lookupDeviceDisk(Session session, String search, SearchRange range, int queryState,
	        AsyncCallback<SearchResult> callback);
	void getDeviceDiskCounts(Session session,
			AsyncCallback<Map<Integer, Long>> callback);
	void modifyDeviceDiskService(Session session, SearchResultRow row, String endtime, int state,
	        AsyncCallback<SearchResultRow> callback);
	void deleteDeviceDiskService(Session session, List<SearchResultRow> list,
	        AsyncCallback<List<SearchResultRow>> callback);
	void deleteDeviceDiskDevice(Session session, List<SearchResultRow> list,
	        AsyncCallback<List<SearchResultRow>> callback);
	void addDeviceDiskService(Session session, SearchResultRow row, String account, String user,
			long used, String starttime, int life, int state, AsyncCallback<SearchResultRow> callback);
	void addDeviceDiskDevice(Session session, String serverMark, String name, long total, int num, AsyncCallback<Boolean> callback);
	void lookupDeviceDiskInfo(Session session, AsyncCallback<DeviceDiskDeviceAddView.DataCache> callback);
	void listDeviceDiskAccounts(Session session, AsyncCallback<List<String>> callback);
	void listDeviceDiskUsersByAccount(Session session, String account, AsyncCallback<List<String>> callback);
	
	void lookupDeviceIP(Session session, String search, SearchRange range, int queryState, int queryType,
            AsyncCallback<SearchResult> callback);
	void getDeviceIPCounts(Session session, int queryType, AsyncCallback<Map<Integer, Integer>> callback);
	void modifyDeviceIPService(Session session, SearchResultRow row, String endtime, int state,
	        AsyncCallback<SearchResultRow> callback);
	void deleteDeviceIPService(Session session, List<SearchResultRow> list,
	        AsyncCallback<List<SearchResultRow>> callback);
	void deleteDeviceIPDevice(Session session, List<SearchResultRow> list,
	        AsyncCallback<List<SearchResultRow>> callback);
	void addDeviceIPService(Session session, SearchResultRow row, String account, String user, String vmMark,
			String starttime, int life, int state, AsyncCallback<SearchResultRow> callback);
	void addDeviceIPDevice(Session session, List<String> publicList, List<String> privateList, AsyncCallback<Boolean> callback);
	void listDeviceIPAccounts(Session session, AsyncCallback<List<String>> callback);
	void listDeviceIPUsersByAccount(Session session, String account, AsyncCallback<List<String>> callback);
	void listDeviceVMsByUser(Session session, String account, String user, AsyncCallback<List<String>> callback);
	
	void lookupDeviceBW(Session session, String search, SearchRange range, AsyncCallback<SearchResult> callback);
	void modifyDeviceBWService(Session session, SearchResultRow row, String endtime,
	        AsyncCallback<SearchResultRow> callback);
	void deleteDeviceBWService(Session session, List<SearchResultRow> list,
	        AsyncCallback<List<SearchResultRow>> callback);
	void addDeviceBWService(Session session, String account, String user, String starttime,
	        int life, String ip, long bandwidth, AsyncCallback<Boolean> callback);
	void listDeviceBWAccounts(Session session, AsyncCallback<List<String>> callback);
	void listDeviceBWUsersByAccount(Session session, String account, AsyncCallback<List<String>> callback);
	void listDeviceIPsByUser(Session session, String account, String user, AsyncCallback<List<String>> callback);

	void lookupDeviceTemplate(Session session, String search, SearchRange range, Date starttime, Date endtime,
	        AsyncCallback<SearchResult> callback);
	void deleteDeviceTemplate(Session session, List<SearchResultRow> list,
	        AsyncCallback<List<SearchResultRow>> callback);
	void addDeviceTemplate(Session session, String mark, String cpu, int ncpus, String mem, String disk, String bw, String image, AsyncCallback<Boolean> callback);
	void modifyDeviceTempate(Session session, SearchResultRow row, String cpu, int ncpus, String mem, String disk, String bw, String image, AsyncCallback<SearchResultRow> callback);
	void listDeviceTemplateCPUNames(Session session, AsyncCallback<List<String>> callback);
	
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
}
