package com.eucalyptus.webui.client.view.device;

import java.util.Date;
import java.util.Set;

import com.eucalyptus.webui.client.service.SearchResultRow;
import com.eucalyptus.webui.client.view.CanDisplaySearchResult;
import com.eucalyptus.webui.client.view.Clearable;
import com.eucalyptus.webui.client.view.KnowsPageSize;
import com.eucalyptus.webui.client.view.MultiSelectionChangeHandler;
import com.eucalyptus.webui.client.view.SearchRangeChangeHandler;
import com.eucalyptus.webui.client.view.SelectionController;
import com.eucalyptus.webui.client.view.device.DBSearchResultTable.DBSearchResultTableClickHandler;
import com.google.gwt.user.client.ui.IsWidget;

public interface DeviceTemplatePriceView extends IsWidget, CanDisplaySearchResult, Clearable, SelectionController {
	
	public static final int DEFAULT_PAGESIZE = 20;
	
	public void setPresenter(Presenter presenter);
	
	public Set<SearchResultRow> getSelectedSet();
	
	public void setSelectedRow(SearchResultRow row);
	
	public interface Presenter extends MultiSelectionChangeHandler, KnowsPageSize, DBSearchResultTableClickHandler, SearchRangeChangeHandler {
		
		public void onAdd();
		
		public void onModify();
		
		public void onDelete();
		
		public void updateSearchResult(Date creationtimeBegin, Date creationtimeEnd, Date modifiedtimeBegin, Date modifiedtimeEnd);
		
	}
	
}
