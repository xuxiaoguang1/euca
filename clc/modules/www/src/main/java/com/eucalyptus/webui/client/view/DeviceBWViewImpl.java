package com.eucalyptus.webui.client.view;

import com.eucalyptus.webui.client.service.SearchResult;
import com.eucalyptus.webui.client.service.SearchResultRow;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.google.gwt.user.client.ui.Anchor;

public class DeviceBWViewImpl extends Composite implements DeviceBWView {

	private static BWViewImplUiBinder uiBinder = GWT.create(BWViewImplUiBinder.class);
	
	interface BWViewImplUiBinder extends UiBinder<Widget, DeviceBWViewImpl> {
	}
	
	@UiField LayoutPanel resultPanel;
	
	public DeviceBWViewImpl() {
		initWidget(uiBinder.createAndBindUi(this));
	}
	
	private Presenter presenter;
	
	private MultiSelectionModel<SearchResultRow> selection;
	
	private SearchResultTable table;
	
	@Override
    public void showSearchResult(SearchResult result) {
		if (table == null) {
			resultPanel.clear();
			selection = new MultiSelectionModel<SearchResultRow>(SearchResultRow.KEY_PROVIDER);
			selection.addSelectionChangeHandler(new Handler() {

				@Override
	            public void onSelectionChange(SelectionChangeEvent event) {
					System.err.println("OnSelectionChange");
	            }
				
			});
			
			// int pageSize = presenter.getPageSize();
			table = new SearchResultTable(DEFAULT_PAGESIZE, result.getDescs(), presenter, selection);
			table.load();
			resultPanel.add(table);
		}
		table.setData(result);
    }
	
	private static final int DEFAULT_PAGESIZE = 25;
	
	@Override
    public void clear() {
		resultPanel.clear();
		table = null;
    }

	@Override
    public void clearSelection() {
		selection.clear();
    }

	@Override
    public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
    }

}
