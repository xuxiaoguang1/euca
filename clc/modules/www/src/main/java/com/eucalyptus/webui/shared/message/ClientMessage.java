package com.eucalyptus.webui.shared.message;

import java.io.Serializable;

public class ClientMessage implements Serializable {
	
	private static final long serialVersionUID = 6866391034179714369L;
	
	private static int LAN_SELECT = 1;
	
	private String[] text;
	
	public ClientMessage() {
	    this("", "");
	}
	
	public ClientMessage(String text0, String text1) {
	    if (text0 == null) {
	        text0 = "";
	    }
	    if (text1 == null) {
	        text1 = "";
	    }
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
	
	public static final ClientMessage PERMISSION_DENIED = new ClientMessage("Permission Denied.", "权限不足 操作无效");
	
	public static ClientMessage invalidValue(String text0, String text1) {
		return new ClientMessage("Invalid \"" + text0 + "\".", "无效的\"" + text1 + "\".");
	}
	
}
