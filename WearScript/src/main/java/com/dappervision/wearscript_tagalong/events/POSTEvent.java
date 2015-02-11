package com.dappervision.wearscript_tagalong.events;

/**
 * Created by christianvazquez on 2/10/15.
 */
public class POSTEvent {

    private final String path;
    private final String address;
    private final String callback;

    public POSTEvent(String filePath, String address, String callback){
        this.path =filePath;
        this.address = address;
        this.callback = callback;
    }

    public String getPath(){
        return path;
    }

    public String getAddress(){
        return address;
    }

    public String getCallback(){
        return callback;
    }

}
