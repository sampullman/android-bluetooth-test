package com.threeDBJ.bluetooth.test;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class ControllerFragment extends BluetoothFragment {

    @Override
    public void settingsModified() {
	clearUI(getView());
	generateUI(getView());
    }

    @Override
    public void handleMessage(Message msg) {
	DebugLog.e(TAG, "Controller message");
    }

    @Override
    public LinearLayout getView() {
	FrameLayout wrapper = (FrameLayout)super.getView();
	return (LinearLayout)((wrapper != null) ? wrapper.getChildAt(0) : null);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
        View v = inflater.inflate(R.layout.controller, container, false);
	restoreState(savedState);
	generateUI(v);
        return v;
    }

    public OnClickListener makeButtonAction(final String action) {
	return new OnClickListener() {
	    public void onClick(View v) {
		btClient.write(action);
	    }
	};
    }

    public byte[] makeSliderMessage(String prepend, int progress) {
	int pLen = prepend.length();
	byte[] bytes = (prepend+" ").getBytes();
	bytes[pLen] = (byte)progress;
	return bytes;
    }

    public OnSeekBarChangeListener makeSliderAction(final String prepend) {
	return new OnSeekBarChangeListener() {
	    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		btClient.write(makeSliderMessage(prepend, progress));
	    }

	    public void onStartTrackingTouch(SeekBar seekBar) {
	    }

	    public void onStopTrackingTouch(SeekBar seekBar) {
		btClient.write(makeSliderMessage(prepend, seekBar.getProgress()));
	    }
	};
    }

    public void clearUI(View v) {
	LinearLayout root = (LinearLayout)v;
	root.removeAllViews();
    }

    public void generateUI(View v) {
	Activity activity = getActivity();
	Context app = activity.getApplicationContext();
	LinearLayout root = (LinearLayout)v;

	TextView title = new TextView(activity);
	title.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 0, 0.1f));
	title.setGravity(Gravity.CENTER);
	title.setText("Custom Bluetooth Controller");
	root.addView(title);

	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(app);
	int numButtons = Settings.getNumButtons(prefs);
	int buttonsPerRow = Settings.getButtonsPerRow(prefs);
	int numRows = (int)Math.ceil((double)numButtons / (double)buttonsPerRow);
	DebugLog.e(TAG, "Num rows, bpr: "+numRows+", "+buttonsPerRow);
	for(int i=0; i<numRows; i+=1) {
	    LinearLayout row = new LinearLayout(activity);
	    row.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 0, 0.1f));
	    for(int j=0; j<buttonsPerRow; j+=1) {
		int buttonNum = buttonsPerRow*i + j;
		if(buttonNum == numButtons) break;
		Button b = new Button(activity);
		b.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1f));
		b.setText(Settings.getButtonTitle(prefs, buttonNum));
		b.setOnClickListener(makeButtonAction(Settings.getButtonValue(prefs, buttonNum)));
		row.addView(b);
	    }
	    root.addView(row);
	}
	int numSliders = Settings.getNumSliders(prefs);
	for(int i=0; i<numSliders; i+=1) {
	    LinearLayout row = new LinearLayout(activity);
	    LayoutParams rowParams = new LayoutParams(LayoutParams.MATCH_PARENT, 0, 0.15f);
	    row.setLayoutParams(rowParams);
	    SeekBar bar = new SeekBar(activity);
	    bar.setMax(255);
	    bar.setOnSeekBarChangeListener(makeSliderAction(Settings.getSliderPrepend(prefs, i)));
	    LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1f);
	    params.gravity = Gravity.CENTER_VERTICAL;
	    bar.setLayoutParams(params);
	    row.addView(bar);
	    root.addView(row);
	}
	View filler = new View(activity);
	float fillerSpace = 1f - (0.1f*(float)(1 + numRows) + ((float)numSliders * 0.15f));
	DebugLog.e(TAG, "fill space: "+fillerSpace);
	filler.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 0, fillerSpace));
	root.addView(filler);
    }

    @Override
    public void saveState(Bundle state) {
    }

    @Override
    public void restoreState(Bundle state) {
    }
}