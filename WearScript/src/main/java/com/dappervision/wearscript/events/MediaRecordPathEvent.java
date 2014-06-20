package com.dappervision.wearscript.events;

public class MediaRecordPathEvent {
    private String path;
    public MediaRecordPathEvent(String path) {
        this.path = path;
    }
    public String getPath() {
        return path;
    }
}
