package com.dappervision.wearscript.managers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.dappervision.wearscript.BackgroundService;
import com.dappervision.wearscript.Log;
import com.dappervision.wearscript.events.CallbackRegistration;

public class RecordingManager extends Manager {
    public static final String FILEPATH = "filepath";
    BroadcastReceiver broadcastReceiver;
    public static String TAG = "RecordingManager";
    public static String SAVED = "SAVED";

    public RecordingManager(BackgroundService service) {
        super(service);
        reset();
    }

    public void onEvent(CallbackRegistration e) {
        if (e.getManager().equals(this.getClass())) {
            registerCallback(e.getEvent(), e.getCallback());
            //startActivity();
            Log.d(TAG, "in onEvent(CallbackRegistration e)!");

            Log.d(TAG, "registering for callback");
            broadcastReceiver = new RecordingBroadcastReceiver();
            IntentFilter intentFilter = new IntentFilter("com.wearscript.record.FILE_WRITTEN_VIDEO");
            intentFilter.addAction("com.wearscript.record.FILE_WRITTEN_AUDIO");
            Intent intent = service.registerReceiver(broadcastReceiver, intentFilter);

        }
    }

    protected void makeCall(String key, String data) {
        super.makeCall(key, "'" + data + "'");
    }

    public class RecordingBroadcastReceiver extends BroadcastReceiver {

        public RecordingBroadcastReceiver() {
            super();
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "in onReceive()");
            if (intent.getAction().equals("com.wearscript.record.FILE_WRITTEN_AUDIO")) {
                Log.d(TAG, "in RecordingBroadcastReceiver");
                RecordingManager.this.makeCall(SAVED, intent.getStringExtra(FILEPATH));
                RecordingManager.this.jsCallbacks.remove(SAVED);
                service.unregisterReceiver(this);
            }
        }
    }
}