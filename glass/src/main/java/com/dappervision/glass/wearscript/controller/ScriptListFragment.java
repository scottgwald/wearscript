package com.dappervision.glass.wearscript.controller;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListAdapter;

import com.dappervision.glass.wearscript.GlassInstalledScripts;
import com.dappervision.glass.wearscript.WearScriptsCardAdapter;
import com.dappervision.wearscript.launcher.InstalledScripts;
import com.google.android.glass.widget.CardScrollAdapter;
import com.google.android.glass.widget.CardScrollView;

public class ScriptListFragment extends com.dappervision.wearscript.launcher.ScriptListFragment {
    public static ScriptListFragment newInstance() {
        return new ScriptListFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LinearLayout layout = new LinearLayout(getActivity());
        CardScrollView view = new CardScrollView(getActivity());
        view.setHorizontalScrollBarEnabled(true);
        view.setAdapter((CardScrollAdapter) mListAdapter);
        view.activate();
        mAdapterView = view;
        mAdapterView.setOnItemClickListener(mOnItemClickListener);
        layout.addView(mAdapterView);
        return layout;
    }

    @Override
    public ListAdapter buildListAdapter() {
        return new WearScriptsCardAdapter(this, mInstalledScripts);
    }

    @Override
    protected InstalledScripts getInstalledScripts() {
        return new GlassInstalledScripts(getActivity());
    }
}
