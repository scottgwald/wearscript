package com.dappervision.wearscript;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import com.dappervision.wearscript.events.SendEvent;

public abstract class ScriptView extends WebView {
    private static final String TAG = "ScriptView";
    protected final Context mContext;


    protected boolean mPaused;

    @TargetApi(19)
    public ScriptView(final Context context) {
        super(context);
        mContext = context;
        mPaused = false;
        // NOTE(brandyn): Fix for KK error: http://stackoverflow.com/questions/20675554/webview-rendering-issue-in-android-kitkat
        //setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        // Enable localStorage in webview
        getSettings().setAllowUniversalAccessFromFileURLs(true);
        getSettings().setDomStorageEnabled(true);
        clearCache(true);
        setWebChromeClient(new WebChromeClient() {
            public boolean onConsoleMessage(ConsoleMessage cm) {
                String msg = cm.message() + " -- From line " + cm.lineNumber() + " of " + cm.sourceId();
                Log.w(TAG, msg);
                Utils.eventBusPost(new SendEvent("log", "WebView: " + msg));
                return true;
            }
        });
        setOverScrollMode(View.OVER_SCROLL_IF_CONTENT_SCROLLS);
        //Do new Chromium WebView stuff here
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setWebContentsDebuggingEnabled(true);
        }
    }


    public void onDestroy() {
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void loadUrl(String url) {
        Log.d(TAG, url); //really useful when writing new initjs stuff
        super.loadUrl(url);
    }
}
