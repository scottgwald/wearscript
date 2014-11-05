package com.dappervision.wearscript.events;


public class SaveAudioEvent {

    private final String path;
    private final String fileName;


    public SaveAudioEvent(String path, String fileName){
        this.path = path;
        this.fileName = fileName;
    }

    public String getPath(){
        return path;
    }

    public String getFileName(){
        return fileName;
    }

}
