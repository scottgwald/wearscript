package com.dappervision.wearscript.events;

public class MediaRecordEvent {

    public enum Type {
        VIDEO,
        AUDIO,
    }

    private final Type type;

    public MediaRecordEvent(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }
}
