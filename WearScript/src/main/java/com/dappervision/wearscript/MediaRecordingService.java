package com.dappervision.wearscript;

import android.app.Service;
import android.content.Intent;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.view.SurfaceView;

import com.dappervision.wearscript.events.MediaPauseEvent;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MediaRecordingService extends Service {
    private static final String TAG = "MediaRecordingService";
    private final IBinder mBinder = new MediaBinder();
    private int maximumWaitTimeForCamera = 5000;
    private Camera camera;
    private MediaRecorder mediaRecorder;
    private SurfaceView dummy;
    private String filePath;
    private long currentRecordingStartTimeMillis;
    private boolean recordingVideo;

    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        android.util.Log.d(TAG, "RecordService created");
        super.onCreate();
        Utils.getEventBus().register(this);
    }

    @Override
    public int onStartCommand(Intent i, int z, int y) {
        camera = getCameraInstanceRetry();
        return super.onStartCommand(i, z, y);
    }

    public String startRecord(boolean video, String path) {
        if (path == null) {
            this.generateOutputMediaFile(video);
        } else {
            filePath = path;
        }
        recordingVideo = video;
        this.startRecording(video);
        return filePath;
    }

    public void onEvent(MediaPauseEvent e) {
        this.stopRecording();
    }

    private boolean prepareVideoRecorder() {
        if( camera == null) {  //remove maybe?
            camera = getCameraInstanceRetry();
        }
        try {
            camera.stopPreview();
            camera.setPreviewDisplay(null);
        } catch (java.io.IOException ioe) {
            android.util.Log.d(TAG, "IOException nullifying preview display: " + ioe.getMessage());
        }
        camera.unlock();

        mediaRecorder = new MediaRecorder();
        mediaRecorder.setCamera(camera);
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        int profileInt = CamcorderProfile.QUALITY_480P;
        android.util.Log.v(TAG, "Checking for profile: " + CamcorderProfile.hasProfile(profileInt));
        CamcorderProfile profile = CamcorderProfile.get(profileInt);
        mediaRecorder.setOutputFormat(profile.fileFormat);
        mediaRecorder.setVideoSize(profile.videoFrameWidth, profile.videoFrameHeight);
        mediaRecorder.setVideoEncoder(profile.videoCodec);
        Log.d("AUDIO",Integer.toString(profile.audioBitRate));
        Log.d("AUDIO",Integer.toString(profile.audioSampleRate));

        mediaRecorder.setAudioEncodingBitRate(32000);
        mediaRecorder.setAudioChannels(profile.audioChannels);
        mediaRecorder.setAudioSamplingRate(16000);  //8000, 11025, 16000, 22050, 44100
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mediaRecorder.setOutputFile(filePath); //must get argument from somewhere, intent maybe?
        mediaRecorder.setPreviewDisplay(dummy.getHolder().getSurface());

        try {
            mediaRecorder.prepare();
        } catch (IllegalStateException e) {
            android.util.Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }
        return true;
    }

    private void startRecording(boolean video) {

        Log.d(TAG, "startRecording()");

        if (mediaRecorder != null) {
            this.releaseMediaRecorder();
        }

        if (video) {
            prepareVideoRecorder();
            mediaRecorder.start();
        } else {
            throw new UnsupportedOperationException("Audio recording not yet implemented");
            //TODO (Blake): implement audio recording
        }

        setCurrentRecordingStartTimeMillis(System.currentTimeMillis());
    }

    public void stopRecording() {
        Log.v(TAG, "Stopping recording.");
        if (recordingVideo) {
            if (mediaRecorder != null)
                mediaRecorder.stop();
            releaseMediaRecorder();
            releaseCamera();
        } else {
            throw new UnsupportedOperationException("Audio recording not yet implemented");
        }
    }


    private void releaseCamera() {
        if (this.camera != null) {
            this.camera.stopPreview();
            this.camera.release();
            this.camera = null;
        }
    }

    private void releaseMediaRecorder() {
        if (mediaRecorder != null) {
            mediaRecorder.reset();
            mediaRecorder.release();
            mediaRecorder = null;
        }
    }

    private Camera getCameraInstanceRetry() {
        Camera c = null;
        Log.v(TAG, "getTheCamera");
        // keep trying to acquire the camera until "maximumWaitTimeForCamera" seconds have passed
        boolean acquiredCam = false;
        int timePassed = 0;
        while (!acquiredCam && timePassed < maximumWaitTimeForCamera) {
            try {
                c = Camera.open();
                Log.v(TAG, "acquired the camera");
                acquiredCam = true;
                return c;
            } catch (Exception e) {
                Log.e(TAG, "Exception encountered opening camera:" + e.getLocalizedMessage());
            }
            try {
                Thread.sleep(200);
            } catch (InterruptedException ee) {
                Log.e(TAG, "Exception encountered sleeping:" + ee.getLocalizedMessage());
            }
            timePassed += 200;
        }
        return c;
    }

    public long getCurrentRecordingStartTimeMillis() {
        return currentRecordingStartTimeMillis;
    }

    public void setCurrentRecordingStartTimeMillis(long currentRecordingStartTimeMillis) {
        this.currentRecordingStartTimeMillis = currentRecordingStartTimeMillis;
    }

    public class MediaBinder extends Binder {
        public MediaRecordingService getService() {
            return MediaRecordingService.this;
        }
    }

    public void setSurfaceView(SurfaceView sv) {
        dummy = sv;
        if (camera == null) {
            camera = getCameraInstanceRetry();
        }

        try {
            camera.setPreviewDisplay(sv.getHolder());
            Camera.Parameters params = camera.getParameters();
            params.setPreviewFormat(ImageFormat.NV21);
            params.setPreviewSize(640, 480);
            List<String> FocusModes = params.getSupportedFocusModes();
            if (FocusModes != null && FocusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
            }
            params.setPreviewFpsRange(10000, 10000);
            camera.setParameters(params);
            dummy.getHolder().setFixedSize(640, 480); //was 360
        } catch (IOException e) {
            android.util.Log.d(TAG, "Error setting camera preview: " + e.getMessage());
        } catch (Throwable tr) {
            android.util.Log.e(TAG, "OH. MY God. Throwable. ", tr);
            camera.release();
        }
    }

    /**
     * Create a File for saving an image or video
     */
    private void generateOutputMediaFile(boolean video) {
        File mediaStorageDir = new File("/sdcard/", "wearscript_video");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(TAG, "failed to create directory");
            }
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        filePath = mediaStorageDir.getPath() + File.separator + timeStamp + (video ? ".mp4" : ".wav");
        Log.v(TAG, "Output file: " + filePath);
    }

    public String getFilePath() {
        return filePath;
    }

    public boolean getRecordingVideo() {
        return recordingVideo;
    }
}
