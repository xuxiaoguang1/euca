package com.eucalyptus.webui.client.view;

import java.util.Date;
import java.util.Set;

import com.eucalyptus.webui.client.service.SearchResultRow;
import com.eucalyptus.webui.client.view.DeviceSearchResultTable.DeviceSearchResultTableClickHandler;
import com.google.gwt.user.client.ui.IsWidget;

public interface DeviceCPUPriceView extends IsWidget, CanDisplaySearchResult, Clearable, SelectionController {
	
	public void setPresenter(Presenter presenter);
	
	public Set<SearchResultRow> getSelectedSet();
	
	public void setSelectedRow(SearchResultRow row);
	
	public int getPageSize();
	
	public interface Presenter extends MultiSelectionChangeHandler, KnowsPageSize, DeviceSearchResultTableClickHandler, SearchRangeChangeHandler {
		
		public void onAddCPUPrice();
		
		public void onModifyCPUPrice();
		
		public void onDeleteCPUPrice();
		
		public boolean canDeleteCPUPrice();
        
        public boolean canModifyCPUPrice();
		
        public void updateSearchResult(Date dateBegin, Date dateEnd);
		
	}
	
}
