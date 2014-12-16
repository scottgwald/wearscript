package com.dappervision.wearscript_tagalong.managers;


import android.content.Context;
import android.media.SoundPool;
import android.util.Log;

import com.dappervision.wearscript_tagalong.BackgroundService;
import com.dappervision.wearscript_tagalong.events.SaveAudioEvent;
import com.dappervision.wearscript_tagalong.events.SoundEvent;
import com.google.android.glass.media.Sounds;

import org.apache.http.util.ByteArrayBuffer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class AudioManager extends Manager {

    private android.media.AudioManager systemAudio;
    private SoundPool mSoundPool;
    private static int MAX_SAMPLES = 100;
    public static final String SAVE_AUDIO= "SAVE_AUDIO";
    public static final String SOUND= "SOUND";
    public static final String STOP= "STOP";
    public static final String PAUSE= "PAUSE";




    public AudioManager(BackgroundService service) {
        super(service);
        systemAudio = (android.media.AudioManager) service.getSystemService(Context.AUDIO_SERVICE);
        mSoundPool = new SoundPool(MAX_SAMPLES, android.media.AudioManager.STREAM_MUSIC, 0);
        reset();
    }

    public void onEvent(SaveAudioEvent event) {

        String DownloadUrl = event.getPath();
        final String fileName = event.getFileName();
        File file = null;

        try {
            File root = android.os.Environment.getExternalStorageDirectory();

            File dir = new File (root.getAbsolutePath() + "/tagalong_audio");
            if(dir.exists()==false) {
                dir.mkdirs();
            }

            URL url = new URL(DownloadUrl);
            file = new File(dir, fileName);

            long startTime = System.currentTimeMillis();


           /* Open a connection to that URL. */
            URLConnection ucon = url.openConnection();

           /*
            * Define InputStreams to read from the URLConnection.
            */
            InputStream is = ucon.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is);

           /*
            * Read bytes to the Buffer until there is nothing more to read(-1).
            */
            ByteArrayBuffer baf = new ByteArrayBuffer(5000);
            int current = 0;
            while ((current = bis.read()) != -1) {
                baf.append((byte) current);
            }


           /* Convert the Bytes read to a String. */
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(baf.toByteArray());
            fos.flush();
            fos.close();

        } catch (IOException e) {
            Log.d("DownloadManager", "Error: " + e);
        }

        int id = -1;

        if (file != null){
            id = mSoundPool.load(file.getAbsolutePath(),1);
        }

        final int resultId = id;

        mSoundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int i, int i2) {
                makeCall(SAVE_AUDIO+fileName,
                        String.format("%d", resultId));

                unregisterCallback(SAVE_AUDIO+fileName);
            }
        });

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
        else if (type.equals(SOUND))
            this.playSound(event.getId());
        else if (type.equals(STOP))
            this.stopSound(event.getId());
        else if (type.equals(PAUSE))
            this.pauseSound(event.getId());

    }

    private void stopSound(int id) {
        mSoundPool.stop(id);
    }

    private void pauseSound(int id) {
        mSoundPool.pause(id);
    }

    private void playSound(int id) {

              mSoundPool.stop(id);
              mSoundPool.play(id,
                                 1 /* leftVolume */,
                                 1 /* rightVolume */,
                                 1,
                                 0 /* loop */,
                                 1f /* rate */);
    }

    public void reset() {
        super.reset();
    }
}
