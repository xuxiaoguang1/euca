package com.eucalyptus.webui.client.view;

import java.util.Date;
import java.util.List;
import java.util.Set;

import com.eucalyptus.webui.client.service.SearchResultRow;
import com.google.gwt.user.client.ui.IsWidget;

public interface DeviceTemplateView extends IsWidget, CanDisplaySearchResult, Clearable, SelectionController {
	
	public static final int DEFAULT_PAGESIZE = 20;
	
	void setPresenter(Presenter presenter);
	
	void openMirrorMode(MirrorModeType type, List<SearchResultRow> data);
	
	void closeMirrorMode();
	
	DeviceMirrorSearchResultTable getMirrorTable();
	
	boolean isMirrorMode();
	
	MirrorModeType getMirrorModeType();
	
	Set<SearchResultRow> getSelectedSet();
	
	enum MirrorModeType {
		MODIFY_TEMPLATE,
		DELETE_TEMPLATE,
	}
	
	public interface Presenter extends SearchRangeChangeHandler, MultiSelectionChangeHandler, KnowsPageSize, DeviceMirrorSearchResultTable.Presenter {
		
		void onAddTemplate();
		
		void onModifyTemplate();
		
		void onDeleteTemplate();
		
		void onClearSelection();
		
		void onMirrorBack();
		
		void onMirrorDeleteAll();
		
		void onSearch(Date starttime, Date endtime);
		
	}

}
