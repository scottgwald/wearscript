package com.dappervision.android.wearscript.controller;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.crittercism.app.Crittercism;
import com.crittercism.app.CrittercismConfig;
import com.dappervision.android.wearscript.BuildConfig;
import com.dappervision.android.wearscript.MobileWearScriptInfo;
import com.dappervision.wearscript.launcher.MainActivity;
import com.dappervision.wearscript.launcher.WearScriptInfo;

public class MobileMainActivity extends MainActivity {

    @Override
    protected Fragment createFragment() {
        return ScriptListFragment.newInstance();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(!BuildConfig.DEBUG) {
            CrittercismConfig config = new CrittercismConfig();
            config.setLogcatReportingEnabled(true);
            Crittercism.initialize(getApplicationContext(), "544090b4466eda745b000005", config);
        }
    }

    protected WearScriptInfo buildInfoForPath(String name, String filePath) {
        return new MobileWearScriptInfo(this, name, filePath);
    }

    @Override
    public void onScriptSelected(WearScriptInfo scriptInfo) {
        startActivity(scriptInfo.getIntent());
    }
}
