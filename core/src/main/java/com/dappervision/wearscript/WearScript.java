package com.dappervision.wearscript;

import android.util.Base64;
import android.webkit.JavascriptInterface;

import com.dappervision.wearscript.events.ActivityEvent;
import com.dappervision.wearscript.events.BluetoothBondEvent;
import com.dappervision.wearscript.events.BluetoothModeEvent;
import com.dappervision.wearscript.events.BluetoothWriteEvent;
import com.dappervision.wearscript.events.CallbackRegistration;
import com.dappervision.wearscript.events.CardTreeEvent;
import com.dappervision.wearscript.events.ChannelSubscribeEvent;
import com.dappervision.wearscript.events.ChannelUnsubscribeEvent;
import com.dappervision.wearscript.events.ControlEvent;
import com.dappervision.wearscript.events.DataLogEvent;
import com.dappervision.wearscript.events.GistSyncEvent;
import com.dappervision.wearscript.events.JsCall;
import com.dappervision.wearscript.events.LiveCardEvent;
import com.dappervision.wearscript.events.LiveCardSetMenuEvent;
import com.dappervision.wearscript.events.MediaActionEvent;
import com.dappervision.wearscript.events.MediaEvent;
import com.dappervision.wearscript.events.NotificationEvent;
import com.dappervision.wearscript.events.PebbleMessageEvent;
import com.dappervision.wearscript.events.PicarusBenchmarkEvent;
import com.dappervision.wearscript.events.PicarusModelCreateEvent;
import com.dappervision.wearscript.events.PicarusModelProcessEvent;
import com.dappervision.wearscript.events.PicarusModelProcessStreamEvent;
import com.dappervision.wearscript.events.PicarusModelProcessWarpEvent;
import com.dappervision.wearscript.events.SaveAudioEvent;
import com.dappervision.wearscript.events.SayEvent;
import com.dappervision.wearscript.events.ScreenEvent;
import com.dappervision.wearscript.events.SendEvent;
import com.dappervision.wearscript.events.SendSubEvent;
import com.dappervision.wearscript.events.SensorJSEvent;
import com.dappervision.wearscript.events.ServerConnectEvent;
import com.dappervision.wearscript.events.ShutdownEvent;
import com.dappervision.wearscript.events.SoundEvent;
import com.dappervision.wearscript.events.SpeechRecognizeEvent;
import com.dappervision.wearscript.events.WarpSetAnnotationEvent;
import com.dappervision.wearscript.events.WarpSetupHomographyEvent;
import com.dappervision.wearscript.events.WifiEvent;
import com.dappervision.wearscript.events.WifiScanEvent;
import com.dappervision.wearscript.managers.AudioManager;
import com.dappervision.wearscript.managers.BarcodeManager;
import com.dappervision.wearscript.managers.BluetoothLEManager;
import com.dappervision.wearscript.managers.BluetoothManager;
import com.dappervision.wearscript.managers.ConnectionManager;
import com.dappervision.wearscript.managers.EyeManager;
import com.dappervision.wearscript.managers.MyoManager;
import com.dappervision.wearscript.managers.IBeaconManager;
import com.dappervision.wearscript.managers.OpenCVManager;
import com.dappervision.wearscript.managers.PebbleManager;
import com.dappervision.wearscript.managers.WifiManager;

import org.json.simple.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.Buffer;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;

public abstract class WearScript {
    protected BackgroundService bs;
    protected String TAG = "WearScript";
    protected TreeMap<String, Integer> sensors;
    protected String sensorsJS;
    protected List<String> touchGesturesList;
    protected List<String> pebbleGesturesList;
    protected List<String> myoGesturesList;

    protected WearScript(BackgroundService bs) {
        this.bs = bs;
        this.sensors = new TreeMap<String, Integer>();
        // Sensor Types
        for (SENSOR s : SENSOR.values()) {
            this.sensors.put(s.toString(), s.id());
        }
        this.sensorsJS = (new JSONObject(this.sensors)).toJSONString();
        String[] touchGestures = {"onGesture", "onFingerCountChanged", "onScroll", "onTwoFingerScroll"};
        touchGesturesList = Arrays.asList(touchGestures);

        String[] pebbleGestures = {"onPebbleSingleClick", "onPebbleLongClick", "onPebbleAccelTap"};
        pebbleGesturesList = Arrays.asList(pebbleGestures);

        String[] myoGestures = {MyoManager.ONMYO};
        myoGesturesList = Arrays.asList(myoGestures);
    }

