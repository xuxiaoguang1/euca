package com.eucalyptus.webui.client.view.device;

import java.util.Date;
import java.util.Set;

import com.eucalyptus.webui.client.activity.device.ClientMessage;
import com.eucalyptus.webui.client.service.SearchResult;
import com.eucalyptus.webui.client.service.SearchResultRow;
import com.eucalyptus.webui.client.view.device.DeviceDateBox.Handler;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.DateBox;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent;

public class DeviceRoomViewImpl extends Composite implements DeviceRoomView {
	
	private static DeviceRoomViewImplUiBinder uiBinder = GWT.create(DeviceRoomViewImplUiBinder.class);
	
	interface DeviceRoomViewImplUiBinder extends UiBinder<Widget, DeviceRoomViewImpl> {
	}
	
	@UiField LayoutPanel resultPanel;
	@UiField Anchor buttonAdd;
	@UiField Anchor buttonDelete;
	@UiField Anchor buttonModify;
	@UiField Anchor buttonClearSelection;
	@UiField DeviceDateBox creationtimeBegin;
	@UiField DeviceDateBox creationtimeEnd;
	@UiField DeviceDateBox modifiedtimeBegin;
	@UiField DeviceDateBox modifiedtimeEnd;
	@UiField Anchor buttonClearDate;
	
	private Presenter presenter;
	private MultiSelectionModel<SearchResultRow> selection;
	private DBSearchResultTable table;
	private DevicePopupPanel popup = new DevicePopupPanel();
	
	private DeviceDateBox[] dateBoxList;
	
	public DeviceRoomViewImpl() {
		initWidget(uiBinder.createAndBindUi(this));
		selection = new MultiSelectionModel<SearchResultRow>(SearchResultRow.KEY_PROVIDER);
		selection.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {

			@Override
			public void onSelectionChange(SelectionChangeEvent event) {
				updateSearchResultButtonStatus();
				presenter.onSelectionChange(selection.getSelectedSet());
			}
			
		});
		updateSearchResultButtonStatus();
		
		popup.setAutoHideEnabled(true);
        
		dateBoxList = new DeviceDateBox[]{creationtimeBegin, creationtimeEnd, modifiedtimeBegin, modifiedtimeEnd};
		
