package com.dappervision.wearscript.events;

public class NotificationEvent {
    private String mTitle;
    private String mText;
    private int mId;

    public NotificationEvent(int id, String title, String text) {
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