    private String classToChar(Class c) {
        if (c.equals(float.class) || c.equals(int.class))
            return "D";
        else if (c.equals(String.class))
            return "S";
        else if (c.equals(boolean.class))
            return "B";
        else if (Buffer.class.isAssignableFrom(c) || c.equals(Buffer.class))
            return "U";
        else if (c.equals(void.class))
            return "V";
        return "?";
    }

    @JavascriptInterface
    public int sensor(String name) {
        return this.sensors.get(name);
    }



    @JavascriptInterface
    public void saveAudioFile(String path,String fileName, String callback) {
        Utils.eventBusPost(new CallbackRegistration(AudioManager.class, callback).setEvent(AudioManager.SAVE_AUDIO+fileName));
        Utils.eventBusPost(new SaveAudioEvent(path,fileName));
    }

    @JavascriptInterface
    public void playSound(int id) {
        Utils.eventBusPost(new SoundEvent(AudioManager.SOUND,id));
    }
    @JavascriptInterface
    public void pauseSound(int id) {
        Utils.eventBusPost(new SoundEvent(AudioManager.PAUSE,id));
    }
    @JavascriptInterface
    public void stopSound(int id) {
        Utils.eventBusPost(new SoundEvent(AudioManager.STOP,id));
    }

    @JavascriptInterface
    public void shutdown() {
        //Global event
        Utils.getEventBus().post(new ShutdownEvent());
    }

    @JavascriptInterface
    public String sensors() {
        return this.sensorsJS;
    }

    @JavascriptInterface
    public void say(String text) {
        Utils.eventBusPost(new SayEvent(text));
        Log.i(TAG, "say: " + text);
    }

    @JavascriptInterface
    public void serverTimeline(String ti) {
        Log.i(TAG, "timeline");
        Utils.eventBusPost(new SendSubEvent("mirror", ti));
    }

    @JavascriptInterface
    public void sensorOn(int type, double sampleTime) {
        Log.i(TAG, "sensorOn: " + Integer.toString(type));
        Utils.eventBusPost(new SensorJSEvent(type, true, sampleTime, null));
    }

    @JavascriptInterface
    public void sensorOn(int type, double sampleTime, String callback) {
        Log.i(TAG, "sensorOn: " + Integer.toString(type) + " callback: " + callback);
        Utils.eventBusPost(new SensorJSEvent(type, true, sampleTime, callback));
    }

    @JavascriptInterface
    public void log(String msg) {
        //Global event
        Utils.eventBusPost(new SendSubEvent("log", msg));
    }

    @JavascriptInterface
    public void sensorOff(int type) {
        Log.i(TAG, "sensorOff: " + Integer.toString(type));
        Utils.eventBusPost(new SensorJSEvent(type, false));
    }

    @JavascriptInterface
    public void mediaLoad(String uri, boolean looping){
        try {
            Utils.eventBusPost(new MediaEvent(new URI(uri), looping));
        } catch (URISyntaxException e) {
            // TODO(kurtisnelson): Handle
        }
    }

    @JavascriptInterface
    public void mediaPlay(){
        Utils.eventBusPost(new MediaActionEvent("play"));
    }

    @JavascriptInterface
    public void mediaPause(){
        Utils.eventBusPost(new MediaActionEvent("pause"));
    }

    @JavascriptInterface
    public void mediaStop(){
        Utils.eventBusPost(new MediaActionEvent("stop"));
    }

    @JavascriptInterface
    public void serverConnect(String server, String callback) {
        Log.i(TAG, "serverConnect: " + server);
        if (server.equals("{{WSUrl}}"))
            server = bs.getDefaultUrl();
        if (server == null) {
            Log.e(TAG, "Lifecycle: Invalid url provided");
            return;
        }
        try {
            Utils.eventBusPost(new ServerConnectEvent(new URI(server), callback));
        } catch (URISyntaxException e) {
            // TODO(brandyn): Handle
        }
    }

    @JavascriptInterface
    public void displayWebView() {
        Log.i(TAG, "displayWebView");
        Utils.eventBusPost(new ActivityEvent(ActivityEvent.Mode.WEBVIEW));
    }

    @JavascriptInterface
    public void displayWarpView() {
        Log.i(TAG, "displayWarpView");
        Utils.eventBusPost(new ActivityEvent(ActivityEvent.Mode.WARP));
    }

