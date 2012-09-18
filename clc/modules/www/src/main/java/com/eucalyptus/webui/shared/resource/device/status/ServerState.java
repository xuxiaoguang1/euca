package com.eucalyptus.webui.shared.resource.device.status;

import java.io.Serializable;

import com.eucalyptus.webui.client.activity.device.ClientMessage;
import com.eucalyptus.webui.shared.checker.InvalidValueException;

public class ServerState implements Serializable {

	private static final long serialVersionUID = 8424624285564107541L;
	
	private int value;
	private ClientMessage message;

	public static final ServerState INUSE = new ServerState(0, new ClientMessage("NORMAL", "使用"));
	public static final ServerState ERROR = new ServerState(1, new ClientMessage("ERROR", "故障"));
	public static final ServerState STOP = new ServerState(2, new ClientMessage("STOP", "停止"));
	
	private ServerState() {
	}

	private ServerState(int value, ClientMessage message) {
		this.value = value;
		this.message = message;
	}

	public int getValue() {
		return value;
	}
	
	public static ServerState parse(String state) {
		if (state.equals(INUSE.toString())) {
			return INUSE;
		}
		else if (state.equals(ERROR.toString())) {
			return ERROR;
		}
		else if (state.equals(STOP.toString())) {
			return STOP;
		}
		throw new InvalidValueException(state);
	}
	
	public static ServerState getServerState(int value) {
		if (value == INUSE.getValue()) {
			return INUSE;
		}
		else if (value == ERROR.getValue()) {
			return ERROR;
		}
		else if (value == STOP.getValue()) {
			return STOP;
		}
		throw new InvalidValueException();
	}
	
	@Override
	public String toString() {
		return message.toString();
	}
	
}
