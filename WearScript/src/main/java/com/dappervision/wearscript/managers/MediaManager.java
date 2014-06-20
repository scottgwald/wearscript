package com.dappervision.wearscript.managers;

import com.dappervision.wearscript.BackgroundService;
import com.dappervision.wearscript.events.MediaGestureEvent;
import com.dappervision.wearscript.events.MediaOnFingerCountChangedEvent;
import com.dappervision.wearscript.events.MediaOnScrollEvent;
import com.dappervision.wearscript.events.MediaOnTwoFingerScrollEvent;
import com.dappervision.wearscript.events.MediaRecordPathEvent;

public class MediaManager extends Manager {

    public MediaManager(BackgroundService service) {
        super(service);
        reset();
    }

    public void reset() {
        super.reset();
    }

    public void onEvent(MediaGestureEvent e) {
        this.makeCall("onGesture", String.format("'%s'", e.getGesture().name()));
        this.makeCall("onGesture" + e.getGesture().name(), "");
    }

    public void onEvent(MediaOnFingerCountChangedEvent e) {
        this.makeCall("onFingerCountChanged",
                String.format("%d, %d", e.getCountOne(), e.getCountTwo()));
    }

    public void onEvent(MediaOnScrollEvent e) {
        this.makeCall("onScroll",
                String.format("%f, %f, %f", e.getDisplacement(), e.getDelta(), e.getVelocity()));
    }

    public void onEvent(MediaOnTwoFingerScrollEvent e) {
        this.makeCall("onTwoFingerScroll",
                String.format("%f, %f, %f", e.getDisplacement(), e.getDelta(), e.getVelocity()));
    }

    public void onEvent(MediaRecordPathEvent e) {
        this.makeCall("startRecording",String.format("'%s'", e.getPath()));
    }
}

