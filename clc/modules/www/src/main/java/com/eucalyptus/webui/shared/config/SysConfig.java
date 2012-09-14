package com.eucalyptus.webui.shared.config;

import java.util.HashMap;


public class SysConfig implements java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	String language;
	
	HashMap<String, String> viewTableSizeConfig = new HashMap<String, String>();
	
	public void setLanguage(String language) {
		this.language = language;
	}
	public String getLanguage() {
		return this.language;
	}
	
	public void setViewTableSizeConfig(HashMap<String, String> viewTableSizeConfig) {
		this.viewTableSizeConfig = viewTableSizeConfig;
	}
	public HashMap<String, String> getViewTableSizeConfig() {
		return this.viewTableSizeConfig;
	}
}
