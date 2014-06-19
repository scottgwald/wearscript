package com.dappervision.wearscript.events;

import com.google.android.glass.touchpad.Gesture;

public class MediaGestureEvent {
    private final Gesture gesture;

    public MediaGestureEvent(Gesture gesture) {
        this.gesture = gesture;
    }

    public Gesture getGesture() {
        return gesture;
    }

}
