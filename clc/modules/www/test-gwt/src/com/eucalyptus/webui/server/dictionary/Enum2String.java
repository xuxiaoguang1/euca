package com.eucalyptus.webui.server.dictionary;

import java.util.Hashtable;

import com.eucalyptus.webui.shared.user.EnumState;
import com.eucalyptus.webui.shared.user.EnumUserType;

public class Enum2String {
	static public Enum2String getInstance() {
		if (enum2String == null)
			enum2String = new Enum2String();
		
		return enum2String;
	}
	
	public String getName(EnumUserType userType) {
		return (String) userTypeDic.get(userType);
	}
	
	public String getName(EnumState userState) {
		return (String) userStateDic.get(userState);
	}
	
	public String getEnumStateName(String integerStr) {
		EnumState userState = EnumState.values()[Integer.valueOf(integerStr)];
		return getName(userState);
	}
	
	public String getEnumUserTypeName(String integerStr) {
		EnumUserType userType = EnumUserType.values()[Integer.valueOf(integerStr)];
		return getName(userType);
	}
	
	private Enum2String () {   
		userTypeDic.put(EnumUserType.ADMIN, USER_TYPE_ADMIN_NAME[1]);
		userTypeDic.put(EnumUserType.USER, USER_TYPE_USER_NAME[1]);
		
		userStateDic.put(EnumState.NORMAL, USER_STATE_NORMAL_NAME[1]);
		userStateDic.put(EnumState.PAUSE, USER_STATE_PAUSE_NAME[1]);
		userStateDic.put(EnumState.BAN, USER_STATE_BAN_NAME[1]);
	}
	
	private final Hashtable<EnumUserType, String> userTypeDic = new Hashtable<EnumUserType, String>();
	private final Hashtable<EnumState, String> userStateDic = new Hashtable<EnumState, String>();
	
	private static Enum2String enum2String= null;
	
	private final String[] USER_TYPE_ADMIN_NAME = {"Administrator", "系统管理员"};
	private final String[] USER_TYPE_USER_NAME = {"User", "普通用户"};
	
	private final String[] USER_STATE_NORMAL_NAME = {"Normal", "正常"};
	private final String[] USER_STATE_PAUSE_NAME = {"Pause", "暂停"};
	private final String[] USER_STATE_BAN_NAME = {"Ban", "禁止"};
}
