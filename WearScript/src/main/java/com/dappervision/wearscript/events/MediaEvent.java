package com.dappervision.wearscript.events;

import android.net.Uri;

import java.net.URI;

public class MediaEvent {
    private final URI uri;
    private final boolean looping;
    private final boolean status;
    private final boolean recordVideo;

    public MediaEvent(URI uri, boolean looping, boolean recordVideo) {
        this.uri = uri;
        this.looping = looping;
        this.status = false;
        this.recordVideo = recordVideo;
    }
    public MediaEvent(boolean recordVideo) {
        this.uri = null;
        this.looping = false;
        this.status = false;
        this.recordVideo = recordVideo;
    }

    public boolean isLooping() {
        return looping;
    }

    public Uri getUri() {
        if (uri == null) {
            return null;
        }
        return android.net.Uri.parse(uri.toString());
    }

    public boolean isRecordVideo() {
        return recordVideo;
    }
}
