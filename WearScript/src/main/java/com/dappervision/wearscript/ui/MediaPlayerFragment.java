package com.dappervision.wearscript.ui;

import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import com.dappervision.wearscript.Log;
import com.dappervision.wearscript.MediaRecordingService;
import com.dappervision.wearscript.R;
import com.dappervision.wearscript.Utils;
import com.dappervision.wearscript.events.MediaActionEvent;
import com.dappervision.wearscript.events.MediaGestureEvent;
import com.dappervision.wearscript.events.MediaOnFingerCountChangedEvent;
import com.dappervision.wearscript.events.MediaOnScrollEvent;
import com.dappervision.wearscript.events.MediaOnTwoFingerScrollEvent;
import com.dappervision.wearscript.events.MediaPauseEvent;
import com.dappervision.wearscript.events.MediaPlayerReadyEvent;
import com.dappervision.wearscript.events.MediaRecordEvent;
import com.dappervision.wearscript.events.MediaRecordPathEvent;
import com.dappervision.wearscript.events.MediaShutDownEvent;
import com.dappervision.wearscript.events.MediaSourceEvent;
import com.dappervision.wearscript.takeTwo.CompositeFile;
import com.dappervision.wearscript.takeTwo.FileEntry;
import com.dappervision.wearscript.takeTwo.FileTimeTuple;
import com.google.android.glass.touchpad.Gesture;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;



