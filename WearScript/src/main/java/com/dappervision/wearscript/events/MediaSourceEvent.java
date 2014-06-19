package com.dappervision.wearscript.events;

import android.net.Uri;
import java.net.URI;

public class MediaSourceEvent {
    private final URI uri;
    private final boolean looping;

    public MediaSourceEvent(URI uri, boolean looping) {
        this.uri = uri;
        this.looping = looping;
    }

    public boolean isLooping() {
        return looping;
    }

    public Uri getUri() {
        return android.net.Uri.parse(uri.toString());
    }
}
