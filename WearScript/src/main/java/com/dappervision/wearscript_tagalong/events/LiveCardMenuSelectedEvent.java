package com.dappervision.wearscript_tagalong.events;

public class LiveCardMenuSelectedEvent {
    private int position;

    public LiveCardMenuSelectedEvent(int position) {
        this.position = position;
    }

    public int getPosition() {
        return position;
    }
}
