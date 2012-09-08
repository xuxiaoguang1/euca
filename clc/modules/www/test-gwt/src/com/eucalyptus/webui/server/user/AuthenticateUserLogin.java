package com.eucalyptus.webui.server.user;

import com.eucalyptus.webui.client.service.EucalyptusServiceException;
import com.eucalyptus.webui.client.session.Session;
import com.eucalyptus.webui.server.WebSessionManager;
import com.eucalyptus.webui.server.dictionary.EucalyptusServiceExceptionMsg;
import com.eucalyptus.webui.shared.user.EnumState;
import com.eucalyptus.webui.shared.user.EnumUserRegStatus;
import com.eucalyptus.webui.shared.user.LoginUserProfile;
import com.eucalyptus.webui.shared.user.UserInfo;

public class AuthenticateUserLogin {
	public Session checkPwdAndUserState( String accountName, String userName, String pwd ) throws EucalyptusServiceException {
		try {
				UserInfoAndState userAndState = dbProc.lookupUserAndState(accountName, userName);
				UserInfo userInfo = userAndState.getUserInfo();
				
				if (!userInfo.getPwd().equals(pwd))
					throw new EucalyptusServiceException(EucalyptusServiceExceptionMsg.PWD_ERROR[1]); 
				
				if (userInfo.getRegStatus() != EnumUserRegStatus.APPROVED)
					throw new EucalyptusServiceException(EucalyptusServiceExceptionMsg.USER_NOT_ACTIVE[1]);
				
				if (!userCanLogin(userAndState))
					throw new EucalyptusServiceException(EucalyptusServiceExceptionMsg.USER_STATE_ERROR[1]);
				
				Session session = new Session(WebSessionManager.getInstance( ).newSession());
				
				LoginUserProfile profile = new LoginUserProfile();
				profile.setUserId(userInfo.getId());
				profile.setUserName(userInfo.getName());
				profile.setUserTitle(userInfo.getTitle());
				profile.setUserMobile(userInfo.getMobile());
				profile.setUserEmail(userInfo.getEmail());
				profile.setAccountId(userInfo.getAccountId());
				profile.setAccountName(accountName);
				profile.setUserType(userInfo.getType());
				
				LoginUserProfileStorer.instance().set(session.getId(), profile);
				
				return session;
				
			} catch ( UserSyncException e ) {
				throw new EucalyptusServiceException(EucalyptusServiceExceptionMsg.USER_NOT_EXISTED[1]);
			}
	}
	
	public void checkUserExisted( String accountName, String userName ) throws EucalyptusServiceException {
		try {
				dbProc.lookupUserAndState(accountName, userName);
			} catch ( UserSyncException e ) {
				throw new EucalyptusServiceException(EucalyptusServiceExceptionMsg.USER_HAS_EXISTED[1]);
			}
	}
	
	private boolean userCanLogin(UserInfoAndState userAndState) {
		if (userAndState == null)
			return false;
		
		UserInfo user = userAndState.getUserInfo();
		if (user == null || user.getState() == EnumState.BAN)
			return false;
		
		if (userAndState.getAccountState() == EnumState.BAN)
			return false;
		
		if (userAndState.getGroupState() == EnumState.BAN)
			return false;
		
		return true;
	}
	
	private static UserDBProcWrapper dbProc = new UserDBProcWrapper();
}
