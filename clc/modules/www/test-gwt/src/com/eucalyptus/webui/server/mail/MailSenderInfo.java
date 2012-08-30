package com.eucalyptus.webui.server.mail;

public class MailSenderInfo {
	private static MailSenderInfo instance;
	private String host;
	private String user;
	private String pwd;
	
	static public MailSenderInfo instance() {
		if (instance == null)
			instance = new MailSenderInfo();
		
		return instance;
	}
	
	private MailSenderInfo() {
	}
	
	public void setHost(String host) {
		this.host = host;
	}
	public String getHost() {
		return this.host;
	}
	
	public void setUser(String user) {
		this.user = user;
	}
	public String getUser() {
		return this.user;
	}
	
	public void setPwd(String pwd) {
		this.pwd = pwd;
	}
	public String getPwd() {
		return this.pwd;
	}
	
}
