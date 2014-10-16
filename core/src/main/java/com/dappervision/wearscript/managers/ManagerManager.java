package com.dappervision.wearscript.managers;

import android.content.pm.PackageManager;

import com.dappervision.wearscript.BackgroundService;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class ManagerManager {

    Map<String, Manager> managers;

    protected ManagerManager() {
        managers = new ConcurrentHashMap<String, Manager>();
    }

    public void newManagers(BackgroundService bs) {
        add(new WifiManager(bs));
        add(new SpeechManager(bs));
        add(new MyoManager(bs));
        add(new ConnectionManager(bs));
        add(new OpenCVManager(bs));
        add(new DataManager(bs));

        //Really just FEATURE_CAMERA_ANY should work, but someone is a dumb head and broke Android.
        if(bs.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY) || bs.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            add(new BarcodeManager(bs));
        }

        if(bs.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)) {
            add(new BluetoothManager(bs));
        }

        if (bs.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)){
            add(new BluetoothLEManager(bs));
        }
    }

    public void add(Manager manager) {
        String name = manager.getClass().getName();
        Manager old = managers.remove(name);
        if (old != null)
            old.shutdown();
        managers.put(name, manager);
    }

    public Manager remove(Class<? extends Manager> manager) {
        String name = manager.getName();
        return managers.remove(name);
    }

    public Manager get(Class<? extends Manager> c) {
        return managers.get(c.getName());
    }

    public void resetAll() {
        for (Manager m : managers.values()) {
            m.reset();
        }
    }

    public void shutdownAll() {
        for (String name : managers.keySet()) {
            Manager m = managers.remove(name);
            m.shutdown();
        }
    }
}
