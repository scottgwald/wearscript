package com.dappervision.wearscript.events;

/**
 * Created by christianvazquez on 6/19/14.
 */
public class MediaRecordEvent {
    private final String filePath;

    public MediaRecordEvent(String path) {
        this.filePath = path;
    }

    public String getFilePath() {
        return filePath;
    }
}
