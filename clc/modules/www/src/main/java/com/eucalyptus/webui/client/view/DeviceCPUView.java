package com.eucalyptus.webui.client.view;

import java.util.List;
import java.util.Set;

import com.eucalyptus.webui.client.activity.DeviceCPUActivity.CPUState;
import com.eucalyptus.webui.client.service.SearchResultRow;
import com.google.gwt.user.client.ui.IsWidget;

public interface DeviceCPUView extends IsWidget, CanDisplaySearchResult, Clearable, SelectionController {
	
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
		MODIFY_SERVICE, MODIFY_DEVICE;
	}
	
	public interface Presenter extends SearchRangeChangeHandler, MultiSelectionChangeHandler, KnowsPageSize, DeviceMirrorSearchResultTable.Presenter {
		
		void onAddService();
		
		void onDelService();
		
		void onExtendService();
		
		void onAddCPU();
		
		void onDelCPU();
		
		void onClearSelection();
		
		void onModifyCPU();
		
		void onMirrorCancel();
		
		int getCounts(CPUState state);
		
		void setQueryState(CPUState state);
		
		CPUState getQueryState();
		
	}

}
