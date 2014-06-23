package com.dappervision.wearscript.events;

public class SubtitleEvent {
    private String filePath;
    private int millis;

    public SubtitleEvent(String filePath, int millis) {
        this.filePath = filePath;
        this.millis = millis;
    }

    public String getFilePath() {
        return filePath;
    }

    public int getMillis() {
        return millis;
    }
}
