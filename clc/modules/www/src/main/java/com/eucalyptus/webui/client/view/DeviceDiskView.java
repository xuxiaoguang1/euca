package com.eucalyptus.webui.client.view;

import java.util.Set;

import com.eucalyptus.webui.client.service.SearchResultRow;
import com.eucalyptus.webui.client.view.DeviceSearchResultTable.DeviceSearchResultTableClickHandler;
import com.eucalyptus.webui.shared.resource.device.status.DiskState;
import com.google.gwt.user.client.ui.IsWidget;

public interface DeviceDiskView extends IsWidget, CanDisplaySearchResult, Clearable, SelectionController {
	
    public void setPresenter(Presenter presenter);
    
    public Set<SearchResultRow> getSelectedSet();
    
    public void setSelectedRow(SearchResultRow row);
    
    public void updateLabels();
    
    public interface Presenter extends SearchRangeChangeHandler, MultiSelectionChangeHandler, KnowsPageSize, DeviceSearchResultTableClickHandler {
        
        public void onAddDisk();
        
        public void onModifyDisk();
        
        public void onDeleteDisk();
        
        public boolean canDeleteDisk();
        
        public boolean canModifyDisk();
        
        public void updateSearchResult();
        
        public DiskState getQueryState();
        
        public long getCounts(DiskState state);
        
        public void setQueryState(DiskState state);

	}

}
