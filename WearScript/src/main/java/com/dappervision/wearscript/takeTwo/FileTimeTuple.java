package com.dappervision.wearscript.takeTwo;


public class FileTimeTuple {
    private final String filePath;
    private final long timeInFile;

    public FileTimeTuple(String path, long time) {
        filePath = path;
        timeInFile = time;
    }

    public long getTimeInFile() {
        return timeInFile;
    }

    public String getFilePath() {
        return filePath;
    }

}
