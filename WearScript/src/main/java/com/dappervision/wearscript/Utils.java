package com.dappervision.wearscript;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.speech.tts.TextToSpeech;

import com.dappervision.wearscript.record.AudioRecordThread;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Locale;

import de.greenrobot.event.EventBus;

public class Utils {
    protected static String TAG = "WearScript:Utils";
    // setting this true will cause MainActivity to try to load
    // assets/DBG_GIST_NAME/glass.html on start
    private static final boolean DBG_PKG = false;
    private static final String DBG_GIST_NAME = "0000";

    public static String SaveData(byte[] data, String path, boolean timestamp, String suffix) {
        try {
            try {
                // TODO(brandyn): Ensure that suffix can't be modified to get out of the directory
                if (suffix.contains("/") || suffix.contains("\\")) {
                    Log.e(TAG, "Suffix contains invalid character: " + suffix);
                    return null;
                }
                File dir = new File(dataPath() + path);
                dir.mkdirs();
                File file;
                if (timestamp)
                    file = new File(dir, Long.toString(System.currentTimeMillis()) + suffix);
                else
                    file = new File(dir, suffix);
                Log.d(TAG, "Lifecycle: SaveData: " + file.getAbsolutePath());
                FileOutputStream outputStream = new FileOutputStream(file);
                outputStream.write(data);
                outputStream.close();
                return file.getAbsolutePath();
            } catch (Exception e) {
                return null;
            }
        } catch (Exception e) {
            Log.e("SaveData", "Bad disc");
            return null;
        }
    }

    static public String dataPath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath() + "/wearscript/";
    }

    static public byte[] LoadFile(File file) {
        try {
            try {
                Log.i(TAG, "LoadFile: " + file.getAbsolutePath());
                FileInputStream inputStream = new FileInputStream(file);
                byte[] data = new byte[(int) file.length()];
                inputStream.read(data);
                inputStream.close();
                return data;
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
                return null;
            }
        } catch (Exception e) {
            Log.e(TAG, "Bad file read");
            return null;
        }
    }

    public static String getPackageGist(Context context) {
        if (DBG_PKG) return DBG_GIST_NAME;
        String gistId;
        String packageName = context.getPackageName();
        String[] nameComponents = packageName.split("\\.");
        try {
            gistId = nameComponents[1].split("_")[1];
        } catch (NullPointerException e) {
            return null;
        } catch (ArrayIndexOutOfBoundsException e) {
            return null;
        }
        return gistId;
    }

    static public byte[] LoadData(String path, String suffix) {
        return LoadFile(new File(new File(dataPath() + path), suffix));
    }

    public static EventBus getEventBus() {
        return EventBus.getDefault();
    }

    public static void eventBusPost(Object event) {
        long startTime = System.nanoTime();
        getEventBus().post(event);
        Log.d(TAG, "Event: " + event.getClass().getName() + " Time: " + (System.nanoTime() - startTime) / 1000000000.);
    }

    public static boolean setupTTS(Context context, TextToSpeech tts) {
        Log.i(TAG, "TTS initialized");
        if (HardwareDetector.isGlass) {
            //The TTS engine works almost instantly on Glass, and is always the right language. No need to try and configure.
            return true;
        }
        Locale userLocale = Locale.ENGLISH;
        int result = tts.isLanguageAvailable(userLocale);
        if (result == TextToSpeech.LANG_AVAILABLE) {
            result = tts.setLanguage(userLocale);
            if (result == TextToSpeech.SUCCESS) {
                Log.i(TAG, "TTS language set");
                return true;
            } else {
                Log.w(TAG, "TTS language failed " + result);
                return false;
            }
        } else if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            Intent installIntent = new Intent();
            installIntent.setAction(
                    TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
            context.startActivity(installIntent);
            return true;
        } else {
            Log.e(TAG, "User Locale not available for TTS: " + result);
            return false;
        }
    }

    public static class AudioMerger {
        public static boolean merge(File file1, File file2, File output) {
            byte[] file1contents, file2contents;
            try {
                file1contents = readAllBytes(file1);
                file2contents = readAllBytes(file2);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }

            byte[] outputContents = new byte[file1contents.length + file2contents.length - AudioRecordThread.WAV_HEADER_LENGTH];
            System.arraycopy(file1contents, 0, outputContents, 0, file1contents.length);
            System.arraycopy(file2contents, AudioRecordThread.WAV_HEADER_LENGTH,
                    outputContents, file1contents.length,
                    file2contents.length - AudioRecordThread.WAV_HEADER_LENGTH);
            try {
                new FileOutputStream(output).write(outputContents);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }

        public static byte[] readAllBytes(File file) throws IOException {
            RandomAccessFile f = new RandomAccessFile(file, "r");
            byte[] b = new byte[(int)f.length()];
            f.read(b);
            return b;
        }
    }
}
