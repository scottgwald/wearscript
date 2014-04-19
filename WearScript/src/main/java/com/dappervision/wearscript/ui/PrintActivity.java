package com.dappervision.wearscript.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintJob;
import android.print.PrintManager;
import android.util.Log;

import com.dappervision.wearscript.BackgroundService;
import com.dappervision.wearscript.R;
import com.dappervision.wearscript.Utils;
import com.dappervision.wearscript.events.BarcodeEvent;
import com.dappervision.wearscript.events.PrintEvent;
import com.dappervision.wearscript.managers.ManagerManager;
import com.dappervision.wearscript.managers.WSPrintManager;

public class PrintActivity extends Activity {
    private static final String TAG = "PrintActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createWebPrintJob();
    }


    public void createWebPrintJob() {

        // Get a PrintManager instance
        PrintManager printManager = (PrintManager) getSystemService(Context.PRINT_SERVICE);

        WSPrintManager pm = (WSPrintManager) ManagerManager.get().get(WSPrintManager.class);
        pm.loadService();
        BackgroundService bs = pm.bs;
        // Get a print adapter instance
        PrintDocumentAdapter printAdapter = bs.getScriptView().createPrintDocumentAdapter();

        // Create a print job with name and adapter instance
        String jobName = getString(R.string.app_name) + " Document";
        PrintJob printJob = printManager.print(jobName, printAdapter,
                new PrintAttributes.Builder().build());

        Log.d(TAG, "Created print job");
        // Save the job object for later status checking
        Utils.eventBusPost(new PrintEvent(printJob));
    }

}
