package com.eucalyptus.webui.server.user;

import java.util.Hashtable;

import com.eucalyptus.webui.shared.user.LoginUserProfile;

public class LoginUserProfileStorer {

	static public LoginUserProfileStorer instance() {
		if (instance == null)
			instance = new LoginUserProfileStorer();
		
		return instance;
	}
	
	public LoginUserProfile get(String sessionId) {
		return this.profileList.get(sessionId);
	}
	
	public void set(String sessionId, LoginUserProfile profile) {
		this.profileList.put(sessionId, profile);
	}
	
	private LoginUserProfileStorer() {
	}
	
	private static LoginUserProfileStorer instance;
	
	private Hashtable<String, LoginUserProfile> profileList = new Hashtable<String, LoginUserProfile>();
}
