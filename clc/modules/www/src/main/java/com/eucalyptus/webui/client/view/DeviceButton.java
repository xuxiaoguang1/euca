package com.eucalyptus.webui.client.view;

import com.google.gwt.user.client.ui.Anchor;

public class DeviceButton extends Anchor {
	
	private String text = null;
	
	@Override
	public void setText(String text) {
		super.setText(text);
		if (text == null) {
			text = "";
		}
		this.text = text;
	}
	
	private void updateStatus() {
		if (text != null) {
			StringBuilder sb = new StringBuilder();
			String color = isEnabled() ? "black" : "#AAAAAA";
			sb.append("<font color='").append(color).append("'>").append(text).append("</font>");
			super.setHTML(sb.toString());
		}
	}
		
	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		updateStatus();
	}
	
	@Override
	public void onLoad() {
		super.onLoad();
		updateStatus();
	}
	
}
