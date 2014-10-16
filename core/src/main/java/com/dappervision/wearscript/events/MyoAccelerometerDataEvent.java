package com.dappervision.wearscript.events;

import com.thalmic.myo.Vector3;

public class MyoAccelerometerDataEvent {
    private long timestamp;
    private Vector3 accel;
    public MyoAccelerometerDataEvent(long timestamp, Vector3 accel) {
        this.timestamp = timestamp;
        this.accel = accel;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public Vector3 getAccel() {
        return accel;
    }
}
