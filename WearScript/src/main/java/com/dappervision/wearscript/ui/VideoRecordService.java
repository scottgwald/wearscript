package com.dappervision.wearscript.ui;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class VideoRecordService extends Service {

    protected static final String TAG = "VideoRecordService";
    private final IBinder mBinder = new LocalBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class LocalBinder extends Binder {
        public VideoRecordService getService() {
            return VideoRecordService.this;
        }
    }

    /** method for clients */
    public void startRecording() {

    }
}
