package com.dappervision.wearscript.dataproviders;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.dappervision.wearscript.Log;
import com.dappervision.wearscript.Utils;
import com.dappervision.wearscript.WearScript;
import com.dappervision.wearscript.events.ActivityEvent;
import com.dappervision.wearscript.events.CardTreeEvent;
import com.dappervision.wearscript.events.MyoAccelerometerDataEvent;
import com.dappervision.wearscript.events.MyoGyroDataEvent;
import com.dappervision.wearscript.events.MyoOrientationDataEvent;
import com.dappervision.wearscript.managers.DataManager;
import com.kelsonprime.cardtree.Tree;
import com.thalmic.myo.Quaternion;
import com.thalmic.myo.Vector3;

import org.json.simple.JSONArray;
import org.json.simple.JSONValue;


public class MyoDataProvider extends DataProvider {

    private static final String TAG = "MyoDataProvider";

    public MyoDataProvider(final DataManager parent, long samplePeriod, int type) {
        super(parent, samplePeriod, type, "Myo");
    }

    @Override
    public void unregister() {
        super.unregister();
    }

    public void onEvent(MyoAccelerometerDataEvent e) {
        if (getType() == WearScript.SENSOR.MYO_ACCELEROMETER.id()) {
            long timestamp = System.nanoTime();
            if (!useSample(timestamp))
                return;
            DataPoint dataPoint = new DataPoint(this, System.currentTimeMillis() / 1000., e.getTimestamp());
            Vector3 accel = e.getAccel();
            dataPoint.addValue(Double.valueOf(accel.x()));
            dataPoint.addValue(Double.valueOf(accel.y()));
            dataPoint.addValue(Double.valueOf(accel.z()));
            parent.queue(dataPoint);
        }
    }

    public void onEvent(MyoOrientationDataEvent e) {
        if (getType() == WearScript.SENSOR.MYO_ORIENTATION.id()) {
            long timestamp = System.nanoTime();
            Log.d(TAG, "MYODEBUG: orientationData: 0");

            if (!useSample(timestamp))
                return;
            Log.d(TAG, "MYODEBUG: orientationData: 1");
            DataPoint dataPoint = new DataPoint(this, System.currentTimeMillis() / 1000., e.getTimestamp());
            Quaternion rotation = e.getRotation();
            dataPoint.addValue(Double.valueOf(rotation.w()));
            dataPoint.addValue(Double.valueOf(rotation.x()));
            dataPoint.addValue(Double.valueOf(rotation.y()));
            dataPoint.addValue(Double.valueOf(rotation.z()));
            dataPoint.addValue(Double.valueOf(Math.toDegrees(Quaternion.pitch(rotation))));
            dataPoint.addValue(Double.valueOf(Math.toDegrees(Quaternion.yaw(rotation))));
            dataPoint.addValue(Double.valueOf(Math.toDegrees(Quaternion.roll(rotation))));
            parent.queue(dataPoint);
        }
    }

    public void onEvent(MyoGyroDataEvent e) {
        if (getType() == WearScript.SENSOR.MYO_GYROSCOPE.id()) {
            long timestamp = System.nanoTime();
            if (!useSample(timestamp))
                return;
            DataPoint dataPoint = new DataPoint(this, System.currentTimeMillis() / 1000., e.getTimestamp());
            Vector3 rotationRate = e.getRotationRate();
            dataPoint.addValue(Double.valueOf(rotationRate.x()));
            dataPoint.addValue(Double.valueOf(rotationRate.y()));
            dataPoint.addValue(Double.valueOf(rotationRate.z()));
            parent.queue(dataPoint);
        }
    }
}
