package com.dappervision.glass.wearscript.controller;

import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;

import com.dappervision.glass.wearscript.GlassWearScript;
import com.dappervision.glass.wearscript.controller.manager.CameraManager;
import com.dappervision.glass.wearscript.controller.manager.GestureManager;
import com.dappervision.glass.wearscript.controller.manager.GlassManagerManager;
import com.dappervision.glass.wearscript.events.CameraEvents;
import com.dappervision.glass.wearscript.view.GlassScriptView;
import com.dappervision.wearscript.BackgroundService;
import com.dappervision.wearscript.Log;
import com.dappervision.wearscript.ScriptView;
import com.dappervision.wearscript.Utils;
import com.dappervision.wearscript.events.ActivityEvent;
import com.dappervision.wearscript.events.SendEvent;
import com.dappervision.glass.wearscript.controller.manager.CardTreeManager;
import com.dappervision.wearscript.managers.ConnectionManager;
import com.dappervision.glass.wearscript.controller.manager.PicarusManager;
import com.dappervision.glass.wearscript.controller.manager.WarpManager;
import com.dappervision.wearscript.managers.ManagerManager;
import com.dappervision.wearscript.ui.ScriptActivity;

import org.msgpack.type.ValueFactory;

public class GlassBackgroundService extends BackgroundService {
    public void onEventAsync(CameraEvents.Frame frameEvent) {
        Log.d(TAG, "CameraFrame Got: " + System.nanoTime());
        try {
            final CameraManager.CameraFrame frame = frameEvent.getCameraFrame();
            // TODO(brandyn): Move this timing logic into the camera manager
            Log.d(TAG, "handeImage Thread: " + Thread.currentThread().getName());
            byte[] frameJPEG = null;
            if (dataLocal) {
                frameJPEG = frame.getJPEG();
                // TODO(brandyn): We can improve timestamp precision by capturing it pre-encoding
                Utils.SaveData(frameJPEG, "data/", true, ".jpg");
            }
            ConnectionManager cm = (ConnectionManager) getManager(ConnectionManager.class);
            String channel = cm.subchannel(ConnectionManager.IMAGE_SUBCHAN);
            if (dataRemote && cm.exists(channel)) {
                if (frameJPEG == null)
                    frameJPEG = frame.getJPEG();
                Utils.eventBusPost(new SendEvent(channel, System.currentTimeMillis() / 1000., ValueFactory.createRawValue(frameJPEG)));
            }
            // NOTE(brandyn): Done from here because the frame must have "done" called on it
            ((WarpManager) getManager(WarpManager.class)).processFrame(frameEvent);
            ((PicarusManager) getManager(PicarusManager.class)).processFrame(frameEvent);
        } finally {
            frameEvent.done();
        }
    }

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
                } else if (mode == ActivityEvent.Mode.WARP) {
                    activityView = ((WarpManager) getManager(WarpManager.class)).getView();
                } else if (mode == ActivityEvent.Mode.CARD_TREE) {
                    activityView = ((CardTreeManager) getManager(CardTreeManager.class)).getView();
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
    public void setMainActivity(ScriptActivity a) {
        super.setMainActivity(a);
        if (GlassManagerManager.hasManager(CardTreeManager.class))
            ((CardTreeManager) getManager(CardTreeManager.class)).setMainActivity(a);
    }

    @Override
    protected ManagerManager getManagerManager() {
        return GlassManagerManager.get();
    }

    @Override
    public void onEventMainThread(ActivityEvent e) {
        if (e.getMode() == ActivityEvent.Mode.CREATE) {
            CameraManager cm = ((CameraManager) getManager(CameraManager.class));
            if (cm != null && cm.getActivityVisible())
                return;

        }
        super.onEventMainThread(e);
    }

    @Override
    public ScriptView createScriptView() {
        ScriptView view = new GlassScriptView(this);
        view.setBackgroundColor(0);
        view.addJavascriptInterface(new GlassWearScript(this), "WSRAW");
        return view;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu, ScriptActivity activity) {
        CardTreeManager cm = ((CardTreeManager) getManager(CardTreeManager.class));
        return cm != null && cm.onPrepareOptionsMenu(menu, activity);
    }

    @Override
    public boolean onBackPressed() {
        CardTreeManager cm = ((CardTreeManager) getManager(CardTreeManager.class));
        return cm == null || activityMode != ActivityEvent.Mode.CARD_TREE || cm.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        CardTreeManager cm = ((CardTreeManager) getManager(CardTreeManager.class));
        return cm != null && cm.onOptionsItemSelected(item);
    }

    @Override
    public void reset() {
        super.reset();
        if(getManagerManager().get(GestureManager.class) == null) {
            getManagerManager().add(new GestureManager(activity, this));
        }
    }
}
