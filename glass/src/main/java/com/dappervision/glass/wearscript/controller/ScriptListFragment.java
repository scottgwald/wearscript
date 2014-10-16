package com.dappervision.glass.wearscript.controller;

import android.widget.ListAdapter;

import com.dappervision.glass.wearscript.WearScriptsCardAdapter;

public class ScriptListFragment extends com.dappervision.wearscript.launcher.ScriptListFragment {
    public static ScriptListFragment newInstance() {
        ScriptListFragment fragment = new ScriptListFragment();
        return fragment;
    }

    @Override
    public ListAdapter buildListAdapter() {
        return new WearScriptsCardAdapter(this, mInstalledScripts);
    }
}
