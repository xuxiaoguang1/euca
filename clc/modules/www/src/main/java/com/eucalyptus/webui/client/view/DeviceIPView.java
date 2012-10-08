package com.eucalyptus.webui.client.view;

import java.util.Date;
import java.util.Set;

import com.eucalyptus.webui.client.service.SearchResultRow;
import com.eucalyptus.webui.client.view.DBSearchResultTable.DBSearchResultTableClickHandler;
import com.eucalyptus.webui.shared.resource.device.status.IPState;
import com.eucalyptus.webui.shared.resource.device.status.IPType;
import com.google.gwt.user.client.ui.IsWidget;

public interface DeviceIPView extends IsWidget, CanDisplaySearchResult, Clearable, SelectionController {
	
    public void setPresenter(Presenter presenter);
    
    public Set<SearchResultRow> getSelectedSet();
    
    public void setSelectedRow(SearchResultRow row);
    
    public void updateLabels();
    
    public int getPageSize();
	
    public interface Presenter extends SearchRangeChangeHandler, MultiSelectionChangeHandler, KnowsPageSize, DBSearchResultTableClickHandler {
        
        public void onAddIP();
        
        public void onModifyIP();
        
        public void onDeleteIP();
        
        public void onAddIPService();
        
        public void onModifyIPService();
        
        public void onDeleteIPService();
        
        public boolean canDeleteIP();
        
        public boolean canModifyIP();
        
        public boolean canAddIPService();
        
        public boolean canDeleteIPService();
        
        public boolean canModifyIPService();
        
        public void updateSearchResult(Date dateBegin, Date dateEnd);
        
        public IPState getQueryState();
        
        public int getCounts(IPState state);
        
        public void setQueryState(IPState state);
        
        public void setQueryType(IPType type);

	}

}
