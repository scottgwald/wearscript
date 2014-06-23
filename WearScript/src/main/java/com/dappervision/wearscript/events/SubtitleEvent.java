package com.dappervision.wearscript.events;

public class SubtitleEvent {
    private String filePath;
    private long startTimeMillis;

    public SubtitleEvent(String filePath, long startTimeMillis) {
        this.filePath = filePath;
        this.startTimeMillis = startTimeMillis;
    }

    public String getFilePath() {
        return filePath;
    }

    public long getStartTimeMillis() {
        return startTimeMillis;
    }

    public static class Pause {}
}
