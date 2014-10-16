package com.dappervision.wearscript;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

public class HardwareDetector {
    public static final boolean isGlass = Build.PRODUCT.equals("glass_1");
    public static final boolean hasGDK = isGlass;

    public static String type(Context context) {
        if(isGlass)
            return "glass";
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_TELEPHONY)) {
            return "phone";
        } else if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_TOUCHSCREEN)) {
            return "tablet";
        } else if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_TELEVISION)){
            return "tv";
        }
        return "device";
    }
}
