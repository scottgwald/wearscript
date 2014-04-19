package com.dappervision.wearscript.events;

import android.print.PrintJob;

/**
 * Created by swgreen on 4/18/14.
 */

public class PrintEvent {
    private String title;
    private PrintJob job;

    public PrintEvent(String title) {
        this.title = title;
    }

    public PrintEvent(PrintJob job) {
        this.job = job;
    }

    public PrintJob getJob() {
        return job;
    }

    public String getTitle() {
        return title;
    }

}
