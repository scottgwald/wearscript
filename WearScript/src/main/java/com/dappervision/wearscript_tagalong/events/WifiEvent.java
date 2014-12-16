package com.dappervision.wearscript_tagalong.events;

public class WifiEvent {
    private boolean status;

    public WifiEvent(boolean status) {
        this.status = status;
    }

    public boolean getStatus() {
        return status;
    }
}
