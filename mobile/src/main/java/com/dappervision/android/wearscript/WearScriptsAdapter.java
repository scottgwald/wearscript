package com.dappervision.android.wearscript;

import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.dappervision.wearscript.launcher.InstalledScripts;
import com.dappervision.wearscript.launcher.WearScriptInfo;

public class WearScriptsAdapter extends ArrayAdapter<WearScriptInfo> {

    private final LayoutInflater mInflator;

    public WearScriptsAdapter(Fragment fragment, InstalledScripts scripts) {
        super(fragment.getActivity(), 0, scripts.getWearScripts());
        mInflator = fragment.getActivity().getLayoutInflater();
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {

        if (view == null) {
            view = mInflator.inflate(android.R.layout.simple_list_item_1, parent, false);
        }

        TextView text = (TextView) view.findViewById(android.R.id.text1);
        text.setText(getItem(position).toString());
        return view;
    }
}