package com.dappervision.wearscript.ui;

import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
<<<<<<< HEAD
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
=======
import android.widget.ProgressBar;
>>>>>>> jump-cut
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
<<<<<<< HEAD
import com.dappervision.wearscript.events.MediaPauseEvent;
import com.dappervision.wearscript.events.MediaRecordEvent;
import com.dappervision.wearscript.events.MediaShutDownEvent;
import com.dappervision.wearscript.events.MediaSourceEvent;
import com.dappervision.wearscript.takeTwo.FragmentedFile;
import com.dappervision.wearscript.events.MediaPlayerReadyEvent;
=======
import com.dappervision.wearscript.events.MediaRecordEvent;
import com.dappervision.wearscript.events.MediaRecordPathEvent;
import com.dappervision.wearscript.events.MediaShutDownEvent;
import com.dappervision.wearscript.events.MediaSourceEvent;
import com.dappervision.wearscript.takeTwo.CompositeFile;
import com.dappervision.wearscript.takeTwo.FileEntry;
import com.dappervision.wearscript.takeTwo.FileTimeTuple;
>>>>>>> jump-cut
import com.google.android.glass.touchpad.Gesture;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

<<<<<<< HEAD
public class MediaPlayerFragment extends GestureFragment implements MediaPlayer.OnErrorListener, MediaPlayer.OnPreparedListener , MediaPlayer.OnCompletionListener, MediaController.MediaPlayerControl {
=======

public class MediaPlayerFragment extends GestureFragment implements MediaPlayer.OnErrorListener, MediaPlayer.OnPreparedListener , MediaPlayer.OnCompletionListener {
>>>>>>> jump-cut
    public static final String ARG_URL = "ARG_URL";
    public static final String ARG_LOOP = "ARG_LOOP";
    private static final String TAG = "MediaPlayerFragment";
    private MediaPlayer mp;
    private Uri mediaUri;
    private SurfaceHolder holder;
    private ProgressBar progressBar;
    private SurfaceView surfaceView;
    private Handler stutterHandler;
    private Runnable stutterThread;
    private int currentTime;
    private boolean interrupt;
    private List<Integer> seekTimes;
    private long prevJumpTime;
    private int seekPosition = 0;
    private RelativeLayout relative;
    private MediaRecordingService rs;
<<<<<<< HEAD
    private ArrayList<String> fileFragments = new ArrayList<String>();
    private String currentFile="";
    private MediaHUD hud;
    private MediaController controller;
    private Handler handler = new Handler();
    private SeekBar seekBar;
    private long currentTime;
    private long recording;
    
=======

    private CompositeFile videos;
    private FileEntry currentFile = null;
>>>>>>> jump-cut

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
<<<<<<< HEAD
        controller = new MediaController(this.getActivity(),false);


=======

        videos = new CompositeFile(true);
>>>>>>> jump-cut
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
            Log.d("TRY",mediaUri.toString());
            mp.setDataSource(getActivity(), mediaUri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mp.setOnErrorListener(this);
        mp.setOnPreparedListener(this);
        mp.setLooping(looping);
        try {
            mp.prepare();
            mp.start();

        } catch(IOException e){}

    }

    public void onEvent(MediaRecordEvent e) {
        hud.showRecording();
        this.recording = System.currentTimeMillis();
    }
    public void onEvent(MediaPauseEvent e) {
        hud.stopRecording();
    }

    public void onEvent(MediaSourceEvent e) {
        this.setMediaSource(e.getUri(), e.isLooping());
    }

    public void onEvent(MediaRecordEvent e) {
        String path = rs.startRecord(e.getFilePath());
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
        }
    }

