package com.eucalyptus.webui.shared.resource.device.status;

import java.io.Serializable;

import com.eucalyptus.webui.client.activity.device.ClientMessage;
import com.eucalyptus.webui.shared.checker.InvalidValueException;

public class IPType implements Serializable {
    
    private static final long serialVersionUID = -7176806962967209807L;
    
    private int value;
    private ClientMessage message;

    public static final IPType PUBLIC = new IPType(0, new ClientMessage("PUBLIC", "公有"));
    public static final IPType PRIVATE = new IPType(1, new ClientMessage("PRIVATE", "私有"));
    
    private IPType() {
    }
    
    private IPType(int value, ClientMessage message) {
        this.value = value;
        this.message = message;
    }
    
    public int getValue() {
        return value;
    }
    
    public static IPType parse(String state) {
        if (state.equals(PUBLIC.toString())) {
            return PUBLIC;
        }
        else if (state.equals(PRIVATE.toString())) {
            return PRIVATE;
        }
        throw new InvalidValueException(state);
    }
    
    public static IPType getIPType(int value) {
        if (value == PUBLIC.getValue()) {
            return PUBLIC;
        }
        else if (value == PRIVATE.getValue()) {
            return PRIVATE;
        }
        throw new InvalidValueException();
    }
    
    @Override
    public String toString() {
        return message.toString();
    }
    
}
