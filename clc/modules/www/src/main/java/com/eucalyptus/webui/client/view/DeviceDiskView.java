package com.eucalyptus.webui.client.view;

import java.util.List;
import java.util.Set;

import com.eucalyptus.webui.client.activity.device.DeviceDiskActivity.DiskState;
import com.eucalyptus.webui.client.service.SearchResultRow;
import com.google.gwt.user.client.ui.IsWidget;

public interface DeviceDiskView extends IsWidget, CanDisplaySearchResult, Clearable, SelectionController {
	
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
		
		long getCounts(DiskState state);
		
		void setQueryState(DiskState state);
		
		DiskState getQueryState();
		
	}

}
