package com.dappervision.wearscript.launcher;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;


public abstract class ScriptListFragment extends Fragment {
    protected BroadcastReceiver mPackageBroadcastReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateUI();
        }
    };
    protected AdapterView.OnItemClickListener mOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            WearScriptInfo info = (WearScriptInfo) mListAdapter.getItem(i);
            mCallbacks.onScriptSelected(info);
        }
    };
    //private static final String TAG = "ScriptListFragment";
    protected InstalledScripts mInstalledScripts;
    protected AdapterView mAdapterView;
    protected Callbacks mCallbacks;
    protected ListAdapter mListAdapter;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCallbacks = (Callbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    public void updateUI() {
        mInstalledScripts.load(getActivity());
    }

    public abstract ListAdapter buildListAdapter();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mInstalledScripts = getInstalledScripts();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        intentFilter.addDataScheme("package");
        getActivity().registerReceiver(mPackageBroadcastReciever, intentFilter);
        mListAdapter = buildListAdapter();
    }

    protected abstract InstalledScripts getInstalledScripts();

    @Override
    public void onResume() {
        super.onResume();
        mAdapterView.requestFocus();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(mPackageBroadcastReciever);
    }

    public interface Callbacks {
        void onScriptSelected(WearScriptInfo scriptInfo);
    }
}
