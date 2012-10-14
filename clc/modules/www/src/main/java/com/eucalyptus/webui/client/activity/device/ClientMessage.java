package com.eucalyptus.webui.client.activity.device;

import java.io.Serializable;

public class ClientMessage implements Serializable {
	
	private static final long serialVersionUID = 6866391034179714369L;
	
	private transient static int LAN_SELECT = 1;
	
	private String[] text;
	
	public ClientMessage() {
	}
	
	public ClientMessage(String text0, String text1) {
	    text = new String[]{text0, text1};
	}
	
	public String getText(int index) {
	    return text[index];
	}
	
	public String getText() {
	    return text[LAN_SELECT];
	}
	
	@Override
	public String toString() {
	    return getText();
	}
	
}
