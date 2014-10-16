package com.dappervision.android.wearscript;

import android.webkit.JavascriptInterface;

import com.dappervision.wearscript.BackgroundService;
import com.dappervision.wearscript.WearScript;

public class MobileWearScript extends WearScript {
    private static final String TAG = "GlassWearScript";

    public MobileWearScript(BackgroundService bs) {
        super(bs);
    }

    @JavascriptInterface
    public void warpPreviewSamplePlane(String callback) {
        requiresGDK();
    }

    @JavascriptInterface
    public void warpPreviewSampleGlass(String callback) {
        requiresGDK();
    }

    @JavascriptInterface
    public void warpARTags(String callback) {
        requiresGDK();
    }

    @JavascriptInterface
    public void warpGlassToPreviewH(String callback) {
        requiresGDK();
    }

    @JavascriptInterface
    public void cameraOff() {
        requiresGDK();
    }

    @JavascriptInterface
    public void cameraPhotoData(String callback) {
        requiresGDK();
    }

    @JavascriptInterface
    public void cameraPhoto(String callback) {
        requiresGDK();
    }

    @JavascriptInterface
    public void cameraPhoto() {
        requiresGDK();
    }

    @JavascriptInterface
    public void cameraVideo() {
        requiresGDK();
    }

    @JavascriptInterface
    public void cameraVideo(String callback) {
        requiresGDK();
    }

    @JavascriptInterface
    public void cameraOn(double imagePeriod, int maxHeight, int maxWidth, boolean background) {
        requiresGDK();
    }

    @JavascriptInterface
    public void cameraOn(double imagePeriod, int maxHeight, int maxWidth, boolean background, String callback) {
        requiresGDK();
    }

    @JavascriptInterface
    public void picarus(String model, String input, String callback) {
        requiresGDK();
    }

    @JavascriptInterface
    public void picarusModelProcessStream(int id, String callback) {
        requiresGDK();
    }

    @JavascriptInterface
    public void picarusModelProcessWarp(int id, String callback) {
        requiresGDK();
    }

    @JavascriptInterface
    public void cardTree(String treeJS) {
        requiresGDK();
    }

    @JavascriptInterface
    public void liveCardCreate(boolean nonSilent, double period, String menu) {
        requiresGDK();
    }

    @JavascriptInterface
    public void liveCardDestroy() {
        requiresGDK();
    }
}
