package com.eucalyptus.webui.client.view.device;

import java.util.List;
import java.util.Set;

import com.eucalyptus.webui.client.activity.device.DeviceCPUActivity.CPUState;
import com.eucalyptus.webui.client.service.SearchResultRow;
import com.eucalyptus.webui.client.view.CanDisplaySearchResult;
import com.eucalyptus.webui.client.view.Clearable;
import com.eucalyptus.webui.client.view.KnowsPageSize;
import com.eucalyptus.webui.client.view.MultiSelectionChangeHandler;
import com.eucalyptus.webui.client.view.SearchRangeChangeHandler;
import com.eucalyptus.webui.client.view.SelectionController;
import com.google.gwt.user.client.ui.IsWidget;

public interface DeviceCPUView extends IsWidget, CanDisplaySearchResult, Clearable, SelectionController {
	
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
		
		int getCounts(CPUState state);
		
		void setQueryState(CPUState state);
		
		CPUState getQueryState();
		
	}

}
