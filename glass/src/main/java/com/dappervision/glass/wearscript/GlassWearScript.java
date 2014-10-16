package com.dappervision.glass.wearscript;

import android.util.Base64;
import android.webkit.JavascriptInterface;

import com.dappervision.glass.wearscript.controller.manager.CameraManager;
import com.dappervision.glass.wearscript.controller.manager.GestureManager;
import com.dappervision.glass.wearscript.controller.manager.PicarusManager;
import com.dappervision.glass.wearscript.controller.manager.WarpManager;
import com.dappervision.glass.wearscript.events.CameraEvents;
import com.dappervision.glass.wearscript.events.WarpModeEvent;
import com.dappervision.wearscript.BackgroundService;
import com.dappervision.wearscript.Log;
import com.dappervision.wearscript.Utils;
import com.dappervision.wearscript.WearScript;
import com.dappervision.wearscript.events.CallbackRegistration;
import com.dappervision.wearscript.events.PicarusEvent;
import com.dappervision.wearscript.events.PicarusModelProcessStreamEvent;
import com.dappervision.wearscript.events.PicarusModelProcessWarpEvent;

public class GlassWearScript extends WearScript {
    private static final String TAG = "GlassWearScript";

    public GlassWearScript(BackgroundService bs) {
        super(bs);
    }

    @JavascriptInterface
    public void warpPreviewSamplePlane(String callback) {
        Log.i(TAG, "warpPreviewsample");
        Utils.eventBusPost(new CallbackRegistration(WarpManager.class, callback).setEvent(WarpManager.SAMPLE));

        Utils.eventBusPost(new WarpModeEvent(WarpManager.Mode.SAMPLEWARPPLANE));
    }

    @JavascriptInterface
    public void warpPreviewSampleGlass(String callback) {
        Log.i(TAG, "warpPreviewsample");
        Utils.eventBusPost(new CallbackRegistration(WarpManager.class, callback).setEvent(WarpManager.SAMPLE));
        Utils.eventBusPost(new WarpModeEvent(WarpManager.Mode.SAMPLEWARPGLASS));
    }

    @JavascriptInterface
    public void warpARTags(String callback) {
        Log.i(TAG, "warpARTags");
        Utils.eventBusPost(new CallbackRegistration(WarpManager.class, callback).setEvent(WarpManager.ARTAGS));
    }

    @JavascriptInterface
    public void warpGlassToPreviewH(String callback) {
        Log.i(TAG, "warpPreviewsample");
        Utils.eventBusPost(new CallbackRegistration(WarpManager.class, callback).setEvent(WarpManager.GLASS2PREVIEWH));
    }

    @JavascriptInterface
    public void cameraOff() {
        Utils.eventBusPost(new CameraEvents.Start(0));
    }

    @JavascriptInterface
    public void cameraPhotoData(String callback) {
        CallbackRegistration cr = new CallbackRegistration(CameraManager.class, callback);
        cr.setEvent(CameraManager.PHOTO);
        Utils.eventBusPost(cr);
    }

    @JavascriptInterface
    public void cameraPhoto(String callback) {
        CallbackRegistration cr = new CallbackRegistration(CameraManager.class, callback);
        cr.setEvent(CameraManager.PHOTO_PATH);
        Utils.eventBusPost(cr);
    }

    @JavascriptInterface
    public void cameraPhoto() {
        // TODO(brandyn): This is a hack, we should have a separate event to take a photo and just register the callback prior to that
        CallbackRegistration cr = new CallbackRegistration(CameraManager.class, null);
        cr.setEvent(CameraManager.PHOTO_PATH);
        Utils.eventBusPost(cr);
    }

    @JavascriptInterface
    public void cameraVideo() {
        CallbackRegistration cr = new CallbackRegistration(CameraManager.class, null);
        cr.setEvent(CameraManager.VIDEO_PATH);
        Utils.eventBusPost(cr);
    }

    @JavascriptInterface
    public void cameraVideo(String callback) {
        CallbackRegistration cr = new CallbackRegistration(CameraManager.class, callback);
        cr.setEvent(CameraManager.VIDEO_PATH);
        Utils.eventBusPost(cr);
    }

    @JavascriptInterface
    public void cameraOn(double imagePeriod, int maxHeight, int maxWidth, boolean background) {
        Utils.eventBusPost(new CameraEvents.Start(imagePeriod, maxHeight, maxWidth, background));
    }

    @JavascriptInterface
    public void cameraOn(double imagePeriod, int maxHeight, int maxWidth, boolean background, String callback) {
        Log.d(TAG, "cameraOn: Callback: " + callback);
        cameraOn(imagePeriod, maxHeight, maxWidth, background);
        CallbackRegistration cr = new CallbackRegistration(CameraManager.class, callback);
        cr.setEvent(0);
        Utils.eventBusPost(cr);
    }

    @JavascriptInterface
    public void picarusModelProcessStream(int id, String callback) {
        Utils.eventBusPost((new CallbackRegistration(PicarusManager.class, callback)).setEvent(PicarusManager.MODEL_STREAM + id));
        Utils.eventBusPost(new PicarusModelProcessStreamEvent(id));
    }

    @JavascriptInterface
    public void picarusModelProcessWarp(int id, String callback) {
        Utils.eventBusPost((new CallbackRegistration(PicarusManager.class, callback)).setEvent(PicarusManager.MODEL_WARP + id));
        Utils.eventBusPost(new PicarusModelProcessWarpEvent(id));
    }

    public void picarus(String model, String input, String callback) {
        Utils.eventBusPost((new CallbackRegistration(PicarusManager.class, callback)).setEvent(callback));
        Utils.eventBusPost(new PicarusEvent(Base64.decode(model.getBytes(), Base64.NO_WRAP),
                Base64.decode(input.getBytes(), Base64.NO_WRAP),
                callback));
    }

    @Override
    protected Class determineGestureRoute(String event) {
        for (String gesture : touchGesturesList)
            if (event.startsWith(gesture)) {
                return GestureManager.class;
            }
        return super.determineGestureRoute(event);
    }
}
