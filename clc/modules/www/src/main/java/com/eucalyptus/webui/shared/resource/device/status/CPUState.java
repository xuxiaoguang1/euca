package com.eucalyptus.webui.shared.resource.device.status;

import java.io.Serializable;

import com.eucalyptus.webui.client.activity.device.ClientMessage;
import com.eucalyptus.webui.shared.checker.InvalidValueException;

public class CPUState implements Serializable {
    
	private static final long serialVersionUID = -2146036006661467990L;
	
	private int value;
    private ClientMessage message;

    public static final CPUState INUSE = new CPUState(0, new ClientMessage("NORMAL", "使用"));
    public static final CPUState STOP = new CPUState(1, new ClientMessage("STOP", "停止"));
    public static final CPUState RESERVED = new CPUState(2, new ClientMessage("RESERVED", "预留"));
    
    private CPUState() {
    }
    
    private CPUState(int value, ClientMessage message) {
        this.value = value;
        this.message = message;
    }
    
    public int getValue() {
        return value;
    }
    
    public CPUState parse(String state) {
        if (state.equals(INUSE.toString())) {
            return INUSE;
        }
        else if (state.equals(STOP.toString())) {
            return STOP;
        }
        else if (state.equals(RESERVED.toString())) {
            return RESERVED;
        }
        throw new InvalidValueException(state);
    }
    
    public static CPUState getCPUState(int value) {
        if (value == INUSE.getValue()) {
            return INUSE;
        }
        else if (value == STOP.getValue()) {
            return STOP;
        }
        else if (value == RESERVED.getValue()) {
            return RESERVED;
        }
        throw new InvalidValueException();
    }
    
    @Override
    public String toString() {
        return message.toString();
    }
    
}
