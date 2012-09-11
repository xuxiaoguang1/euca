package com.eucalyptus.webui.client.view;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

public class DevicePopupPanel extends PopupPanel {
	
	private static DevicePopupPanelUiBinder uiBinder = GWT.create(DevicePopupPanelUiBinder.class);
	
	interface DevicePopupPanelUiBinder extends UiBinder<Widget, DevicePopupPanel> {
	}
	
	@UiField LayoutPanel panel;
	
	private Timer timer;
	
	public DevicePopupPanel() {
		super(true);
		setWidget(uiBinder.createAndBindUi(this));
		this.addCloseHandler(new CloseHandler<PopupPanel>() {

            @Override
            public void onClose(CloseEvent<PopupPanel> event) {
            	if (timer != null) {
            		timer.cancel();
            	}
            }
            
		});
		getElement().getStyle().setZIndex(10000);
	}
	
	public void setHTML(int left, int top, String width, String height, HTML html) {
	    panel.clear();
	    panel.add(html);
	    panel.setHeight(height);
	    panel.setWidth(width);
	    setPopupPosition(left, top);
	    show();
	    timer = new Timer() {

			@Override
			public void run() {
				if (timer == this) {
					hide();
				}
			}
			
	    };
	    timer.schedule(4000);
	}
	
}
