package com.eucalyptus.webui.client.view;

import java.util.Collections;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.LongBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class DeviceMemoryDeviceAddViewImpl extends DialogBox implements DeviceMemoryDeviceAddView {
	
	private static DeviceMemoryDeviceAddViewImplUiBinder uiBinder = GWT.create(DeviceMemoryDeviceAddViewImplUiBinder.class);
	
	interface DeviceMemoryDeviceAddViewImplUiBinder extends UiBinder<Widget, DeviceMemoryDeviceAddViewImpl> {
	}
	
	public DeviceMemoryDeviceAddViewImpl() {
		super(false);
		setWidget(uiBinder.createAndBindUi(this));
	}
	
	private DeviceMemoryDeviceAddView.Presenter presenter;

	@Override
    public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
    }
	
	private DataCache cache = null;
	private boolean initialized = false;
	
	private void init() {
		if (initialized) {
			return ;
		}
		initialized = true;
		memoryNumList.clear();
		for (int i = 1; i <= 32; i ++) {
			memoryNumList.addItem(Integer.toString(i));
		}
		memoryTotalText.addChangeHandler(new LongBoxChangeHandler(memoryTotalText));
	}
	
	class LongBoxChangeHandler implements ChangeHandler {
		
		LongBox box;
		
		LongBoxChangeHandler(LongBox box) {
			this.box = box;
			box.setText(Long.toString(0));
		}

		@Override
        public void onChange(ChangeEvent event) {
			String value = Long.toString(longValue(box.getText()));
			if (!value.equals(box.getText())) {
				box.setText(value);
			}
        }
		
	}
	
	private Long longValue(String s) {
		try {
			return Long.parseLong(s);
		}
		catch (Exception e) {
			return (long)0;
		}
	}

	@Override
    public void popup() {
		if (cache == null) {
			presenter.lookupDevicesInfo();
		}
		init();
	    show();
    }
	
	@UiField ListBox serverMarkList;
	@UiField ListBox serverNameList;
	@UiField ListBox memoryNameList;
	@UiField TextBox memoryNameText;
	@UiField LongBox memoryTotalText;
	@UiField ListBox memoryNumList;
	
	void resetList(ListBox list, List<String> values, boolean sort) {
		if (sort) {
			Collections.sort(values);
		}
		list.clear();
		for (String s : values) {
			list.addItem(s);
		}
		list.setSelectedIndex(-1);
	}
	
	@Override
	public void setDevicesInfo(DataCache cache) {
		this.cache = cache;
		assert(cache.serverMarkList.size() == cache.serverNameList.size());
		resetList(serverMarkList, cache.serverMarkList, false);
		resetList(serverNameList, cache.serverNameList, false);
		resetList(memoryNameList, cache.memoryNameList, true);
		assert(serverMarkList.getItemCount() == serverNameList.getItemCount());
		if (serverMarkList.getItemCount() != 0) {
			serverMarkList.setSelectedIndex(0);
			serverNameList.setSelectedIndex(0);
		}
		memoryNameText.setText("");
		memoryTotalText.setText(Long.toString(0));
	}
	
	@UiHandler("serverMarkList")
	void handleServerMarkListChange(ChangeEvent event) {
		handleServerListChange(serverMarkList, serverNameList);
	}
	
	@UiHandler("serverNameList")
	void handleServerNameListChange(ChangeEvent event) {
		handleServerListChange(serverNameList, serverMarkList);
	}
	
	@UiHandler("memoryNameList")
	void handleNameListChange(ChangeEvent event) {
		handleDeviceListChange(memoryNameList, memoryNameText);
	}
	
	void handleServerListChange(ListBox list1, ListBox list2) {
		list2.setSelectedIndex(list1.getSelectedIndex());
	}
	
	void handleDeviceListChange(ListBox list, TextBox text) {
		int index = list.getSelectedIndex();
		if (index != -1) {
			text.setText(list.getItemText(index));
			list.setSelectedIndex(-1);
		}
	}

	@Override
    public void clearCache() {
		cache = null;
    }
	
	private String getValue(ListBox box) {
		int index = box.getSelectedIndex();
		return index == -1 ? null : box.getItemText(index);
	}
	
	@UiHandler("buttonOK")
	void handleButtonOk(ClickEvent event) {
		String serverMark = getValue(serverMarkList);
		String memoryName = memoryNameText.getText();
		Long memoryTotal = longValue(memoryTotalText.getText());
		int memoryNum = memoryNumList.getSelectedIndex() + 1;
		if (presenter.onOK(serverMark, memoryName, memoryTotal, memoryNum)) {
			this.hide();
		}
	}
	
	@UiHandler("buttonCancel")
	void handleButtonCancel(ClickEvent event) {
		this.hide();
	}

}
