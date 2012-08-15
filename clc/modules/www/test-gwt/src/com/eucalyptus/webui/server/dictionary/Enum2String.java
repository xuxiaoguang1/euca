package com.eucalyptus.webui.server.dictionary;

import java.util.Hashtable;

import com.eucalyptus.webui.shared.user.EnumState;
import com.eucalyptus.webui.shared.user.EnumUserAppResult;
import com.eucalyptus.webui.shared.user.EnumUserAppState;
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
	
	public String getName(EnumUserAppState userAppState) {
		return (String) userAppStateDic.get(userAppState);
	}
	
	public String getName(EnumUserAppResult userAppResult) {
		return (String) userAppResultDic.get(userAppResult);
	}
	
	public String getEnumStateName(String integerStr) {
		EnumState userState = EnumState.values()[Integer.valueOf(integerStr)];
		return getName(userState);
	}
	
	public String getEnumUserTypeName(String integerStr) {
		EnumUserType userType = EnumUserType.values()[Integer.valueOf(integerStr)];
		return getName(userType);
	}
	
	public String getUserAppStateName(String integerStr) {
		EnumUserAppState userAppState = EnumUserAppState.values()[Integer.valueOf(integerStr)];
		return getName(userAppState);
	}
	
	public String getUserAppResultName(String integerStr) {
		EnumUserAppResult userAppResult = EnumUserAppResult.values()[Integer.valueOf(integerStr)];
		return getName(userAppResult);
	}
	
	public String getActiveState(boolean active) {
		if (active)
			return USER_KEY_ACTIVE[1];
		else
			return USER_KEY_NON_ACTIVE[1];
	}
	
	public String getRevokedState(boolean active) {
		if (active)
			return USER_KEY_REVOKED[1];
		else
			return USER_KEY_NON_REVOKED[1];
	}
	
	private Enum2String () {   
		userTypeDic.put(EnumUserType.ADMIN, USER_TYPE_ADMIN_NAME[1]);
		userTypeDic.put(EnumUserType.USER, USER_TYPE_USER_NAME[1]);
		
		userStateDic.put(EnumState.NORMAL, USER_STATE_NORMAL_NAME[1]);
		userStateDic.put(EnumState.PAUSE, USER_STATE_PAUSE_NAME[1]);
		userStateDic.put(EnumState.BAN, USER_STATE_BAN_NAME[1]);
		
		userAppStateDic.put(EnumUserAppState.SOLVING, USER_APP_STATE_SOLVING[1]);
		userAppStateDic.put(EnumUserAppState.SOLVED, USER_APP_STATE_SOLVED[1]);
		userAppStateDic.put(EnumUserAppState.TOSOLVE, USER_APP_STATE_TOSOLVE[1]);
		
		userAppResultDic.put(EnumUserAppResult.APPROVED, USER_APP_RESULT_APPROVED[1]);
		userAppResultDic.put(EnumUserAppResult.REJECTED, USER_APP_RESULT_REJECTED[1]);
	}
	
	private final Hashtable<EnumUserType, String> userTypeDic = new Hashtable<EnumUserType, String>();
	private final Hashtable<EnumState, String> userStateDic = new Hashtable<EnumState, String>();
	private final Hashtable<EnumUserAppState, String> userAppStateDic = new Hashtable<EnumUserAppState, String>();
	private final Hashtable<EnumUserAppResult, String> userAppResultDic = new Hashtable<EnumUserAppResult, String>();
	
	private static Enum2String enum2String= null;
	
	private final String[] USER_TYPE_ADMIN_NAME = {"Administrator", "系统管理员"};
	private final String[] USER_TYPE_USER_NAME = {"User", "普通用户"};
	
	private final String[] USER_STATE_NORMAL_NAME = {"Normal", "正常"};
	private final String[] USER_STATE_PAUSE_NAME = {"Pause", "暂停"};
	private final String[] USER_STATE_BAN_NAME = {"Ban", "禁止"};
	
	private final String[] USER_KEY_ACTIVE = {"true", "激活"};
	private final String[] USER_KEY_NON_ACTIVE = {"false", "未激活"};
	
	private final String[] USER_KEY_REVOKED = {"true", "是"};
	private final String[] USER_KEY_NON_REVOKED = {"false", "否"};
	
	private final String[] USER_APP_STATE_SOLVING = {"Solving", "处理中"};
	private final String[] USER_APP_STATE_SOLVED = {"Solved", "处理完毕"};
	private final String[] USER_APP_STATE_TOSOLVE = {"ToSolve", "待处理"};
	
	private final String[] USER_APP_RESULT_APPROVED = {"Approved", "批准"};
	private final String[] USER_APP_RESULT_REJECTED = {"Rejected", "拒绝"};
}
