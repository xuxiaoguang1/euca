package com.eucalyptus.webui.client.service;

import java.util.ArrayList;
import java.util.HashMap;

import com.eucalyptus.webui.shared.config.EnumService;
import com.eucalyptus.webui.shared.config.SearchTableCol;

public class ViewSearchTableClientConfig {

	public static ViewSearchTableClientConfig instance() {
		if (instance == null) {
			instance = new ViewSearchTableClientConfig();
		}
		
		return instance;
	}
	
	public ArrayList<SearchTableCol> getConfig(EnumService srvName) {
		return this.tableCols.get(srvName);
	}
	
	public int getSearchTableColIndex(EnumService srvName, String db_field) {
		ArrayList<SearchTableCol> cols = this.tableCols.get(srvName);
		
		if (cols == null)
			return -1;
		
		for (int i=0; i<cols.size(); i++) {
			if (cols.get(i).getDbField().equalsIgnoreCase(db_field))
				return i;
		}
		
		return -1;
	}
	
	public void setViewTableSizeConfig(HashMap<EnumService, String> HashMap) {
		this.searchTableSizeConfList = HashMap;
	}
	
	public void setViewTableColConfig(HashMap<EnumService, ArrayList<SearchTableCol>> tableCols) {
		this.tableCols = tableCols;
	}
	
	public int getPageSize(EnumService srv) {
		Object tableSize = this.searchTableSizeConfList.get(srv);
		
		if (tableSize != null)
			return Integer.parseInt(tableSize.toString());
		
		return this.DEFAULT_SEARCH_TABLE_SIZE; 
	}
	
	private ViewSearchTableClientConfig() {
	}
	
	private HashMap<EnumService, ArrayList<SearchTableCol>> tableCols;
	private HashMap<EnumService, String> searchTableSizeConfList;
	
	private int DEFAULT_SEARCH_TABLE_SIZE = 10;
	
	private static ViewSearchTableClientConfig instance;
}
