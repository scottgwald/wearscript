package com.dappervision.wearscript.events;

public class WearNotificationEvent {
    private String mTitle;
    private String mText;
    private int mId;

    public WearNotificationEvent(int id, String title, String text) {
        mId = id;
        mTitle = title;
        mText = text;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getText() {
        return mText;
    }

    public int getId() {
        return mId;
    }
}
