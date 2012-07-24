package com.eucalyptus.webui.server.user;

import com.eucalyptus.webui.client.session.Session;
import com.eucalyptus.webui.shared.user.LoginUserProfile;

public class UserPermissionChecker {
	public static UserPermissionChecker instance() {
		if (instance == null)
			instance = new UserPermissionChecker();
		
		return instance;
	}
	
	public boolean isSysAdmin(Session session) {
		LoginUserProfile profile = LoginUserProfileStorer.instance().get(session.getId());
		return profile.isSystemAdmin();
	}
	
	public boolean isAccountAdmin(Session session) {
		LoginUserProfile profile = LoginUserProfileStorer.instance().get(session.getId());
		return profile.isAccountAdmin();
	}
	
	public boolean isUser(Session session) {
		return !isSysAdmin(session) && !isAccountAdmin(session);
	}
	
	private UserPermissionChecker() {
	}
	
	static UserPermissionChecker instance;
}
