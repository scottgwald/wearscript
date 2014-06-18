package com.dappervision.wearscript.events;

import com.google.android.glass.touchpad.Gesture;


/**
 * Created by christianvazquez on 6/11/14.
 */
public class MediaGestureEvent {
    private final Gesture gesture;

    public MediaGestureEvent(Gesture gesture) {
        this.gesture = gesture;
    }

    public Gesture getGesture() {
        return gesture;
    }

}