		for (final DeviceDateBox dateBox : dateBoxList) {
		    dateBox.setErrorHandler(new Handler() {

				@Override
				public void onErrorHappens() {
					updateDateButtonStatus();
					int x = dateBox.getAbsoluteLeft();
		            int y = dateBox.getAbsoluteTop() + dateBox.getOffsetHeight();
					popup.setHTML(x, y, "15EM", "3EM", getDateErrorHTML(dateBox));
				}

				@Override
				public void onValueChanged() {
					updateDateButtonStatus();
                	int x = dateBox.getAbsoluteLeft();
		            int y = dateBox.getAbsoluteTop() + dateBox.getOffsetHeight();
                    DeviceDateBox box0, box1, pair;
                    if (dateBox == creationtimeBegin || dateBox == creationtimeEnd) {
                        box0 = creationtimeBegin;
                        box1 = creationtimeEnd;
                    }
                    else {
                        box0 = modifiedtimeBegin;
                        box1 = modifiedtimeEnd;
                    }
                    pair = (box0 != dateBox ? box0 : box1);
                    if (!pair.hasError()) {
                    	Date date0 = box0.getValue(), date1 = box1.getValue();
                    	if (date0 != null && date1 != null) {
                    		if (date0.getTime() > date1.getTime()) {
                    			popup.setHTML(x, y, "12EM", "2EM", getDateErrorHTML(box0, box1));
                    			return;
                    		}
                    	}
                    	updateSearchRange();
                    }
				}
				
		    });
		}
		updateDateButtonStatus();
	}
	
	private HTML getDateErrorHTML(DeviceDateBox dateBox) {
	    StringBuilder sb = new StringBuilder();
	    sb.append("<div>");
	    sb.append("<font color='").append("black").append("'>");
	    sb.append(new ClientMessage("", "无效的日期格式: "));
	    sb.append("</font>");
	    sb.append("<font color='").append("red").append("'>");
	    sb.append("'").append(dateBox.getText()).append("'");
	    sb.append("</font>");
	    sb.append("</div>");
	    sb.append("<div>");
	    sb.append("<font color='").append("black").append("'>");
	    sb.append(new ClientMessage("", "请输入有效格式")).append(": 'YYYY-MM-DD'");
	    sb.append("</font>");
	    sb.append("<div>");
	    sb.append("</div>");
	    sb.append("<font color='").append("black").append("'>");
	    sb.append(new ClientMessage("", "例如: '2012-07-01'"));
	    sb.append("</font>");
	    sb.append("</div>");
	    return new HTML(sb.toString());
	}
	
	private HTML getDateErrorHTML(DeviceDateBox box0, DeviceDateBox box1) {
	    StringBuilder sb = new StringBuilder();
	    sb.append("<div>");
	    sb.append("<font color='").append("black").append("'>");
	    sb.append(new ClientMessage("", "无效的日期查询: "));
	    sb.append("</font>");
	    sb.append("</div>");
	    sb.append("<div>");
	    sb.append("<font color='").append("darkred").append("'>");
	    sb.append("'").append(box0.getText()).append("' > '").append(box1.getText()).append("'");
	    sb.append("</font>");
	    sb.append("</div>");
	    return new HTML(sb.toString());
	}
	
	private void updateSearchResultButtonStatus() {
		int size = selection.getSelectedSet().size();
		buttonAdd.setEnabled(true);
		buttonDelete.setEnabled(size != 0);
		buttonModify.setEnabled(size == 1);
		buttonClearSelection.setEnabled(size != 0);
	}
	
	private void updateDateButtonStatus() {
	    for (DeviceDateBox dateBox : dateBoxList) {
	    	if (!isEmpty(dateBox.getText())) {
	            buttonClearDate.setEnabled(true);
	            return;
	        }
	    }
	    buttonClearDate.setEnabled(false);
	}
	
	public boolean isEmpty(String s) {
		return s == null || s.length() == 0;
	}
	
	private void updateSearchRange() {
		for (DeviceDateBox dateBox : dateBoxList) {
			if (dateBox.hasError()) {
				return;
			}
		}
		presenter.updateSearchResult(creationtimeBegin.getValue(), creationtimeEnd.getValue(),
				modifiedtimeBegin.getValue(), modifiedtimeEnd.getValue());
	}
	
	@Override
	public void showSearchResult(SearchResult result) {
		if (table == null) {
			table = new DBSearchResultTable(DEFAULT_PAGESIZE, result.getDescs(), selection);
			table.setRangeChangeHandler(presenter);
			table.setClickHandler(presenter);
			table.load();
			resultPanel.add(table);
		}
		table.setData(result);
	}

	@Override
	public void clear() {
		resultPanel.clear();
		table = null;
	}
	
	@Override
	public Set<SearchResultRow> getSelectedSet() {
		return selection.getSelectedSet();
	}
	
	@Override
	public void setSelectedRow(SearchResultRow row) {
		clearSelection();
		if (row != null) {
			selection.setSelected(row, true);
		}
		updateSearchResultButtonStatus();
	}
	
	@Override
	public void clearSelection() {
		selection.clear();
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}
	
	@UiHandler("buttonAdd")
	void handleButtonAdd(ClickEvent event) {
		if (buttonAdd.isEnabled()) {
			presenter.onAdd();
		}
	}
	
	@UiHandler("buttonDelete")
	void handleButtonDelete(ClickEvent event) {
		if (buttonDelete.isEnabled()) {
			presenter.onDelete();
		}
	}
	
	@UiHandler("buttonModify")
	void handleButtonModify(ClickEvent event) {
		if (buttonModify.isEnabled()) {
			presenter.onModify();
		}
	}
	
	@UiHandler("buttonClearSelection")
	void handleButtonClearSelection(ClickEvent event) {
		if (buttonClearSelection.isEnabled()) {
			clearSelection();
		}
	}
	
	@UiHandler("buttonClearDate")
	void handleButtonClearDate(ClickEvent event) {
	    if (buttonClearDate.isEnabled()) {
    	    for (DateBox dateBox : dateBoxList) {
    	        dateBox.setValue(null);
    	    }
    	    updateDateButtonStatus();
    	    updateSearchRange();
	    }
	}
	
}
