package com.threeDBJ.bluetooth;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
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

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class BluetoothActivity extends SherlockActivity implements BluetoothClient {
    static final int CONNECT_ID = 1;
    BluetoothAdapter btAdapter;
    private ProgressDialog progressDialog;
    private boolean connected = false;
    private String connectedDeviceName;
    private BluetoothService btService;
    BluetoothProfile myProfile, connectedProfile;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
	btAdapter = BluetoothAdapter.getDefaultAdapter();
	if (btAdapter == null) {
	    // If no bluetooth adapter is available, quit the app
	    Util.AlertBox(this, "Error", "Bluetooth not available, exiting app", true);
	    finish();
	} else if (!btAdapter.isEnabled()) {
	    // Enable the bluetooth adapter if necessary
	    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
	    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
	} else {
	    setupService();
	}
	Button control1 = (Button)findViewById(R.id.control1);
	control1.setOnClickListener(new OnClickListener() {
		public void onClick(View v) {
		    if(connected) {
			send(new BluetoothMessage(1));
		    }
		}
	    });
	Button control2 = (Button)findViewById(R.id.control2);
	control2.setOnClickListener(new OnClickListener() {
		public void onClick(View v) {
		    if(connected) {
			send(new BluetoothMessage(2));
		    }
		}
	    });

	// Initialize a profile describing this device
	myProfile = new BluetoothProfile(ANDROID);
    }

    @Override
    public BluetoothProfile getProfile() {
	return myProfile;
    }

    @Override
    public void onResume() {
	super.onResume();
	if (btService != null) {
	    DebugLog.e(TAG, "onresume service not null");
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (btService.getState() == BluetoothService.STATE_NONE) {
		DebugLog.e(TAG, "onresume service status none");
		// Start the Bluetooth services
		btService.start();
            }
        }
    }

    @Override
    public void onPause() {
	super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Stop the Bluetooth services
        if (btService != null) btService.stop();
	DebugLog.e(TAG, "DESTROYED");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, CONNECT_ID, 0, "Connect")
            .setIcon(R.drawable.ic_bluetooth_search)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
	MenuItem connectButton = menu.findItem(CONNECT_ID);     
	if(connected) {
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
	    if(connected) {
		// TODO -- is there a better way of disconnecting a device?
		btService.stop();
		btService.start();
	    } else {
		Intent connectIntent = new Intent(BluetoothActivity.this, BluetoothDiscoverList.class);
		startActivityForResult(connectIntent, REQUEST_CONNECT_BT);
	    }
            break;
        }
        return true;
    }

    /**
     * Function called when another activity started from this one with startActivityForResult(int) returns.
     * REQUEST_ENABLE_BT - User has chosen whether to enable bluetooth.
     * REQUEST_CONNECT_BT - User has potentially selected an external bluetooth device to connect to.
     *
     * @param requestCode The int argument to our startActivityForResult(int) call.
     * @param resultCode The result specified by the returning activity in its setResult(int) call. Default 0 (RESULT_CANCELED)
     * @param data The intent returned from a setResult(int, Intent) call, or null
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
	if(requestCode == REQUEST_ENABLE_BT) {
	    if(resultCode == RESULT_OK) {
                setupService();
	    } else {
		Util.AlertBox(this, "Error", "This app requires bluetooth to be enabled.", true);
	    }
	} else if(requestCode == REQUEST_CONNECT_BT) {
	    if(resultCode == RESULT_OK) {
		connectDevice(data, true);
	    }
	}
    }

    /* BluetoothClient interface functions */

    public Handler getHandler() {
	return clientHandler;
    }

    public Handler getProgressHandler() {
	return progressHandler;
    }
    
    private void connectDevice(Intent data, boolean secure) {
        String address = data.getExtras().getString("device_address");
        BluetoothDevice device = btAdapter.getRemoteDevice(address);
        btService.connect(device, secure);
    }

    public void send(BluetoothMessage msg) {
	if(connected) {
	    btService.send(msg);
	}
    }

    private final Handler progressHandler = new Handler() {
	    @Override
	    public void handleMessage(Message msg) {
		switch(msg.what) {
	    }
	    }
	};

    private void setupService() {
	btService = new BluetoothService(this);
    }

    private final Handler clientHandler = new Handler() {
	    @Override
	    public void handleMessage(Message msg) {
		Bitmap bitmap;
		switch (msg.what) {
		case MESSAGE_STATE_CHANGE:
		    switch (msg.arg1) {
		    case BluetoothService.STATE_CONNECTED:
			connected = true;
			supportInvalidateOptionsMenu();
			DebugLog.e(TAG, "MESSAGE_STATE_CHANGE: CONNECTED");
			//send(new BluetoothMessage(REQUEST_PROFILE));
			break;
		    case BluetoothService.STATE_CONNECTING:
			DebugLog.e(TAG, "MESSAGE_STATE_CHANGE: CONNECTING");
			break;
		    case BluetoothService.STATE_LISTEN:
			DebugLog.e(TAG, "MESSAGE_STATE_CHANGE: LISTEN");
		    case BluetoothService.STATE_NONE:
			connected = false;
			DebugLog.e(TAG, "MESSAGE_STATE_CHANGE: NONE");
			break;
		    }
		    break;
		case CLIENT_PROFILE:
		    connectedProfile = (BluetoothProfile)msg.obj;
		    if(connectedProfile != null) {
			DebugLog.e(TAG, "identified as: "+connectedProfile.getType());
		    }
		    break;
		case MESSAGE_DEVICE_NAME:
		    // save the connected device's name
		    connectedDeviceName = msg.getData().getString(DEVICE_NAME);
		    Util.shortToast(getApplicationContext(), "Connected to " + connectedDeviceName);
		    break;
		case MESSAGE_TOAST:
		    Util.shortToast(getApplicationContext(), msg.getData().getString(TOAST));
		    break;
		}
	    }
	};

}
