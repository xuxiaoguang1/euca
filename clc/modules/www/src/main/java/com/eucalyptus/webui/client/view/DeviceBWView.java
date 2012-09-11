package com.eucalyptus.webui.client.view;

import java.util.List;
import java.util.Set;

import com.eucalyptus.webui.client.service.SearchResultRow;
import com.eucalyptus.webui.client.view.DeviceMirrorSearchResultTable.Presenter;
import com.google.gwt.user.client.ui.IsWidget;

public interface DeviceBWView extends IsWidget, CanDisplaySearchResult, Clearable, SelectionController {
	
	public static final int DEFAULT_PAGESIZE = 22;
	
	void setPresenter(Presenter presenter);
	
	void openMirrorMode(MirrorModeType type, List<SearchResultRow> data);
	
	void closeMirrorMode();
	
	DeviceMirrorSearchResultTable getMirrorTable();
	
	boolean isMirrorMode();
	
	MirrorModeType getMirrorModeType();
	
	Set<SearchResultRow> getSelectedSet();
	
	enum MirrorModeType {
		MODIFY_SERVICE,
		DELETE_SERVICE,
	}
	
	public interface Presenter extends SearchRangeChangeHandler, MultiSelectionChangeHandler, KnowsPageSize, DeviceMirrorSearchResultTable.Presenter {
		
		void onAddService();
		
		void onModifyService();
		
		void onDeleteService();
		
		void onClearSelection();
		
		void onMirrorBack();
		
		void onMirrorDeleteAll();
		
	}

}
