package com.dappervision.wearscript.managers;

import android.content.Intent;
import android.util.Log;

import com.dappervision.wearscript.BackgroundService;
import com.dappervision.wearscript.Utils;
import com.dappervision.wearscript.events.MyoAccelerometerDataEvent;
import com.dappervision.wearscript.events.MyoGyroDataEvent;
import com.dappervision.wearscript.events.MyoOrientationDataEvent;
import com.dappervision.wearscript.events.MyoTrainEvent;
import com.thalmic.android.myo.AbstractDeviceListener;
import com.thalmic.android.myo.DeviceListener;
import com.thalmic.android.myo.Hub;
import com.thalmic.android.myo.Myo;
import com.thalmic.android.myo.Pose;
import com.thalmic.android.myo.math.Quaternion;
import com.thalmic.android.myo.math.Vector3;
import com.thalmic.android.myo.trainer.TrainActivity;

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
        hub.pairWithAnyMyo();
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
            intent.putExtra(TrainActivity.EXTRA_ADDRESS, myo.getAddress());
            service.getApplication().startActivity(intent);
        }
    }

    public void setup() {
        mListener = new AbstractDeviceListener() {

            @Override
            public void onConnect(Myo myo, long timestamp) {
                MyoManager.this.myo = myo;
            }

            @Override
            public void onDisconnect(Myo myo, long timestamp) {
            }

            // onOrientationData() is called whenever the Myo device provides its current orientation,
            // which is represented as a quaternion
            @Override
            public void onOrientationData(Myo myo, long timestamp, Quaternion rotation) {
                Utils.eventBusPost(new MyoOrientationDataEvent(timestamp, rotation));
                /*
                // Calculate Euler angles (roll, pitch, and yaw) from the quaternion.
                float rotationZ = (float) Math.toDegrees(Quaternion.roll(rotation));
                float rotationX = (float) Math.toDegrees(Quaternion.pitch(rotation));
                float rotationY = (float) Math.toDegrees(Quaternion.yaw(rotation));
                Log.d(TAG, String.format("Orientation: %d %f %f %f ", timestamp, rotationX, rotationY, rotationZ));
                */
            }

            @Override
            public void onGyroData(Myo myo, long timestamp, Vector3 rotationRate) {
                Utils.eventBusPost(new MyoGyroDataEvent(timestamp, rotationRate));
                //Log.d(TAG, String.format("Gyro: %d %f %f %f", timestamp, rotationRate.x, rotationRate.y, rotationRate.z));
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
