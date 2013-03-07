package com.eucalyptus.webui.client.view;

import java.util.Set;

import com.eucalyptus.webui.client.service.SearchResultRow;
import com.eucalyptus.webui.client.view.DeviceSearchResultTable.DeviceSearchResultTableClickHandler;
import com.eucalyptus.webui.shared.resource.device.status.CPUState;
import com.google.gwt.user.client.ui.IsWidget;

public interface DeviceCPUView extends IsWidget, CanDisplaySearchResult, Clearable, SelectionController {
    
    public void setPresenter(Presenter presenter);
    
    public Set<SearchResultRow> getSelectedSet();
    
    public void setSelectedRow(SearchResultRow row);
    
    public void updateLabels();
    
    public interface Presenter extends SearchRangeChangeHandler, MultiSelectionChangeHandler, KnowsPageSize, DeviceSearchResultTableClickHandler {

        public void onAddCPU();
        
        public void onModifyCPU();
        
        public void onDeleteCPU();
        
        public boolean canDeleteCPU();
        
        public boolean canModifyCPU();
        
        public void updateSearchResult();
        
        public CPUState getQueryState();
        
        public int getCounts(CPUState state);
        
        public void setQueryState(CPUState state);
        
    }

}
