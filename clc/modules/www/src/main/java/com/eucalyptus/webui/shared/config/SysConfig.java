package com.eucalyptus.webui.shared.config;

import java.util.ArrayList;
import java.util.HashMap;

public class SysConfig implements java.io.Serializable {
	
	public void setLanguage(EnumLanguage language) {
		this.language = language;
	}
	public EnumLanguage getLanguage() {
		return this.language;
	}
	
	public void setViewTableSizeConfig(EnumService srv, String size) {
		this.viewTableSizeConfig.put(srv, size);
	}
	public HashMap<EnumService, String> getViewTableSizeConfig() {
		return this.viewTableSizeConfig;
	}
	
	public void setViewTableColConfig(EnumService srv, ArrayList<SearchTableCol> cols) {
		this.tableCols.put(srv, cols);
	}
	public HashMap<EnumService, ArrayList<SearchTableCol>> getViewTableColConfig() {
		return this.tableCols;
	}
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	EnumLanguage language;
	
	HashMap<EnumService, String> viewTableSizeConfig = new HashMap<EnumService, String>();
	HashMap<EnumService, ArrayList<SearchTableCol>> tableCols = new HashMap<EnumService, ArrayList<SearchTableCol>> ();
}
