package com.dappervision.glass.wearscript.controller;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ProgressBar;

import com.dappervision.glass.wearscript.R;
import com.dappervision.wearscript.Log;
import com.dappervision.wearscript.Utils;
import com.dappervision.wearscript.events.MediaActionEvent;
import com.google.android.glass.touchpad.Gesture;

import java.io.IOException;

public class MediaPlayerFragment extends GestureFragment implements MediaPlayer.OnErrorListener, MediaPlayer.OnPreparedListener {
    public static final String ARG_URL = "ARG_URL";
    public static final String ARG_LOOP = "ARG_LOOP";
    private static final String TAG = "MediaPlayerFragment";
    private MediaPlayer mp;
    private Uri mediaUri;
    private ProgressBar progressBar;
    private SurfaceView surfaceView;

    public static MediaPlayerFragment newInstance(Uri uri, boolean looping) {
        Bundle args = new Bundle();
        args.putParcelable(ARG_URL, uri);
        args.putBoolean(ARG_LOOP, looping);
        MediaPlayerFragment f = new MediaPlayerFragment();
        f.setArguments(args);
        return f;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Utils.getEventBus().register(this);
        setRetainInstance(true);
        mediaUri = getArguments().getParcelable(ARG_URL);
        createMediaPlayer();
    }

    private void createMediaPlayer(){
        if(progressBar != null)
            progressBar.setVisibility(View.VISIBLE);
        mp = new MediaPlayer();
        try {
            mp.setDataSource(getActivity(), mediaUri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mp.setOnErrorListener(this);
        mp.setOnPreparedListener(this);

        if (getArguments().getBoolean(ARG_LOOP))
            mp.setLooping(true);
        mp.prepareAsync();
    }

    public void onEvent(MediaActionEvent e) {
        String action = e.getAction();
        if (action.equals("play")) {
            mp.start();
        } else if (action.equals("stop")) {
            mp.stop();
            getActivity().finish();
        } else if (action.equals("pause")) {
            mp.pause();
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_media_player, container, false);
        surfaceView = (SurfaceView) v.findViewById(R.id.fragment_media_player_media_surface);
        progressBar = (ProgressBar) v.findViewById(R.id.fragment_media_player_video_progressBar);
        SurfaceHolder holder = surfaceView.getHolder();
        holder.addCallback(new SurfaceHolder.Callback() {

            public void surfaceCreated(SurfaceHolder holder) {
                if (mp != null) {
                    mp.setDisplay(holder);
                }
            }

            public void surfaceDestroyed(SurfaceHolder holder) {
                if (mp != null) {
                    mp.setDisplay(null);
                }
            }

            public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
            }
        });

        return v;
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getActivity().finish();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        if (mp.isPlaying())
            mp.stop();
        mp.release();
        mp = null;
        Utils.getEventBus().unregister(this);
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i2) {
       Log.e(TAG, "MediaPlayer Error: ");
        if(i == MediaPlayer.MEDIA_ERROR_SERVER_DIED) {
            Log.w(TAG, "Server Died");
            mediaPlayer.release();
            mp = null;
            createMediaPlayer();
        }else if (i == MediaPlayer.MEDIA_ERROR_UNKNOWN) {
            Log.w(TAG, "Unknown Error, resetting");
            mediaPlayer.release();
            mp = null;
            createMediaPlayer();
        }
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        if(progressBar != null){
            progressBar.setVisibility(View.GONE);
        }
        surfaceView.setVisibility(View.VISIBLE);
        mediaPlayer.start();
    }

    @Override
    public boolean onGesture(Gesture gesture) {
        if (gesture == Gesture.TAP) {
            if (mp.isPlaying()) {
                mp.pause();
            } else {
                mp.start();
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean onScroll(float v, float v2, float v3) {
        if(mp.isPlaying())
            return false;
        int newPosition = mp.getCurrentPosition() + (int)(v * 10);
        if(newPosition < 0)
            newPosition = 0;
        if(newPosition > mp.getDuration())
            newPosition = mp.getDuration() - 5;
        mp.seekTo(newPosition);
        return true;
    }
}
