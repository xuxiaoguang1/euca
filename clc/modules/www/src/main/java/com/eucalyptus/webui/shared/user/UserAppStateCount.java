package com.eucalyptus.webui.shared.user;

//UserApp统计值
public class UserAppStateCount implements java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private EnumUserAppStatus state;
	private int count;
	
	public void setCountValue(EnumUserAppStatus state, int count) {
		this.state = state;
		this.count = count;
	}
	
	public EnumUserAppStatus getAppState() {
		return this.state;
	}
	public int getCount() {
		return this.count;
	}
}
