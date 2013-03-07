package com.eucalyptus.webui.client.view;

import java.util.Set;

import com.eucalyptus.webui.client.service.SearchResultRow;
import com.eucalyptus.webui.client.view.DeviceSearchResultTable.DeviceSearchResultTableClickHandler;
import com.eucalyptus.webui.shared.resource.device.status.MemoryState;
import com.google.gwt.user.client.ui.IsWidget;

public interface DeviceMemoryView extends IsWidget, CanDisplaySearchResult, Clearable, SelectionController {
	
    public void setPresenter(Presenter presenter);
    
    public Set<SearchResultRow> getSelectedSet();
    
    public void setSelectedRow(SearchResultRow row);
    
    public void updateLabels();
    
    public interface Presenter extends SearchRangeChangeHandler, MultiSelectionChangeHandler, KnowsPageSize, DeviceSearchResultTableClickHandler {
        
        public void onAddMemory();
        
        public void onModifyMemory();
        
        public void onDeleteMemory();
        
        public boolean canDeleteMemory();
        
        public boolean canModifyMemory();
        
        public void updateSearchResult();
        
        public MemoryState getQueryState();
        
        public long getCounts(MemoryState state);
        
        public void setQueryState(MemoryState state);

	}

}
