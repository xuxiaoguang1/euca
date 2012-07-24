package com.eucalyptus.webui.shared.user;

public class AccountInfo implements java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public AccountInfo() {
		
	}

	public AccountInfo(int id, String name, String email, String description, EnumState state) {
		this.setId(id);
		this.setName(name);
		this.setEmail(email);
		this.setDescription(description);
		this.setState(state);
	}
	
	public int getId() {
		return accountId;
	}
	public void setId(int accountId) {
		this.accountId = accountId;
	}
		
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}	
	
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	public EnumState getState() {
		return state;
	}
	public void setState(EnumState state) {
		this.state = state;
	}
	
	private int accountId;
	private String name;
	private String email;
	private String description;
	private EnumState state;
}
