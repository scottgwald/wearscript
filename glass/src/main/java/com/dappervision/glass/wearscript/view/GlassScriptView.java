package com.dappervision.glass.wearscript.view;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.os.Handler;
import android.view.SurfaceHolder;
import android.view.View;

import com.dappervision.glass.wearscript.controller.GlassBackgroundService;
import com.dappervision.wearscript.Log;
import com.dappervision.wearscript.ScriptView;
import com.dappervision.wearscript.Utils;
import com.dappervision.wearscript.events.LiveCardEvent;
import com.dappervision.wearscript.ui.MenuActivity;
import com.google.android.glass.timeline.DirectRenderingCallback;
import com.google.android.glass.timeline.LiveCard;

public class GlassScriptView extends ScriptView implements DirectRenderingCallback, SurfaceHolder.Callback {
    private static final String TAG = "GlassScriptView";
    private LiveCard mLiveCard;
    protected SurfaceHolder mHolder;
    protected long mDrawFrequency;
    protected final Handler mHandler;

    public GlassScriptView(Context context) {
        super(context);
        Utils.getEventBus().register(this);
        mHandler = new Handler();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Utils.getEventBus().unregister(this);
        if (mLiveCard != null && mLiveCard.isPublished()) {
            Log.d(TAG, "Unpublishing LiveCard");
            liveCardUnpublish();
        }
    }

    public void liveCardPublish(boolean nonSilent, long drawFrequency) {
        if (mLiveCard != null)
            return;
        this.mDrawFrequency = drawFrequency;
        mLiveCard = new LiveCard(mContext, "myid");
        Log.d(TAG, "Publishing LiveCard");
        mLiveCard.setDirectRenderingEnabled(true).getSurfaceHolder().addCallback(this);
        Intent intent = new Intent(mContext, MenuActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        mLiveCard.setAction(PendingIntent.getActivity(mContext, 0, intent, 0));
        if (nonSilent)
            mLiveCard.publish(LiveCard.PublishMode.REVEAL);
        else
            mLiveCard.publish(LiveCard.PublishMode.SILENT);
        Log.d(TAG, "Done publishing LiveCard");
    }

    public void liveCardUnpublish() {
        if (mLiveCard != null) {
            mLiveCard.getSurfaceHolder().removeCallback(this);
            mLiveCard.unpublish();
        }
        mLiveCard = null;
    }

    public void update() {
        if (mDrawFrequency < 0 || mLiveCard == null)
            return;
        mHandler.postDelayed(new Runnable() {
            public void run() {
                if (mDrawFrequency < 0 || mLiveCard == null)
                    return;
                if (!mPaused)
                    draw2();
                update();
            }
        }, mDrawFrequency);
    }

    public void onEvent(LiveCardEvent e) {
        if (e.getPeriod() > 0) {
            liveCardPublish(e.isNonSilent(), Math.round(e.getPeriod() * 1000.));
        } else {
            liveCardUnpublish();
        }
    }

    @Override
    public void renderingPaused(SurfaceHolder surfaceHolder, boolean b) {
        this.mPaused = b;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // Nothing to do here.
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "Surface created");
        this.mHolder = holder;
        update();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG, "Surface destroyed");
        this.mHolder = null;
    }

    private void draw2() {
        if (mLiveCard == null)
            return;
        Log.d(TAG, "Drawing");
        Canvas canvas;
        try {
            canvas = mHolder.lockCanvas();
        } catch (Exception e) {
            return;
        }
        if (canvas != null) {
            // Tell the view where to draw.
            View v = ((GlassBackgroundService)mContext).getActivityView();
            int measuredWidth = View.MeasureSpec.makeMeasureSpec(
                    canvas.getWidth(), View.MeasureSpec.EXACTLY);
            int measuredHeight = View.MeasureSpec.makeMeasureSpec(
                    canvas.getHeight(), View.MeasureSpec.EXACTLY);

            v.measure(measuredWidth, measuredHeight);
            v.layout(0, 0, getMeasuredWidth(), getMeasuredHeight());
            v.draw(canvas);
            mHolder.unlockCanvasAndPost(canvas);
        }
    }
}
