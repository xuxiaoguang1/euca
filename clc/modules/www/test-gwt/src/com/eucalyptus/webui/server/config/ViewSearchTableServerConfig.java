package com.eucalyptus.webui.server.config;

import java.util.ArrayList;
import java.util.HashMap;

import com.eucalyptus.webui.client.service.SearchResultFieldDesc;
import com.eucalyptus.webui.shared.config.EnumLanguage;
import com.eucalyptus.webui.shared.config.EnumService;
import com.eucalyptus.webui.shared.config.SearchTableCol;

public class ViewSearchTableServerConfig {

	public static ViewSearchTableServerConfig instance() {
		if (instance == null) {
			instance = new ViewSearchTableServerConfig();
		}
		
		return instance;
	}
	
	public void setConfig(EnumService srvName, ArrayList<SearchTableCol> tableCol) {
		
		int lanCount = EnumLanguage.values().length;
		
		@SuppressWarnings("unchecked")
		ArrayList<SearchResultFieldDesc>[] fields = new ArrayList [lanCount];
		
		for (int i=0; i<lanCount; i++) {
			fields[i] = new ArrayList<SearchResultFieldDesc>();
			
			for (SearchTableCol col : tableCol) {
				SearchResultFieldDesc field = new SearchResultFieldDesc(col.getTitle()[i], col.getSortable(),
																		col.getWidth(), col.getDisplay(), col.getTextType(),
																		col.getEditable(), col.getHidden());
				fields[i].add(field);
			}
		}
		
		this.tableCols.put(srvName, tableCol);
		this.fieldDesc.put(srvName, fields);
	}
	
	public ArrayList<SearchResultFieldDesc> getConfig(EnumService srvName,  EnumLanguage language) {
		return this.fieldDesc.get(srvName)[language.ordinal()];
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
	
	private ViewSearchTableServerConfig() {
	}
	
	private HashMap<EnumService, ArrayList<SearchTableCol>> tableCols = new HashMap<EnumService, ArrayList<SearchTableCol>>();
	private HashMap<EnumService, ArrayList<SearchResultFieldDesc>[]> fieldDesc = new HashMap<EnumService, ArrayList<SearchResultFieldDesc>[]>(); 	
	private HashMap<EnumService, String> searchTableSizeConfList;
	
	private int DEFAULT_SEARCH_TABLE_SIZE = 10;
	
	private static ViewSearchTableServerConfig instance;
}
