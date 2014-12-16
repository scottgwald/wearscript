package com.dappervision.wearscript_tagalong.events;

public class BluetoothModeEvent {
    private boolean enable;

    public BluetoothModeEvent(boolean enable) {
        this.enable = enable;
    }

    public boolean isEnable() {
        return enable;
    }
}
