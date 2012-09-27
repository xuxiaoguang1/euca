package com.eucalyptus.webui.server.ws;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient;
import com.amazonaws.services.identitymanagement.model.AccessKey;
import com.amazonaws.services.identitymanagement.model.AddUserToGroupRequest;
import com.amazonaws.services.identitymanagement.model.CreateAccessKeyRequest;
import com.amazonaws.services.identitymanagement.model.CreateGroupRequest;
import com.amazonaws.services.identitymanagement.model.CreateUserRequest;
import com.amazonaws.services.identitymanagement.model.DeleteAccessKeyRequest;
import com.amazonaws.services.identitymanagement.model.DeleteGroupPolicyRequest;
import com.amazonaws.services.identitymanagement.model.DeleteGroupRequest;
import com.amazonaws.services.identitymanagement.model.DeleteUserPolicyRequest;
import com.amazonaws.services.identitymanagement.model.DeleteUserRequest;
import com.amazonaws.services.identitymanagement.model.PutGroupPolicyRequest;
import com.amazonaws.services.identitymanagement.model.PutUserPolicyRequest;
import com.amazonaws.services.identitymanagement.model.RemoveUserFromGroupRequest;
import com.eucalyptus.webui.client.session.Session;
import com.eucalyptus.webui.server.config.WSConfig;
import com.eucalyptus.webui.server.user.LoginUserProfileStorer;

public class EucaWSAdapter {

	static public EucaWSAdapter instance() {
		if (instance == null)
			instance = new EucaWSAdapter();
		
		return instance;
	}
	
	/**
	 * @param session
	 * @throws EucaWSException
	 */
	public void verify(Session session) throws EucaWSException {
		
		if (session == null)
			throw new EucaWSException("Invalid Eucalyptus web service para");
	    
		boolean updatingAIMC = false;
		
		if (this.session == null || this.session.getId() != session.getId()) {
			this.session = session;
			updatingAIMC = true;
		}
		
		if (this.AIMC == null || updatingAIMC) {
			int userId = LoginUserProfileStorer.instance().get(session.getId()).getUserId();
			this.AIMC = this.getAIMC(getKeys(this.session, userId));
		}
	}
	
	/**
	 * @param accountName
	 * @param accountPwd
	 * @return
	 * @throws EucaWSException
	 */
	
	String[] createAccount(String accountName, String accountPwd) throws EucaWSException {
		
		return null;
	}
	
	/**
	 * @param accountKey
	 * @throws EucaWSException
	 */
	void deleteAccount(String accountKey) throws EucaWSException {
		
	}
	
	/**
	 * @param accountKey
	 * @param accountName
	 * @throws EucaWSException
	 */
	void modifyAccount(String accountKey, String accountName) throws EucaWSException {
		
	}
	
	/**
	 * @param accountId
	 * @param userName
	 * @param userPath
	 * @return userIDExt
	 * @throws EucaWSException
	 */
	String createUser(String accountId, String userName, String userPath) throws EucaWSException {
		CreateUserRequest request = new CreateUserRequest(userName);
		request.setPath(userPath);
		this.AIMC.createUser(request);
		
		return null;
	}
	
	/**
	 * @param userIDExt
	 * @throws EucaWSException
	 */
	void deleteUser(String userName) throws EucaWSException {
		DeleteUserRequest request = new DeleteUserRequest(userName);
		this.AIMC.deleteUser(request);
	}
	
	/**
	 * @param userIdExt
	 * @param groupName
	 * @throws EucaWSException
	 */
	void addUserToGroup(String userName, String groupName) throws EucaWSException {
		AddUserToGroupRequest request = new AddUserToGroupRequest(groupName, userName);
		this.AIMC.addUserToGroup(request);
	}
	
	/**
	 * @param userIdExt
	 * @param groupName
	 * @throws EucaWSException
	 */
	void removeUserFromGroup(String userName, String groupName) throws EucaWSException {
		RemoveUserFromGroupRequest request = new RemoveUserFromGroupRequest();
		this.AIMC.removeUserFromGroup(request);
	}
	
	/**
	 * @param accountId
	 * @param groupName
	 * @param groupPath
	 * @return groupId
	 * @throws EucaWSException
	 */
	String createGroup(String accountId, String groupName, String groupPath) throws EucaWSException {
		CreateGroupRequest request = new CreateGroupRequest(groupName);
		request.setPath(groupPath);
		
		this.AIMC.createGroup(request);
		return null;
	}
	
	/**
	 * @param groupId
	 * @throws EucaWSException
	 */
	void deleteGroup(String groupName) throws EucaWSException {
		DeleteGroupRequest request = new DeleteGroupRequest(groupName);
		this.AIMC.deleteGroup(request);
	}
	
