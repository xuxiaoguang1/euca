package com.eucalyptus.webui.server.user;

import com.eucalyptus.webui.shared.user.EnumState;
import com.eucalyptus.webui.shared.user.UserInfo;

public class UserInfoAndState {

	public void setUserInfo(UserInfo userInfo) {
		this.userInfo = userInfo;
	}
	public UserInfo getUserInfo() {
		return this.userInfo;
	}
	
	public void setGroupState(EnumState groupState) {
		this.groupState = groupState;
	}
	public EnumState getGroupState() {
		return this.groupState;
	}
	
	public void setAccountState(EnumState accountState) {
		this.accountState = accountState;
	}
	public EnumState getAccountState() {
		return this.accountState;
	}
	
	private UserInfo userInfo;
	private EnumState groupState;
	private EnumState accountState;
}
