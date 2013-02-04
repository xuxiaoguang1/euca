package com.eucalyptus.webui.client.view;

import java.util.Date;
import java.util.Set;

import com.eucalyptus.webui.client.service.SearchResultRow;
import com.eucalyptus.webui.client.view.DeviceSearchResultTable.DeviceSearchResultTableClickHandler;
import com.google.gwt.user.client.ui.IsWidget;

public interface DeviceTemplatePriceView extends IsWidget, CanDisplaySearchResult, Clearable, SelectionController {
	
	public void setPresenter(Presenter presenter);
	
	public Set<SearchResultRow> getSelectedSet();
	
	public void setSelectedRow(SearchResultRow row);
	
	public int getPageSize();
	
	public interface Presenter extends MultiSelectionChangeHandler, KnowsPageSize, DeviceSearchResultTableClickHandler, SearchRangeChangeHandler {
		
		public void onAddTemplatePrice();
		
		public void onModifyTemplatePrice();
		
		public void onDeleteTemplatePrice();
		
		public boolean canModifyTemplatePrice();
		
		public boolean canDeleteTemplatePrice();
		
		public void updateSearchResult(Date dateBegin, Date dateEnd);
		
	}
	
}
