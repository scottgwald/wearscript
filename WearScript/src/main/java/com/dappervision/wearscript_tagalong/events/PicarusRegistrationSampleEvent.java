package com.dappervision.wearscript_tagalong.events;

public class PicarusRegistrationSampleEvent {
    private byte[] jpeg;

    public PicarusRegistrationSampleEvent(byte[] jpeg) {
        this.jpeg = jpeg;
    }

    public byte[] getJPEG() {
        return jpeg;
    }
}
