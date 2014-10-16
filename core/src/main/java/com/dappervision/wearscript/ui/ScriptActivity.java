package com.dappervision.wearscript.ui;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.IBinder;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.WindowManager;

import com.dappervision.wearscript.BackgroundService;
import com.dappervision.wearscript.Log;
import com.dappervision.wearscript.Utils;
import com.dappervision.wearscript.events.ActivityResultEvent;
import com.dappervision.wearscript.events.CameraButtonPressedEvent;
import com.dappervision.wearscript.events.ScriptActivityPauseEvent;
import com.dappervision.wearscript.events.ScriptActivityResumeEvent;
import com.dappervision.wearscript.events.ScriptEvent;
import com.dappervision.wearscript.events.StartActivityEvent;

public abstract class ScriptActivity extends Activity {
    protected static final String TAG = "ScriptActivity";
    private static final String EXTRA_NAME = "extra";
    public BackgroundService bs;
    private boolean isGlass = true, isForeground = true;
    protected ServiceConnection mConnection;
    private String extra;
    private boolean mHadUrlExtra = false;

    public ScriptActivity() {
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (bs.onOptionsItemSelected(item))
            return true;
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        //#TODO We should be able to make this more efficient and not constantly reinflate.
        Log.d(TAG, "onPrepareOptionsMenu");
        return bs.onPrepareOptionsMenu(menu, this);
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.register(this);
        Utils.getEventBus().register(this);
        Log.i(TAG, "Lifecycle: Activity onCreate");
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Intent thisIntent = getIntent();
        if (thisIntent.getStringExtra(EXTRA_NAME) != null) {
            mHadUrlExtra = true;
            extra = thisIntent.getStringExtra(EXTRA_NAME);
            Log.d(TAG, "Found extra: " + extra);
        } else {
            Log.d(TAG, "Did not find extra.");
        }
        // Bind Service
        mConnection = new ServiceConnection() {
            public void onServiceConnected(ComponentName className, IBinder service) {
                Log.i(TAG, "Service Connected");
                bs = ((BackgroundService.LocalBinder) service).getService();
                bs.setMainActivity(ScriptActivity.this);

                // If we already have a view and aren't specifying a script to run, reuse the old script
                if (bs.hasWebView() && extra == null) {
                    // Remove view's parent so that we can re-add it later to a new activity
                    Log.i(TAG, "Lifecycle: Recycling webview");
                    bs.refreshActivityView();
                    if (isForeground)
                        Utils.eventBusPost(new ScriptActivityResumeEvent());
                    else
                        Utils.eventBusPost(new ScriptActivityPauseEvent());
                    return;
                }
                Log.i(TAG, "Lifecycle: Creating new webview");

                if (extra != null) {
                    Log.i(TAG, "Extra script: " + extra);
                    Utils.eventBusPost(new ScriptEvent(extra));
                } else {
                    Log.i(TAG, "Default script");
                    bs.startDefaultScript();
                }
            }

            public void onServiceDisconnected(ComponentName className) {
                Log.i(TAG, "Service Disconnected");
            }
        };
        Log.i(TAG, "Calling bindService");
        startAndBindService();
    }

    protected abstract void startAndBindService();

    @Override
    public void onPause() {
        Log.i(TAG, "Lifecycle: ScriptActivity: onPause");
        isForeground = false;
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Utils.getEventBus().post(new ScriptActivityPauseEvent());
        super.onPause();
    }

    @Override
    public void onResume() {
        Log.i(TAG, "Lifecycle: ScriptActivity: onResume");
        isForeground = true;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Utils.getEventBus().post(new ScriptActivityResumeEvent());
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "Back pressed");
        if (bs == null || bs.onBackPressed())
            super.onBackPressed();
    }

    public void onDestroy() {
        Log.i(TAG, "Lifecycle: ScriptActivity: onDestroy");
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (mConnection != null) {
            unbindService(mConnection);
            mConnection = null;
        }
        Utils.getEventBus().unregister(this);
        super.onDestroy();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_CAMERA) {
            Utils.getEventBus().post(new CameraButtonPressedEvent());
            return false;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        bs.onConfigurationChanged(newConfig);
    }

    public void onEventBackgroundThread(StartActivityEvent event) {
        startActivityForResult(event.getIntent(), event.getRequestCode());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        Log.i(TAG, "Request code: " + requestCode + " Result code: " + resultCode);
        Utils.eventBusPost(new ActivityResultEvent(requestCode, resultCode, intent));
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        // NOTE(brandyn): If you return true then the cardscroll won't get the gesture
        // TODO(brandyn): Consider registering overrides
        // TODO(brandyn): We may need the return value here
        //return gm.onMotionEvent(event);
        Utils.eventBusPost(event);
        return false;
    }
}
