package com.dappervision.android.wearscript.controller;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.dappervision.android.wearscript.MobileInstalledScripts;
import com.dappervision.android.wearscript.R;
import com.dappervision.android.wearscript.WearScriptsAdapter;
import com.dappervision.wearscript.launcher.InstalledScripts;

public class ScriptListFragment extends com.dappervision.wearscript.launcher.ScriptListFragment {
    public static ScriptListFragment newInstance() {
        return new ScriptListFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_script_list, container, false);
        mAdapterView = (ListView) view.findViewById(R.id.fragment_script_list_list);
        mAdapterView.setAdapter(mListAdapter);
        mAdapterView.setOnItemClickListener(mOnItemClickListener);
        return view;
    }

    @Override
    public WearScriptsAdapter buildListAdapter() {
        return new WearScriptsAdapter(this, mInstalledScripts);
    }

    @Override
    protected InstalledScripts getInstalledScripts() {
        return new MobileInstalledScripts(getActivity());
    }
}
