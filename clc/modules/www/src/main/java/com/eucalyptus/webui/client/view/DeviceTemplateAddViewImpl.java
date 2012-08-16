package com.eucalyptus.webui.client.view;

import java.util.LinkedList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.LongBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.uibinder.client.UiField;

public class DeviceTemplateAddViewImpl extends DialogBox implements DeviceTemplateAddView {

	private static DeviceTemplateServiceAddViewImplUiBinder uiBinder = GWT.create(DeviceTemplateServiceAddViewImplUiBinder.class);
	
	interface DeviceTemplateServiceAddViewImplUiBinder extends UiBinder<Widget, DeviceTemplateAddViewImpl> {
	}
	
	@UiField
	TextBox markText;
	@UiField
	ListBox cpuNameList;
	@UiField
	LongBox memText;
	@UiField
	LongBox diskText;
	@UiField
	LongBox bwText;
	@UiField
	TextBox imageText;
	@UiField
	ListBox cpuNumList;

	public DeviceTemplateAddViewImpl() {
		super(false);
		setWidget(uiBinder.createAndBindUi(this));
	}

	private DeviceTemplateAddView.Presenter presenter;

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}
	
	private boolean isEmpty(String s) {
		return s == null || s.length() == 0;
	}
	
	@Override
	public void setCPUNameList(List<String> list) {
		String selected = getSelectedItem(cpuNameList);
		cpuNameList.clear();
		cpuNameList.addItem("");
		for (String name : list) {
			if (!isEmpty(name)) {
				cpuNameList.addItem(name);
			}
		}
		setSelected(selected);
	}
	
	public void setSelected(String selected) {
		if (isEmpty(selected)) {
			return;
		}
		List<String> list = new LinkedList<String>();
		for (int i = 1; i < cpuNameList.getItemCount(); i ++) {
			String name = cpuNameList.getItemText(i);
			if (name.equals(selected)) {
				cpuNameList.setSelectedIndex(i);
				return;
			}
			list.add(name);
		}
		cpuNameList.clear();
		cpuNameList.addItem("");
		cpuNameList.addItem(selected);
		for (String name : list) {
			cpuNameList.addItem(name);
		}
		cpuNameList.setSelectedIndex(1);
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
				box.setText("");
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
	
	private boolean initialized = false;
	
	private void init() {
		if (initialized) {
			return;
		}
		initialized = true;
		memText.addChangeHandler(new LongBoxChangeHandler(memText));
		diskText.addChangeHandler(new LongBoxChangeHandler(diskText));
		bwText.addChangeHandler(new LongBoxChangeHandler(bwText));
		cpuNameList.addItem("");
		for (int i = 0; i <= 32; i ++) {
			cpuNumList.addItem(Integer.toString(i));
		}
	}

	@Override
	public void popup() {
		init();
		markText.setText("");
		memText.setText("");
		diskText.setText("");
		bwText.setText("");
		imageText.setText("");
		cpuNumList.setSelectedIndex(0);
		cpuNameList.addChangeHandler(new ChangeHandler() {

			@Override
            public void onChange(ChangeEvent event) {
				if (isEmpty(getSelectedItem(cpuNameList))) {
					cpuNumList.setSelectedIndex(0);
				}
            }
			
		});
		presenter.lookupCPUNames();
		show();
	}
	
	private String getSelectedItem(ListBox list) {
		int index = list.getSelectedIndex();
		if (index != -1) {
			return list.getItemText(index);
		}
		return "";
	}

	@UiHandler("buttonOK")
	void handleButtonOK(ClickEvent event) {
		if (presenter.onOK(markText.getText(), getSelectedItem(cpuNameList), cpuNumList.getSelectedIndex(),
				memText.getText(), diskText.getText(), bwText.getText(), imageText.getText())) {
			this.hide();
		}
	}
	
	@UiHandler("buttonCancel")
	void handleButtonCancel(ClickEvent event) {
		this.hide();
	}

}
