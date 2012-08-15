package com.eucalyptus.webui.shared.auth;

import java.io.Serializable;

public class Policy implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private int id;
	
	private String name;
	
	private String version;
	
	private String text;
	
	private String accountId = null;
	
	private String groupId = null;
	
	private String userId = null;
	
	public Policy(){
		
	}
	
	public Policy(int id, String name, String version, String text, String accountId, String groupId, String userId){
		this.id = id;
		this.name = name;
		this.version = version;
		this.text = text;
		this.accountId = accountId;
		this.groupId = groupId;
		this.userId = userId;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getAccountId() {
		return accountId;
	}

	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}
	
	

}