    @JavascriptInterface
    public void displayWarpView(String homography) {
        Log.i(TAG, "displayWarpView");
        Utils.eventBusPost(new WarpSetupHomographyEvent(homography));
        Utils.eventBusPost(new ActivityEvent(ActivityEvent.Mode.WARP));
    }

    @JavascriptInterface
    public void cvInit(String callback) {
        Utils.eventBusPost(new CallbackRegistration(OpenCVManager.class, callback).setEvent(OpenCVManager.LOAD));
    }

    @JavascriptInterface
    public abstract void warpPreviewSamplePlane(String callback);

    @JavascriptInterface
    public abstract void warpPreviewSampleGlass(String callback);

    @JavascriptInterface
    public abstract void warpARTags(String callback);

    @JavascriptInterface
    public abstract void warpGlassToPreviewH(String callback);

    @JavascriptInterface
    public void warpSetOverlay(String image) {
        Utils.eventBusPost(new WarpSetAnnotationEvent(Base64.decode(image, Base64.NO_WRAP)));
    }

    @JavascriptInterface
    public abstract void cameraOff();

    @JavascriptInterface
    public abstract void cameraPhotoData(String callback);

    @JavascriptInterface
    public abstract void cameraPhoto(String callback);

    @JavascriptInterface
    public abstract void cameraPhoto();

    @JavascriptInterface
    public abstract void cameraVideo();

    @JavascriptInterface
    public abstract void cameraVideo(String callback);

    @JavascriptInterface
    public abstract void cameraOn(double imagePeriod, int maxHeight, int maxWidth, boolean background);

    @JavascriptInterface
    public abstract void cameraOn(double imagePeriod, int maxHeight, int maxWidth, boolean background, String callback);

    @JavascriptInterface
    public void activityCreate() {
        Utils.eventBusPost(new ActivityEvent(ActivityEvent.Mode.CREATE));
    }

    @JavascriptInterface
    public void activityDestroy() {
        Utils.eventBusPost(new ActivityEvent(ActivityEvent.Mode.DESTROY));
    }

    @JavascriptInterface
    public void wifiOff() {
        Utils.eventBusPost(new WifiEvent(false));
    }

    @JavascriptInterface
    public void wifiOn() {
        Utils.eventBusPost(new WifiEvent(true));
    }

    @JavascriptInterface
    public void wifiOn(String callback) {
        CallbackRegistration cr = new CallbackRegistration(WifiManager.class, callback);
        cr.setEvent(WifiManager.WIFI);
        Utils.eventBusPost(cr);
        Utils.eventBusPost(new WifiEvent(true));
    }

    @JavascriptInterface
    public void wifiScan() {
        Utils.eventBusPost(new WifiScanEvent());
    }

    @JavascriptInterface
    public void dataLog(boolean local, boolean server, double sensorDelay) {
        Utils.eventBusPost(new DataLogEvent(local, server, sensorDelay));
    }

    @JavascriptInterface
    public boolean scriptVersion(int version) {
        if (version == 1) {
            return false;
        } else {
            Utils.eventBusPost(new SayEvent("Script version incompatible with client"));
            return true;
        }
    }

    @JavascriptInterface
    public void wake() {
        Log.i(TAG, "wake");
        Utils.eventBusPost(new ScreenEvent(true));
    }

    @JavascriptInterface
    public void qr(String cb) {
        Log.i(TAG, "QR");
        Utils.eventBusPost(new CallbackRegistration(BarcodeManager.class, cb).setEvent(BarcodeManager.QR_CODE));
    }

    @JavascriptInterface
    public void subscribe(String name, String cb) {
        Log.i(TAG, "subscribe");
        Utils.eventBusPost(new ChannelSubscribeEvent(name, cb));
    }

    @JavascriptInterface
    public void unsubscribe(String name) {
        Log.i(TAG, "unsubscribe");
        Utils.eventBusPost(new ChannelUnsubscribeEvent(name));
    }

    @JavascriptInterface
    public void publish(String channel, String data) {
        Log.i(TAG, "publish " + channel);
        Utils.eventBusPost(new SendEvent(channel, Base64.decode(data, Base64.NO_WRAP)));
    }

    @JavascriptInterface
    public String echo(String data) {
        Log.i(TAG, "echo");
        return data;
    }

    @JavascriptInterface
    public int echolen(String data) {
        Log.i(TAG, "echolen");
        return data.length();
    }

    @JavascriptInterface
    public void echocall(String data) {
        Log.i(TAG, "echocb");
        Utils.eventBusPost(new JsCall(data));
    }