    private synchronized void jump(int jumpVectorMSecs) {
        //positive jumpVector jumps forward / negative vector jumps backwards total milliseconds
        if (jumpVectorMSecs == 0) return;

<<<<<<< HEAD
        if(jumpVectorMSecs < 0) {
            hud.showSkipBack();
        } else {
            hud.showSkipForward();
        }
        int newPosition = mp.getCurrentPosition() + jumpVectorMSecs;
        if (jumpVectorMSecs > 0 && newPosition > mp.getDuration()) {
            mp.seekTo(mp.getDuration());
        } else if (jumpVectorMSecs < 0 && newPosition < 0) {
            mp.seekTo(0);
=======
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
>>>>>>> jump-cut
        } else {
            if (currentFile == null) {
                if (jumpVectorMSecs > 0) {
                    //TODO: show icon saying this operation is not allowed
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
                if (videos.getTail().getFilePath().equals(fileTimeToSeek.getFilePath())) {
                    cutTail();
                    fileTimeToSeek = videos.getFileFromTime(videos.getTime(fileTimeToSeek));
                }
            }
        }

        seekToFileTime(fileTimeToSeek);
    }

    private void cutTail() {
        rs.stopRecording();
        String newFilePath = rs.startRecord(null); // start recording with an automatically generated file name
        videos.setTailDuration(getDuration(videos.getTail().getFilePath()));
        videos.addFile(newFilePath, -1);
    }

    private void seekToFileTime(FileTimeTuple fileTime) {
        FileEntry file = videos.getFileEntry(fileTime.getFilePath());
        if (videos.numBreaksAfter(file) > 0) {
            long mSecsFromBeginning = videos.getTime(fileTime);
            videos.flattenFile();
            FileTimeTuple fileTimeToSeek = videos.getFileFromTime(mSecsFromBeginning);
            seekToFileTime(fileTimeToSeek);
            return;
        }

        String filePathToSeek = fileTime.getFilePath();
        Uri newUri = Uri.fromFile(new File(filePathToSeek));
        if (!newUri.equals(mediaUri)) {
            setMediaSource(newUri, false);
            int timeToSeek = (int)fileTime.getTimeInFile();
            mp.seekTo(timeToSeek);
            if (timeToSeek > mp.getDuration()) {
                //TODO: show icon saying this operation is not allowed
            }
            currentFile = videos.getFileEntry(filePathToSeek);
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

                        mp.seekTo(currentTime);
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
//        final String prevFile = rs.getCurrentFile();
//        if(prevFile != null)
//        Log.d("FILE", prevFile);
//        if (prevFile != null && !this.currentFile.equals(prevFile)) {
//            this.setMediaSource(android.net.Uri.parse(prevFile), false);
//            this.playReverseFromEnd(200);
//        } else {
//            this.rewind(speed);
//        }
        CompositeFile file = new CompositeFile(true);
        File a = new File ("/sdcard/test1.mp4");
        File b = new File ("/sdcard/test2.mp4");
        File c = new File ("/sdcard/dos.mp4");
        file.addFile(a.getPath(), -1);
        file.setTailDuration(this.getDuration(a.getPath()));
        file.addFile(b.getPath(), -1);
        file.setTailDuration(this.getDuration(b.getPath()));
        file.addFile(c.getPath(), -1);
        file.setTailDuration(this.getDuration(c.getPath()));

        Log.d("CompositeFile","File for time 1350: "+file.getFileFromTime(1350).getFilePath());
        Log.d("CompositeFile","In time: "+file.getFileFromTime(1350).getTimeInFile());

        file.flattenFile();

        Log.d("CompositeFile","File for time 34816: "+file.getFileFromTime(34816).getFilePath());
        Log.d("CompositeFile","In time: "+file.getFileFromTime(34816).getTimeInFile());

        Log.d("CompositeFile","File for time 34815: "+file.getFileFromTime(34815).getFilePath());
        Log.d("CompositeFile","In time: "+file.getFileFromTime(34815).getTimeInFile());


        Log.d("CompositeFile","jump for time 1000 from test1 1000: "+file.getFileFromJump(1000, 1000, "/sdcard/test1-test2.mp4").getFilePath());
        Log.d("CompositeFile","In time: "+file.getFileFromJump(1000, 1000, "/sdcard/test1-test2.mp4").getTimeInFile());

        Log.d("CompositeFile","jump for time -1 from test2 34816: "+file.getFileFromJump(1, 0, "/sdcard/test1-test2.mp4").getFilePath());
        Log.d("CompositeFile","In time: "+file.getFileFromJump(1, 0, "/sdcard/test1-test2.mp4").getTimeInFile());
        file.print();
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
        seekBar.setMax(100);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(10,700);
        seekBar.setLayoutParams(params);
        seekBar.setProgress(1);
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
        relative = (RelativeLayout) v.findViewById(R.id.relative);
        relative.addView(new View(this.getActivity()));
        relative.addView(hud);
        relative.addView(seekBar);
        holder = surfaceView.getHolder();

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


        handler.post(new Runnable() {

            public void run() {


            }
        });

        mediaPlayer.start();
        Utils.eventBusPost(new MediaPlayerReadyEvent());
    }


    @Override
    public boolean onGesture(Gesture gesture) {
        Utils.eventBusPost(new MediaGestureEvent(gesture));
        if(!gesture.name().equals("SWIPE_DOWN"))
        controller.show(200);
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
<<<<<<< HEAD
        Log.d("HERE","on Completion called");
        hud.showStop();
//        String nextFile = rs.getNextFile();
//        if(nextFile != null)
//        Log.d("HERE",nextFile);
//        if (nextFile == null) {
//            return;
//        } else {
//            this.setMediaSource(android.net.Uri.parse(nextFile), false);
//        }
=======
>>>>>>> jump-cut
    }

    public long getDuration(String path) {
        MediaPlayer mp = MediaPlayer.create(this.getActivity(), Uri.parse(path));
        return mp.getDuration();
    }

    public void setServiceHandle(MediaRecordingService service) {
        rs = service;
    }


    public void start() {
        mp.start();
    }

    public void pause() {
        mp.pause();
    }

    public int getDuration() {
        return mp.getDuration();
    }

    public int getCurrentPosition() {
        return mp.getCurrentPosition();
    }

    public void seekTo(int i) {
        mp.seekTo(i);
    }

    public boolean isPlaying() {
        return mp.isPlaying();
    }

    public int getBufferPercentage() {
        return 0;
    }

    public boolean canPause() {
        return false;
    }

    public boolean canSeekBackward() {
        return false;
    }

    public boolean canSeekForward() {
        return false;
    }
    public int getAudioSessionId(){return 0;}
}
