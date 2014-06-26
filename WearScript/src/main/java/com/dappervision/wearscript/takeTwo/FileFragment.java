package com.dappervision.wearscript.takeTwo;


public class FileFragment {
    private final String filePath;
    private long fileDuration;
    private long startTime;

    public FileFragment (String filePath, long startTime , long fileDuration) {
        this.filePath = filePath;
        this.startTime = startTime;
        this.fileDuration = fileDuration;
    }

    public String getFilePath() {
        return filePath;
    }

    public long getFileDuration() {
        return fileDuration;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setFileDuration (long duration) {
        fileDuration = duration;
    }

    public void setStartTime (long startTime) {
        this.startTime = startTime;
    }
}
