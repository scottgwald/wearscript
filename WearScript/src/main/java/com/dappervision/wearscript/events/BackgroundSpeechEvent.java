package com.dappervision.wearscript.events;

public class BackgroundSpeechEvent {
    private String callback;

    public BackgroundSpeechEvent(String callback)
    {
        this.callback = callback;
    }

    public String getCallback() {
        return callback;
    }
}
