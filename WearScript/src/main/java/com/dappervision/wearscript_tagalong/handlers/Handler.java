package com.dappervision.wearscript_tagalong.handlers;

import com.dappervision.wearscript_tagalong.Utils;

public abstract class Handler {

    public Handler() {
        Utils.getEventBus().register(this);
    }

    public void shutdown() {
        if(Utils.getEventBus().isRegistered(this))
            Utils.getEventBus().unregister(this);
    }
}
