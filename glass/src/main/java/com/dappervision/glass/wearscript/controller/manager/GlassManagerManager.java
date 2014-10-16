package com.dappervision.glass.wearscript.controller.manager;

import android.content.pm.PackageManager;

import com.dappervision.wearscript.BackgroundService;
import com.dappervision.wearscript.managers.EyeManager;
import com.dappervision.wearscript.managers.LiveCardManager;
import com.dappervision.wearscript.managers.Manager;
import com.dappervision.wearscript.managers.ManagerManager;

public class GlassManagerManager extends ManagerManager {
    private static GlassManagerManager singleton;

    public static GlassManagerManager get() {
        if (singleton != null) {
            return singleton;
        }
        singleton = new GlassManagerManager();
        return singleton;
    }

    public static boolean hasManager(Class<? extends Manager> c) {
        return get().get(c) != null;
    }

    @Override
    public void newManagers(BackgroundService bs) {
        super.newManagers(bs);
        if(bs.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY) || bs.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            add(new CameraManager(bs));
        }
        add(new PicarusManager(bs));
        add(new WarpManager(bs));
        add(new LiveCardManager(bs));
        add(new CardTreeManager(bs));
        add(new EyeManager(bs));
        add(new AudioManager(bs));

    }
}
