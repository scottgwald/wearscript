package com.dappervision.wearscript.managers;

import android.content.Intent;
import android.util.Log;

import com.dappervision.wearscript.BackgroundService;
import com.dappervision.wearscript.events.MediaGestureEvent;
import com.dappervision.wearscript.events.MediaOnFingerCountChangedEvent;
import com.dappervision.wearscript.events.MediaOnScrollEvent;
import com.dappervision.wearscript.events.MediaOnTwoFingerScrollEvent;
import com.dappervision.wearscript.events.MediaRecordEvent;
import com.dappervision.wearscript.ui.RecordActivity;

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

    public void onEvent(MediaRecordEvent e) {
        if (e.getType() == MediaRecordEvent.Type.VIDEO) {
            service.startActivity(new Intent(service, RecordActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        } else if (e.getType() == MediaRecordEvent.Type.AUDIO) {
            Log.d(TAG, "Don't know how to record audio in onEvent()");
        }
    }
}

