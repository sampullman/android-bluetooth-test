package com.threeDBJ.bluetooth.test;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.MediaStore.Images.Media;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import com.threeDBJ.bluetooth.*;

public class ApplicationActivity extends BluetoothActivity {
    /* Bluetooth API commands */
    public static int REQUEST_PROFILE = 5;
    public static int CLIENT_PROFILE = 6;

    static final String CONTROLLER_TAG = "Controller";
    static final String CONSOLE_TAG = "Console";
    static final String FILE_TRANSFER_TAG = "File Transfer";

    static final int CONNECT_ID=1, SETTINGS_ID=2, DISCOVERABLE_ID=3;
    static final String ACTIVE_TAB = "activeTab";
    private ProgressDialog progressDialog;
    BluetoothProfile myProfile, connectedProfile;
    BluetoothFragment currentFragment;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
	btService = new BluetoothService(this);
	final ActionBar actionBar = getSupportActionBar();
	actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
	// add tabs
	Tab tab1 = actionBar.newTab()
	    .setText("Controller")
	    .setTabListener(new TabListener<ControllerFragment>(this, CONTROLLER_TAG, ControllerFragment.class));
	actionBar.addTab(tab1);

	Tab tab2 = actionBar.newTab()
	    .setText("Console")
	    .setTabListener(new TabListener<ConsoleFragment>(this, CONSOLE_TAG, ConsoleFragment.class));
	actionBar.addTab(tab2);

	// check if there is a saved state to select active tab
	if( savedInstanceState != null ) {
	    int currentTab = savedInstanceState.getInt(ACTIVE_TAB);
	    actionBar.setSelectedNavigationItem(currentTab);
	}

	// Initialize a profile describing this device
	myProfile = new BluetoothProfile(ANDROID);
    }

    public void tabSelected(BluetoothFragment frag) {
	currentFragment = frag;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
	// save active tab
	outState.putInt(ACTIVE_TAB,
			getSupportActionBar().getSelectedNavigationIndex());
	super.onSaveInstanceState(outState);
    }

    @Override
    public BluetoothProfile getProfile() {
	return myProfile;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
	menu.add(0, DISCOVERABLE_ID, 0, "Discoverable")
	    .setIcon(R.drawable.ic_mobile_phone)
	    .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        menu.add(0, CONNECT_ID, 0, "Connect")
            .setIcon(R.drawable.ic_bluetooth_search)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        menu.add(0, SETTINGS_ID, 0, "Settings")
            .setIcon(R.drawable.ic_settings)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
	MenuItem connectButton = menu.findItem(CONNECT_ID);     
	if(isConnected()) {
	    connectButton.setIcon(R.drawable.ic_bluetooth_connected);
	} else {
	    connectButton.setIcon(R.drawable.ic_bluetooth_search);
	}
	return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            break;
        case CONNECT_ID:
	    if(isConnected()) {
		// TODO -- is there a better way of disconnecting a device?
		btService.stop();
		btService.start();
	    } else {
		Intent connectIntent = new Intent(ApplicationActivity.this, BluetoothDiscoverList.class);
		startActivityForResult(connectIntent, REQUEST_CONNECT_BT);
	    }
            break;
	case SETTINGS_ID:
	    Intent settingsIntent = new Intent(ApplicationActivity.this, Settings.class);
	    startActivityForResult(settingsIntent, SETTINGS_ID);
	    break;
	case DISCOVERABLE_ID:
	    Intent discoverableIntent = new
		Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
	    discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 600);
	    startActivityForResult(discoverableIntent, MAKE_DISCOVERABLE);
	    break;
        }
        return true;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
	if(requestCode == SETTINGS_ID) {
	    if(resultCode == Settings.RESULT_CHANGED) {
		if(currentFragment != null) {
		    currentFragment.settingsModified();
		}
	    }
	} else {
	    super.onActivityResult(requestCode, resultCode, data);
	}
    }

    /* BluetoothClient interface functions */

    public Handler getProgressHandler() {
	return progressHandler;
    }

    private final Handler progressHandler = new Handler() {
	    @Override
	    public void handleMessage(Message msg) {
		switch(msg.what) {
		}
	    }
	};

    /* Bluetooth activity abstract method overrides */

    public void receivedData(InputStream inStream) throws IOException {
	int cmd = inStream.read();
	DebugLog.e(TAG, "Message: "+cmd);
	switch(cmd) {
	case BluetoothClient.REQUEST_PROFILE:
	    send(getProfile());
	    break;
	case BluetoothClient.CLIENT_PROFILE:
	    Object client = BluetoothProfile.build(inStream);
	    getHandler().obtainMessage(cmd, -1, -1, client).sendToTarget();
	    break;
	default:
	    getHandler().obtainMessage(cmd).sendToTarget();
	    break;
	}
    }

    public void setConnected(boolean connected) {
	super.setConnected(connected);
	supportInvalidateOptionsMenu();
    }

    public void deviceConnecting() {}

    public void handleUIMessage(Message msg) {
	Bitmap bitmap;
	switch (msg.what) {
	case MESSAGE_TOAST:
	    Util.shortToast(getApplicationContext(), msg.getData().getString(TOAST));
	    break;
	case BluetoothClient.CLIENT_PROFILE:
	    connectedProfile = (BluetoothProfile)msg.obj;
	    if(connectedProfile != null) {
		DebugLog.e(TAG, "identified as: "+connectedProfile.getType());
	    }
	    break;
	default:
	    if(currentFragment != null) {
		currentFragment.handleMessage(msg);
	    } else {
		DebugLog.e(TAG, "fragment null");
	    }
	    break;
	}
    }

    public class TabListener<T extends BluetoothFragment> implements ActionBar.TabListener {
	private BluetoothFragment fragment;
	private final ApplicationActivity activity;
	private final String tag;
	private final Class<T> cls;

	public TabListener(ApplicationActivity activity, String tag, Class<T> cls) {
	    this.activity = activity;
	    this.tag = tag;
	    this.cls = cls;
	}

	public void onTabSelected(Tab tab, FragmentTransaction ft) {
	    // Check if the fragment is already initialized
	    if (fragment == null) {
		// If not, instantiate and add it to the activity
		fragment = (BluetoothFragment) Fragment.instantiate(activity, cls.getName());
		//fragment.setProviderId(tag); // id for event provider
		ft.add(android.R.id.content, fragment, tag);
	    } else {
		// If it exists, simply attach it in order to show it
		ft.attach(fragment);
	    }
	    activity.tabSelected(fragment);
	}

	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
	    if (fragment != null) {
		// Detach the fragment, because another one is being attached
		ft.detach(fragment);
	    }
	}

	public void onTabReselected(Tab tab, FragmentTransaction ft) {
	    // User selected the already selected tab. Usually do nothing.
	}
    }

}
