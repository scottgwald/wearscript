package com.dappervision.wearscript.record;

import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder.AudioSource;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class AudioRecordThread extends Thread {

    private static final String LOG_TAG = "AudioRecordThread";

    public static final int RECORDER_SAMPLERATE = 8000;
    public static final int ENCODING_TYPE = AudioFormat.ENCODING_PCM_16BIT;
    public static final int WAV_HEADER_LENGTH = 44;
    public static final String FILEPATH = "filepath";
    private final int CHANNEL_TYPE = AudioFormat.CHANNEL_IN_MONO;
    private final int NUM_CHANNELS = 1;
    private byte BITS_PER_SAMPLE = 16;
    private final int AUDIO_SOURCE = AudioSource.MIC;
    private final int BYTE_RATE = RECORDER_SAMPLERATE * NUM_CHANNELS * (BITS_PER_SAMPLE / 8);

    public static final String directory = Environment.getExternalStorageDirectory() + File.separator + "wearscript";
    public static final String directoryAudio = directory + File.separator+"audio";

    private final int bufferSize = 160; //Each buffer holds 1/100th of a second.
    private boolean pollingBuffer = false;

    AudioRecord recorder = null;
    FileOutputStream os = null;
    String nextFilePath;

    /**
     * Give the thread high priority so that it's not cancelled unexpectedly, and start it
     */

    private ArrayList<byte[]> buffers;
    private byte[] totalBuffer;

    private AudioTrack audioTrack;

    Context context;

    public AudioRecordThread(Context context, String filePath) {
        this.context = context;
        writeWavHeader(filePath);

        audioTrack = new AudioTrack(
                AudioManager.STREAM_VOICE_CALL,
                AudioRecordThread.RECORDER_SAMPLERATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioRecordThread.ENCODING_TYPE,
                AudioTrack.getMinBufferSize(
                        AudioRecordThread.RECORDER_SAMPLERATE,
                        AudioFormat.CHANNEL_OUT_MONO,
                        AudioRecordThread.ENCODING_TYPE),
                AudioTrack.MODE_STREAM);
        audioTrack.play();
    }

    @Override
    public void run() {
        Log.d(LOG_TAG, "Running Audio Thread");

        buffers  = new ArrayList<byte[]>();
        int N = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE, CHANNEL_TYPE,ENCODING_TYPE);
        Log.d(LOG_TAG, "" + N);
        try {
            recorder = new AudioRecord(AUDIO_SOURCE, RECORDER_SAMPLERATE, CHANNEL_TYPE, ENCODING_TYPE, N*10);
        }
        catch (Throwable e) {
            Log.d(LOG_TAG, e.toString());
            return;
        }
        recorder.startRecording();

        try {
            while (!interrupted()) {
                synchronized (this) {
                    buffers.add(new byte[bufferSize]);
                    recorder.read(buffers.get(buffers.size() - 1), 0, bufferSize);
                    Log.d(LOG_TAG, "writing to audioTrack in read loop: " + bufferSize + " bytes");
                    audioTrack.write(buffers.get(buffers.size() - 1), 0, bufferSize);
                }
                Thread.sleep(5);
                yield();
            }
        }
        catch (Throwable x) {
            Log.d(LOG_TAG, "Error reading voice audio", x);
        }
        /*
         * Frees the thread's resources after the loop completes so that it can be run again
         */
        finally {
            recorder.stop();
            recorder.release();
            context.stopService(new Intent(context, AudioRecorder.class));
            Log.d(LOG_TAG, "Thread Terminated");
            audioTrack.release();
        }
    }

    private void mergeBuffers() {
        Log.d(LOG_TAG, "in mergeBuffers()");
        int i;
        int j;
        int ix = 0;

        ArrayList<byte[]> copy;
        synchronized (this) {
            copy = (ArrayList<byte[]>) buffers.clone();
            buffers = new ArrayList<byte[]>();
        }

        totalBuffer = new byte[copy.size() * bufferSize];
        for (i = 0; i < copy.size(); i++) {
            for(j = 0; j<bufferSize; j++) {
                byte x = copy.get(i)[j];
                totalBuffer[ix] = x;
                ix++;
            }
        }
    }

    private String audioFileName(String fileName) {
        return directoryAudio + File.separator + fileName + ".wav";
    }

    private void writeWavHeader(String filePath) {
        byte header[] = new byte[WAV_HEADER_LENGTH];

        try {
            this.nextFilePath = filePath;
            os = new FileOutputStream(filePath);
            Log.d(LOG_TAG, "file path: " + filePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        /* Test whether Android media player will play back a WAV file whose header indicates the
         * wrong length file! */
        int totalAudioLen = Integer.MAX_VALUE - 36;
        int totalDataLen = Integer.MAX_VALUE;

        header[0] = 'R';  // RIFF/WAVE header
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f';  // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16;  // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1;  // format = 1
        header[21] = 0;
        header[22] = (byte) NUM_CHANNELS;
        header[23] = 0;
        header[24] = (byte) (RECORDER_SAMPLERATE & 0xff);
        header[25] = (byte) ((RECORDER_SAMPLERATE >> 8) & 0xff);
        header[26] = (byte) ((RECORDER_SAMPLERATE >> 16) & 0xff);
        header[27] = (byte) ((RECORDER_SAMPLERATE >> 24) & 0xff);
        header[28] = (byte) (BYTE_RATE & 0xff);
        header[29] = (byte) ((BYTE_RATE >> 8) & 0xff);
        header[30] = (byte) ((BYTE_RATE >> 16) & 0xff);
        header[31] = (byte) ((BYTE_RATE >> 24) & 0xff);
        header[32] = (byte) (NUM_CHANNELS * BITS_PER_SAMPLE / 8);
        header[33] = 0;
        header[34] = BITS_PER_SAMPLE;
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);

        try {
            os.write(header, 0, header.length);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopRecording() {
        Log.d(LOG_TAG, "in stopRecording()");
        writeAudioDataToFile();

        try {
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        interrupt();
    }

    public void writeAudioDataToFile() {
        Log.d(LOG_TAG, "in writeAudioDataToFile()");
        mergeBuffers();
    }
}
