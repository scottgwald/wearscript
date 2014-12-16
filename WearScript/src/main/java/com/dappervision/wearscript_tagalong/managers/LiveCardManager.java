package com.dappervision.wearscript_tagalong.managers;

import android.util.Log;

import com.dappervision.wearscript_tagalong.BackgroundService;
import com.dappervision.wearscript_tagalong.events.LiveCardAddItemsEvent;
import com.dappervision.wearscript_tagalong.events.LiveCardMenuSelectedEvent;
import com.dappervision.wearscript_tagalong.events.LiveCardSetMenuEvent;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.util.ArrayList;

public class LiveCardManager extends Manager{
    private static final String TAG = "LiveCardManager";
    private ArrayList<String> menu;

    public LiveCardManager(BackgroundService service) {
        super(service);
        reset();
    }

    @Override
    public void reset() {
        super.reset();
        menu = new ArrayList<String>();
    }

    public void onEventBackgroundThread(LiveCardMenuSelectedEvent event) {
        makeCall("item:" + event.getPosition(), "");
        Log.d(TAG, "Position: " + event.getPosition());
    }

    public void onEventBackgroundThread(LiveCardSetMenuEvent event) {
        Log.d(TAG, "SetMenuActivity: " + event.getMenu());
        JSONArray data = (JSONArray)JSONValue.parse(event.getMenu());
        reset();
        for (Object item: data) {
            JSONObject itemJS = (JSONObject)item;
            registerCallback("item:" + menu.size(), (String)itemJS.get("callback"));
            menu.add((String)itemJS.get("label"));
        }
    }

    public void onEvent(LiveCardAddItemsEvent event) {
        Log.d(TAG, "AddMenuItems");
        event.getActivity().addMenuItems(menu);
    }
}
