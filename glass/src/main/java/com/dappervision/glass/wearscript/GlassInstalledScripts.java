package com.dappervision.glass.wearscript;

import android.content.Context;

import com.dappervision.wearscript.launcher.InstalledScripts;
import com.dappervision.wearscript.launcher.WearScriptInfo;

public class GlassInstalledScripts extends InstalledScripts {
    public GlassInstalledScripts(Context c) {
        super(c);
    }

    @Override
    protected WearScriptInfo getBuilder() {
        return new GlassWearScriptInfo();
    }
}
