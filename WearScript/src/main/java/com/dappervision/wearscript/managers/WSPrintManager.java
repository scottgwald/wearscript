package com.dappervision.wearscript.managers;

import android.content.Intent;
import android.print.PrintJob;
import android.print.PrintJobInfo;
import android.util.Base64;
import android.util.Log;

import com.dappervision.wearscript.BackgroundService;
import com.dappervision.wearscript.events.BarcodeEvent;
import com.dappervision.wearscript.events.CallbackRegistration;
import com.dappervision.wearscript.events.PrintEvent;
import com.dappervision.wearscript.ui.PrintActivity;
import com.dappervision.wearscript.ui.QRActivity;

import org.json.simple.JSONObject;

import java.util.ArrayList;

public class WSPrintManager extends Manager {
    public static String PRINT_JOB = "PRINT_JOB";
    private ArrayList<PrintJob> mPrintJobs = new ArrayList<PrintJob>();
    public BackgroundService bs;

    public WSPrintManager(BackgroundService bs) {
        super(bs);
        reset();
    }

    public void loadService() {
        bs = service;
    }

    public void onEvent(CallbackRegistration e) {
        if (e.getManager().equals(this.getClass())) {
            Log.d(TAG, "Registering callback.");
            registerCallback(e.getEvent(), e.getCallback());
            startActivity();
        }
    }

    public String jobToJSON(PrintJobInfo pj) {
        JSONObject jo = new JSONObject();
        jo.put("printerId", pj.getPrinterId());
        jo.put("state", pj.getState());
        jo.put("label", pj.getLabel());
        return jo.toJSONString();
    }

    public void onEvent(PrintEvent e) {
        String data = "Some awesome data";
        makeCall(PRINT_JOB, String.format("'%s'", data));
//        makeCall(PRINT_JOB, jobToJSON(e.getJob().getInfo()));
    }

    public void startActivity() {
        Intent printIntent = new Intent(service.getBaseContext(), PrintActivity.class);
        // leaving this in, maybe not necessary / wrong
        printIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        service.getApplication().startActivity(printIntent);
    }

}
