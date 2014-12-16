package com.dappervision.wearscript_tagalong.dataproviders;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.dappervision.wearscript_tagalong.Utils;
import com.dappervision.wearscript_tagalong.events.PebbleAccelerometerDataEvent;
import com.dappervision.wearscript_tagalong.managers.PebbleManager;
import com.dappervision.wearscript_tagalong.managers.PebbleManager.Cmd;
import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

import java.util.UUID;


public class PebbleEventReceiver extends PebbleKit.PebbleDataReceiver{
    private static final String TAG = "PebbleEventReceiver";
    private Handler mHandler;
    private PebbleManager mPebbleManager;

    public PebbleEventReceiver(UUID PEBBLE_APP_UUID, PebbleManager pebbleManager) {
        super(PEBBLE_APP_UUID);
        mHandler = new Handler();
        mPebbleManager = pebbleManager;
    }

    @Override
    public void receiveData(final Context context, final int transactionId, final PebbleDictionary data) {
        final int key = data.getUnsignedInteger(0).intValue();

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                // All data received from the Pebble must be ACK'd, otherwise you'll hit time-outs in the
                // watch-app which will cause the watch to feel "laggy" during periods of frequent
                // communication.
                PebbleKit.sendAckToPebble(context, transactionId);
                switch (key) {
                    case Cmd.Cmd_singleClick:
                        int click = data.getUnsignedInteger(1).intValue();
                        Log.v(TAG + " Single Click", " " + click);
                        mPebbleManager.onPebbleSingleClick(PebbleManager.parseButton(click));
                        break;
                    case Cmd.Cmd_longClick:
                        int lClick = data.getUnsignedInteger(1).intValue();
                        Log.v(TAG + " Long Click", " " + lClick);
                        mPebbleManager.onPebbleLongClick(PebbleManager.parseButton(lClick));
                        break;
                    case PebbleManager.Cmd.Cmd_accelTap:
                        int axis = data.getUnsignedInteger(1).intValue();
                        int direction = data.getInteger(2).intValue();
                        Log.v(TAG + " Accel Tap", " " + axis + " " + direction);
                        mPebbleManager.onPebbleAccelTap(axis, direction);
                        break;
                    case Cmd.Cmd_accelData:
                        int num_samples = data.getUnsignedInteger(2).intValue();
                        byte[] accelData = data.getBytes(3);
                        Log.i(TAG + " Accel Data", " " + num_samples + " x: " + accelData[0] + " y: " + accelData[1] + " z: " + accelData[2]);
                        Utils.eventBusPost(new PebbleAccelerometerDataEvent(accelData));
                    default:
                        break;
                }
            }
        });
    }
}
