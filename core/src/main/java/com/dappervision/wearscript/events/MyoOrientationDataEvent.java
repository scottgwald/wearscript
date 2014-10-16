package com.dappervision.wearscript.events;

import com.thalmic.myo.Quaternion;

public class MyoOrientationDataEvent {
    private final long timestamp;
    private final Quaternion rotation;

    public MyoOrientationDataEvent(long timestamp, Quaternion rotation) {
        this.timestamp = timestamp;
        this.rotation = rotation;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public Quaternion getRotation() {
        return rotation;
    }
}
