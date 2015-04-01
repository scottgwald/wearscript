package com.dappervision.wearscript_tagalong.events;

/**
 * Created by christianvazquez on 2/10/15.
 */
public class POSTEvent {

    private final String path;
    private final String address;
    private final String callback;

    public String getUuid() {
        return uuid;
    }

    private final String uuid;

    public POSTEvent(String filePath, String address, String callback, String uuid){
        this.path =filePath;
        this.address = address;
        this.callback = callback;
        this.uuid = uuid;
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
