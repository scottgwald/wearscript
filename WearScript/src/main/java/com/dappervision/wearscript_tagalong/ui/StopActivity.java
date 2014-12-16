package com.dappervision.wearscript_tagalong.ui;

import android.app.Activity;
import android.os.Bundle;

import com.dappervision.wearscript_tagalong.Utils;
import com.dappervision.wearscript_tagalong.events.ShutdownEvent;

public class StopActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.getEventBus().post(new ShutdownEvent());
        finish();
    }
}
