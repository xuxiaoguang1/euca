package com.eucalyptus.webui.server;

import java.util.ArrayList;

import com.eucalyptus.webui.client.service.SearchRange;
import com.eucalyptus.webui.server.config.ViewSearchTableServerConfig;
import com.eucalyptus.webui.shared.config.EnumService;
import com.eucalyptus.webui.shared.config.SearchTableCol;

public class SorterProxy {

	public SorterProxy(EnumService srvName) {
		this.srvName = srvName;
	}
	
	public String orderBy(SearchRange range) {
		if (range == null)
			return null;
		
		int sortField = range.getSortField();
		if (sortField < 0)
			return null;
		
		ArrayList<SearchTableCol> tableCols = ViewSearchTableServerConfig.instance().getConfig(srvName);
		
		if (tableCols == null)
			return null;
		
		SearchTableCol tableCol = tableCols.get(sortField);
		
		if (tableCol.getSortable() == false)
			return null;
		
		String dbField = tableCols.get(sortField).getDbField();
		
		if (dbField == null)
			return null;
		
		StringBuilder orderBy = new StringBuilder(" ORDER BY ");
		orderBy.append(dbField);
		
		if (range.isAscending())
			orderBy.append(" ASC");
		else
			orderBy.append(" DESC");
		
		return orderBy.toString();
	}
	
	private EnumService srvName;
}
