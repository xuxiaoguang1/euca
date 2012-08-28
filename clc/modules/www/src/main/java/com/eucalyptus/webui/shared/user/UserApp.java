package com.eucalyptus.webui.shared.user;

import java.util.Date;


public class UserApp implements java.io.Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private int id;
	private Date apptime;
	private EnumUserAppState state;
	private EnumUserAppResult result;
	private int del;
	private String comment;
	
	private int userId;
	private int templateId;
	private int vmImageTypeId;
	
	private Date srvStartingTime;
	private Date srvEndingTime;
	
	public void setUAId(int ua_id) {
		this.id = ua_id;
	}
	public int getUAId() {
		return this.id;
	}
	
	public void setAppTime(Date apptime) {
		this.apptime = apptime;
	}
	public Date getAppTime() {
		return this.apptime;
	}
	
	public void setState(EnumUserAppState state) {
		this.state = state;
	}
	public EnumUserAppState getState() {
		return this.state;
	}
	
	public void setResult(EnumUserAppResult result) {
		this.result = result;
	}
	public EnumUserAppResult getResult() {
		return this.result;
	}
	
	public void setDelState(int del) {
		this.del = del;
	}
	public int getDelState() {
		return this.del;
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
	
	public void setVmImageTypeId(int vmImageTypeId) {
		this.vmImageTypeId = vmImageTypeId;
	}
	public int getVmIdImageTypeId() {
		return this.vmImageTypeId;
	}
	
	public void setSrvStartingTime(Date startingTime) {
		this.srvStartingTime = startingTime;
	}
	public Date getSrvStartingTime() {
		return this.srvStartingTime;
	}
	
	public void setSrvEndingingTime(Date endingTime) {
		this.srvEndingTime = endingTime;
	}
	public Date getSrvEndingTime() {
		return this.srvEndingTime;
	}
}
