package com.eucalyptus.webui.shared.resource.device.status;

import java.io.Serializable;

import com.eucalyptus.webui.client.activity.device.ClientMessage;
import com.eucalyptus.webui.shared.checker.InvalidValueException;

public class DiskState implements Serializable {
    
    private static final long serialVersionUID = -2146036006661467990L;
    
    private int value;
    private ClientMessage message;

    public static final DiskState INUSE = new DiskState(0, new ClientMessage("NORMAL", "使用"));
    public static final DiskState STOP = new DiskState(1, new ClientMessage("STOP", "停止"));
    public static final DiskState RESERVED = new DiskState(2, new ClientMessage("RESERVED", "预留"));
    
    private DiskState() {
    }
    
    private DiskState(int value, ClientMessage message) {
        this.value = value;
        this.message = message;
    }
    
    public int getValue() {
        return value;
    }
    
    public static DiskState parse(String state) {
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
    
    public static DiskState getDiskState(int value) {
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
