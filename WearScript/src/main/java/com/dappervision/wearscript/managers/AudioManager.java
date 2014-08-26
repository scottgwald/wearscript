package com.dappervision.wearscript.managers;


import android.content.Context;
import android.media.SoundPool;

import com.dappervision.wearscript.BackgroundService;
import com.dappervision.wearscript.R;
import com.dappervision.wearscript.events.SoundEvent;
import com.google.android.glass.media.Sounds;

public class AudioManager extends Manager {
    private android.media.AudioManager systemAudio;
    private SoundPool mSoundPool;
    private static int buzzId;


    public AudioManager(BackgroundService service) {
        super(service);
        systemAudio = (android.media.AudioManager) service.getSystemService(Context.AUDIO_SERVICE);
        mSoundPool = new SoundPool(1, android.media.AudioManager.STREAM_MUSIC, 0);
        buzzId= mSoundPool.load(service.getApplicationContext(), R.raw.bass8, 1);

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
        else if (type.equals("BUZZ"))
            this.buzz();
    }
    private void buzz(){
      mSoundPool.play(buzzId,
                1 /* leftVolume */,
                1 /* rightVolume */,
                1,
                0 /* loop */,
                2f /* rate */);
    }

    public void reset() {
        super.reset();
    }
}
