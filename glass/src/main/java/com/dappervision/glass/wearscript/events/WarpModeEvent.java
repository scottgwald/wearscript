package com.dappervision.glass.wearscript.events;

import com.dappervision.glass.wearscript.controller.manager.WarpManager;

public class WarpModeEvent {
    private WarpManager.Mode mode;

    public WarpModeEvent(WarpManager.Mode mode) {
        this.mode = mode;
    }

    public WarpManager.Mode getMode() {
        return mode;
    }
}