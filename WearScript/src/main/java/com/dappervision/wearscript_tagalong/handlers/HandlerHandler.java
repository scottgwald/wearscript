package com.dappervision.wearscript_tagalong.handlers;

import com.dappervision.wearscript_tagalong.HardwareDetector;

import java.util.ArrayList;

public class HandlerHandler {
    private static HandlerHandler singleton;
    ArrayList<Handler> handlers;

    public HandlerHandler() {
        handlers = new ArrayList<Handler>();
        newHandlers();
    }

    public static HandlerHandler get() {
        if (singleton != null) {
            return singleton;
        }
        singleton = new HandlerHandler();
        return singleton;
    }

    public void resetAll() {
        shutdownAll();
        newHandlers();
    }

    public void shutdownAll() {
        for (Handler h : handlers) {
            h.shutdown();
        }
        while (!handlers.isEmpty())
            handlers.remove(0);
    }

    public void newHandlers() {
        if(!HardwareDetector.hasGDK){
            handlers.add(new ControlHandler());
        }
        handlers.add(new AdbHandler());
    }
}