public class MediaPlayerFragment extends GestureFragment implements MediaPlayer.OnErrorListener, MediaPlayer.OnPreparedListener , MediaPlayer.OnCompletionListener {
    public static final String ARG_URL = "ARG_URL";
    public static final String ARG_LOOP = "ARG_LOOP";
    private static final String TAG = "MediaPlayerFragment";
    private  MediaPlayer mp;
    private Uri mediaUri;
    private SurfaceHolder holder;
    private ProgressBar progressBar;
    private SurfaceView surfaceView;
    private Handler stutterHandler;
    private Runnable stutterThread;
    private boolean interrupt;
    private List<Integer> seekTimes;
    private long prevJumpTime;
    private int seekPosition = 0;
    private RelativeLayout relative;
    private MediaRecordingService rs;
    private ArrayList<String> fileFragments = new ArrayList<String>();
    private MediaHUD hud;
    private MediaController controller;
    private Handler handler = new Handler();
    private SeekBar seekBar;
    private long currentTime;
    private long recording;
    private CompositeFile videos;
    private FileEntry currentFile = null;
    private Handler seekBarHandler = new Handler();
    private Runnable updateSeekBar;
    private RelativeLayout barBackground;
    private boolean isWaitingTap = false;
    private Handler mergeHandler;

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
        videos = new CompositeFile(true);
        mergeHandler = new Handler();
    }

    private void createMediaPlayer() {
        if (progressBar != null && mediaUri != null)
            progressBar.setVisibility(View.VISIBLE);
        mp = new MediaPlayer();
        if (mediaUri != null) {
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
        } else {
            if (progressBar != null)
                progressBar.setVisibility(View.INVISIBLE);
        }

        mp.setOnCompletionListener(this);
    }

    private void setMediaSource(Uri uri, boolean looping) {
        mediaUri = uri;
        if (mp == null) {
            return;
        }
        if (mp.isPlaying()) {

            mp.stop();
        }
        try {
            mp.reset();
            Log.d("TRY", mediaUri.toString());
            String tail = videos.getTail().getFilePath();
            Log.d("TRY",tail);
            if(tail.equals(mediaUri.toString())) {
                throw new IllegalStateException("Cannot play tail");
            }
            mp.setDataSource(getActivity(), mediaUri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mp.setOnErrorListener(this);
        mp.setOnPreparedListener(this);
        mp.setLooping(looping);
        try {
            hud.clear();
            hud.hidePresent();
            mp.prepare();
            mp.start();
            //seekBarHandler.post(updateSeekBar); before

        } catch(IOException e){}
    }


    public void onEvent(MediaPauseEvent e) {
        hud.stopRecording();
    }

    public void onEvent(MediaSourceEvent e) {
        this.setMediaSource(e.getUri(), e.isLooping());
    }

    public void onEvent(MediaRecordEvent e) {
        hud.showPresent();
        hud.showRecording();
        Log.d(TAG, "in onEvent(MediaRecordEvent e)");
        Log.d(TAG, "rs: " + rs);
        Log.d(TAG, "e: " + e);
        Log.d(TAG, "e.getFilePath(): " + e.getFilePath());
        if(rs == null) {
            Log.d("TAG","rs is null");
        }
        String path = rs.startRecord(e.getFilePath());
        seekBarHandler.post(updateSeekBar);
        Utils.eventBusPost(new MediaRecordPathEvent(path));
        videos.addFile(path, -1);
    }

    public void onEvent(MediaActionEvent e) {
        String action = e.getAction();
        Log.d(TAG, "in onEvent()");
        Log.d(TAG, "action: " + e.getAction());

        if (action.equals("play")) {
            interrupt = true;
            hud.clear();
            if(isWaitingTap) {
                    jump(600);
                }
            mp.start();

        } else if (action.equals("stop")) {
            interrupt = true;
            mp.stop();
            getActivity().finish();
        } else if (action.equals("pause")) {
            interrupt = true;
            hud.showPause();
            mp.pause();
        } else if (action.equals("playReverse")) {
            playReverseFromEnd(e.getMsecs());
        } else if (action.equals("jump")) {
            interrupt = true;
            jump(e.getMsecs());
        } else if (action.equals("playFastForward")) {
            playFastForwardFromBeginning(e.getMsecs());
        } else if (action.equals("rewind")) {
            rewind(e.getMsecs());
        } else if (action.equals("fastForward")) {
            fastForward(e.getMsecs());
        } else if (action.equals("takeTwoRewind")) {
            takeTwoRewind(e.getMsecs());
        } else if (action.equals("seekTo")) {
            mp.seekTo(e.getMsecs());
        } else if (action.equals("seekBackwards")) {
            seekBackwards(e.getMsecs());
        } else if (action.equals("jumpToPresent")) {
            this.jumpToPresent();
        }
    }

    private synchronized void jump(int jumpVectorMSecs) {
        //positive jumpVector jumps forward / negative vector jumps backwards total milliseconds
        if (jumpVectorMSecs == 0) return;

        this.isWaitingTap = false;

        if(jumpVectorMSecs < 0) {
            hud.showSkipBack();
        } else {
            hud.showSkipForward(true);
        }

        FileTimeTuple fileTimeToSeek;
        if (videos.isTailFinished()) {
            if (currentFile == null) {
                // at the tail!
                //TODO: implement this logic
                fileTimeToSeek = null;
            } else {
                // normal case
                fileTimeToSeek = videos.getFileFromJump(
                        jumpVectorMSecs,
                        mp.getCurrentPosition(),
                        mediaUri.getPath());
            }
        } else {
            if (currentFile == null) {
                if (jumpVectorMSecs > 0) {
                    //TODO: show icon saying this operation is not allowed
                    hud.showSkipForward(false);
                    return;
                }
                long start = rs.getCurrentRecordingStartTimeMillis();
                long now = System.currentTimeMillis();
                if (now + jumpVectorMSecs < start) {
                    // jump to previous recorded file, let recording continue
                    fileTimeToSeek = videos.getFileFromJump(
                            videos.endOfFile(videos.getLastRecordedFile()),
                            jumpVectorMSecs + (now - start));
                } else {
                    cutTail();
                    // seek to desired location in new file
                    fileTimeToSeek = videos.getFileFromJump(
                            videos.endOfFile(videos.getLastRecordedFile()),
                            jumpVectorMSecs);
                }
            } else {
                fileTimeToSeek = videos.getFileFromJump(
                        jumpVectorMSecs,
                        mp.getCurrentPosition(),
                        currentFile.getFilePath());
                        //mediaUri.getPath());
                long start = rs.getCurrentRecordingStartTimeMillis();
                long now = System.currentTimeMillis();
                Log.d(TAG,videos.getTail().getFilePath());
                Log.d(TAG,fileTimeToSeek.getFilePath());
                if (videos.getTail().getFilePath().equals(fileTimeToSeek.getFilePath())) {
                    Log.d(TAG,"Cutting tail");
                    cutTail();
                    fileTimeToSeek = videos.getFileFromTime(videos.getTime(fileTimeToSeek)); //this is returning the tail
                    Log.d(TAG,"new FileTimeToSeek: "+fileTimeToSeek.getFilePath().toString());
                    Log.d(TAG,"tail: "+videos.getTail().getFilePath());
                    if ( fileTimeToSeek.getFilePath().equals(videos.getTail().getFilePath())) {
                        fileTimeToSeek = new FileTimeTuple(videos.getLastRecordedFile().getFilePath(),0);
                    }


                    if (fileTimeToSeek.getTimeInFile() > now - start - 5000) {
                        // trying to fast forward to < 5 seconds behind the present
                        // make user be at least 5 seconds behind
                        fileTimeToSeek = new FileTimeTuple(fileTimeToSeek.getFilePath(), now - start - 5000);
                    }
                }
            }
        }

        seekToFileTime(fileTimeToSeek);

//        Runnable cutInBackground = new Runnable() {
//            @Override
//            public void run() {
//                videos.flattenFile();
//                FileEntry newCurrentFile = videos.getLastRecordedFile();
//                setMediaSource(Uri.fromFile(new File(newCurrentFile.getFilePath())), false);
//                mp.seekTo((int) getCurrentPosition());
//                currentFile = newCurrentFile;
//            }
//        };
//
//
//        if (videos.numBreaksAfter(currentFile) > 0) {
//            Log.d(TAG,"merging");
//            mergeHandler.post(cutInBackground);
//        }
    }

    private void jumpToPresent() {
        if (updateSeekBar != null) {
            seekBarHandler.removeCallbacks(updateSeekBar);
        }
        this.currentFile = null;
        seekBarHandler.post(updateSeekBar);
        mp.stop();
        seekBar.setProgress(seekBar.getMax());
        hud.showPresent();
        videos.flattenFile();
    }

    private void cutTail() {
        rs.stopRecording();
        String newFilePath = rs.startRecord(null); // start recording with an automatically generated file name
        videos.setTailDuration(getDuration(videos.getTail().getFilePath()));
        videos.addFile(newFilePath, -1);
        /*new AsyncTask<Void, Void, Long>() {
            @Override
            public Long doInBackground(Void... args) {
                videos.flattenFile();
                FileEntry newCurrentFile = videos.getLastRecordedFile();
                setMediaSource(Uri.fromFile(new File(newCurrentFile.getFilePath())), false);
                mp.seekTo((int) getCurrentPosition());
                currentFile = newCurrentFile;
                return currentFile.getFileDuration();
            }
        }.execute();*/
    }

    public long getCurrentPosition() {
        return currentFile.getStartTime() + mp.getCurrentPosition();
    }

    private void seekToFileTime(FileTimeTuple fileTime) {
        String filePathToSeek = fileTime.getFilePath();
        Uri newUri = Uri.fromFile(new File(filePathToSeek));
        if (!newUri.equals(mediaUri)) {
            Log.d(TAG,"new URI: "+newUri.toString());
            if(mediaUri != null)
            Log.d(TAG,"old URI: "+mediaUri.toString());
            currentFile = videos.getFileEntry(filePathToSeek); //fix
            setMediaSource(newUri, false);

            int timeToSeek = (int)fileTime.getTimeInFile();
            mp.seekTo(timeToSeek);
            if (timeToSeek > mp.getDuration()) {
                //TODO: show icon saying this operation is not allowed
                hud.showSkipForward(false);
            }
        }
        mp.seekTo((int)fileTime.getTimeInFile());
    }

    private void seekBackwards(int msecs) {
        mp.seekTo(mp.getDuration());
        jump(-msecs);
    }

    private void stutter(int period) {

        final int p = period;
        stutterHandler = new Handler();
        mp.seekTo(mp.getDuration());
        currentTime = mp.getDuration();
        stutterThread = new Runnable() {
            public void run() {

                if (!interrupt) {
                    currentTime = currentTime - p;
                    if (currentTime <= 0) {
                        mp.seekTo(0);
                        mp.start();
                    } else {

                        //mp.seekTo(currentTime);
                        mp.start();
                        stutterHandler.postDelayed(stutterThread, p);

                    }
                }
            }
        };
        stutterHandler.postDelayed(stutterThread, period);
    }


    @Override
    public boolean onScroll(float v, float v2, float v3) {
        Utils.eventBusPost(new MediaOnScrollEvent(v, v2, v3));
        return false;
    }

    private void modifiedSpeedPlayback(final int speed, boolean forward, boolean fromEndpoint) {
        if (speed <= 0) return;
        mp.pause();
        final int startDelay = 100;
        seekTimes = new ArrayList<Integer>();
        final int duration = mp.getDuration();

        if (!forward) {
            if (fromEndpoint) {
                for (int i = duration; i >= 0; i -= speed) {
                    seekTimes.add(i);
                }
            } else {
                for (int i = mp.getCurrentPosition(); i >= 0; i -= speed) {
                    seekTimes.add(i);
                }
            }
        } else {
            if (fromEndpoint) {
                for (int i = 0; i < duration; i += speed) {
                    seekTimes.add(i);
                }
            } else {
                for (int i = mp.getCurrentPosition(); i < duration; i += speed) {
                    seekTimes.add(i);
                }
            }
        }

        mp.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
            @Override
            public void onSeekComplete(MediaPlayer mediaPlayer) {
                long currentTime = System.currentTimeMillis();
                //if time between jumps is more than twice the speed of jumps, skip a frame
                if (currentTime > prevJumpTime + 2 * speed) {
                    seekPosition++;
                }
                if (seekPosition < seekTimes.size() && !interrupt) {
                    seekPosition++;
                    prevJumpTime = currentTime;
                    mp.seekTo(seekTimes.get(seekPosition - 1));
                } else {
                    seekPosition = 0;
                    if (!interrupt)
                        mp.start();
                }
            }
        });
        interrupt = false;
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                seekPosition++;
                prevJumpTime = System.currentTimeMillis();
                if (seekTimes.size() > 0)
                    mp.seekTo(seekTimes.get(0));
            }
        }, startDelay);
    }

    private void takeTwoRewind(final int speed) {

    }


    private void rewind(final int speed) {
        modifiedSpeedPlayback(speed, false, false);
    }

    private void fastForward(final int speed) {
        modifiedSpeedPlayback(speed, true, false);
    }

    private void playReverseFromEnd(final int speed) {
        modifiedSpeedPlayback(speed, false, true);
    }

    private void playFastForwardFromBeginning(int speed) {
        modifiedSpeedPlayback(speed, true, true);
    }

    private void setUpSeekBar() {

        barBackground = new RelativeLayout(this.getActivity());
        RelativeLayout.LayoutParams bParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        //bParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        barBackground.setBackgroundColor(R.color.black); //ignore error for cool effect
        bParams.topMargin = 315;

        barBackground.setLayoutParams(bParams);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        seekBar.setLayoutParams(params);
        seekBar.setMax(100);
        seekBar.setProgress(100);
        barBackground.addView(seekBar);

        final ArrayList<Float> timeMarkers = new ArrayList<Float>();

        updateSeekBar = new Runnable() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();
                long start = rs.getCurrentRecordingStartTimeMillis();

                long totalTime = videos.getDuration() + (now - start);

                if ((int)totalTime/1000 <= 0)
                    seekBar.setMax(1000);
                else
                    seekBar.setMax((int)totalTime/1000);

                if(currentFile == null) {

                    seekBar.setProgress(seekBar.getMax());
                }

                timeMarkers.clear();
                for (FileEntry file : videos.files) {
                    timeMarkers.add((float)(file.getStartTime()) / totalTime);
                }
                Log.d("UPDATE","updating time");
                hud.updateTotalTime(totalTime);
                if (currentFile == null)
                    hud.updateCurrentPosition(totalTime);



                if (mp != null && currentFile != null ) {

                    hud.updateTimeMarkers(timeMarkers);
                    hud.updateCurrentPosition(getCurrentPosition());

                    long mCurrentPosition = getCurrentPosition()/1000;
                    if (currentFile.getStartTime() + currentFile.getFileDuration() >= mCurrentPosition && !currentFile.equals(videos.getTail().getFilePath()))
                    seekBar.setProgress((int) mCurrentPosition);
                    //if (currentFile != null) //concur

                }

                seekBarHandler.postDelayed(updateSeekBar, 1000);

            }
        };


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_media_player, container, false);
        hud = new MediaHUD(this.getActivity());
        hud.setZOrderMediaOverlay(true);
        hud.getHolder().setFormat(PixelFormat.TRANSPARENT);
        seekBar = new SeekBar(this.getActivity());
        this.setUpSeekBar();
        surfaceView = (SurfaceView) v.findViewById(R.id.media_surface);
        ViewGroup.LayoutParams layoutParams = surfaceView.getLayoutParams();



        layoutParams.height = 480;
        layoutParams.width = 640;

        surfaceView.setLayoutParams(layoutParams);

        relative = (RelativeLayout) v.findViewById(R.id.relative);
        relative.addView(new View(this.getActivity()));
        relative.addView(hud);
        relative.addView(barBackground);
        holder = surfaceView.getHolder();


        //holder.setSizeFromLayout();

        holder.addCallback(new SurfaceHolder.Callback() {

            public void surfaceCreated(SurfaceHolder holder) {
                if (mp != null) {
                    //holder.setFixedSize(176, 144);
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
        if (mp != null) {
            if (mp.isPlaying())
                mp.stop();
            mp.release();
            mp = null;
        }
        Utils.getEventBus().unregister(this);

    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i2) {
        Log.e(TAG, "MediaPlayer Error: ");
        if (i == MediaPlayer.MEDIA_ERROR_SERVER_DIED) {
            Log.w(TAG, "Server Died");
            mediaPlayer.release();
            mp = null;
            createMediaPlayer();
        } else if (i == MediaPlayer.MEDIA_ERROR_UNKNOWN) {
            Log.w(TAG, "Unknown Error, resetting");
            mediaPlayer.release();
            mp = null;
            createMediaPlayer();
        }
        return false;
    }

    public void onEvent(MediaShutDownEvent e) {
        this.getActivity().finish();
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
        surfaceView.setVisibility(View.VISIBLE);
        mediaPlayer.start();
        Utils.eventBusPost(new MediaPlayerReadyEvent());
    }


    @Override
    public boolean onGesture(Gesture gesture) {
        Utils.eventBusPost(new MediaGestureEvent(gesture));
        return false;
    }

    public void onFingerCountChanged(int i, int i1) {
        Utils.eventBusPost(new MediaOnFingerCountChangedEvent(i, i1));
    }

    boolean onTwoFingerScroll(float v, float v1, float v2) {
        Utils.eventBusPost(new MediaOnTwoFingerScrollEvent(v, v1, v2));
        return false;
    }

    public void setSurfaceView(SurfaceView sv){

    }

    public void onCompletion(MediaPlayer mp) {
        Log.d("HERE", "on Completion called");
        this.isWaitingTap = true;
        hud.tapToContinue();
    }

    public long getDuration(String path) {
        MediaPlayer mp = MediaPlayer.create(this.getActivity(), Uri.parse(path));
        return mp.getDuration();
    }

    public void setServiceHandle(MediaRecordingService service) {
        rs = service;
    }


}