    @JavascriptInterface
    public void gistSync() {
        Log.i(TAG, "gistSync");
        Utils.eventBusPost(new GistSyncEvent());
    }

    @JavascriptInterface
    public void gestureCallback(String event, String callback) {
        Log.i(TAG, "gestureCallback: " + event + " " + callback);
        Class route = determineGestureRoute(event);

        Utils.eventBusPost(new CallbackRegistration(route, callback).setEvent(event));
    }

    protected Class determineGestureRoute(String event) {
        for (String pebbleGesture : pebbleGesturesList) {
            if (event.startsWith(pebbleGesture)) {
                return PebbleManager.class;
            }
        }

        for (String gesture : myoGesturesList)
            if (event.startsWith(gesture)) {
                return MyoManager.class;
            }
        return EyeManager.class;
    }

    @JavascriptInterface
    public void pebbleSetTitle(String title, boolean clear) {
        Log.i(TAG, "pebbleSetTitle: " + title);
        Utils.eventBusPost(new PebbleMessageEvent("setTitle", title, clear));
    }

    @JavascriptInterface
    public void pebbleSetSubtitle(String subTitle, boolean clear) {
        Log.i(TAG, "pebbleSetSubtitle: " + subTitle);
        Utils.eventBusPost(new PebbleMessageEvent("setSubtitle", subTitle, clear));
    }

    @JavascriptInterface
    public void pebbleSetBody(String body, boolean clear) {
        Log.i(TAG, "pebbleSetSubTitle: " + body);
        Utils.eventBusPost(new PebbleMessageEvent("setBody", body, clear));
    }

    @JavascriptInterface
    public void pebbleVibe(int type) {
        Log.i(TAG, "pebbleVibe: " + type);
        Utils.eventBusPost(new PebbleMessageEvent("vibe", type));
    }

    @JavascriptInterface
    public void speechRecognize(String prompt, String callback) {
        Utils.eventBusPost(new SpeechRecognizeEvent(prompt, callback));
    }

    @JavascriptInterface
    public void liveCardCreate(boolean nonSilent, double period, String menu) {
        requiresGDK();
        Utils.eventBusPost(new LiveCardSetMenuEvent(menu));
        Utils.eventBusPost(new LiveCardEvent(nonSilent, period));
    }

    @JavascriptInterface
    public void liveCardDestroy() {
        requiresGDK();
        Utils.eventBusPost(new LiveCardEvent(false, 0));
    }

    @JavascriptInterface
    public void displayCardTree() {
        Utils.eventBusPost(new ActivityEvent(ActivityEvent.Mode.CARD_TREE));
    }

    @JavascriptInterface
    public void sound(String type) {
        Log.i(TAG, "sound");
        Utils.eventBusPost(new SoundEvent(type));
    }

    @JavascriptInterface
    public void cardTree(String treeJS) {
        requiresGDK();
        Utils.eventBusPost(new CardTreeEvent(treeJS));
    }

    @JavascriptInterface
    public void bluetoothList(String callback) {
        Utils.eventBusPost(new CallbackRegistration(BluetoothManager.class, callback).setEvent(BluetoothManager.LIST));
    }

    @JavascriptInterface
    public void beacon(String range, String enter, String exit) {
        if(range != "null")
            Utils.eventBusPost(new CallbackRegistration(IBeaconManager.class, range).setEvent(IBeaconManager.RANGE_NOTIFICATION));
        if(enter != "null")
            Utils.eventBusPost(new CallbackRegistration(IBeaconManager.class, enter).setEvent(IBeaconManager.ENTER_REGION));
        if(exit != "null")
            Utils.eventBusPost(new CallbackRegistration(IBeaconManager.class, exit).setEvent(IBeaconManager.EXIT_REGION));
    }

    @JavascriptInterface
    public void bluetoothList(String callback, boolean btle) {
        if(!btle) {
            Utils.eventBusPost(new CallbackRegistration(BluetoothManager.class, callback).setEvent(BluetoothManager.LIST));
        }else{
            Utils.eventBusPost(new CallbackRegistration(BluetoothLEManager.class, callback).setEvent(BluetoothLEManager.LIST));
        }
    }

    @JavascriptInterface
    public void bluetoothDiscover(String callback) {
        Utils.eventBusPost(new CallbackRegistration(BluetoothManager.class, callback).setEvent(BluetoothManager.DISCOVERY_START));
    }

