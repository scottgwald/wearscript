package com.dappervision.wearscript_tagalong.managers;

import android.view.WindowManager;

import com.dappervision.wearscript_tagalong.BackgroundService;
import com.dappervision.wearscript_tagalong.Log;
import com.dappervision.wearscript_tagalong.Utils;
import com.dappervision.wearscript_tagalong.events.CallbackRegistration;
import com.dappervision.wearscript_tagalong.events.OpenCVLoadEvent;
import com.dappervision.wearscript_tagalong.events.OpenCVLoadedEvent;
import com.dappervision.wearscript_tagalong.events.SayEvent;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

public class OpenCVManager extends Manager {
    private static final String TAG = "OpenCVManager";
    public static final String LOAD = "LOAD";
    public enum State {
        UNLOADED, LOADING, LOADED
    }
    private State state = State.UNLOADED;

    public OpenCVManager(BackgroundService bs) {
        super(bs);
        reset();
    }

    public void onEventBackgroundThread(OpenCVLoadEvent event) {
        synchronized (this) {
            loadOpenCV();
        }
    }

    public void setupCallback(CallbackRegistration r) {
        // Entry point for capturing photos/videos
        super.setupCallback(r);
        Log.d(TAG, "setupCallback");
        if (r.getEvent().equals(LOAD)) {
            loadOpenCV();
        }
    }

    public void callLoaded() {
        synchronized (this) {
            makeCall(LOAD, "");
            unregisterCallback(LOAD);
            Utils.eventBusPost(new OpenCVLoadedEvent());
        }
    }

    public void loadOpenCV() {
        synchronized (this) {
            if (state == State.LOADED) {
                Log.w(TAG, "Already Loaded: camflow");
                callLoaded();
                return;
            }
            if (state != State.UNLOADED) {
                return;
            }
            state = State.LOADING;
            BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(service) {
                @Override
                public void onManagerConnected(int status) {
                    switch (status) {
                        case LoaderCallbackInterface.SUCCESS: {
                            Log.i(TAG, "Lifecycle: OpenCV loaded successfully: camflow");
                            synchronized (this) {
                                state = State.LOADED;
                                callLoaded();
                            }
                        }
                        break;
                        default: {
                            super.onManagerConnected(status);
                        }
                        break;
                    }
                }
            };
            try {
                OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, service, mLoaderCallback); //TODO:get camera
            } catch (WindowManager.BadTokenException e) {
                Log.w(TAG, "OpenCV apk not installed");
                Utils.eventBusPost(new SayEvent("Please install open CV.  See wearscript.com for details"));
            }
        }
    }
}
