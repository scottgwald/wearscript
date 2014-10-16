package com.dappervision.glass.wearscript.controller;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.crittercism.app.Crittercism;
import com.crittercism.app.CrittercismConfig;
import com.dappervision.glass.wearscript.BuildConfig;
import com.dappervision.wearscript.launcher.MainActivity;


public class GlassMainActivity extends MainActivity {

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
            Crittercism.initialize(getApplicationContext(), "53cd76d9bb94751895000002", config);
        }
    }
}
