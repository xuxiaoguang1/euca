package com.eucalyptus.webui.shared.resource.device.status;

import java.io.Serializable;

import com.eucalyptus.webui.shared.checker.InvalidValueException;
import com.eucalyptus.webui.shared.message.ClientMessage;

public class IPState implements Serializable {
    
    private static final long serialVersionUID = -8041076419299428583L;
    
    private int value;
    private ClientMessage message;

    public static final IPState INUSE = new IPState(0, new ClientMessage("NORMAL", "使用"));
    public static final IPState STOP = new IPState(1, new ClientMessage("STOP", "停止"));
    public static final IPState RESERVED = new IPState(2, new ClientMessage("RESERVED", "预留"));
    
    public IPState() {
    }
    
    public IPState(int value, ClientMessage message) {
        this.value = value;
        this.message = message;
    }
    
    public int getValue() {
        return value;
    }
    
    public static IPState parse(String state) {
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
    
    public static IPState getIPState(int value) {
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
