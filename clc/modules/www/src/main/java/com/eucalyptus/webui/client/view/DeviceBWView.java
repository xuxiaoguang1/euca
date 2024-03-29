package com.eucalyptus.webui.client.view;

import java.util.Set;

import com.eucalyptus.webui.client.service.SearchResultRow;
import com.eucalyptus.webui.client.view.DeviceSearchResultTable.DeviceSearchResultTableClickHandler;
import com.google.gwt.user.client.ui.IsWidget;

public interface DeviceBWView extends IsWidget, CanDisplaySearchResult, Clearable, SelectionController {
	
    public void setPresenter(Presenter presenter);
    
    public Set<SearchResultRow> getSelectedSet();
    
    public void setSelectedRow(SearchResultRow row);
    
    public interface Presenter extends SearchRangeChangeHandler, MultiSelectionChangeHandler, KnowsPageSize, DeviceSearchResultTableClickHandler {
        
        public void onAddBWService();
        
        public void onModifyBWService();
        
        public void onDeleteBWService();
        
        public boolean canDeleteBWService();
        
        public boolean canModifyBWService();
        
        public void updateSearchResult();
        
	}

}
