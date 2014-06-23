package com.dappervision.wearscript.events;

public class MediaActionEvent {

    private final String action;
    private int msecs = 0;
    private String filePath = null;

    public MediaActionEvent(String action)
    {
        this.action = action;
        this.msecs = 0;
    }
    public MediaActionEvent(String action, int msecs) {
        this.action = action;
        this.msecs = msecs;
    }

    public MediaActionEvent(String action, String filePath) {
        this.action = action;
        this.filePath = filePath;
    }

    public String getAction() {
        return action;
    }
    public int getMsecs() {return msecs;}
    public String getFilePath() {
        return filePath;
    }
}
