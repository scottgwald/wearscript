package com.dappervision.wearscript.events;

/**
 * Created by christianvazquez on 6/19/14.
 */
public class MediaRecordEvent {
    private final boolean video;
    private final String filePath;

    public MediaRecordEvent(boolean video, String path) {
        this.video = video;
        this.filePath = path;
    }

    public String getFilePath() {
        return filePath;
    }

    public boolean recordVideo() {
        return video;
    }
}
