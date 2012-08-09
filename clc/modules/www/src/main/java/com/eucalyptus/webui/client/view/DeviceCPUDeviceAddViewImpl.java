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
import com.google.gwt.user.client.ui.DoubleBox;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class DeviceCPUDeviceAddViewImpl extends DialogBox implements DeviceCPUDeviceAddView {
	
	private static DeviceCPUDeviceAddViewImplUiBinder uiBinder = GWT.create(DeviceCPUDeviceAddViewImplUiBinder.class);
	
	interface DeviceCPUDeviceAddViewImplUiBinder extends UiBinder<Widget, DeviceCPUDeviceAddViewImpl> {
	}
	
	public DeviceCPUDeviceAddViewImpl() {
		super(false);
		setWidget(uiBinder.createAndBindUi(this));
	}
	
	private DeviceCPUDeviceAddView.Presenter presenter;

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
		cpuNumList.clear();
		for (int i = 1; i <= 32; i ++) {
			cpuNumList.addItem(Integer.toString(i));
		}
		cpuNumList.setSelectedIndex(0);
		cpuGHzText.addChangeHandler(new DoubleBoxChangeHandler(cpuGHzText));
		cpuCacheText.addChangeHandler(new DoubleBoxChangeHandler(cpuCacheText));
	}
	
	class DoubleBoxChangeHandler implements ChangeHandler {

		DoubleBox box;

		DoubleBoxChangeHandler(DoubleBox box) {
			this.box = box;
			this.box.setText(stringValue(0));
		}
		
		@Override
        public void onChange(ChangeEvent event) {
			String value = stringValue(doubleValue(box.getText()));
			if (!value.equals(box.getText())) {
				box.setText(value);
			}
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
	@UiField ListBox cpuNameList;
	@UiField ListBox cpuVendorList;
	@UiField ListBox cpuModelList;
	@UiField ListBox cpuNumList;
	@UiField TextBox cpuNameText;
	@UiField TextBox cpuVendorText;
	@UiField TextBox cpuModelText;
	@UiField DoubleBox cpuGHzText;
	@UiField DoubleBox cpuCacheText;
	
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
	
	private String stringValue(double v) {
		StringBuilder sb = new StringBuilder();
		long i = (long)v;
		int j = (int)((long)(v * 100) - i * 100) % 100;
		sb.append(i).append(".");
		if (j < 10) {
			sb.append("0");
		}
		sb.append(j);
		return sb.toString();
	}

	
	private double doubleValue(String v) {
		try {
			return Double.parseDouble(v);
		}
		catch (Exception e) {
			return 0;
		}
	}
	
	@Override
	public void setDevicesInfo(DataCache cache) {
		this.cache = cache;
		assert(cache.serverMarkList.size() == cache.serverNameList.size());
		resetList(serverMarkList, cache.serverMarkList, false);
		resetList(serverNameList, cache.serverNameList, false);
		resetList(cpuNameList, cache.cpuNameList, true);
		resetList(cpuVendorList, cache.cpuVendorList, true);
		resetList(cpuModelList, cache.cpuModelList, true);
		assert(serverMarkList.getItemCount() == serverNameList.getItemCount());
		if (serverMarkList.getItemCount() != 0) {
			serverMarkList.setSelectedIndex(0);
			serverNameList.setSelectedIndex(0);
		}
		cpuNameText.setText("");
		cpuVendorText.setText("");
		cpuModelText.setText("");
		cpuGHzText.setText(stringValue(0));
		cpuCacheText.setText(stringValue(0));
	}
	
	@UiHandler("serverMarkList")
	void handleServerMarkListChange(ChangeEvent event) {
		handleServerListChange(serverMarkList, serverNameList);
	}
	
	@UiHandler("serverNameList")
	void handleServerNameListChange(ChangeEvent event) {
		handleServerListChange(serverNameList, serverMarkList);
	}
	
	void handleServerListChange(ListBox list1, ListBox list2) {
		list2.setSelectedIndex(list1.getSelectedIndex());
	}
	
	@UiHandler("cpuNameList")
	void handleNameListChange(ChangeEvent event) {
		handleDeviceListChange(cpuNameList, cpuNameText);
	}
	
	@UiHandler("cpuVendorList")
	void handleVendorListChange(ChangeEvent event) {
		handleDeviceListChange(cpuVendorList, cpuVendorText);
	}
	
	@UiHandler("cpuModelList")
	void handleModelListChange(ChangeEvent event) {
		handleDeviceListChange(cpuModelList, cpuModelText);
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
		String cpuName = cpuNameText.getText();
		String cpuVendor = cpuVendorText.getText();
		String cpuModel = cpuModelText.getText();
		double cpuGHz = doubleValue(cpuGHzText.getText());
		double cpuCache = doubleValue(cpuCacheText.getText());
		int cpuNum = cpuNumList.getSelectedIndex() + 1;
		if (presenter.onOK(serverMark, cpuName, cpuVendor, cpuModel, cpuGHz, cpuCache, cpuNum)) {
			this.hide();
		}
	}
	
	@UiHandler("buttonCancel")
	void handleButtonCancel(ClickEvent event) {
		this.hide();
	}

}
