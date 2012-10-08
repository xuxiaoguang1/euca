package com.eucalyptus.webui.client.view;

import java.util.Date;
import java.util.Set;

import com.eucalyptus.webui.client.service.SearchResultRow;
import com.eucalyptus.webui.client.view.DBSearchResultTable.DBSearchResultTableClickHandler;
import com.eucalyptus.webui.shared.resource.device.status.DiskState;
import com.google.gwt.user.client.ui.IsWidget;

public interface DeviceDiskView extends IsWidget, CanDisplaySearchResult, Clearable, SelectionController {
	
    public void setPresenter(Presenter presenter);
    
    public Set<SearchResultRow> getSelectedSet();
    
    public void setSelectedRow(SearchResultRow row);
    
    public void updateLabels();
    
    public int getPageSize();
	
    public interface Presenter extends SearchRangeChangeHandler, MultiSelectionChangeHandler, KnowsPageSize, DBSearchResultTableClickHandler {
        
        public void onAddDisk();
        
        public void onModifyDisk();
        
        public void onDeleteDisk();
        
        public void onAddDiskService();
        
        public void onModifyDiskService();
        
        public void onDeleteDiskService();
        
        public boolean canDeleteDisk();
        
        public boolean canModifyDisk();
        
        public boolean canAddDiskService();
        
        public boolean canDeleteDiskService();
        
        public boolean canModifyDiskService();
        
        public void updateSearchResult(Date dateBegin, Date dateEnd);
        
        public DiskState getQueryState();
        
        public long getCounts(DiskState state);
        
        public void setQueryState(DiskState state);

	}

}
