package com.dappervision.android.wearscript.controller.manager;

import com.dappervision.wearscript.managers.Manager;
import com.dappervision.wearscript.managers.ManagerManager;

public class MobileManagerManager extends ManagerManager {
    private static MobileManagerManager singleton;

    public static MobileManagerManager get() {
        if (singleton != null) {
            return singleton;
        }
        singleton = new MobileManagerManager();
        return singleton;
    }

    public static boolean hasManager(Class<? extends Manager> c) {
        return get().get(c) != null;
    }
}
