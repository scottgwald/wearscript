package com.dappervision.wearscript.managers;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Base64;

import com.dappervision.wearscript.BackgroundService;
import com.dappervision.wearscript.Log;
import com.dappervision.wearscript.Utils;
import com.dappervision.wearscript.events.ActivityResultEvent;
import com.dappervision.wearscript.events.BackgroundSpeechEvent;
import com.dappervision.wearscript.events.SpeechRecognizeEvent;
import com.dappervision.wearscript.events.StartActivityEvent;
import com.dappervision.wearscript.events.SubtitleEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SpeechManager extends Manager {
    static private String SPEECH = "SPEECH";
    static final String TAG = "SpeechManager";
    private SpeechRecognizer recognizer;
    private List list = Collections.synchronizedList(new ArrayList());

    public SpeechManager(BackgroundService service) {
        super(service);
        recognizer = SpeechRecognizer.createSpeechRecognizer(service.getBaseContext());
        recognizer.setRecognitionListener(new SpeechListener());
        reset();
    }

    public void onEvent(ActivityResultEvent event) {
        int requestCode = event.getRequestCode(), resultCode = event.getResultCode();
        Intent intent = event.getIntent();
        if (requestCode == 1002) {
            Log.d(TAG, "Spoken Text Result");
            if (resultCode == Activity.RESULT_OK) {
                List<String> results = intent.getStringArrayListExtra(
                        RecognizerIntent.EXTRA_RESULTS);
                String spokenText = results.get(0);
                Log.d(TAG, "Spoken Text: " + spokenText);
                spokenText = Base64.encodeToString(spokenText.getBytes(), Base64.NO_WRAP);
                makeCall(SPEECH, String.format("\"%s\"", spokenText));
            } else if (resultCode == Activity.RESULT_CANCELED) {

            }
        }
    }

    public void onEventMainThread(BackgroundSpeechEvent e) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, "voice.recognition.test");
        recognizer.startListening(intent);
    }

    public void onEventMainThread(SubtitleEvent e) {
        Utils.eventBusPost(new BackgroundSpeechEvent(""));
        // subtitle file stuff here
    }

    public void onEvent(SpeechRecognizeEvent e) {
        registerCallback(SPEECH, e.getCallback());
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, e.getPrompt());
        Utils.eventBusPost(new StartActivityEvent(intent, 1002));
    }


    class SpeechListener implements RecognitionListener {
        public SpeechListener() {}

        public SpeechListener(String filePath, int millis) {
            startSubtitleFile(filePath, millis);
        }

        public void startSubtitleFile(String filePath, int millis) {
            throw new UnsupportedOperationException("Not yet implemented");
        }

        static final String TAG = "SpeechListener";

        public void onReadyForSpeech(Bundle params) {
            Log.d(TAG, "onReadyForSpeech");
        }

        public void onBeginningOfSpeech() {
            Log.d(TAG, "onBeginningOfSpeech");
        }

        public void onRmsChanged(float rmsdB) {}

        public void onBufferReceived(byte[] buffer) {
            Log.d(TAG, "onBufferReceived");
        }

        public void onEndOfSpeech() {
            Log.d(TAG, "onEndofSpeech");
        }

        public void onError(int error) {}

        public void onResults(Bundle results) {
            ArrayList<String> data = results
                    .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            Log.d(TAG, "onResults: " + data.get(0));
            String spokenText = data.get(0);
            spokenText = Base64.encodeToString(spokenText.getBytes(), Base64.NO_WRAP);
            SpeechManager.this.makeCall("finalResult", String.format("\"%s\"", spokenText));
        }

        public void onPartialResults(Bundle partialResults) {
            ArrayList<String> data = partialResults
                    .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            String spokenText = data.get(0);
            spokenText = Base64.encodeToString(spokenText.getBytes(), Base64.NO_WRAP);
            SpeechManager.this.makeCall("partialResult", String.format("\"%s\"", spokenText));
        }

        public void onEvent(int eventType, Bundle params) {
            Log.d(TAG, "onEvent " + eventType);
        }
    }
}
