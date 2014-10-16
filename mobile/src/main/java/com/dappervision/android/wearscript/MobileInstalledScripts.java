package com.dappervision.android.wearscript;

import android.content.Context;

import com.dappervision.wearscript.launcher.InstalledScripts;
import com.dappervision.wearscript.launcher.WearScriptInfo;

public class MobileInstalledScripts extends InstalledScripts {
    public MobileInstalledScripts(Context c) {
        super(c);
    }

    @Override
    protected WearScriptInfo getBuilder() {
        return new MobileWearScriptInfo();
    }
}
