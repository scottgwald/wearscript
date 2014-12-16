package com.dappervision.wearscript_tagalong.events;

import com.dappervision.wearscript_tagalong.managers.WarpManager;

public class WarpModeEvent {
    WarpManager.Mode mode;

    public WarpModeEvent(WarpManager.Mode mode) {
        this.mode = mode;
    }

    public WarpManager.Mode getMode() {
        return mode;
    }
}