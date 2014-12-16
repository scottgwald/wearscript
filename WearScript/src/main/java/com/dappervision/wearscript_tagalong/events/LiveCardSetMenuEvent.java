package com.dappervision.wearscript_tagalong.events;

public class LiveCardSetMenuEvent {
    private String menu;

    public LiveCardSetMenuEvent(String menu) {
        this.menu = menu;
    }

    public String getMenu() {
        return menu;
    }
}
