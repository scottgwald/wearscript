package com.dappervision.wearscript.record;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.dappervision.wearscript.R;
import com.dappervision.wearscript.Utils;

import java.io.File;
import java.util.ArrayList;

public class AudioMergeActivity extends Activity {
    private static final String TAG = "AudioMergeActivity";

    private AudioRecorder mService;
    private boolean mBound = false;
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            AudioRecorder.LocalBinder binder = (AudioRecorder.LocalBinder) service;
            mService = binder.getService();
            mBound = true;

            int numFiles = 5;
            ArrayList<File> files = new ArrayList<File>();
            for (int i = 0; i < numFiles; ++i) {
                files.add(new File("/sdcard/wearscript/audio/part-" + i + ".wav"));
            }

            String pathOutput = "/sdcard/wearscript/audio/merged1.wav";

            mService.startRecording(files.get(0).toString());

            for (int i = 1; i < numFiles; ++i) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Log.d(TAG, "saving file " + i);
                if (i < numFiles - 1) {
                    mService.saveAndStartNewFile(files.get(i).toString());
                } else {
                    mService.saveFile();
                }
            }

            mService.stopRecording();

            Utils.AudioMerger.merge(files, new File(pathOutput));
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_merge);
        Log.d(TAG, "in onCreate()");
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Bind to service AudioRecorder
        Intent audioIntent = new Intent(this, AudioRecorder.class);
        startService(audioIntent);
        bindService(audioIntent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.audio_merge, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }
}
