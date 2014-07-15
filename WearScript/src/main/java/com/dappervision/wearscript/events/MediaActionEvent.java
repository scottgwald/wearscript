package com.dappervision.wearscript.events;

public class MediaActionEvent {

    private final String action;
    private final int msecs;
    private final long mStartTime;

    public MediaActionEvent(String action) {
        this.action = action;
        this.msecs = 0;
        this.mStartTime = System.currentTimeMillis();
    }
    public MediaActionEvent(String action, int msecs) {
        this.action = action;
        this.msecs = msecs;
        this.mStartTime = System.currentTimeMillis();
    }

    public String getAction() {
        return action;
    }

    public int getMsecs() {
        return msecs;
    }

    public long getMStartTime() {
        return mStartTime;
    }
}
