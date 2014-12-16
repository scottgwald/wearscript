package com.dappervision.wearscript_tagalong;

import android.os.Build;

public class HardwareDetector {
    public static final boolean isGlass = Build.PRODUCT.equals("glass_1");
    public static final boolean hasGDK = isGlass;
}
