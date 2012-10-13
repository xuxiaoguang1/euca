package com.eucalyptus.webui.client.view;

import java.util.Date;
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
	
	public int getPageSize();

	public interface Presenter extends SearchRangeChangeHandler, MultiSelectionChangeHandler, KnowsPageSize, DeviceSearchResultTableClickHandler {

		public void onAddCPU();
		
		public void onModifyCPU();
		
		public void onDeleteCPU();
		
		public void onAddCPUService();
		
		public void onModifyCPUService();
		
		public void onDeleteCPUService();
		
		public boolean canDeleteCPU();
		
		public boolean canModifyCPU();
		
		public boolean canAddCPUService();
		
		public boolean canDeleteCPUService();
		
		public boolean canModifyCPUService();
		
		public void updateSearchResult(Date dateBegin, Date dateEnd);
		
		public CPUState getQueryState();
		
		public int getCounts(CPUState state);
		
		public void setQueryState(CPUState state);
		
	}

}
