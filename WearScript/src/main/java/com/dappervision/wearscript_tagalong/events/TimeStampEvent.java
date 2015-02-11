package com.dappervision.wearscript_tagalong.events;

/**
 * Created by christianvazquez on 2/11/15.
 */
public class TimeStampEvent {
    private final String timestamp;

    public TimeStampEvent(String timestamp){
        this.timestamp = timestamp;
    }

    public String getTimestamp(){
        return timestamp;
    }
}
