package com.threeDBJ.bluetooth.test;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Handler;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import com.threeDBJ.bluetooth.Util;

public class TestUtil extends Util {
    public static String TAG="LED";

    public static StringBuilder convertMessage(StringBuilder message, int modeFrom, int modeTo) {
	if(modeFrom == Settings.ASCII_MODE) {
	    if(modeTo == Settings.BYTE_MODE) {
		return asciiToByte(message);
	    }
	} else if(modeFrom == Settings.BYTE_MODE) {
	    if(modeTo == Settings.ASCII_MODE) {
		return byteToAscii(message);
	    }
	}
	return new StringBuilder();
    }

    public static StringBuilder byteToAscii(StringBuilder bytes) {
	StringBuilder ascii = new StringBuilder();
	String[] byteArray = bytes.toString().split(" ");
	for(int i=0;i<byteArray.length;i+=1) {
	    ascii.append((char)Integer.parseInt(byteArray[i]));
	}
	return ascii;
    }

    public static StringBuilder asciiToByte(StringBuilder ascii) {
	int len = ascii.length();
	StringBuilder bytes = new StringBuilder();
	for(int i=0;i<len;i+=1) {
	    bytes.append(Integer.toString(ascii.charAt(i)));
	    if(i != len-1) bytes.append(" ");
	}
	return bytes;
    }

}