package com.threeDBJ.bluetooth;

import android.os.Handler;

import java.io.DataInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

public interface BluetoothClient {
    public static String TAG = "LEDControl";
    public static int DEVICE_ID = 0xA7;

    public static int CHUNK_SIZE = 8 * 1024;

    /* Client types */
    public static String DEVICE = "device";
    public static String ANDROID = "android";

    /* Intent constants */
    public static int REQUEST_ENABLE_BT = 10;
    public static int REQUEST_CONNECT_BT = 11;
    public static int REQUEST_CONNECT_COMP = 12;
    public static int MAKE_DISCOVERABLE = 13;

    /* Bluetooth API commands */
    public static int REQUEST_PROFILE = 5;
    public static int CLIENT_PROFILE = 6;

    public static int LINE_ACK=77, LINE_NACK=78;

    /* Bundle keys */
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    /* State change message commands */
    public static final int MESSAGE_STATE_CHANGE = 200;
    public static final int MESSAGE_DEVICE_NAME = 203;
    public static final int MESSAGE_TOAST = 204;

    public BluetoothProfile getProfile();
    public Handler getHandler();
    public Handler getProgressHandler();
    public void send(BluetoothMessage msg);

    public static interface PictureRequestCallback {
	public void onPictureRequestComplete(byte[] jpeg);
    }

    public class BluetoothProfile extends BluetoothMessage {
	String type;
	public BluetoothProfile(String type) {
	    this.type = type;
	}
	public String getType() {
	    return type;
	}
	public static BluetoothProfile build(InputStream stream) {
	    try {
		DataInputStream data = new DataInputStream(stream);
		int id = (data.read() << 4) | data.read();
		if(id != DEVICE_ID) {
		    DebugLog.e(TAG, "Bad profile id: "+id);
		    return null;
		} else {
		    String type = Util.readLine(data);
		    return new BluetoothProfile(type);
		}
	    } catch(Exception e) {
		DebugLog.e(TAG, "Failed to get profile");
		return null;
	    }
	}
	public void write(OutputStream out) throws IOException {
	    String typeLine = type + "\n";
	    out.write(CLIENT_PROFILE);
	    out.write((DEVICE_ID & 0xF0) >> 4);
	    out.write(DEVICE_ID & 0x0F);
	    out.write(typeLine.getBytes());
	}
    }
}