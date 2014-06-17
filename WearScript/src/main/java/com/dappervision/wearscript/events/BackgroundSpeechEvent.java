package com.dappervision.wearscript.events;

import android.content.Context;

/**
 * Created by christianvazquez on 6/12/14.
 */
public class BackgroundSpeechEvent
{
    private String callback;
    private Context activity;

    public BackgroundSpeechEvent(String callback)
    {
        this.callback = callback;
    }

    public String getCallback() {
        return callback;
    }

    public Context getContext(){return activity;}

}
