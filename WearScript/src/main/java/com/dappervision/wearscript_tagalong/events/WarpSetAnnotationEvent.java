package com.dappervision.wearscript_tagalong.events;

public class WarpSetAnnotationEvent {

    private final byte[] image;

    public WarpSetAnnotationEvent(byte image[]) {
        this.image = image;
    }

    public byte[] getImage() {
        return image;
    }
}