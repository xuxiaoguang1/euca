package com.eucalyptus.webui.client.view;

import java.util.LinkedList;
import java.util.List;

import com.eucalyptus.webui.shared.resource.device.status.ServerState;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class DeviceServerAddViewImpl extends DialogBox implements DeviceServerAddView {
	
	private static DeviceServerAddViewImplUiBinder uiBinder = GWT.create(DeviceServerAddViewImplUiBinder.class);
	
	interface DeviceServerAddViewImplUiBinder extends UiBinder<Widget, DeviceServerAddViewImpl> {
	}
	
	@UiField ListBox areaNameList;
	@UiField ListBox roomNameList;
	@UiField ListBox cabinetNameList;
	
	@UiField TextBox serverName;
	@UiField TextArea serverDesc;
	@UiField TextBox serverIP;
	@UiField IntegerBox serverBW;
	@UiField ListBox serverStateList;
		
	private ServerState[] serverStateValue = new ServerState[]{ServerState.INUSE, ServerState.STOP, ServerState.ERROR};

	public DeviceServerAddViewImpl() {
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
		center();
		hide();
	}
	
	private String getServerName() {
		return getInputText(serverName);
	}
	
	private String getServerDesc() {
		return getInputText(serverDesc);
	}
	
	private String getServerIP() {
		return getInputText(serverIP);
	}
	
	private String getServerBW() {
		return getInputText(serverBW);
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
	
	private String getInputText(IntegerBox textbox) {
		String text = textbox.getText();
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
	
	private DeviceServerAddView.Presenter presenter;
	
	@Override
    public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
    }
	
	@Override
    public void popup() {
		serverName.setText("");
		serverDesc.setText("");
		serverIP.setText("");
		serverBW.setText("");
		List<String> values = new LinkedList<String>();
		for (int i = 0; i < serverStateValue.length; i ++) {
			values.add(serverStateValue[i].toString());
		}
		setListBox(serverStateList, values);
		areaNameList.clear();
		roomNameList.clear();
		cabinetNameList.clear();
		presenter.lookupAreaNames();
		show();
    }

	@UiHandler("buttonOK")
	void handleButtonOK(ClickEvent event) {
		if (presenter.onOK(getServerName(), getServerDesc(), getServerIP(), getServerBW(),
				serverStateValue[serverStateList.getSelectedIndex()], getCabinetName())) {
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
