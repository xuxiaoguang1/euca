package com.eucalyptus.webui.server.config;

public class WSConfig {

	public static WSConfig instance() {
		if (instance == null) {
			instance = new WSConfig();
		}
		
		return instance;
	}
	
	public void set(String url) {
		this.url = url;
	}
	
	public String url() {
		return this.url;
	}
	
	String url = null;
	
	private static WSConfig instance;
}
