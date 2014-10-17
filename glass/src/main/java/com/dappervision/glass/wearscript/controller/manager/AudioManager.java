package com.dappervision.glass.wearscript.controller.manager;


import android.content.Context;

import com.dappervision.wearscript.BackgroundService;
import com.dappervision.wearscript.events.SoundEvent;
import com.dappervision.wearscript.managers.Manager;
import com.google.android.glass.media.Sounds;

class AudioManager extends Manager {
    private android.media.AudioManager systemAudio;

    public AudioManager(BackgroundService service) {
        super(service);
        systemAudio = (android.media.AudioManager) service.getSystemService(Context.AUDIO_SERVICE);
        reset();
    }

    public void onEvent(SoundEvent event) {
        String type = event.getType();
        switch (type) {
            case "TAP":
                systemAudio.playSoundEffect(Sounds.TAP);
                break;
            case "DISALLOWED":
                systemAudio.playSoundEffect(Sounds.DISALLOWED);
                break;
            case "DISMISSED":
                systemAudio.playSoundEffect(Sounds.DISMISSED);
                break;
            case "ERROR":
                systemAudio.playSoundEffect(Sounds.ERROR);
                break;
            case "SELECTED":
                systemAudio.playSoundEffect(Sounds.SELECTED);
                break;
            case "SUCCESS":
                systemAudio.playSoundEffect(Sounds.SUCCESS);
                break;
        }
    }
}