    @JavascriptInterface
    public void bluetoothBond(String address, String pin) {
        Utils.eventBusPost(new BluetoothBondEvent(address, pin));
    }

    @JavascriptInterface
    public void bluetoothRead(String device, String callback) {
        Utils.eventBusPost(new CallbackRegistration(BluetoothManager.class, callback).setEvent(BluetoothManager.READ + device));
    }

    @JavascriptInterface
    public void bluetoothLeRead(String device, String callback) {
        Utils.eventBusPost(new CallbackRegistration(BluetoothLEManager.class, callback).setEvent(BluetoothLEManager.READ + device));
    }

    @JavascriptInterface
    public void bluetoothWrite(String address, String data) {
        Utils.eventBusPost(new BluetoothWriteEvent(address, data));
    }

    @JavascriptInterface
    public void bluetoothEnable() {
        Utils.eventBusPost(new BluetoothModeEvent(true));
    }

    @JavascriptInterface
    public void bluetoothDisable() {
        Utils.eventBusPost(new BluetoothModeEvent(false));
    }

    @JavascriptInterface
    public String groupDevice() {
        ConnectionManager cm = (ConnectionManager) bs.getManager(ConnectionManager.class);
        if (cm == null)
            return null;
        return cm.groupDevice();
    }

    @JavascriptInterface
    public String group() {
        ConnectionManager cm = (ConnectionManager) bs.getManager(ConnectionManager.class);
        if (cm == null)
            return null;
        return cm.group();
    }

    @JavascriptInterface
    public String device() {
        ConnectionManager cm = (ConnectionManager) bs.getManager(ConnectionManager.class);
        if (cm == null)
            return null;
        return cm.device();
    }

    @JavascriptInterface
    public void control(String event, boolean adb){
        Utils.eventBusPost(new ControlEvent(event, adb));
    }

    public abstract void picarus(String model, String input, String callback);

    @JavascriptInterface
    public void picarusBenchmark() {
        Utils.eventBusPost(new PicarusBenchmarkEvent());
    }

    @JavascriptInterface
    public void picarusModelCreate(String model, int id, String callback) {
        Utils.eventBusPost(new PicarusModelCreateEvent(Base64.decode(model.getBytes(), Base64.NO_WRAP), id, callback));
    }

    @JavascriptInterface
    public void picarusModelProcess(int id, String input, String callback) {
        Utils.eventBusPost(new PicarusModelProcessEvent(id, Base64.decode(input.getBytes(), Base64.NO_WRAP), callback));
    }

    @JavascriptInterface
    public abstract void picarusModelProcessStream(int id, String callback);

    @JavascriptInterface
    public abstract void picarusModelProcessWarp(int id, String callback);

    @JavascriptInterface
    public void myoPair(String callback) {
        Utils.eventBusPost(new CallbackRegistration(MyoManager.class, callback).setEvent(MyoManager.PAIR));
    }

    protected void requiresGDK() {
        if (HardwareDetector.hasGDK)
            return;
        Utils.eventBusPost(new SendSubEvent("log", "Script requires glass"));
        Utils.eventBusPost(new SayEvent("This script requires Glass"));
        throw new RuntimeException("GDK not available");
    }

    @JavascriptInterface
    public void notify(int id, String title, String text) {
        Log.i(TAG, "wearNotification " + title + " " + text);
        Utils.eventBusPost(new NotificationEvent(id, title, text));
    }

    public static enum SENSOR {
        PEBBLE_ACCELEROMETER("pebbleAccelerometer", -7),
        MYO_GYROSCOPE("myoGyroscope", -6),
        MYO_ACCELEROMETER("myoAccelerometer", -5),
        MYO_ORIENTATION("myoOrientation", -4),
        BATTERY("battery", -3),
        PUPIL("pupil", -2),
        GPS("gps", -1),
        ACCELEROMETER("accelerometer", 1),
        MAGNETIC_FIELD("magneticField", 2),
        ORIENTATION("orientation", 3),
        GYROSCOPE("gyroscope", 4),
        LIGHT("light", 5),
        GRAVITY("gravity", 9),
        LINEAR_ACCELERATION("linearAcceleration", 10),
        ROTATION_VECTOR("rotationVector", 11);

        private final int id;
        private final String name;

        private SENSOR(String name, final int id) {
            this.id = id;
            this.name = name;
        }

        public int id() {
            return id;
        }

        public String toString() {
            return name;
        }
    }
}
