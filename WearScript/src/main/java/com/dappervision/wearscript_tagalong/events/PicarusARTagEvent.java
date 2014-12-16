package com.dappervision.wearscript_tagalong.events;

public class PicarusARTagEvent {
    private byte[] jpeg;

    public PicarusARTagEvent(byte[] jpeg) {
        this.jpeg = jpeg;
    }

    public byte[] getJPEG() {
        return jpeg;
    }
}
