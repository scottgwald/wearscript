package com.dappervision.wearscript_tagalong.events;

public class ChannelSubscribeEvent {
    private String channel;
    private String callback;

    public ChannelSubscribeEvent(String channel, String callback) {
        this.channel = channel;
        this.callback = callback;
    }

    public String getChannel() {
        return channel;
    }

    public String getCallback() {
        return callback;
    }
}
