package com.dappervision.wearscript_tagalong.managers;

import android.app.Activity;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.util.Base64;

import com.dappervision.wearscript_tagalong.BackgroundService;
import com.dappervision.wearscript_tagalong.Log;
import com.dappervision.wearscript_tagalong.Utils;
import com.dappervision.wearscript_tagalong.events.ActivityResultEvent;
import com.dappervision.wearscript_tagalong.events.SpeechRecognizeEvent;
import com.dappervision.wearscript_tagalong.events.StartActivityEvent;

import java.util.List;

public class SpeechManager extends Manager {
    static private String SPEECH = "SPEECH";

    public SpeechManager(BackgroundService service) {
        super(service);
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

    public void onEvent(SpeechRecognizeEvent e) {
        registerCallback(SPEECH, e.getCallback());
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, e.getPrompt());
        Utils.eventBusPost(new StartActivityEvent(intent, 1002));
    }
}
