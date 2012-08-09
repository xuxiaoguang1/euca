package com.eucalyptus.webui.client.view;

import java.util.List;
import java.util.Set;

import com.eucalyptus.webui.client.activity.DeviceIPActivity.IPState;
import com.eucalyptus.webui.client.activity.DeviceIPActivity.IPType;
import com.eucalyptus.webui.client.service.SearchResultRow;
import com.google.gwt.user.client.ui.IsWidget;

public interface DeviceIPView extends IsWidget, CanDisplaySearchResult, Clearable, SelectionController {
	
	public static final int DEFAULT_PAGESIZE = 13;
	
	void setPresenter(Presenter presenter);
	
	void updateLabels();
	
	void openMirrorMode(MirrorModeType type, List<SearchResultRow> data);
	
	void closeMirrorMode();
	
	DeviceMirrorSearchResultTable getMirrorTable();
	
	boolean isMirrorMode();
	
	MirrorModeType getMirrorModeType();
	
	Set<SearchResultRow> getSelectedSet();
	
	enum MirrorModeType {
		MODIFY_SERVICE,
		DELETE_SERVICE,
		DELETE_DEVICE,
		ADD_SERVICE,
	}
	
	public interface Presenter extends SearchRangeChangeHandler, MultiSelectionChangeHandler, KnowsPageSize, DeviceMirrorSearchResultTable.Presenter {
		
		void onAddService();
		
		void onModifyService();
		
		void onDeleteService();
		
		void onDeleteDevice();
		
		void onAddDevice();
		
		void onClearSelection();
		
		void onMirrorBack();
		
		void onMirrorDeleteAll();
		
		int getCounts(IPState state);
		
		void setQueryState(IPState state);
		
		void setQueryType(IPType type);
		
		IPState getQueryState();
		
	}

}
