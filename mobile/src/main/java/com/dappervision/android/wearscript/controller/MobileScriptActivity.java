package com.dappervision.android.wearscript.controller;

import android.content.Context;
import android.content.Intent;

import com.dappervision.wearscript.BackgroundService;
import com.dappervision.wearscript.ui.ScriptActivity;

public class MobileScriptActivity extends ScriptActivity {

    @Override
    protected void startAndBindService() {
        startService(new Intent(this, MobileBackgroundService.class));
        bindService(new Intent(this,
                MobileBackgroundService.class), mConnection, Context.BIND_AUTO_CREATE);
    }
}
