package com.eucalyptus.webui.shared.dictionary;

import java.util.ArrayList;
import com.eucalyptus.webui.shared.user.EnumState;
import com.eucalyptus.webui.shared.user.EnumUserAppStatus;
import com.eucalyptus.webui.shared.user.EnumUserType;

public class Enum2String {
	static public Enum2String getInstance() {
		if (enum2String == null)
			enum2String = new Enum2String();
		
		return enum2String;
	}
	
	public String getEnumStateName(String integerStr) {
		EnumState userState = EnumState.values()[Integer.valueOf(integerStr)];
		return getName(userState);
	}
	public EnumState getEnumState(String str) {
		for (Pair<EnumState, String> ele : this.enumState2String) {
			if (ele.second.equals(str))
				return ele.first;
		}	
		return EnumState.NONE;
	}
	
	public String getEnumUserTypeName(String integerStr) {
		EnumUserType userType = EnumUserType.values()[Integer.valueOf(integerStr)];
		return getName(userType);
	}
	public EnumUserType getEnumUserType(String str) {
		for (Pair<EnumUserType, String> ele : this.enumUserType2String) {
			if (ele.second.equals(str))
				return ele.first;
		}
		return EnumUserType.NONE;
	}
	
	public String getUserAppStateName(String integerStr) {
		EnumUserAppStatus userAppState = EnumUserAppStatus.values()[Integer.valueOf(integerStr)];
		return getName(userAppState);
	}
	
	public String getActiveState(boolean active) {
		if (active)
			return USER_KEY_ACTIVE[1];
		else
			return USER_KEY_NON_ACTIVE[1];
	}
	
	public String getVMAction(boolean start) {
		if (start)
			return HISTORY_START[1];
		else
			return HISTORY_STOP[1];
	}
	
	public String getRevokedState(boolean active) {
		if (active)
			return USER_KEY_REVOKED[1];
		else
			return USER_KEY_NON_REVOKED[1];
	}
	
	private String getName(EnumUserType userType) {
		for (Pair<EnumUserType, String> ele : this.enumUserType2String) {
			if (ele.first == userType)
				return ele.second;
		}
		return null;
	}
	
	private String getName(EnumState userState) {
		for (Pair<EnumState, String> ele : this.enumState2String) {
			if (ele.first == userState)
				return ele.second;
		}
		return null;
	}
	
	private String getName(EnumUserAppStatus userAppState) {
		for (Pair<EnumUserAppStatus, String> ele : this.EnumUserAppStatus2String) {
			if (ele.first == userAppState)
				return ele.second;
		}
		return null;
	}
	
	private Enum2String () {
		enumUserType2String.add(new Pair(EnumUserType.NONE, USER_TYPE_NONE_NAME[1]));
		enumUserType2String.add(new Pair(EnumUserType.ADMIN, USER_TYPE_ADMIN_NAME[1]));
		enumUserType2String.add(new Pair(EnumUserType.USER, USER_TYPE_USER_NAME[1]));
		
		enumState2String.add(new Pair(EnumState.NONE, STATE_NONE_NAME[1]));
		enumState2String.add(new Pair(EnumState.NORMAL, STATE_NORMAL_NAME[1]));
		enumState2String.add(new Pair(EnumState.PAUSE, STATE_PAUSE_NAME[1]));
		enumState2String.add(new Pair(EnumState.BAN, STATE_BAN_NAME[1]));
		
		EnumUserAppStatus2String.add(new Pair(EnumUserAppStatus.NONE, USER_APP_STATE_NONE[1]));
		EnumUserAppStatus2String.add(new Pair(EnumUserAppStatus.APPLYING, USER_APP_STATE_SOLVING[1]));
		EnumUserAppStatus2String.add(new Pair(EnumUserAppStatus.APPROVED, USER_APP_STATE_SOLVED[1]));
		EnumUserAppStatus2String.add(new Pair(EnumUserAppStatus.REJECTED, USER_APP_STATE_TOSOLVE[1]));
	}
	
	private final ArrayList<Pair<EnumUserType, String>> enumUserType2String = new ArrayList<Pair<EnumUserType, String>>();
	private final ArrayList<Pair<EnumState, String>> enumState2String = new ArrayList<Pair<EnumState, String>>();
	
	private final ArrayList<Pair<EnumUserAppStatus, String>> EnumUserAppStatus2String = new ArrayList<Pair<EnumUserAppStatus, String>>();
	
	private static Enum2String enum2String= null;
	
	private final String[] USER_TYPE_NONE_NAME = {"None", "未定义"};
	private final String[] USER_TYPE_ADMIN_NAME = {"Administrator", "管理员"};
	private final String[] USER_TYPE_USER_NAME = {"User", "普通用户"};
	
	private final String[] STATE_NONE_NAME = {"None", "未定义"};
	private final String[] STATE_NORMAL_NAME = {"Normal", "正常"};
	private final String[] STATE_PAUSE_NAME = {"Pause", "暂停"};
	private final String[] STATE_BAN_NAME = {"Ban", "禁止"};
	
	private final String[] USER_KEY_ACTIVE = {"true", "激活"};
	private final String[] USER_KEY_NON_ACTIVE = {"false", "未激活"};
	
	private final String[] HISTORY_START = {"start", "启动"};
	private final String[] HISTORY_STOP = {"stop", "停止"};
	
	private final String[] USER_KEY_REVOKED = {"true", "是"};
	private final String[] USER_KEY_NON_REVOKED = {"false", "否"};
	
	private final String[] USER_APP_STATE_NONE = {"None", "未初始化"};
	private final String[] USER_APP_STATE_SOLVING = {"Applying", "处理中"};
	private final String[] USER_APP_STATE_SOLVED = {"Approved", "批准"};
	private final String[] USER_APP_STATE_TOSOLVE = {"Rejected", "拒绝"};
	
	private class Pair<A, B> implements java.io.Serializable {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
		public final A first;
		public final B second;
		
		public Pair() {
			first = null;
			second = null;
		}
		public Pair(A first, B second) {
			this.first = first;
			this.second = second;
		}
	};
}
