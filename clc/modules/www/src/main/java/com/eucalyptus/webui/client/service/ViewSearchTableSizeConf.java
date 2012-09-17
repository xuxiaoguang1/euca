package com.eucalyptus.webui.client.service;

import java.util.HashMap;

public class ViewSearchTableSizeConf {
	
	public static ViewSearchTableSizeConf instance() {
		if (instance == null) {
			instance = new ViewSearchTableSizeConf();
		}
		
		return instance;
	}
	
	public void setViewTableSizeConfig(HashMap<String, String> hashMap) {
		searchTableSizeConfList = hashMap;
	}
	
	public int getPageSize(String viewActivityName) {
		Object tableSize = this.searchTableSizeConfList.get(viewActivityName.toLowerCase());
		
		if (tableSize != null)
			return Integer.parseInt(tableSize.toString());
		
		return this.DEFAULT_SEARCH_TABLE_SIZE; 
	}
	
	private ViewSearchTableSizeConf() {
	}
	
	private int DEFAULT_SEARCH_TABLE_SIZE = 10;
	
	private HashMap<String, String> searchTableSizeConfList;
	
	private static ViewSearchTableSizeConf instance;
}