	/**
	 * @param accountId
	 * @param policyName
	 * @param policyDoc
	 * @return policyId
	 * @throws EucaWSException
	 */
	String addAccountPolicy(String accountId, String policyName, String policyDoc) throws EucaWSException {
		return null;
	}
	
	/**
	 * @param groupId
	 * @param policyName
	 * @param policyDoc
	 * @return policyId
	 * @throws EucaWSException
	 */
	String addGroupPolicy(String groupName, String policyName, String policyDoc) throws EucaWSException {
		PutGroupPolicyRequest request = new PutGroupPolicyRequest(groupName, policyName, policyDoc);
		this.AIMC.putGroupPolicy(request);
		
		return null;
	}
	
	/**
	 * @param userId
	 * @param policyName
	 * @param policyDoc
	 * @return policyId
	 * @throws EucaWSException
	 */
	String addUserPolicy(String userName, String policyName, String policyDoc) throws EucaWSException {
		PutUserPolicyRequest request = new PutUserPolicyRequest(userName, policyName, policyDoc);
		this.AIMC.putUserPolicy(request);
		return null;
	}
	
	/**
	 * @param policyId
	 * @param policyName
	 * @param accountName
	 * @param groupName
	 * @param userName
	 * @throws EucaWSException
	 */
	void deletePolicy(String policyId, String policyName, String accountName, String groupName, String userName) throws EucaWSException {
		
		if (groupName != null && userName == null) {
			DeleteGroupPolicyRequest request = new DeleteGroupPolicyRequest(groupName, policyName);
			this.AIMC.deleteGroupPolicy(request);
		}
		else if (groupName == null && userName != null) {
			DeleteUserPolicyRequest request = new DeleteUserPolicyRequest(userName, policyName);
			this.AIMC.deleteUserPolicy(request);
		}
	}
	
	/**
	 * @param userId
	 * @return [0], keyId; [1], secret key
	 * @throws EucaWSException
	 */
	String[] createAccessKey(String userName) throws EucaWSException {
		CreateAccessKeyRequest request = new CreateAccessKeyRequest();
		request.setUserName(userName);
		AccessKey accessKey = this.AIMC.createAccessKey(request).getAccessKey();
		
		String[] keys = new String[] {accessKey.getAccessKeyId(), accessKey.getSecretAccessKey()};
		return keys;
	}
	
	/**
	 * @param keyId
	 * @throws EucaWSException
	 */
	void deleteAccessKey(String keyId) throws EucaWSException {
		DeleteAccessKeyRequest request = new DeleteAccessKeyRequest(keyId);
		this.AIMC.deleteAccessKey(request);
	}
	
	/**
	 * 
	 * @param userId
	 * @param pem
	 * @return cert id
	 * @throws EucaWSException
	 */
	String addCertificate(String userId, String pem) throws EucaWSException {
		return null;
	}
	
	/**
	 * @param userId
	 * @param certId
	 * @throws EucaWSException
	 */
	void deleteCertificate(String userId, String certId) throws EucaWSException {
		
	}
	
	private EucaWSAdapter() {
		
	}
	
	  
	private String[] _getKeys(int userID) {
	    //real code here
	    if (keys == null) {
	    	this.keys = new String[2];
	    	this.keys[0] = FALLBACK_ACCESS_KEY;
	    	this.keys[1] = FALLBACK_SECRET_KEY;
	    }
	    
	    return keys;
	}
	
	private String[] getKeys(Session session, int userID) {
	    //TODO: check if session is admin, otherwise should not be able to use others' userID
	    return _getKeys(userID);
	}
	
	private String getEndPoint() {
	    return WSConfig.instance().url();
	}

	private AmazonEC2 getEC2(String[] keys) {
		AWSCredentials credentials = new BasicAWSCredentials(keys[0], keys[1]);
		ClientConfiguration cc = new ClientConfiguration();
		AmazonEC2 ec2 = new AmazonEC2Client(credentials, cc);
		ec2.setEndpoint(getEndPoint());
			//System.out.println(ec2.describeAvailabilityZones());
		return ec2;
	}
	
	private AmazonIdentityManagementClient getAIMC(String[] keys) {
		AWSCredentials credentials = new BasicAWSCredentials(keys[0], keys[1]);
		AmazonIdentityManagementClient aimc = new AmazonIdentityManagementClient (credentials);
		aimc.setEndpoint(getEndPoint());
		
		return aimc;
	}
	
	static EucaWSAdapter instance = null;
	
	AmazonEC2 EC2;
	AmazonIdentityManagementClient AIMC;
	
	Session session;
	
	String[] keys;
	static final String FALLBACK_ACCESS_KEY="6CQT6BWUN4CDB9HTTHZOX";
	static final String FALLBACK_SECRET_KEY="cboRkSRFMd2hhokkIcVAxDbwTO6828wIB1mD2x5H";
}
