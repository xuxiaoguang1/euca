package com.eucalyptus.webui.client.view.device;

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

public class DeviceDiskDeviceAddViewImpl extends DialogBox implements DeviceDiskDeviceAddView {
	
	private static DeviceDiskDeviceAddViewImplUiBinder uiBinder = GWT.create(DeviceDiskDeviceAddViewImplUiBinder.class);
	
	interface DeviceDiskDeviceAddViewImplUiBinder extends UiBinder<Widget, DeviceDiskDeviceAddViewImpl> {
	}
	
	public DeviceDiskDeviceAddViewImpl() {
		super(false);
		setWidget(uiBinder.createAndBindUi(this));
	}
	
	private DeviceDiskDeviceAddView.Presenter presenter;

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
		diskNumList.clear();
		for (int i = 1; i <= 32; i ++) {
			diskNumList.addItem(Integer.toString(i));
		}
		diskTotalText.addChangeHandler(new LongBoxChangeHandler(diskTotalText));
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
	@UiField ListBox diskNameList;
	@UiField TextBox diskNameText;
	@UiField LongBox diskTotalText;
	@UiField ListBox diskNumList;
	
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
		resetList(diskNameList, cache.diskNameList, true);
		assert(serverMarkList.getItemCount() == serverNameList.getItemCount());
		if (serverMarkList.getItemCount() != 0) {
			serverMarkList.setSelectedIndex(0);
			serverNameList.setSelectedIndex(0);
		}
		diskNameText.setText("");
		diskTotalText.setText(Long.toString(0));
	}
	
	@UiHandler("serverMarkList")
	void handleServerMarkListChange(ChangeEvent event) {
		handleServerListChange(serverMarkList, serverNameList);
	}
	
	@UiHandler("serverNameList")
	void handleServerNameListChange(ChangeEvent event) {
		handleServerListChange(serverNameList, serverMarkList);
	}
	
	@UiHandler("diskNameList")
	void handleNameListChange(ChangeEvent event) {
		handleDeviceListChange(diskNameList, diskNameText);
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
		String diskName = diskNameText.getText();
		Long diskTotal = longValue(diskTotalText.getText());
		int diskNum = diskNumList.getSelectedIndex() + 1;
		if (presenter.onOK(serverMark, diskName, diskTotal, diskNum)) {
			this.hide();
		}
	}
	
	@UiHandler("buttonCancel")
	void handleButtonCancel(ClickEvent event) {
		this.hide();
	}

}
