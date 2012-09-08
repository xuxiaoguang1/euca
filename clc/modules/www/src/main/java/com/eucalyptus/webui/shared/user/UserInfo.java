package com.eucalyptus.webui.shared.user;

public class UserInfo implements java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public UserInfo () {
		
	}
	public UserInfo(int id, String name, String pwd, String title, String mobile, String email, EnumState state, EnumUserType type, int groupId, int accountId) {
		this.setId(id);
		this.setName(name);
		this.setPwd(pwd);
		this.setTitle(title);
		this.setMobile(mobile);
		this.setEmail(email);
		this.setState(state);
		this.setType(type);
		this.setGroupId(groupId);
		this.setAccountId(accountId);
	}
	
	public int getId() {
		return userId;
	}
	public void setId( int userId) {
		this.userId = userId;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public String getPwd() {
		return pwd;
	}
	public void setPwd(String pwd) {
		this.pwd = pwd;
	}
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	
	public String getMobile() {
		return mobile;
	}
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	
	public EnumUserType getType() {
		return type;
	}
	public void setType(EnumUserType type) {
		this.type = type;
	}
	
	public EnumState getState() {
		return state;
	}
	public void setState(EnumState state) {
		this.state = state;
	}
	
	public EnumUserRegStatus getRegStatus() {
		return regStatus;
	}
	public void setRegStatus(EnumUserRegStatus regStatus) {
		this.regStatus = regStatus;
	}
	
	public int getGroupId() {
		return groupId;
	}
	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}
	
	public int getAccountId() {
		return accountId;
	}
	public void setAccountId(int accountId) {
		this.accountId = accountId;
	}
	
	private int userId;
	private String name;
	private String pwd;
	
	private String title;
	private String mobile;
	private String email;
	
	private EnumUserType type;
	
	private EnumState state;
	
	private EnumUserRegStatus regStatus;
	
	private int groupId;
	private int accountId;
}
