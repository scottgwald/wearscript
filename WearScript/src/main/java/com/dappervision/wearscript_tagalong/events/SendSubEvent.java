package com.dappervision.wearscript_tagalong.events;

public class SendSubEvent {
    private String subchannel;
    private Object[] data;


    public SendSubEvent(String subchannel, Object... data) {
        this.subchannel = subchannel;
        this.data = data;
    }

    public Object[] getData() {
        return data;
    }

    public String getSubChannel() {
        return subchannel;
    }

}
