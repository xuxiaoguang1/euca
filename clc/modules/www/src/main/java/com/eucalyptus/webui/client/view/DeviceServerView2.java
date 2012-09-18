package com.eucalyptus.webui.client.view;

import java.util.List;
import java.util.Set;

import com.eucalyptus.webui.client.service.SearchResultRow;
import com.eucalyptus.webui.shared.resource.device.status.ServerState;
import com.google.gwt.user.client.ui.IsWidget;

public interface DeviceServerView2 extends IsWidget, CanDisplaySearchResult, Clearable, SelectionController {
	
	public static final int DEFAULT_PAGESIZE = 20;
	
	void setPresenter(Presenter presenter);
	
	void updateLabels();
	
	void openMirrorMode(MirrorModeType type, List<SearchResultRow> data);
	
	void closeMirrorMode();
	
	DeviceMirrorSearchResultTable getMirrorTable();
	
	boolean isMirrorMode();
	
	MirrorModeType getMirrorModeType();
	
	Set<SearchResultRow> getSelectedSet();
	
	enum MirrorModeType {
		MODIFY_STATE,
		DELETE_DEVICE,
	}
	
	public interface Presenter extends SearchRangeChangeHandler, MultiSelectionChangeHandler, KnowsPageSize, DeviceMirrorSearchResultTable.Presenter {
		
		void onModifyState();
		
		void onDeleteDevice();
		
		void onAddDevice();
		
		void onClearSelection();
		
		void onMirrorBack();
		
		void onMirrorDeleteAll();
		
		int getCounts(ServerState state);
		
		void setQueryState(ServerState state);
		
		ServerState getQueryState();
		
	}

}
