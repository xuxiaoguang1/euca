package com.eucalyptus.webui.client.view;

import java.util.Set;

import com.eucalyptus.webui.client.service.SearchResultRow;
import com.eucalyptus.webui.client.view.DeviceSearchResultTable.DeviceSearchResultTableClickHandler;
import com.eucalyptus.webui.shared.resource.device.status.IPState;
import com.eucalyptus.webui.shared.resource.device.status.IPType;
import com.google.gwt.user.client.ui.IsWidget;

public interface DeviceIPView extends IsWidget, CanDisplaySearchResult, Clearable, SelectionController {
    
    public void setPresenter(Presenter presenter);
    
    public Set<SearchResultRow> getSelectedSet();
    
    public void setSelectedRow(SearchResultRow row);
    
    public void updateLabels();
    
    public interface Presenter extends SearchRangeChangeHandler, MultiSelectionChangeHandler, KnowsPageSize, DeviceSearchResultTableClickHandler {
        
        public void onAddIPService();
        
        public void onDeleteIPService();
        
        public boolean canDeleteIPService();
        
        public void updateSearchResult();
        
        public IPState getQueryState();
        
        public int getCounts(IPState state);
        
        public void setQueryState(IPState state);
        
        public void setQueryType(IPType type);

    }

}
