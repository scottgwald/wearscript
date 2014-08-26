package com.dappervision.wearscript.events;

public class WearNotificationEvent {
    private String title;
    private String text;

    public WearNotificationEvent(String title, String text) {
        this.title = title;
        this.text = text;
    }

    public String getTitle() {
        return title;
    }

    public String getText() {
        return text;
    }
}
