package com.dappervision.wearscript.takeTwo;


public class FileFragment {
    private final String filePath;
    private long fileDuration;
    private long relativeTimeInFile;

    public FileFragment (String filePath, long relativeTimeInFile , long fileDuration) {
        this.filePath = filePath;
        this.relativeTimeInFile = relativeTimeInFile;
        this.fileDuration = fileDuration;
    }

    public String getFilePath() {
        return filePath;
    }

    public long getFileDuration() {
        return fileDuration;
    }

    public long getRelativeTimeInFile() {
        return relativeTimeInFile;
    }

    public void setFileDuration (long duration) {
        fileDuration = duration;
    }

    public void setFileRelativeTime (long relative) {relativeTimeInFile = relative;}
}
