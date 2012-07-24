package com.eucalyptus.webui.shared.user;

public class GroupInfo implements java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public GroupInfo () {
		
	}
	public GroupInfo(int id, String name, String description, int groupId, EnumState state, int accountId) {
		this.setId(id);
		this.setName(name);
		this.setDescription(description);
		this.setState(state);
		this.setAccountId(accountId);
	}
	
	public int getId() {
		return groupId;
	}
	public void setId(int groupId) {
		this.groupId = groupId;
	}
		
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
		
	public int getAccountId() {
		return accountId;
	}
	public void setAccountId(int accountId) {
		this.accountId = accountId;
	}
	
	public EnumState getState() {
		return state;
	}
	public void setState(EnumState state) {
		this.state = state;
	}
	
	private int groupId;
	private String name;
	private String description;
	private int accountId;
	private EnumState state;
}
