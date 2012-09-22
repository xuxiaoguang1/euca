package com.eucalyptus.webui.server.config;

public class DBConfig {

	public static DBConfig instance() {
		if (instance == null) {
			instance = new DBConfig();
		}
		
		return instance;
	}
	
	public void set(String url, String usr, String pwd) {
		this.url = url;
		this.usr = usr;
		this.pwd = pwd;
	}
	
	public String url() {
		return this.url;
	}
	
	public String usr() {
		return this.usr;
	}
	
	public String pwd() {
		return this.pwd;
	}
	
	String url = null;
	String usr = null;
	String pwd = null;
	
	private static DBConfig instance;
}
