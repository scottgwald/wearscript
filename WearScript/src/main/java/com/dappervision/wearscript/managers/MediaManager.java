package com.dappervision.wearscript.managers;

import android.util.Log;

import com.dappervision.wearscript.BackgroundService;
import com.dappervision.wearscript.events.CallbackRegistration;
import com.dappervision.wearscript.events.MediaGestureEvent;
import com.dappervision.wearscript.events.MediaOnFingerCountChangedEvent;
import com.dappervision.wearscript.events.MediaOnScrollEvent;
import com.dappervision.wearscript.events.MediaOnTwoFingerScrollEvent;
import com.dappervision.wearscript.events.MediaPlayerReadyEvent;

public class MediaManager extends Manager {
    public static final String TAG = "MediaManager";
    public static final String MEDIA_PLAYER_PREPARED = "MEDIA_PLAYER_PREPARED";

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

    public void onEvent(CallbackRegistration e) {
        registerCallback(e.getEvent(), e.getCallback());
    }

    public void onEvent(MediaPlayerReadyEvent e) {
        onMediaPlayerPrepared();
    }

    public void onMediaPlayerPrepared() {
        Log.d(TAG, "in onMediaPlayerPrepared()");
        makeCall(MEDIA_PLAYER_PREPARED, "");
        jsCallbacks.remove(MEDIA_PLAYER_PREPARED);
    }
}

