package com.dappervision.android.wearscript.controller;

import android.widget.ListAdapter;

import com.dappervision.android.wearscript.WearScriptsAdapter;

public class ScriptListFragment extends com.dappervision.wearscript.launcher.ScriptListFragment {
    public static ScriptListFragment newInstance() {
        ScriptListFragment fragment = new ScriptListFragment();
        return fragment;
    }

    @Override
    public ListAdapter buildListAdapter() {
        return new WearScriptsAdapter(this, mInstalledScripts);
    }
}
