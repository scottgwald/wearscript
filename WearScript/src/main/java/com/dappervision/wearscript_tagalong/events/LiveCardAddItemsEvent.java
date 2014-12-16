package com.dappervision.wearscript_tagalong.events;

import com.dappervision.wearscript_tagalong.ui.MenuActivity;

public class LiveCardAddItemsEvent {
    private MenuActivity activity;

    public LiveCardAddItemsEvent(MenuActivity activity) {
        this.activity = activity;
    }
    public MenuActivity getActivity() {
        return activity;
    }
}
