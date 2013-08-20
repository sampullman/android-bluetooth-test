package com.threeDBJ.bluetooth;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;

public class BluetoothMessage {
    public int cmd, arg=-1;
    public byte[] bytes;

    public BluetoothMessage() {}

    public BluetoothMessage(int cmd) {
	this.cmd = cmd;
    }

    public BluetoothMessage(int cmd, byte[] bytes) {
	this(cmd);
	this.bytes = bytes;
    }

    public BluetoothMessage(int cmd, int arg, byte[] bytes) {
	this(cmd, bytes);
	this.arg = arg;
    }

    public boolean hasArg() {
	return arg != -1;
    }

    public void write(OutputStream out) throws IOException {
	out.write(cmd);
	if(hasArg()) {
	    String strArg = arg+"\n";
	    out.write(strArg.getBytes());
	}
	if(bytes != null) {
	    String len = Integer.toString(bytes.length)+"\n";
	    out.write(len.getBytes());
	    out.write(bytes);
	}
    }
}