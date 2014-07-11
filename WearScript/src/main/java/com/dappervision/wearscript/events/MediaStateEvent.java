package com.dappervision.wearscript.events;

public class MediaStateEvent {
    private final boolean isPlaying;

    public MediaStateEvent(boolean playing) {
        isPlaying = playing;
    }

    public boolean getPlayStatus() {
        return isPlaying;
    }
}
