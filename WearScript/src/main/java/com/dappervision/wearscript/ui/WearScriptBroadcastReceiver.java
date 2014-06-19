package com.dappervision.wearscript.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class WearScriptBroadcastReceiver extends BroadcastReceiver {

    public static final String PLAYBACK_ACTION = "com.wearscript.video.PLAYBACK";
    public static final String RECORD_ACTION = "com.wearscript.video.RECORD";
    public static final String RECORD_RESULT_ACTION = "com.wearscript.video.RECORD_RESULT";
    private static final String TAG = "WearScriptBroadcastReceiver";
    private static final boolean DBG = true;

    @Override
    public void onReceive(Context context, Intent intent) {
    }
}
