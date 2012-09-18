package com.eucalyptus.webui.client.view;

import java.util.Date;
import java.util.Set;

import com.eucalyptus.webui.client.service.SearchResultRow;
import com.eucalyptus.webui.client.view.DBSearchResultTable.DBSearchResultTableClickHandler;
import com.eucalyptus.webui.shared.resource.device.status.ServerState;
import com.google.gwt.user.client.ui.IsWidget;

public interface DeviceServerView extends IsWidget, CanDisplaySearchResult, Clearable, SelectionController {
	
	public void setPresenter(Presenter presenter);
	
	public Set<SearchResultRow> getSelectedSet();
	
	public void setSelectedRow(SearchResultRow row);
	
	public void updateLabels();
	
	public int getPageSize();
	
	public interface Presenter extends SearchRangeChangeHandler, MultiSelectionChangeHandler, KnowsPageSize, DBSearchResultTableClickHandler {
		
		public void onAddServer();
		
		public void onDeleteServer();
		
		public void onModifyServer();
		
		public void onOperateServer();
		
		public void updateSearchResult(Date dateBegin, Date dateEnd);
		
		public ServerState getQueryState();

		public int getCounts(ServerState state);
		
		public void setQueryState(ServerState queryState);
		
	}

}
