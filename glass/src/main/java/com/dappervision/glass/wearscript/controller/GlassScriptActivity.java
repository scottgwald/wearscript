package com.dappervision.glass.wearscript.controller;

import android.content.Context;
import android.content.Intent;

import com.dappervision.wearscript.BackgroundService;
import com.dappervision.wearscript.events.MediaEvent;

public class GlassScriptActivity extends com.dappervision.wearscript.ui.ScriptActivity {
    public void onEventMainThread(MediaEvent e) {
        Intent intent = new Intent(this, MediaActivity.class);
        intent.putExtra(MediaActivity.MODE_KEY, MediaActivity.MODE_MEDIA);
        intent.putExtra(MediaPlayerFragment.ARG_URL, e.getUri());
        intent.putExtra(MediaPlayerFragment.ARG_LOOP, e.isLooping());
        startActivity(intent);
    }

    @Override
    protected void startAndBindService() {
        startService(new Intent(this, GlassBackgroundService.class));
        bindService(new Intent(this,
                GlassBackgroundService.class), mConnection, Context.BIND_AUTO_CREATE);
    }
}
