package com.eucalyptus.webui.server.config;

import java.util.ArrayList;
import java.util.Hashtable;

import com.eucalyptus.webui.client.service.SearchResultFieldDesc;
import com.eucalyptus.webui.shared.config.EnumLanguage;

public class ViewSearchTableColConfig {

	public static ViewSearchTableColConfig instance() {
		if (instance == null) {
			instance = new ViewSearchTableColConfig();
		}
		
		return instance;
	}
	
	public void setConfig(String srvName, ArrayList<SearchTableCol> tableCol) {
		
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
	
	public ArrayList<SearchResultFieldDesc> getConfig(String srvName,  EnumLanguage language) {
		return this.fieldDesc.get(srvName)[language.ordinal()];
	}
	
	public ArrayList<SearchTableCol> getConfig(String srvName) {
		return this.tableCols.get(srvName);
	}
	
	private ViewSearchTableColConfig() {
	}
	
	private Hashtable<String, ArrayList<SearchTableCol>> tableCols = new Hashtable<String, ArrayList<SearchTableCol>>();
	private Hashtable<String, ArrayList<SearchResultFieldDesc>[]> fieldDesc = new Hashtable<String, ArrayList<SearchResultFieldDesc>[]>(); 
	private static ViewSearchTableColConfig instance;
}
