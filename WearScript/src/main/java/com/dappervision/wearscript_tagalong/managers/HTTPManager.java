package com.dappervision.wearscript_tagalong.managers;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import com.dappervision.wearscript_tagalong.BackgroundService;
import com.dappervision.wearscript_tagalong.events.POSTEvent;
import com.dappervision.wearscript_tagalong.events.SoundEvent;
import com.dappervision.wearscript_tagalong.events.TimeStampEvent;
import com.google.android.glass.media.Sounds;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.apache.http.client.methods.HttpPost;

import java.io.ByteArrayOutputStream;


/**
 * Created by christianvazquez on 2/10/15.
 */
public class HTTPManager extends Manager {

    Context context;
    public static final String POST = "POST";
    private String latestPictureTimestamp ="";

    public HTTPManager(BackgroundService service) {
        super(service);
        context = service.getApplicationContext();
        reset();
    }

    public void onEvent(TimeStampEvent e){
        latestPictureTimestamp = e.getTimestamp();
    }
    public void onEvent(POSTEvent event) {
        String filePath = event.getPath();
        String address = event.getAddress();
        Ion.with(context).load(address)
                .setBodyParameter("image", filePath)
                .setBodyParameter("timestamp",latestPictureTimestamp)
                .asString().setCallback(new FutureCallback<String>() {
            @Override
            public void onCompleted(Exception e, String result) {
                if(e != null){
                    e.printStackTrace();
                } else {
                    makeCall(HTTPManager.POST,String.format("%d", 1));
                }
            }
        });





    }

    public void reset() {
        super.reset();
    }
}
