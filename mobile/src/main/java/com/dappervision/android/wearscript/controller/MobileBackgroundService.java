package com.dappervision.android.wearscript.controller;

import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;

import com.dappervision.android.wearscript.MobileWearScript;
import com.dappervision.android.wearscript.controller.manager.MobileManagerManager;
import com.dappervision.android.wearscript.view.MobileScriptView;
import com.dappervision.wearscript.BackgroundService;
import com.dappervision.wearscript.Log;
import com.dappervision.wearscript.ScriptView;
import com.dappervision.wearscript.events.ActivityEvent;
import com.dappervision.wearscript.ui.ScriptActivity;

public class MobileBackgroundService extends BackgroundService {

    @Override
    public void updateActivityView(final ActivityEvent.Mode mode) {

        Log.d(TAG, "updateActivityView: " + mode.toString());
        if (activity == null) {
            Log.d(TAG, "updateActivityView - activity is null, not setting");
            return;
        }
        final ScriptActivity a = activity;
        a.runOnUiThread(new Thread() {
            public void run() {
                activityMode = mode;
                if (mode == ActivityEvent.Mode.WEBVIEW && webview != null) {
                    activityView = webview;
                }
                if (activityView != null) {
                    ViewGroup parentViewGroup = (ViewGroup) activityView.getParent();
                    if (parentViewGroup != null)
                        parentViewGroup.removeAllViews();
                    a.setContentView(activityView);
                } else {
                    Log.i(TAG, "Not setting activity view because it is null: " + mode);
                }
            }
        });

    }

    @Override
    protected MobileManagerManager getManagerManager() {
        return MobileManagerManager.get();
    }

    @Override
    public ScriptView createScriptView() {
        ScriptView view = new MobileScriptView(this);
        view.setBackgroundColor(0);
        view.addJavascriptInterface(new MobileWearScript(this), "WSRAW");
        return view;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return false;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu, ScriptActivity scriptActivity) {
        return false;
    }

    @Override
    public boolean onBackPressed() {
        return true;
    }
}
