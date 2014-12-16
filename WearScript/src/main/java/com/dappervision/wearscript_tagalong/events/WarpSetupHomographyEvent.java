package com.dappervision.wearscript_tagalong.events;

public class WarpSetupHomographyEvent {
    String h;

    public WarpSetupHomographyEvent(String h) {
        this.h = h;
    }

    public String getHomography() {
        return h;
    }
}
