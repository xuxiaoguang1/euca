package com.eucalyptus.webui.server.user;

import java.util.Date;

import com.eucalyptus.webui.shared.user.EnumUserAppState;

public class UserApp {
	private int id;
	private Date time;
	private EnumUserAppState state;
	private int del;
	private String content;
	private String comment;
	
	private int userId;
	private int templateId;
	
	public void setUAId(int ua_id) {
		this.id = ua_id;
	}
	public int getUAId() {
		return this.id;
	}
	
	public void setTime(Date time) {
		this.time = time;
	}
	public Date getTime() {
		return this.time;
	}
	
	public void setState(EnumUserAppState state) {
		this.state = state;
	}
	public EnumUserAppState getState() {
		return this.state;
	}
	
	public void setDelState(int del) {
		this.del = del;
	}
	public int getDelState() {
		return this.del;
	}
	
	public void setContent(String content) {
		this.content = content;
	}
	public String getContent() {
		return this.content;
	}
	
	public void setComment(String comment) {
		this.comment = comment;
	}
	public String getComments() {
		return this.comment;
	}
	
	public void setUserId(int userId) {
		this.userId = userId;
	}
	public int getUserId() {
		return this.userId;
	}
	
	public void setTemplateId(int templateId) {
		this.templateId = templateId;
	}
	public int getTemplateId() {
		return this.templateId;
	}
}
