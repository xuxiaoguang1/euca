package com.eucalyptus.webui.server.mail;

import com.sun.net.httpserver.Authenticator;
import com.sun.net.httpserver.HttpExchange;

public class MailSenderAuthenticator extends Authenticator {

	static public MailSenderAuthenticator instance() {
		if (instance == null)
			instance = new MailSenderAuthenticator();
		
		return  instance;
	}
	
	private MailSenderAuthenticator() {
		super();
	}
	
	public void setInfo(String user, String pwd) {
		this.user = user;
		this.pwd = pwd;
	}
	
	public String getUser() {
		return this.user;
	}
	
	public String getPwd() {
		return this.pwd;
	}
	
	@Override
	public Result authenticate(HttpExchange arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	private static MailSenderAuthenticator instance;
	
	private String user;
	private String pwd;
}
