package com.eucalyptus.webui.client.activity.device;

import java.io.Serializable;

public class ClientMessage implements Serializable {
	
	private static final long serialVersionUID = 6866391034179714369L;
	
	private static int LAN_SELECT = 1;
	
	private String[] text;
	
	public ClientMessage() {
	}
	
	public ClientMessage(String text0, String text1) {
		text = new String[]{text0, text1};
	}
	
	@Override
	public String toString() {
		return text[LAN_SELECT];
	}
	
}
