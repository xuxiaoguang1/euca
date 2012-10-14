package com.eucalyptus.webui.client.view;

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
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class DeviceCPUAddViewImpl extends DialogBox implements DeviceCPUAddView {
	
	private static DeviceCPUAddViewImplUiBinder uiBinder = GWT.create(DeviceCPUAddViewImplUiBinder.class);
	
	interface DeviceCPUAddViewImplUiBinder extends UiBinder<Widget, DeviceCPUAddViewImpl> {
	}
	
	@UiField ListBox areaNameList;
	@UiField ListBox roomNameList;
	@UiField ListBox cabinetNameList;
	@UiField ListBox serverNameList;
	
	@UiField TextBox cpuName;
	@UiField TextArea cpuDesc;
	@UiField TextBox cpuVendor;
	@UiField TextBox cpuModel;
	@UiField DoubleBox cpuGHz;
	@UiField DoubleBox cpuCache;
	@UiField ListBox numList;
		
	public DeviceCPUAddViewImpl() {
		super(false);
		setWidget(uiBinder.createAndBindUi(this));
		areaNameList.addChangeHandler(new ChangeHandler() {

			@Override
			public void onChange(ChangeEvent event) {
				String area_name = getAreaName();
				if (!isEmpty(area_name)) {
					presenter.lookupRoomNamesByAreaName(area_name);
				}
			}
			
		});
        roomNameList.addChangeHandler(new ChangeHandler() {

            @Override
            public void onChange(ChangeEvent event) {
                String room_name = getRoomName();
                if (!isEmpty(room_name)) {
                    presenter.lookupCabinetNamesByRoomName(room_name);
                }
            }
            
        });
        cabinetNameList.addChangeHandler(new ChangeHandler() {

            @Override
            public void onChange(ChangeEvent event) {
                String cabinet_name = getCabinetName();
                if (!isEmpty(cabinet_name)) {
                    presenter.lookupServerNameByCabinetName(cabinet_name);
                }
            }
            
        });
        for (int i = 1; i <= 64; i ++) {
        	numList.addItem(Integer.toString(i));
        }
		center();
		hide();
	}
	
	private String getCPUName() {
		return getInputText(cpuName);
	}
	
	private String getCPUDesc() {
		return getInputText(cpuDesc);
	}
	
	private String getCPUVendor() {
		return getInputText(cpuVendor);
	}
	
	private String getCPUModel() {
		return getInputText(cpuModel);
	}
	
	private double getCPUGHz() {
		return cpuGHz.getValue();
	}
	
	private double getCPUCache() {
		return cpuCache.getValue();
	}
	
	private int getCPUTotal() {
		return numList.getSelectedIndex() + 1;
	}
	
	private String getAreaName() {
		return getSelectedText(areaNameList); 
	}
	
	private String getRoomName() {
		return getSelectedText(roomNameList);
	}
	
	private String getCabinetName() {
	    return getSelectedText(cabinetNameList);
	}
	
	private String getServerName() {
		return getSelectedText(serverNameList);
	}
	
	private String getInputText(TextBox textbox) {
		String text = textbox.getText();
		if (text == null) {
			return "";
		}
		return text;
	}
	
	private String getInputText(TextArea textarea) {
		String text = textarea.getText();
		if (text == null) {
			return "";
		}
		return text;
	}
	
	private String getSelectedText(ListBox listbox) {
	    int index = listbox.getSelectedIndex();
	    if (index == -1) {
	    	return "";
	    }
	    return listbox.getItemText(index);
	}
	
	private boolean isEmpty(String s) {
	    return s == null || s.length() == 0;
	}
	
	private DeviceCPUAddView.Presenter presenter;
	
	@Override
    public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
    }
	
	@Override
    public void popup() {
		cpuName.setValue("");
		cpuName.setValue("");
		cpuDesc.setValue("");
		cpuVendor.setValue("");
		cpuModel.setValue("");
		cpuGHz.setValue(0.0);
		cpuCache.setValue(0.0);
		areaNameList.clear();
		roomNameList.clear();
		cabinetNameList.clear();
		serverNameList.clear();
		presenter.lookupAreaNames();
		numList.setSelectedIndex(0);
		show();
    }

	@UiHandler("buttonOK")
	void handleButtonOK(ClickEvent event) {
		if (presenter.onOK(getCPUName(), getCPUDesc(), getCPUTotal(), getCPUVendor(), getCPUModel(), getCPUGHz(), getCPUCache(), getServerName())) {
			hide();
		}
	}
	
	@UiHandler("buttonCancel")
	void handleButtonCancel(ClickEvent event) {
		hide();
	}

    @Override
    public void setAreaNameList(List<String> area_name_list) {
    	setListBox(areaNameList, area_name_list);
    	setListBox(roomNameList, null);
    	setListBox(cabinetNameList, null);
    	setListBox(serverNameList, null);
        String area_name = getAreaName();
        if (!isEmpty(area_name)) {
            presenter.lookupRoomNamesByAreaName(area_name);
        }
    }

    @Override
    public void setRoomNameList(String area_name, List<String> room_name_list) {
        if (getAreaName().equals(area_name)) {
	        setListBox(roomNameList, room_name_list);
	        setListBox(cabinetNameList, null);
	        setListBox(serverNameList, null);
	        String room_name = getRoomName();
	        if (!isEmpty(room_name)) {
	            presenter.lookupCabinetNamesByRoomName(room_name);
	        }
        }
    }
    
    @Override
    public void setCabinetNameList(String room_name, List<String> cabinet_name_list) {
        if (getRoomName().equals(room_name)) {
        	setListBox(cabinetNameList, cabinet_name_list);
        	setListBox(serverNameList, null);
        	String cabinet_name = getCabinetName();
        	if (!isEmpty(cabinet_name)) {
        		presenter.lookupServerNameByCabinetName(cabinet_name);
        	}
        }
    }
    
	@Override
	public void setServerNameList(String cabinet_name, List<String> server_name_list) {
		if (getCabinetName().equals(cabinet_name)) {
			setListBox(serverNameList, server_name_list);
		}
	}
    
    private void setListBox(ListBox listbox, List<String> values) {
    	listbox.clear();
    	if (values != null && !values.isEmpty()) {
	    	for (String value : values) {
	    		listbox.addItem(value);
	    	}
	    	listbox.setSelectedIndex(0);
    	}
    }
    
}
