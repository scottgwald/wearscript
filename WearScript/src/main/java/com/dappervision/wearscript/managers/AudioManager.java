package com.dappervision.wearscript.managers;


import android.content.Context;

import com.dappervision.wearscript.BackgroundService;
import com.dappervision.wearscript.Log;
import com.dappervision.wearscript.events.SoundEvent;
import com.dappervision.wearscript.events.VolumeChangeEvent;
import com.google.android.glass.media.Sounds;

public class AudioManager extends Manager {
    private android.media.AudioManager systemAudio;

    public AudioManager(BackgroundService service) {
        super(service);
        systemAudio = (android.media.AudioManager) service.getSystemService(Context.AUDIO_SERVICE);
        reset();
    }

    public void onEvent(SoundEvent event) {
        String type = event.getType();
        if (type.equals("TAP"))
            systemAudio.playSoundEffect(Sounds.TAP);
        else if (type.equals("DISALLOWED"))
            systemAudio.playSoundEffect(Sounds.DISALLOWED);
        else if (type.equals("DISMISSED"))
            systemAudio.playSoundEffect(Sounds.DISMISSED);
        else if (type.equals("ERROR"))
            systemAudio.playSoundEffect(Sounds.ERROR);
        else if (type.equals("SELECTED"))
            systemAudio.playSoundEffect(Sounds.SELECTED);
        else if (type.equals("SUCCESS"))
            systemAudio.playSoundEffect(Sounds.SUCCESS);
    }

    public void onEvent (VolumeChangeEvent e) {
        double volume = e.getNewVolume()/100;
        int maxVolumeM = systemAudio.getStreamMaxVolume(android.media.AudioManager.STREAM_MUSIC);
        int maxVolumeS = systemAudio.getStreamMaxVolume(android.media.AudioManager.STREAM_SYSTEM);
        int newVolumeMusic = (int)  (volume * maxVolumeM);
        int newVolumeSystem = (int) (volume * maxVolumeS);
        systemAudio.setStreamVolume(android.media.AudioManager.STREAM_MUSIC, newVolumeMusic, 0);
        systemAudio.setStreamVolume(android.media.AudioManager.STREAM_SYSTEM, newVolumeSystem, 0);

    }

    public void reset() {
        super.reset();
    }
}
