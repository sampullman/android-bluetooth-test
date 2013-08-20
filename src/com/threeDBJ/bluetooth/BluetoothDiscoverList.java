package com.threeDBJ.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class BluetoothDiscoverList extends SherlockActivity {
    static final int REFRESH_ID = 1;
    static final String NO_DEVICE_TEXT = "No devices found";
    BluetoothAdapter bluetoothAdapter;
    ArrayAdapter<String> btDevices;
    ProgressBar progress;

    @Override
    public void onCreate(Bundle instanceState) {
	super.onCreate(instanceState);
	setContentView(R.layout.bluetooth_discover);
	btDevices = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1); //android.R.id.text1

	bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

	ListView list = (ListView)findViewById(R.id.device_list);
	list.setAdapter(btDevices);
	list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
		    String info[] = ((TextView) view).getText().toString().split("\n");
		    if(info != null && info.length == 2) {
			DebugLog.e("TapCam", "Chose device: "+info[0]+", "+info[1]);
			Intent intent = new Intent();
			intent.putExtra("device_address", info[1]);
			setResult(RESULT_OK, intent);
		    } else {
			setResult(RESULT_CANCELED);
		    }
		    finish();
		}
	    });
	progress = (ProgressBar)findViewById(R.id.spinner);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, REFRESH_ID, 0, "Refresh")
            .setIcon(R.drawable.ic_refresh)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
	    setResult(RESULT_CANCELED);
	    finish();
            break;
        case REFRESH_ID:
	    btDevices.clear();
	    bluetoothAdapter.cancelDiscovery();
	    bluetoothAdapter.startDiscovery();
            break;
	}
	return true;
    }

    @Override
    public void onResume() {
	super.onResume();
	btDevices.clear();
	IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
	registerReceiver(bluetoothReceiver, filter);
	bluetoothAdapter.startDiscovery();
    }

    @Override
    public void onPause() {
	super.onPause();
	bluetoothAdapter.cancelDiscovery();
	unregisterReceiver(bluetoothReceiver);
    }

    public void addDevice(String device) {
	btDevices.add(device);
	if(btDevices.getCount() == 1) {
	    progress.setVisibility(View.GONE);
	}
    }

    private final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
	    public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (BluetoothDevice.ACTION_FOUND.equals(action)) {
		    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
		    addDevice(device.getName() + "\n" + device.getAddress());
		} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
		    addDevice(NO_DEVICE_TEXT);
		}
	    }
	};

}