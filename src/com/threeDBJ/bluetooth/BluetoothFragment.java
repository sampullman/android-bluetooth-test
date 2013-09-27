package com.threeDBJ.bluetooth.test;

import android.app.Activity;
import android.os.Bundle;
import android.os.Message;

import com.actionbarsherlock.app.SherlockFragment;

import com.threeDBJ.bluetooth.*;

public abstract class BluetoothFragment extends SherlockFragment {
    public static String TAG = "BTAndroid";
    BluetoothClient btClient;

    @Override
    public void onAttach(Activity activity) {
	super.onAttach(activity);
	btClient = (BluetoothClient)activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle savedState) {
        super.onSaveInstanceState(savedState);
	saveState(savedState);
    }

    public void onDestroy() {
	super.onDestroy();
	btClient = null;
    }

    public abstract void settingsModified();
    public abstract void handleMessage(Message msg);
    abstract void saveState(Bundle savedState);
    abstract void restoreState(Bundle restoreState);

}