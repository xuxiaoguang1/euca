package com.eucalyptus.webui.client.view;

import java.util.Set;

import com.eucalyptus.webui.client.service.SearchResultRow;
import com.eucalyptus.webui.client.view.DeviceSearchResultTable.DeviceSearchResultTableClickHandler;
import com.google.gwt.user.client.ui.IsWidget;

public interface DeviceTemplateView extends IsWidget, CanDisplaySearchResult, Clearable, SelectionController {
    
    public void setPresenter(Presenter presenter);
    
    public Set<SearchResultRow> getSelectedSet();
    
    public void setSelectedRow(SearchResultRow row);
    
    public int getPageSize();
    
    public interface Presenter extends SearchRangeChangeHandler, MultiSelectionChangeHandler, KnowsPageSize, DeviceSearchResultTableClickHandler {
        
        public void onAddTemplate();
        
        public void onModifyTemplate();
        
        public void onDeleteTemplate();
        
        public boolean canDeleteTemplate();
        
        public boolean canModifyTemplate();
        
        public void updateSearchResult();
        
    }

}
