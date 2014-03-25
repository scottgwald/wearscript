package com.dappervision.wearscript.managers;

import android.content.Intent;
import android.util.Log;

import com.dappervision.wearscript.BackgroundService;
import com.dappervision.wearscript.Utils;
import com.dappervision.wearscript.events.MyoAccelerometerDataEvent;
import com.dappervision.wearscript.events.MyoGyroDataEvent;
import com.dappervision.wearscript.events.MyoOrientationDataEvent;
import com.dappervision.wearscript.events.MyoTrainEvent;
import com.thalmic.myo.AbstractDeviceListener;
import com.thalmic.myo.DeviceListener;
import com.thalmic.myo.Hub;
import com.thalmic.myo.Myo;
import com.thalmic.myo.Pose;
import com.thalmic.myo.Quaternion;
import com.thalmic.myo.Vector3;
import com.thalmic.myo.trainer.TrainActivity;

public class MyoManager extends Manager {

    public static final String ONMYO = "onMyo";
    private DeviceListener mListener;
    private Myo myo;

    public MyoManager(BackgroundService bs) {
        super(bs);
        pair();
        reset();
    }

    @Override
    public void reset() {
        super.reset();
    }

    public void pair() {
        if (mListener == null)
            setup();
        // First, we initialize the Hub singleton.
        Hub hub = Hub.getInstance();
        if (!hub.init(this.service)) {
            return;
        }
        // Next, register for DeviceListener callbacks.
        hub.addListener(mListener);
        // Finally, scan for Myo devices and connect to the first one found.
        hub.pairWithAdjacent();
    }

    public void onEvent(MyoTrainEvent e) {
        train();
    }

    public void unpair() {
        // We don't want any callbacks when the Activity is gone, so unregister the listener.
        Hub.getInstance().removeListener(mListener);
        // The Activity is finishing, so shutdown the Hub. This will disconnect from the Myo.
        Hub.getInstance().shutdown();
    }

    public void train() {
        if (myo != null) {
            Intent intent = new Intent(service.getBaseContext(), TrainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(TrainActivity.EXTRA_ADDRESS, myo.getMacAddress());
            service.getApplication().startActivity(intent);
        }
    }

    public void setup() {
        mListener = new AbstractDeviceListener() {

            @Override
            public void onConnect(Myo myo, long timestamp) {
                MyoManager.this.myo = myo;
                Log.d(TAG, "Myo connected");
            }

            @Override
            public void onDisconnect(Myo myo, long timestamp) {
            }

            // onOrientationData() is called whenever the Myo device provides its current orientation,
            // which is represented as a quaternion
            @Override
            public void onOrientationData(Myo myo, long timestamp, Quaternion rotation) {
                Utils.eventBusPost(new MyoOrientationDataEvent(timestamp, rotation));
            }

            @Override
            public void onGyroscopeData(Myo myo, long timestamp, Vector3 rotationRate) {
                Utils.eventBusPost(new MyoGyroDataEvent(timestamp, rotationRate));
            }

            @Override
            public void onAccelerometerData(Myo myo, long timestamp, Vector3 accel) {
                Utils.eventBusPost(new MyoAccelerometerDataEvent(timestamp, accel));
            }

            @Override
            public void onPose(Myo myo, long timestamp, Pose pose) {
                makeCall(ONMYO + pose.toString(), "");
                makeCall(ONMYO, "'" + pose.toString() + "'");
                Log.d(TAG, String.format("Pose: %d %s", timestamp, pose.toString()));
            }
        };
    }
}
